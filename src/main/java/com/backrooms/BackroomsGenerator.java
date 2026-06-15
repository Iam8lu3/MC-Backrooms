package com.backrooms;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BackroomsGenerator extends ChunkGenerator {

    private static final int[] FLOORS = {40, 55, 70, 85, 100};
    private static final int ROOM_HEIGHT = 6;
    private static final int MODULE = 40;

    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo,
                              @NotNull Random random,
                              int chunkX,
                              int chunkZ,
                              @NotNull ChunkData chunkData) {

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

                    if (shouldGenerateWall(worldX, worldZ)
                            && !isDoorway(worldX, worldZ)) {

                        for (int y = floorY + 1; y < ceilingY; y++) {
                            chunkData.setBlock(x, y, z, wallMaterial);
                        }
                    }

                    if (shouldGenerateLight(worldX, worldZ)
                            && !shouldGenerateWall(worldX, worldZ)) {

                        chunkData.setBlock(
                                x,
                                ceilingY - 1,
                                z,
                                Material.SEA_LANTERN
                        );
                    }

                    if (shouldGenerateFurniture(worldX, worldZ, floorY)
                            && !shouldGenerateWall(worldX, worldZ)) {

                        generateFurniture(
                                chunkData,
                                x,
                                floorY,
                                z,
                                worldX,
                                worldZ
                        );
                    }

                    if (shouldGenerateWaterRoom(worldX, worldZ, floorY)) {
                        chunkData.setBlock(
                                x,
                                floorY + 1,
                                z,
                                Material.WATER
                        );
                    }

                    if (shouldGenerateLavaRoom(worldX, worldZ, floorY)) {
                        chunkData.setBlock(
                                x,
                                floorY,
                                z,
                                Material.NETHERRACK
                        );

                        chunkData.setBlock(
                                x,
                                floorY + 1,
                                z,
                                Material.LAVA
                        );
                    }

                    if (shouldGenerateVoidShaft(worldX, worldZ)) {

                        for (int y = FLOORS[0];
                             y <= FLOORS[FLOORS.length - 1] + ROOM_HEIGHT;
                             y++) {

                            chunkData.setBlock(
                                    x,
                                    y,
                                    z,
                                    Material.AIR
                            );
                        }
                    }
                }

                generateStairwells(
                        chunkData,
                        x,
                        z,
                        worldX,
                        worldZ
                );
            }
        }
    }

    private boolean shouldGenerateWall(
            int worldX,
            int worldZ
    ) {

        int localX = Math.floorMod(worldX, MODULE);
        int localZ = Math.floorMod(worldZ, MODULE);

        boolean mainNorthSouthHall =
                localX >= 18 && localX <= 21;

        boolean mainEastWestHall =
                localZ >= 18 && localZ <= 21;

        if (mainNorthSouthHall || mainEastWestHall) {
            return false;
        }

        boolean roomBorder =
                localX == 8 ||
                localX == 16 ||
                localX == 24 ||
                localX == 32 ||
                localZ == 8 ||
                localZ == 16 ||
                localZ == 24 ||
                localZ == 32;

        boolean outerWall =
                localX == 0 ||
                localZ == 0 ||
                localX == 39 ||
                localZ == 39;

        return roomBorder || outerWall;
    }

    private boolean isDoorway(
            int worldX,
            int worldZ
    ) {

        int localX = Math.floorMod(worldX, MODULE);
        int localZ = Math.floorMod(worldZ, MODULE);

        return

                (localX == 8  && localZ >= 10 && localZ <= 12) ||
                (localX == 16 && localZ >= 18 && localZ <= 20) ||
                (localX == 24 && localZ >= 26 && localZ <= 28) ||
                (localX == 32 && localZ >= 10 && localZ <= 12) ||

                (localZ == 8  && localX >= 10 && localX <= 12) ||
                (localZ == 16 && localX >= 18 && localX <= 20) ||
                (localZ == 24 && localX >= 26 && localX <= 28) ||
                (localZ == 32 && localX >= 10 && localX <= 12);
    }

    private boolean shouldGenerateLight(
            int worldX,
            int worldZ
    ) {

        return
                Math.floorMod(worldX, 6) == 3 &&
                Math.floorMod(worldZ, 6) == 3;
    }

    private boolean shouldGenerateFurniture(
            int worldX,
            int worldZ,
            int floorY
    ) {

        long seed =
                worldX * 73428767L ^
                worldZ * 912931L ^
                floorY * 19349663L;

        Random r = new Random(seed);

        return r.nextDouble() < 0.02;
    }
    private boolean shouldGenerateWaterRoom(
            int worldX,
            int worldZ,
            int floorY
    ) {

        if (floorY < 70) return false;

        int localX = Math.floorMod(worldX, MODULE);
        int localZ = Math.floorMod(worldZ, MODULE);

        int moduleX = Math.floorDiv(worldX, MODULE);
        int moduleZ = Math.floorDiv(worldZ, MODULE);

        long seed =
                moduleX * 87178291199L ^
                moduleZ * 48271L ^
                floorY;

        Random r = new Random(seed);

        return
                r.nextDouble() < 0.08 &&
                localX > 5 &&
                localX < 15 &&
                localZ > 25 &&
                localZ < 35;
    }

    private boolean shouldGenerateLavaRoom(
            int worldX,
            int worldZ,
            int floorY
    ) {

        if (floorY < 85) return false;

        int localX = Math.floorMod(worldX, MODULE);
        int localZ = Math.floorMod(worldZ, MODULE);

        int moduleX = Math.floorDiv(worldX, MODULE);
        int moduleZ = Math.floorDiv(worldZ, MODULE);

        long seed =
                moduleX * 192837465L ^
                moduleZ * 918273645L ^
                floorY;

        Random r = new Random(seed);

        return
                r.nextDouble() < 0.05 &&
                localX > 25 &&
                localX < 35 &&
                localZ > 5 &&
                localZ < 15;
    }

    private boolean shouldGenerateVoidShaft(
            int worldX,
            int worldZ
    ) {

        int localX = Math.floorMod(worldX, MODULE);
        int localZ = Math.floorMod(worldZ, MODULE);

        int moduleX = Math.floorDiv(worldX, MODULE);
        int moduleZ = Math.floorDiv(worldZ, MODULE);

        long seed =
                moduleX * 341873128712L ^
                moduleZ * 132897987541L;

        Random r = new Random(seed);

        return
                r.nextDouble() < 0.03 &&
                localX >= 36 &&
                localX <= 39 &&
                localZ >= 36 &&
                localZ <= 39;
    }

    private void generateFurniture(
            ChunkData chunkData,
            int x,
            int floorY,
            int z,
            int worldX,
            int worldZ
    ) {

        long seed =
                worldX * 92821L +
                worldZ * 37199L +
                floorY;

        Random r = new Random(seed);

        double roll = r.nextDouble();

        if (roll < 0.15) {

            chunkData.setBlock(
                    x,
                    floorY + 1,
                    z,
                    Material.BARREL
            );
        }
        else if (roll < 0.30) {

            chunkData.setBlock(
                    x,
                    floorY + 1,
                    z,
                    Material.BOOKSHELF
            );
        }
        else if (roll < 0.45) {

            chunkData.setBlock(
                    x,
                    floorY + 1,
                    z,
                    Material.CHEST
            );
        }
        else if (roll < 0.60) {

            chunkData.setBlock(
                    x,
                    floorY + 1,
                    z,
                    Material.CRAFTING_TABLE
            );
        }
        else if (roll < 0.75) {

            chunkData.setBlock(
                    x,
                    floorY + 1,
                    z,
                    Material.OAK_FENCE
            );

            chunkData.setBlock(
                    x,
                    floorY + 2,
                    z,
                    Material.OAK_PRESSURE_PLATE
            );
        }
        else {

            chunkData.setBlock(
                    x,
                    floorY + 1,
                    z,
                    Material.LECTERN
            );
        }
    }

    private void generateStairwells(
            ChunkData chunkData,
            int x,
            int z,
            int worldX,
            int worldZ
    ) {

        int localX = Math.floorMod(worldX, MODULE);
        int localZ = Math.floorMod(worldZ, MODULE);

        int moduleX = Math.floorDiv(worldX, MODULE);
        int moduleZ = Math.floorDiv(worldZ, MODULE);

        long seed =
                moduleX * 456789123L ^
                moduleZ * 987654321L;

        Random r = new Random(seed);

        if (r.nextDouble() > 0.25)
            return;

        if (!(localX >= 20 &&
              localX <= 30 &&
              localZ >= 20 &&
              localZ <= 30))
            return;

        for (int i = 0; i < FLOORS.length - 1; i++) {

            int lower = FLOORS[i];
            int upper = FLOORS[i + 1];

            for (int y = lower;
                 y <= upper + ROOM_HEIGHT;
                 y++) {

                chunkData.setBlock(
                        x,
                        y,
                        z,
                        Material.AIR
                );
            }
            if (localX == 20 ||
                localX == 30 ||
                localZ == 20 ||
                localZ == 30) {

                for (int y = lower;
                     y < upper;
                     y++) {

                    chunkData.setBlock(
                            x,
                            y,
                            z,
                            Material.LIGHT_GRAY_CONCRETE
                    );
                }
            }

            int step = localX - 21;

            if (localZ >= 24 &&
                localZ <= 26 &&
                step >= 0 &&
                step <= 8) {

                int stairY = lower + 1 + step;

                Material stairMaterial;

                if (lower < 55) {

                    stairMaterial =
                            Material.SANDSTONE_STAIRS;

                } else if (lower < 85) {

                    stairMaterial =
                            Material.STONE_BRICK_STAIRS;

                } else {

                    stairMaterial =
                            Material.NETHER_BRICK_STAIRS;
                }

                chunkData.setBlock(
                        x,
                        stairY,
                        z,
                        stairMaterial
                );

                if (stairY + 1 <= upper) {

                    chunkData.setBlock(
                            x,
                            stairY + 1,
                            z,
                            Material.AIR
                    );
                }

                if (stairY + 2 <= upper) {

                    chunkData.setBlock(
                            x,
                            stairY + 2,
                            z,
                            Material.AIR
                    );
                }
            }

            if (localX >= 23 &&
                localX <= 27 &&
                localZ >= 23 &&
                localZ <= 27) {

                int landingY =
                        lower +
                        ((upper - lower) / 2);

                chunkData.setBlock(
                        x,
                        landingY,
                        z,
                        Material.SMOOTH_STONE
                );
            }

            if ((localX == 25 &&
                 localZ == 20) ||

                (localX == 25 &&
                 localZ == 30)) {

                for (int y = lower + 1;
                     y < lower + 5;
                     y++) {

                    chunkData.setBlock(
                            x,
                            y,
                            z,
                            Material.AIR
                    );
                }
            }
        }
    }

    private Material getFloorMaterial(
            int floorY
    ) {

        if (floorY < 55)
            return Material.YELLOW_CARPET;

        if (floorY < 70)
            return Material.SMOOTH_SANDSTONE;

        if (floorY < 85)
            return Material.GRAY_CONCRETE;

        if (floorY < 100)
            return Material.DARK_PRISMARINE;

        return Material.BLACKSTONE;
    }

    private Material getWallMaterial(
            int floorY
    ) {

        if (floorY < 55)
            return Material.YELLOW_TERRACOTTA;

        if (floorY < 70)
            return Material.LIGHT_GRAY_CONCRETE;

        if (floorY < 85)
            return Material.POLISHED_ANDESITE;

        if (floorY < 100)
            return Material.BRICKS;

        return Material.BLACK_CONCRETE;
    }

    private Material getCeilingMaterial(
            int floorY
    ) {

        if (floorY < 70)
            return Material.SMOOTH_SANDSTONE;

        if (floorY < 100)
            return Material.STRIPPED_OAK_WOOD;

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