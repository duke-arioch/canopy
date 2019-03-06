package org.sandcast.canopy;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.sandcast.canopy.command.CanopyCommand;
import org.sandcast.canopy.config.PluginConfig;
import org.sandcast.canopy.config.Recipe;
import org.sandcast.canopy.listener.GlobalEventsListener;
import org.sandcast.canopy.matrix.Matrix;
import org.sandcast.canopy.util.DescriptionUtils;
import org.sandcast.canopy.worldedit.WorldEditOperations;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Canopy Plugin class.
 */

public class CanopyPlugin extends JavaPlugin {

    private PluginConfig config;
    private WorldEditOperations worldEditOperations;

    public CanopyPlugin() {
        super();
    }

    protected CanopyPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public final void onEnable() {
        try {
            getLogger().info("Loading the configuration from " + getDataFolder());
            getDataFolder().mkdirs();
            config = load(getDataFolder());
            if (Bukkit.getPluginManager().getPlugin("WorldEdit") == null) {
                getLogger().warning("WorldEdit must be installed to use SCHEMATIC mode");
            } else if (!WorldEditOperations.checkWorldEditVersion()) {
                getLogger().warning("Incorrect WorldEdit version. Current accepted ones are : "
                        + String.join(", ", WorldEditOperations.WORLDEDIT_VERSIONS) + ".");
            }
            worldEditOperations = new WorldEditOperations(getFile(), config.getSchematicsDirectory());
            Bukkit.getPluginManager().registerEvents(new GlobalEventsListener(this), this);
            getCommand("canopy").setExecutor(new CanopyCommand());
            final PluginDescriptionFile description = getDescription();
            getLogger().info("Enabled " + ChatColor.GREEN + getName() + " v" + description.getVersion() + ChatColor.GOLD + " by "
                    + String.join(" ", description.getAuthors()) + ChatColor.RESET + " !");
        } catch (final Exception ex) {
            getLogger().log(Level.SEVERE, "Unable to start the plugin !", ex);
            ex.printStackTrace();
        }
    }

    public Block[][] getSourceGrid(Recipe recipe, StructureGrowEvent event) {
        int recipeSize = recipe.getLayout().size();
        int matrixSize = 2 * recipeSize - 1;
        Block[][] worldMatrix = new Block[matrixSize][matrixSize];
        Location eventLocation = event.getLocation();
        int eventY = eventLocation.getBlockY();
        int startX = eventLocation.getBlockX() - recipeSize + 1;
        int startZ = eventLocation.getBlockZ() - recipeSize + 1;
        for (int z = 0; z < matrixSize; z++) {
            for (int x = 0; x < matrixSize; x++) {
                worldMatrix[x][z] = eventLocation.getWorld().getBlockAt(startX + x, eventY, startZ + z);
            }
        }
        return worldMatrix;
    }

    public String[][] getRecipeMatrix(Recipe recipe) {
        int recipeSize = recipe.getLayout().size();
        String[][] recipeMatrix = new String[recipeSize][recipeSize];
        for (int i = 0; i < recipeSize; i++) {
            recipeMatrix[i] = recipe.getLayout().get(i).toArray(new String[]{});
        }
        return recipeMatrix;
    }


