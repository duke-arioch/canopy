package org.sandcast.canopy.config;

import java.util.Map;

/**
 * The plugin configuration.
 */

public class PluginConfig {

    private Map<String, Recipe> recipes;

    private String schematicsDirectory = "schematics";

    public Map<String, Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(Map<String, Recipe> recipes) {
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