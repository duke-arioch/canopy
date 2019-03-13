package org.sandcast.canopy.config;

import java.util.List;

/**
 * The plugin configuration.
 */

public class PluginConfig {

    private List<Recipe> recipes;

    private String schematicsDirectory = "schematics";

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    public String getSchematicsDirectory() {
        return schematicsDirectory;
    }

    public void setSchematicsDirectory(String schematicsDirectory) {
        this.schematicsDirectory = schematicsDirectory;
    }

    public PluginConfig() {
    }
}