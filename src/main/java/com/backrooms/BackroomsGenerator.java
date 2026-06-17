package com.backrooms;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Door;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class BackroomsGenerator extends ChunkGenerator {

    private static final int[] FLOORS = {40, 55, 70, 85, 100, 115, 130, 145, 160, 175, 190, 205, 220, 235};
    private static final int ROOM_HEIGHT = 6;
    private static final int MODULE = 40;

    private static final BlockData OAK_DOOR_BOTTOM;
    private static final BlockData OAK_DOOR_TOP;
    private static final BlockData SPRUCE_DOOR_BOTTOM;
    private static final BlockData SPRUCE_DOOR_TOP;
    private static final BlockData IRON_DOOR_BOTTOM;
    private static final BlockData IRON_DOOR_TOP;

    static {
        Door oakB = (Door) org.bukkit.Bukkit.createBlockData(Material.OAK_DOOR);
        oakB.setHalf(Bisected.Half.BOTTOM);
        OAK_DOOR_BOTTOM = oakB;
        Door oakT = (Door) org.bukkit.Bukkit.createBlockData(Material.OAK_DOOR);
        oakT.setHalf(Bisected.Half.TOP);
        OAK_DOOR_TOP = oakT;

        Door spruceB = (Door) org.bukkit.Bukkit.createBlockData(Material.SPRUCE_DOOR);
        spruceB.setHalf(Bisected.Half.BOTTOM);
        SPRUCE_DOOR_BOTTOM = spruceB;
        Door spruceT = (Door) org.bukkit.Bukkit.createBlockData(Material.SPRUCE_DOOR);
        spruceT.setHalf(Bisected.Half.TOP);
        SPRUCE_DOOR_TOP = spruceT;

        Door ironB = (Door) org.bukkit.Bukkit.createBlockData(Material.IRON_DOOR);
        ironB.setHalf(Bisected.Half.BOTTOM);
        IRON_DOOR_BOTTOM = ironB;
        Door ironT = (Door) org.bukkit.Bukkit.createBlockData(Material.IRON_DOOR);
        ironT.setHalf(Bisected.Half.TOP);
        IRON_DOOR_TOP = ironT;
    }

    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo,
                              @NotNull Random random,
                              int chunkX,
                              int chunkZ,
                              @NotNull ChunkData chunkData) {

        int[][] regionCache = new int[16][16];
        for (int x = 0; x < 16; x++) {
            int worldX = (chunkX << 4) + x;
            for (int z = 0; z < 16; z++) {
                int worldZ = (chunkZ << 4) + z;
                regionCache[x][z] = BackroomsRegionManager.getRegionType(worldX, worldZ);
            }
        }

        for (int x = 0; x < 16; x++) {
            int worldX = (chunkX << 4) + x;
            int localX = Math.floorMod(worldX, MODULE);

            for (int z = 0; z < 16; z++) {
                int worldZ = (chunkZ << 4) + z;
                int localZ = Math.floorMod(worldZ, MODULE);
                int region = regionCache[x][z];

                // Name aligned back to target spec to match dependencies
                if (shouldGenerateVoidShaft(localX, localZ, region)) {
                    chunkData.setRegion(x, FLOORS[0], z, x + 1, FLOORS[FLOORS.length - 1] + ROOM_HEIGHT + 1, z + 1, Material.AIR);
                    continue;
                }

                boolean baseWall = shouldGenerateWall(localX, localZ, region);
                boolean doorway = isDoorway(localX, localZ);
                boolean waterRoom = shouldGenerateWaterRoom(localX, localZ, region);
                boolean lavaRoom = shouldGenerateLavaRoom(localX, localZ, region);

                for (int floorY : FLOORS) {
                    int ceilingY = floorY + ROOM_HEIGHT;

                    if (floorY == 40 && worldX >= 8 && worldX <= 32 && worldZ >= 8 && worldZ <= 32) {
                        continue;
                    }

                    Material floorMaterial = BackroomsRegionManager.getFloorMaterial(region, floorY, FLOORS[0]);
                    Material wallMaterial = BackroomsRegionManager.getWallMaterial(region);
                    Material ceilingMaterial = BackroomsRegionManager.getCeilingMaterial(region);

                    chunkData.setBlock(x, floorY - 1, z, floorY == FLOORS[0] ? Material.BEDROCK : Material.STONE);
                    
                    if (floorY > 100 && region == BackroomsRegionManager.DARK && (worldX % 7 == 0 || worldZ % 7 == 0)) {
                        chunkData.setBlock(x, floorY, z, Material.MAGMA_BLOCK);
                    } else {
                        chunkData.setBlock(x, floorY, z, floorY == FLOORS[0] ? Material.BEDROCK : floorMaterial);
                    }
                    
                    chunkData.setBlock(x, ceilingY, z, ceilingMaterial);

                    if (isCorruptedDeadEnd(worldX, worldZ, localX, localZ, floorY)) {
                        chunkData.setRegion(x, floorY + 1, z, x + 1, ceilingY, z + 1, wallMaterial);
                        continue;
                    }

                    if (baseWall) {
                        if (!doorway) {
                            chunkData.setRegion(x, floorY + 1, z, x + 1, ceilingY, z + 1, wallMaterial);
                        } else if (shouldPlaceDoor(localX, localZ, worldX, worldZ, floorY)) {
                            placeDoor(chunkData, x, floorY, z, localX);
                        }
                    }

                    if (shouldGenerateLight(worldX, worldZ, region) && !baseWall) {
                        chunkData.setBlock(x, ceilingY - 1, z, Material.SEA_LANTERN);
                    }

                    if (shouldGenerateFurniture(worldX, worldZ, floorY) && !baseWall) {
                        BackroomsFurnitureGenerator.generateFurniture(chunkData, x, floorY, z, worldX, worldZ, region);
                    }
                }
            }
        }

        BackroomsRoomGenerator.generateMacroRooms(chunkData, chunkX, chunkZ, FLOORS, ROOM_HEIGHT);
        BackroomsShaftGenerator.generateShaftTraversals(chunkData, chunkX, chunkZ, FLOORS);
    }

    @Override
    public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        return List.of(
            new BackroomsLootPopulator(),
            new BackroomsDecorationPopulator(),
            new BackroomsMobPopulator()
        );
    }

    public @NotNull Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
        return new Location(world, 20.5, FLOORS[0] + 1, 20.5);
    }

    private boolean isCorruptedDeadEnd(int worldX, int worldZ, int localX, int localZ, int floorY) {
        long hash = ((long) worldX * 31213L) ^ ((long) worldZ * 82137L) ^ floorY;
        hash = (hash ^ (hash >>> 16)) * 0x45d9f3bL;
        double chance = Math.abs(hash % 1000) / 1000.0;
        return (localX == 19 && localZ == 12 && chance < 0.04) || (localX == 12 && localZ == 19 && chance < 0.04);
    }

    private boolean shouldGenerateWall(int localX, int localZ, int region) {
        if (region == BackroomsRegionManager.INDUSTRIAL) {
            boolean hallX = localX >= 16 && localX <= 23;
            boolean hallZ = localZ >= 16 && localZ <= 23;
            if (hallX || hallZ) return false;
            return localX % 10 == 0 || localZ % 10 == 0;
        }

        if (region == BackroomsRegionManager.FROZEN) {
            return localX == 0 || localZ == 0 || localX == 39 || localZ == 39;
        }

        boolean mainNorthSouthHall = localX >= 18 && localX <= 21;
        boolean mainEastWestHall = localZ >= 18 && localZ <= 21;

        if (mainNorthSouthHall || mainEastWestHall) return false;

        boolean roomBorder = localX == 8 || localX == 16 || localX == 24 || localX == 32 ||
                             localZ == 8 || localZ == 16 || localZ == 24 || localZ == 32;

        boolean outerWall = localX == 0 || localZ == 0 || localX == 39 || localZ == 39;

        return roomBorder || outerWall;
    }

    private boolean isDoorway(int localX, int localZ) {
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

    private boolean shouldPlaceDoor(int localX, int localZ, int worldX, int worldZ, int floorY) {
        boolean center = localX == 11 || localX == 19 || localX == 27 || localZ == 11 || localZ == 19 || localZ == 27;
        if (!center) return false;

        long hash = ((long) worldX * 92821L) ^ ((long) worldZ * 37199L) ^ floorY;
        return Math.abs(hash % 100) < 55;
    }

    private void placeDoor(ChunkData chunkData, int x, int floorY, int z, int localX) {
        long hash = ((long) x * 71237L) ^ floorY;
        int typeSelect = (int) Math.abs(hash % 3);
        
        BlockFace facing = (localX == 8 || localX == 16 || localX == 24 || localX == 32) ? BlockFace.EAST : BlockFace.SOUTH;
        
        Directional bottom = (Directional) (typeSelect == 0 ? OAK_DOOR_BOTTOM.clone() : (typeSelect == 1 ? SPRUCE_DOOR_BOTTOM.clone() : IRON_DOOR_BOTTOM.clone()));
        Directional top = (Directional) (typeSelect == 0 ? OAK_DOOR_TOP.clone() : (typeSelect == 1 ? SPRUCE_DOOR_TOP.clone() : IRON_DOOR_TOP.clone()));
        
        bottom.setFacing(facing);
        top.setFacing(facing);

        chunkData.setBlock(x, floorY + 1, z, bottom);
        chunkData.setBlock(x, floorY + 2, z, top);
    }

    private boolean shouldGenerateLight(int worldX, int worldZ, int region) {
        if (region == BackroomsRegionManager.DARK) {
            return Math.floorMod(worldX, 16) == 8 && Math.floorMod(worldZ, 16) == 8;
        }
        return Math.floorMod(worldX, 6) == 3 && Math.floorMod(worldZ, 6) == 3;
    }

    private boolean shouldGenerateFurniture(int worldX, int worldZ, int floorY) {
        long hash = ((long) worldX * 73428767L) ^ ((long) worldZ * 912931L) ^ floorY;
        return Math.abs(hash % 100) < 5;
    }

    private boolean shouldGenerateWaterRoom(int localX, int localZ, int region) {
        if (region == BackroomsRegionManager.FLOODED) return true;
        return localX > 5 && localX < 15 && localZ > 25 && localZ < 35;
    }

    private boolean shouldGenerateLavaRoom(int localX, int localZ, int region) {
        if (region == BackroomsRegionManager.FLOODED || region == BackroomsRegionManager.INDUSTRIAL) return false;
        return localX > 25 && localX < 35 && localZ > 5 && localZ < 15;
    }

    private boolean shouldGenerateVoidShaft(int localX, int localZ, int region) {
        if (region == BackroomsRegionManager.CLASSIC || region == BackroomsRegionManager.FLOODED) return false;
        return localX >= 36 && localX <= 39 && localZ >= 36 && localZ <= 39;
    }

    @Override
    public boolean shouldGenerateBedrock() { return false; }

    @Override
    public boolean shouldGenerateCaves() { return false; }
}