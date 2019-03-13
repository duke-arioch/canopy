package org.sandcast.canopy.config;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.event.world.StructureGrowEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sandcast.canopy.CanopyPlugin;
import org.sandcast.canopy.matrix.Matrix;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PluginConfigTest {
    private static ServerMock server;
    private CanopyPlugin plugin;
    private static WorldMock world;

    @BeforeAll
    public static void singleSetup() {
        world = spy(new WorldMock());
        doReturn(Biome.FOREST).when(world).getBiome(anyInt(), anyInt());
        doReturn(true).when(world).generateTree(any(Location.class), any(TreeType.class));
        server = spy(MockBukkit.mock());
        doReturn(world).when(server).getWorld(anyString());
        doReturn(world).when(server).getWorld(any(UUID.class));
        when(server.getWorlds()).thenReturn(Arrays.asList(world));

//        PluginDescriptionFile description = new PluginDescriptionFile("WorldEdit", "6", MockPlugin.class.getName());
//        JavaPlugin instance = server.getPluginManager().loadPlugin(MockPlugin.class, description);
//        server.getPluginManager().enablePlugin(instance);
//        MockPlugin worldEdit = spy(MockBukkit.createMockPlugin());
//        doReturn("WorldEdit").when(worldEdit).getDescription();
    }

    @BeforeEach
    public void setUp() {

        plugin = MockBukkit.load(CanopyPlugin.class);
    }

    @AfterAll
    public static void tearDown() {
        MockBukkit.unload();
    }

    @Test
    void testYamlSaveLoad() {
        Yaml yaml = new Yaml();
        PluginConfig config = CanopyPlugin.getDefaultConfig();
        String output = yaml.dumpAs(config, Tag.MAP, DumperOptions.FlowStyle.AUTO);
        System.out.println(output);
        PluginConfig config2;
        config2 = yaml.loadAs(output, PluginConfig.class);
        Recipe r = config2.getRecipes().get(0);
        assertThat(r.getBuilder().getStrategy().name(), is("SCHEMATIC"));
    }

    @Test
    void testStartup() throws IOException {
        for (int x = 0; x < 2; x++) {
            for (int z = 0; z < 2; z++) {
                Block block = world.getBlockAt(x, 0, z);
                block.setType(Material.SAPLING);
                block.setData(TreeSpecies.DARK_OAK.getData());
            }
        }
        world.getBlockAt(0, 0, 0).setData(TreeSpecies.ACACIA.getData());
        StructureGrowEvent event = new StructureGrowEvent(world.getBlockAt(0, 0, 0).getLocation(), TreeType.ACACIA, false, null, null);
        plugin.growTree(event);
        String directory = plugin.getPluginConfig().getSchematicsDirectory();
        assertThat(directory, is("schematics"));
        assertThat(Arrays.asList(new File(directory).list()),
                hasItems("acacia,birch,brown_mushroom,dark_oak,jungle,oak,red_mushroom,spruce".split(",")));
    }

    @Test
    void testRegex() {
        String regex = "birch/small/Birch[23]";
        String against1 = "schematics/birch/small/Birch4.schematic";
        String against2 = "schematics/birch/small/Birch3.schematic";
        String against3 = "schematics/jungle/large/JungleLarge1.schematic";

        Matcher matcher = Pattern.compile(regex).matcher("");
        assertThat(matcher.reset(against1).find(), is(false));
        assertThat(matcher.reset(against2).find(), is(true));
        assertThat(matcher.reset(against3).find(), is(false));
    }

    @Test
    void testGrid() {
        String[][] dest = new String[][]{
                {"A", "X"},
                {"A", "Q"}
        };
        String[][] source = new String[][]{
                {"B", "A", "H"},
                {"A", "A", "A"},
                {"A", "Q", "X"},
        };
        System.out.println("Beginning match process");
        Matrix<String> strings = Matrix.of(source);
        System.out.println(strings.toString());
        Matrix<String> rotatedOnce = strings.rotate();
        System.out.println(rotatedOnce.toString());
        Matrix<String> rotatedTwice = rotatedOnce.rotate();
        System.out.println(rotatedTwice.toString());
        Matrix<String> transformed = rotatedTwice.view(s -> "!" + s + "!");
        System.out.println(transformed.toString());
        System.out.println("containsRotated recipe: " + strings.containsRotated((Matrix<String>) Matrix.of(dest), (s, t) -> s.equals(t)));
    }
}
