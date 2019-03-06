package org.sandcast.canopy.util;

import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;

public class DescriptionUtils {

    public static String describe(Block block) {
        final Material material = block.getType();
        final byte data = block.getData();
        final String description;
        switch(material) {
            case SAPLING:
                    TreeSpecies species = TreeSpecies.getByData((byte) (data % 8));
                    description = species == null ? "SAPLING" : species.name() + "_SAPLING";
                    break;
            default: description = material.name();
        }
        return description;
    }


}
