package com.backrooms;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BackroomsGenerator extends ChunkGenerator {

    private static final int[] FLOORS = {40, 55, 70, 85, 100};
    private static final int ROOM_HEIGHT = 6;
    private static final int MODULE = 32;

    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                int worldX = (chunkX << 4) + x;
                int worldZ = (chunkZ << 4) + z;

                for (int floorY : FLOORS) {
                    int ceilingY = floorY + ROOM_HEIGHT;

                    Material floorMaterial = getFloorMaterial(floorY);
                    Material wallMaterial = getWallMaterial(floorY);
                    Material ceilingMaterial = getCeilingMaterial(floorY);

                    chunkData.setBlock(x, floorY - 1, z, Material.STONE);
                    chunkData.setBlock(x, floorY, z, floorMaterial);

                    for (int y = floorY + 1; y < ceilingY; y++) {
                        chunkData.setBlock(x, y, z, Material.AIR);
                    }

                    chunkData.setBlock(x, ceilingY, z, ceilingMaterial);

                    if (shouldGenerateWall(worldX, worldZ) && !isDoorway(worldX, worldZ)) {
                        for (int y = floorY + 1; y < ceilingY; y++) {
                            chunkData.setBlock(x, y, z, wallMaterial);
                        }
                    }

                    if (shouldGenerateLight(worldX, worldZ) && !shouldGenerateWall(worldX, worldZ)) {
                        chunkData.setBlock(x, ceilingY - 1, z, Material.SEA_LANTERN);
                    }

                    if (shouldGenerateFurniture(worldX, worldZ, floorY) && !shouldGenerateWall(worldX, worldZ)) {
                        generateFurniture(chunkData, x, floorY, z);
                    }

                    if (shouldGenerateWaterRoom(worldX, worldZ, floorY)) {
                        chunkData.setBlock(x, floorY + 1, z, Material.WATER);
                    }

                    if (shouldGenerateLavaRoom(worldX, worldZ, floorY)) {
                        chunkData.setBlock(x, floorY, z, Material.NETHERRACK);
                        chunkData.setBlock(x, floorY + 1, z, Material.LAVA);
                    }

                    if (shouldGenerateVoidShaft(worldX, worldZ)) {
                        for (int y = FLOORS[0]; y <= FLOORS[FLOORS.length - 1] + ROOM_HEIGHT; y++) {
                            chunkData.setBlock(x, y, z, Material.AIR);
                        }
                    }
                }

                generateStairwells(chunkData, x, z, worldX, worldZ);
            }
        }
    }

    private boolean shouldGenerateWall(int worldX, int worldZ) {
        int localX = Math.floorMod(worldX, MODULE);
        int localZ = Math.floorMod(worldZ, MODULE);

        boolean mainNorthSouthHall = localX >= 14 && localX <= 17;
        boolean mainEastWestHall = localZ >= 14 && localZ <= 17;

        if (mainNorthSouthHall || mainEastWestHall) {
            return false;
        }

        boolean roomBorder =
                localX == 6 || localX == 13 || localX == 18 || localX == 25 ||
                localZ == 6 || localZ == 13 || localZ == 18 || localZ == 25;

        boolean outerModuleWall =
                localX == 0 || localZ == 0 || localX == 31 || localZ == 31;

        boolean brokenPartition =
                ((worldX + worldZ) % 19 == 0 || (worldX - worldZ) % 23 == 0) &&
                localX > 3 && localX < 28 &&
                localZ > 3 && localZ < 28;

        return roomBorder || outerModuleWall || brokenPartition;
    }

    private boolean isDoorway(int worldX, int worldZ) {
        int localX = Math.floorMod(worldX, MODULE);
        int localZ = Math.floorMod(worldZ, MODULE);

        boolean doorIntoNorthRoom = localZ == 13 && localX >= 14 && localX <= 17;
        boolean doorIntoSouthRoom = localZ == 18 && localX >= 14 && localX <= 17;
        boolean doorIntoWestRoom = localX == 13 && localZ >= 14 && localZ <= 17;
        boolean doorIntoEastRoom = localX == 18 && localZ >= 14 && localZ <= 17;

        boolean extraDoorway =
                (localX == 6 && localZ == 10) ||
                (localX == 25 && localZ == 21) ||
                (localZ == 6 && localX == 21) ||
                (localZ == 25 && localX == 10);

        return doorIntoNorthRoom || doorIntoSouthRoom || doorIntoWestRoom || doorIntoEastRoom || extraDoorway;
    }

    private boolean shouldGenerateLight(int worldX, int worldZ) {
        return Math.floorMod(worldX, 8) == 4 && Math.floorMod(worldZ, 8) == 4;
    }

    private boolean shouldGenerateFurniture(int worldX, int worldZ, int floorY) {
        long seed = worldX * 73428767L ^ worldZ * 912931L ^ floorY * 19349663L;
        Random r = new Random(seed);
        return r.nextDouble() < 0.006;
    }

    private boolean shouldGenerateWaterRoom(int worldX, int worldZ, int floorY) {
        if (floorY < 70) return false;

        int localX = Math.floorMod(worldX, MODULE);
        int localZ = Math.floorMod(worldZ, MODULE);

        int moduleX = Math.floorDiv(worldX, MODULE);
        int moduleZ = Math.floorDiv(worldZ, MODULE);

        long seed = moduleX * 87178291199L ^ moduleZ * 48271L ^ floorY;
        Random r = new Random(seed);

        return r.nextDouble() < 0.08 &&
                localX > 4 && localX < 13 &&
                localZ > 19 && localZ < 28;
    }

    private boolean shouldGenerateLavaRoom(int worldX, int worldZ, int floorY) {
        if (floorY < 85) return false;

        int localX = Math.floorMod(worldX, MODULE);
        int localZ = Math.floorMod(worldZ, MODULE);

        int moduleX = Math.floorDiv(worldX, MODULE);
        int moduleZ = Math.floorDiv(worldZ, MODULE);

        long seed = moduleX * 192837465L ^ moduleZ * 918273645L ^ floorY;
        Random r = new Random(seed);

        return r.nextDouble() < 0.05 &&
                localX > 19 && localX < 28 &&
                localZ > 4 && localZ < 13;
    }

    private boolean shouldGenerateVoidShaft(int worldX, int worldZ) {
        int localX = Math.floorMod(worldX, MODULE);
        int localZ = Math.floorMod(worldZ, MODULE);

        int moduleX = Math.floorDiv(worldX, MODULE);
        int moduleZ = Math.floorDiv(worldZ, MODULE);

        long seed = moduleX * 341873128712L ^ moduleZ * 132897987541L;
        Random r = new Random(seed);

        return r.nextDouble() < 0.035 &&
                localX >= 28 && localX <= 30 &&
                localZ >= 28 && localZ <= 30;
    }

    private void generateFurniture(ChunkData chunkData, int x, int floorY, int z) {
        chunkData.setBlock(x, floorY + 1, z, Material.OAK_FENCE);
        chunkData.setBlock(x, floorY + 2, z, Material.OAK_PRESSURE_PLATE);
    }

    private void generateStairwells(ChunkData chunkData, int x, int z, int worldX, int worldZ) {
        int localX = Math.floorMod(worldX, MODULE);
        int localZ = Math.floorMod(worldZ, MODULE);

        int moduleX = Math.floorDiv(worldX, MODULE);
        int moduleZ = Math.floorDiv(worldZ, MODULE);

        long seed = moduleX * 456789123L ^ moduleZ * 987654321L;
        Random r = new Random(seed);

        if (r.nextDouble() > 0.18) return;

        boolean insideStairwell =
                localX >= 21 && localX <= 27 &&
                localZ >= 21 && localZ <= 27;

        if (!insideStairwell) return;

        for (int i = 0; i < FLOORS.length - 1; i++) {
            int lower = FLOORS[i];
            int upper = FLOORS[i + 1];

            for (int y = lower + 1; y <= upper + ROOM_HEIGHT; y++) {
                chunkData.setBlock(x, y, z, Material.AIR);
            }

            chunkData.setBlock(x, lower, z, Material.SMOOTH_SANDSTONE);

            int step = localX - 21;

            if (localZ >= 23 && localZ <= 25 && step >= 0 && step <= 6) {
                int stairY = lower + 1 + step * 2;

                if (stairY < upper) {
                    chunkData.setBlock(x, stairY, z, Material.SMOOTH_SANDSTONE);
                    chunkData.setBlock(x, stairY + 1, z, Material.SMOOTH_SANDSTONE);
                }
            }

            if (localX == 21 || localX == 27 || localZ == 21 || localZ == 27) {
                for (int y = lower + 1; y < upper; y++) {
                    chunkData.setBlock(x, y, z, Material.YELLOW_TERRACOTTA);
                }
            }

            if ((localX == 24 && localZ == 21) || (localX == 24 && localZ == 27)) {
                for (int y = lower + 1; y < lower + 4; y++) {
                    chunkData.setBlock(x, y, z, Material.AIR);
                }
            }
        }
    }

    private Material getFloorMaterial(int floorY) {
        if (floorY < 55) return Material.YELLOW_CARPET;
        if (floorY < 70) return Material.SMOOTH_SANDSTONE;
        if (floorY < 85) return Material.GRAY_CONCRETE;
        if (floorY < 100) return Material.DARK_PRISMARINE;
        return Material.BLACKSTONE;
    }

    private Material getWallMaterial(int floorY) {
        if (floorY < 55) return Material.YELLOW_TERRACOTTA;
        if (floorY < 70) return Material.LIGHT_GRAY_CONCRETE;
        if (floorY < 85) return Material.POLISHED_ANDESITE;
        if (floorY < 100) return Material.BRICKS;
        return Material.BLACK_CONCRETE;
    }

    private Material getCeilingMaterial(int floorY) {
        if (floorY < 70) return Material.SMOOTH_SANDSTONE;
        if (floorY < 100) return Material.STRIPPED_OAK_WOOD;
        return Material.DEEPSLATE_TILES;
    }

    @Override
    public boolean shouldGenerateBedrock() {
        return false;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }
}