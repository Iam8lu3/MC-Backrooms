package com.backrooms;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BackroomsGenerator extends ChunkGenerator {

    private static final int LAYER_HEIGHT = 6; 
    private static final int FLOOR_THICKNESS = 2; 

    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        long seed = worldInfo.getSeed();
        int minY = worldInfo.getMinHeight();
        int maxY = worldInfo.getMaxHeight();

        boolean isSpawnChunk = (chunkX == 0 && chunkZ == 0);

        // Deterministic seed for the 5% rare resource vault roll
        long chunkSeed = (long) chunkX * 341873128712L + (long) chunkZ * 132897987541L ^ seed;
        Random chunkRand = new Random(chunkSeed);
        boolean isOreCoreChunk = !isSpawnChunk && (chunkRand.nextInt(100) < 5);

        for (int y = minY; y < maxY - (LAYER_HEIGHT + FLOOR_THICKNESS); y += (LAYER_HEIGHT + FLOOR_THICKNESS)) {
            int floorY1 = y;
            int floorY2 = y + 1;
            int ceilingY1 = y + LAYER_HEIGHT;
            int ceilingY2 = y + LAYER_HEIGHT + 1;

            for (int localX = 0; localX < 16; localX++) {
                for (int localZ = 0; localZ < 16; localZ++) {

                    // Floors
                    chunkData.setBlock(localX, floorY1, localZ, Material.LIGHT_GRAY_CONCRETE);
                    chunkData.setBlock(localX, floorY2, localZ, Material.LIGHT_GRAY_CONCRETE);

                    // Ceilings
                    chunkData.setBlock(localX, ceilingY1, localZ, Material.LIGHT_GRAY_CONCRETE);
                    chunkData.setBlock(localX, ceilingY2, localZ, Material.LIGHT_GRAY_CONCRETE);

                    // Fluorescent Lights
                    if (localX % 4 == 0 && localZ % 4 == 0) {
                        chunkData.setBlock(localX, ceilingY1, localZ, Material.PEARLESCENT_FROGLIGHT);
                    }

                    // Room Volume Fill
                    for (int roomY = floorY2 + 1; roomY < ceilingY1; roomY++) {
                        if (isSpawnChunk) {
                            if (localX == 0 || localX == 15 || localZ == 0 || localZ == 15) {
                                chunkData.setBlock(localX, roomY, localZ, Material.YELLOW_TERRACOTTA);
                            } else {
                                chunkData.setBlock(localX, roomY, localZ, Material.AIR);
                            }
                            continue;
                        }

                        if (isOreCoreChunk) {
                            if (localX == 0 || localX == 15 || localZ == 0 || localZ == 15) {
                                chunkData.setBlock(localX, roomY, localZ, Material.YELLOW_TERRACOTTA);
                            } else {
                                int oreRoll = chunkRand.nextInt(100);
                                if (oreRoll < 2) {
                                    chunkData.setBlock(localX, roomY, localZ, Material.DIAMOND_ORE);
                                } else if (oreRoll < 10) {
                                    chunkData.setBlock(localX, roomY, localZ, Material.GOLD_ORE);
                                } else if (oreRoll < 25) {
                                    chunkData.setBlock(localX, roomY, localZ, Material.IRON_ORE);
                                } else {
                                    chunkData.setBlock(localX, roomY, localZ, Material.COBBLESTONE);
                                }
                            }
                            continue;
                        }

                        // Standard layout
                        if (localX == 0 || localX == 15 || localZ == 0 || localZ == 15) {
                            chunkData.setBlock(localX, roomY, localZ, Material.YELLOW_TERRACOTTA);
                        } else {
                            chunkData.setBlock(localX, roomY, localZ, Material.AIR);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull ChunkData chunkData) {}

    @Override
    public void generateBedrock(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull ChunkData chunkData) {}
}