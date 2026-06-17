package com.backrooms;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.generator.ChunkGenerator.ChunkData;

public class BackroomsShaftGenerator {

    private static final int MODULE = 40;
    private static final int ROOM_HEIGHT = 6;
    private static final BlockData LADDER_NORTH;

    static {
        Ladder lad = (Ladder) org.bukkit.Bukkit.createBlockData(Material.LADDER);
        lad.setFacing(BlockFace.NORTH);
        LADDER_NORTH = lad;
    }

    public static void generateShaftTraversals(ChunkData chunkData, int chunkX, int chunkZ, int[] floors) {
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        int sampleWorldX = startX + 8;
        int sampleWorldZ = startZ + 8;
        int moduleX = Math.floorDiv(sampleWorldX, MODULE);
        int moduleZ = Math.floorDiv(sampleWorldZ, MODULE);

        long mHash = ((long) moduleX * 456789123L) ^ ((long) moduleZ * 987654321L);
        if (Math.abs(mHash % 100) > 25) return;

        int minX = Math.max(0, (moduleX * MODULE + 22) - startX);
        int maxX = Math.min(16, (moduleX * MODULE + 29) - startX);
        int minZ = Math.max(0, (moduleZ * MODULE + 22) - startZ);
        int maxZ = Math.min(16, (moduleZ * MODULE + 29) - startZ);

        if (minX >= maxX || minZ >= maxZ) return;

        int lowestY = floors[0];
        int highestY = floors[floors.length - 1] + ROOM_HEIGHT;

        chunkData.setRegion(minX, lowestY, minZ, maxX, highestY + 1, maxZ, Material.AIR);

        for (int x = minX; x < maxX; x++) {
            int worldX = startX + x;
            int localX = Math.floorMod(worldX, MODULE);

            for (int z = minZ; z < maxZ; z++) {
                int worldZ = startZ + z;
                int localZ = Math.floorMod(worldZ, MODULE);

                boolean frameWall = localX == 22 || localX == 28 || localZ == 22 || localZ == 28;

                for (int i = 0; i < floors.length - 1; i++) {
                    int lower = floors[i];
                    int upper = floors[i + 1];

                    if (frameWall) {
                        chunkData.setRegion(x, lower, z, x + 1, upper, z + 1, Material.LIGHT_GRAY_CONCRETE);
                    }

                    int stepIndex = localX - 23;
                    if (localZ >= 24 && localZ <= 25 && stepIndex >= 0 && stepIndex <= 4) {
                        int slabY = lower + 1 + stepIndex;
                        if (slabY <= upper) {
                            chunkData.setBlock(x, slabY, z, Material.SMOOTH_STONE_SLAB);
                        }
                    }

                    if (localX == 23 && localZ == 23) {
                        for (int y = lower + 1; y <= upper + 1; y++) {
                            chunkData.setBlock(x, y, z, LADDER_NORTH);
                        }
                    }
                }
            }
        }
    }
}