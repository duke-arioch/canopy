package org.sandcast.canopy.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.plugin.Plugin;
import org.sandcast.canopy.config.Recipe;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Represents available WorldEdit operations.
 */

public class WorldEditOperations {

    public static final String[] WORLDEDIT_VERSIONS = new String[]{"6"};
    private final File zipFile;
    private final String schematicsDirectory;
    private final Set<String> trees = new HashSet<>();

    public WorldEditOperations(final File zipFile, String schematicsDirectory) {
        this.zipFile = zipFile;
        this.schematicsDirectory = schematicsDirectory;
    }

    public static boolean checkWorldEditVersion() {
        Plugin worldEditPlugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (worldEditPlugin != null) {
            final String version = worldEditPlugin.getDescription().getVersion();
            return Arrays.stream(WORLDEDIT_VERSIONS).anyMatch(version::startsWith);
        } else return false;
    }

    private ClipboardHolder loadSchematic(final String schematic, Location location) throws IOException {
        final File file = new File(schematic);
        if (!file.exists()) {
            throw new FileNotFoundException("Schematic not found : " + schematic + ".");
        }

        final ClipboardFormat format = ClipboardFormat.findByFile(file);
        if (format == null) {
            throw new IllegalArgumentException("Unknown schematic format.");
        }

        final Closer closer = Closer.create();
        try {
            final FileInputStream fileInputStream = closer.register(new FileInputStream(file));
            final BufferedInputStream bufferedInputStream = closer.register(new BufferedInputStream(fileInputStream));
            final ClipboardReader reader = format.getReader(bufferedInputStream);
            final World world = new BukkitWorld(location.getWorld());
            final EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
            Clipboard clipboard = reader.read(session.getWorld().getWorldData());
            return new ClipboardHolder(clipboard, session.getWorld().getWorldData());
        } catch (final Exception ex) {
            closer.rethrow(ex);
        } finally {
            closer.close();
        }
        return null;
    }

    public boolean growTree(final StructureGrowEvent event, Recipe recipe) {
        List<String> args = recipe.getBuilder().getArguments();
        for (String arg : args) {
            Matcher matcher = Pattern.compile(arg).matcher("");
            List<String> possibleSchematics = trees.stream()
                    .filter(tree -> matcher.reset(tree).find())
                    .collect(Collectors.toList());
            if (possibleSchematics.isEmpty()) {
                System.out.println("found no matching schematic for " + arg);
            } else {
                String tree = possibleSchematics.get(new Random().nextInt(possibleSchematics.size()));
                System.out.println("found matching schematic for " + arg + ": " + tree);
                Location location = event.getLocation();
                try {
                    final ClipboardHolder holder = loadSchematic((tree), location);
                    System.out.println("random rotation setting is: " + recipe.getRandomRotation());

                    if (recipe.getRandomRotation()) {
                        final int degrees = new Random().nextInt(4) * 90;
                        System.out.println("random rotation was: " + degrees);
                        if (degrees != 0) {
                            final AffineTransform transform = new AffineTransform();
                            transform.rotateY(degrees);
                            holder.setTransform(transform);
                        }
                    }

                    final Vector dimensions = holder.getClipboard().getDimensions();
//            if (plugin.getPluginConfig().schematicsCheckHeight && !checkHeight(dimensions, location)) {
//                return false;
//            }
                    swap(holder.getClipboard());
                    holder.getClipboard().setOrigin(getBasePoint(holder.getClipboard()));
                    Vector locationVector = new Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                    final World world = new BukkitWorld(location.getWorld());
                    final EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
                    final Operation operation = holder
                            .createPaste(session, session.getWorld().getWorldData())
                            .to(locationVector)
                            .ignoreAirBlocks(true)
                            .build();
                    Operations.completeLegacy(operation);

                    session.flushQueue();
                    return true;
                } catch (final Exception ex) {
                    Bukkit.getConsoleSender().sendMessage("Unable to load the schematic : \"" + tree + "\".");
                    ex.printStackTrace();
                }
            }
        }
        return false;
    }


    /**
     * Checks if a there is no floor above the tree.
     *
     * @param dimensions Tree dimensions.
     * @param location   Tree location.
     * @return Whether there is a floor above the tree.
     */

    private boolean checkHeight(final Vector dimensions, final Location location) {
        final org.bukkit.World world = location.getWorld();
        final int blockX = location.getBlockX();
        final int blockY = location.getBlockY();
        final int blockZ = location.getBlockZ();
        for (int x = blockX; x < blockX + dimensions.getBlockX(); x++) {
            for (int z = blockZ; z < blockZ + dimensions.getBlockZ(); z++) {
                for (int y = blockY + 1; y < blockY + dimensions.getBlockY(); y++) {
                    if (world.getBlockAt(x, y, z).getType() != Material.AIR) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private Vector getBasePoint(Clipboard clipboard) {
        Bukkit.getConsoleSender().sendMessage("checking");
        final Vector retval;
        Vector min = clipboard.getMinimumPoint();
        Vector max = clipboard.getMaximumPoint();
        long xtot = 0, ztot = 0, count = 0;
        int bottom = min.getBlockY();
        int top = min.getBlockY();
        for (int x = min.getBlockX(); x < max.getBlockX(); x++) {
            for (int z = min.getBlockZ(); z < max.getBlockZ(); z++) {
                final Vector where = new Vector(x, bottom, z);
                if (!clipboard.getBlock(where).isAir()) {
                    xtot += x;
                    ztot += z;
                    count++;
                }
            }
        }
        if (count != 0) {
            retval = new Vector(Math.floor(xtot / count), top, Math.floor(ztot / count));
        } else {
            retval = new Vector(0, 0, 0);
        }
        Bukkit.getConsoleSender().sendMessage("got:" + retval.toString());
        Bukkit.getConsoleSender().sendMessage("and:" + xtot + ":" + ztot + ":" + count);
        return retval;
    }

    private void swap(Clipboard clipboard) {
//        InputExtent extent = clipboard;
//        BlockMask mask = new BlockMask(clipboard);
//        mask.add(new BaseBlock(BlockID.LONG_GRASS));
//        EditSession session;
//        session.replaceBlocks(clipboard.getRegion(), mask, null);

        clipboard.getRegion().forEach(blockVector -> {
            final int type = clipboard.getBlock(blockVector).getType();
            if (type == BlockType.LONG_GRASS.getID()) {
                try {
                    clipboard.setBlock(blockVector, new BaseBlock(BlockID.AIR));
                } catch (Exception e) {
                    Bukkit.getConsoleSender().sendMessage("Failed to set block due to " + e.getMessage());
                }
            }
        });
    }

    public Set<String> getTrees() {
        return trees;
    }

    private void log(String message) {
        System.out.println("[" + "" + "] " + message);
    }
}