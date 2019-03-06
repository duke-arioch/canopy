# Canopy Spigot Plugin

Grow custom trees on your Spigot server. Designed to work with Realistic Biomes. Soft dependency on WorldEdit for schematics rendering.
 
Some aspects borrowed from [OwnGarden](https://github.com/Skyost/OwnGarden)

## Concepts
### Recipes
A recipe includes an ingredient grid and a Strategy. 

The ingredient grid lays out what must exist on the ground in order for the recipe to be active. These *must* be a square (length = height) 

Any received structure grow event triggers search for applicable recipes.

Each recipe is assocated with a strategy, which tells Canopy which tree construction framework to invoke and passes it a string array of arguments.

### SCHEMATIC Strategy

If WorldEdit is loaded and a recipe calling for the SCHEMATIC strategy is invoked, the argument list represents regexes for pattern matching the schematics files.  

For example, birch/small/Birch[24] will match either birch/small/Birch2 or birch/small/Birch4 schematic file. When more than one match is found, a match is selected randomly.

### PROCEDURAL Strategy

If someone wants to contribute this I am not averse to the idea. Something along the lines of the [Terasology/GrowingFlora](https://github.com/Terasology/GrowingFlora)

## Configuration File Example
```yaml
recipes:
  chimera:
    biomes: [FOREST]
    builder:
      arguments: ['birch/small/Birch[2,3]']
      strategy: SCHEMATIC
    layout:
    - [DARK_OAK_SAPLING, DARK_OAK_SAPLING]
    - [DARK_OAK_SAPLING, ACACIA_SAPLING]
    randomRotation: true
  chimera2:
    biomes: [FOREST]
    builder:
      arguments: ['JungleLarge[1234]']
      strategy: SCHEMATIC
    layout:
    - [ACACIA_SAPLING, DARK_OAK_SAPLING]
    - [BIRCH_SAPLING, ACACIA_SAPLING]
    randomRotation: true
schematicsDirectory: schematics
```