    public void growTree(StructureGrowEvent event) {
        Map<String, Recipe> recipes = config.getRecipes();
        //loop through these based upon order in the file. should be a list.
        for (Recipe recipe : recipes.values()) {
            String[][] ingredients = getRecipeMatrix(recipe);
            Block[][] worldBlocks = getSourceGrid(recipe, event);
            Matrix<String> ingredientMatrix = Matrix.of(ingredients);
            Matrix<Block> blockMatrix = Matrix.of(worldBlocks);
            boolean recipeMatchesEventArea = blockMatrix.containsRotated(ingredientMatrix, (block, s) -> s.equals(DescriptionUtils.describe(block)));
            if (recipeMatchesEventArea) {
                int matrixSize = 2 * recipe.getLayout().size() - 1;
                for (int z = 0; z < matrixSize; z++) {
                    for (int x = 0; x < matrixSize; x++) {
                        Block block = worldBlocks[x][z];
                        if (block.getType().equals(Material.SAPLING)) {
                            block.setType(Material.AIR);
                        }
                    }
//                    boolean biomeMatches = recipe.getBiomes().contains(event.getLocation().getBlock().getBiome().name());
                    Recipe.Builder.Strategy strategy = recipe.getBuilder().getStrategy();
                    switch (strategy) {
                        case SCHEMATIC:
                            if (worldEditOperations.growTree(event, recipe)) {
                                getLogger().info("Schematic tree grew successfully");
                            } else {
                                getLogger().warning("Schematic tree did not grow!");
                            }
                            break;
                        case PROCEDURAL:
                            break;
                        default:
                    }
                }
            } else {
                getLogger().info("no match so grew a generic tree off of " + event.getSpecies().name());
                TreeType treeType = event.getSpecies();
                Block block = event.getLocation().getBlock();
                if (event.getSpecies() == TreeType.BIG_TREE) {
                    if (DescriptionUtils.describe(block).equals("BIRCH_SAPLING")) {
                        treeType = TreeType.TALL_BIRCH;
                    }
                    if (DescriptionUtils.describe(block).equals("REDWOOD_SAPLING")) {
                        treeType = TreeType.MEGA_REDWOOD;
                    }
                }
                block.setType(Material.AIR);
                event.getLocation().getWorld().generateTree(event.getLocation(), treeType);
            }
        }
    }
    public PluginConfig getPluginConfig() {
        return config;
    }

    public PluginConfig load(File dataFolder) throws IOException {

        final PluginConfig config;
        Yaml yaml = new Yaml();
        File configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            getLogger().info("creating new config file");
            if (configFile.createNewFile()) {
                getLogger().info("new config file created");
                try (final FileWriter writer = new FileWriter(configFile)) {
                    Recipe recipe = new Recipe();
                    List<String> row1 = Arrays.asList("DARK_OAK_SAPLING", "DARK_OAK_SAPLING");
                    List<String> row2 = Arrays.asList("DARK_OAK_SAPLING", "ACACIA_SAPLING");
                    recipe.getLayout().add(row1);
                    recipe.getLayout().add(row2);
                    recipe.setBiomes(Arrays.asList("FOREST"));
                    Recipe.Builder builder = new Recipe.Builder();
                    builder.setArguments(Arrays.asList("birch/small/Birch[2,3]"));
                    recipe.setBuilder(builder);

                    Recipe recipe2 = new Recipe();
                    List<String> row21 = Arrays.asList("ACACIA_SAPLING", "DARK_OAK_SAPLING");
                    List<String> row22 = Arrays.asList("BIRCH_SAPLING", "ACACIA_SAPLING");
                    recipe2.getLayout().add(row21);
                    recipe2.getLayout().add(row22);
                    recipe2.setBiomes(Arrays.asList("FOREST"));
                    Recipe.Builder builder2 = new Recipe.Builder();
                    builder2.setArguments(Arrays.asList("JungleLarge[1234]"));
                    recipe2.setBuilder(builder2);

                    final PluginConfig newConfig = new PluginConfig();
                    Map<String, Recipe> recipes = new HashMap<>();
                    recipes.put("chimera", recipe);
                    recipes.put("chimera2", recipe2);
                    newConfig.setRecipes(recipes);
                    newConfig.setSchematicsDirectory("schematics");
                    String output = yaml.dumpAs(newConfig, Tag.MAP, DumperOptions.FlowStyle.AUTO);
                    writer.write(output);
                    writer.flush();
                    getLogger().info("configuration file created");
                } catch (IOException ioe1) {
                    getLogger().log(Level.SEVERE, "Configuration error due to ", ioe1);
                }
            } else {
                getLogger().info("CanopyPlugin unable to create new config file");
            }
        }
//        try {
//            getLogger().info("about to try the get config with class thing");
//            System.out.println("plugins classloader is " + PluginConfig.class.getClassLoader().getClass().getCanonicalName());
//            System.out.println("my classloader is " + this.getClassLoader().getClass().getCanonicalName());
//            Class.forName(PluginConfig.class.getCanonicalName(), false, PluginConfig.class.getClassLoader());
//        } catch (ClassNotFoundException ex) {
//            ex.printStackTrace();
//        }
        FileReader reader = new FileReader(configFile);
        PluginConfig p2 = new PluginConfig();
        Yaml yaml2 = new Yaml(new CustomClassLoaderConstructor(getClassLoader()));
        config = yaml2.loadAs(reader, PluginConfig.class);
        getLogger().info("Canopy Plugin configuration loaded");
        return config;
    }
}