package org.sandcast.canopy.config;

import java.util.ArrayList;
import java.util.List;

public class Recipe  {

    private List<List<String>> layout = new ArrayList<>();
    private List<String> biomes;
    private Builder builder;
    private Boolean randomRotation = true;

    public Boolean getRandomRotation() {
        return randomRotation;
    }

    public void setRandomRotation(Boolean randomRotation) {
        this.randomRotation = randomRotation;
    }

    public List<List<String>> getLayout() {
        return layout;
    }

    public void setLayout(List<List<String>> layout) {
        this.layout = layout;
    }

    public List<String> getBiomes() {
        return biomes;
    }

    public void setBiomes(List<String> biomes) {
        this.biomes = biomes;
    }

    public Builder getBuilder() {
        return builder;
    }

    public void setBuilder(Builder builder) {
        this.builder = builder;
    }

    public static class Builder {
        public enum Strategy {SCHEMATIC, PROCEDURAL, DEFAULT}

        private Strategy strategy = Strategy.SCHEMATIC;
        private List<String> arguments = new ArrayList<>();

        public Strategy getStrategy() {
            return strategy;
        }

        public void setStrategy(Strategy strategy) {
            this.strategy = strategy;
        }

        public List<String> getArguments() {
            return arguments;
        }

        public void setArguments(List<String> arguments) {
            this.arguments = arguments;
        }
    }
}
