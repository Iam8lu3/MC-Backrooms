package com.backrooms;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

public class BackroomsRoomGenerator {

    private static final int MODULE = 40;

    public static void generateMacroRooms(ChunkData chunkData, int chunkX, int chunkZ, int[] floors, int roomHeight) {
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        for (int floorY : floors) {
            if (floorY < 70) continue;

            int moduleX = Math.floorDiv(startX + 5, MODULE);
            int moduleZ = Math.floorDiv(startZ + 25, MODULE);
            int region = BackroomsRegionManager.getRegionType(moduleX * MODULE + 5, moduleZ * MODULE + 25);

            if (region == BackroomsRegionManager.FLOODED) {
                chunkData.setRegion(0, floorY + 1, 0, 16, floorY + roomHeight, 16, Material.WATER);
            } else {
                int localRoomStartX = Math.max(0, (moduleX * MODULE + 6) - startX);
                int localRoomEndX = Math.min(16, (moduleX * MODULE + 15) - startX);
                int localRoomStartZ = Math.max(0, (moduleZ * MODULE + 26) - startZ);
                int localRoomEndZ = Math.min(16, (moduleZ * MODULE + 35) - startZ);

                if (localRoomStartX < localRoomEndX && localRoomStartZ < localRoomEndZ) {
                    chunkData.setRegion(localRoomStartX, floorY + 1, localRoomStartZ, localRoomEndX, floorY + roomHeight, localRoomEndZ, Material.WATER);
                }
            }
        }

        for (int floorY : floors) {
            if (floorY < 85) continue;

            int moduleX = Math.floorDiv(startX + 25, MODULE);
            int moduleZ = Math.floorDiv(startZ + 5, MODULE);
            int region = BackroomsRegionManager.getRegionType(moduleX * MODULE + 25, moduleZ * MODULE + 5);

            if (region != BackroomsRegionManager.FLOODED && region != BackroomsRegionManager.INDUSTRIAL) {
                int localRoomStartX = Math.max(0, (moduleX * MODULE + 26) - startX);
                int localRoomEndX = Math.min(16, (moduleX * MODULE + 35) - startX);
                int localRoomStartZ = Math.max(0, (moduleZ * MODULE + 6) - startZ);
                int localRoomEndZ = Math.min(16, (moduleZ * MODULE + 15) - startZ);

                if (localRoomStartX < localRoomEndX && localRoomStartZ < localRoomEndZ) {
                    chunkData.setRegion(localRoomStartX, floorY, localRoomStartZ, localRoomEndX, floorY + 1, localRoomEndZ, Material.NETHERRACK);
                    chunkData.setRegion(localRoomStartX, floorY + 1, localRoomStartZ, localRoomEndX, floorY + roomHeight - 1, localRoomEndZ, Material.LAVA);
                }
            }
        }

        if (floors[0] == 40 && startX <= 32 && startX >= -16 && startZ <= 32 && startZ >= -16) {
            generateSpawnRoom(chunkData, chunkX, chunkZ, roomHeight);
        }
    }

    private static void generateSpawnRoom(ChunkData chunkData, int chunkX, int chunkZ, int roomHeight) {
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        int floorY = 40;
        int ceilingY = floorY + roomHeight;

        int minX = Math.max(0, 8 - startX);
        int maxX = Math.min(16, 33 - startX); // Bound clip fix
        int minZ = Math.max(0, 8 - startZ);
        int maxZ = Math.min(16, 33 - startZ); // Bound clip fix

        if (minX >= maxX || minZ >= maxZ) return;

        // Bedrock compressed to floorY exactly to stop floor compression loss
        chunkData.setRegion(minX, floorY - 1, minZ, maxX, floorY, maxZ, Material.BEDROCK);
        chunkData.setRegion(minX, floorY, minZ, maxX, floorY + 1, maxZ, Material.BEDROCK);
        chunkData.setRegion(minX, floorY + 1, minZ, maxX, ceilingY, maxZ, Material.AIR);
        chunkData.setRegion(minX, ceilingY, minZ, maxX, ceilingY + 1, maxZ, Material.SMOOTH_SANDSTONE);

        for (int x = minX; x < maxX; x++) {
            int worldX = startX + x;
            for (int z = minZ; z < maxZ; z++) {
                int worldZ = startZ + z;

                boolean wall = worldX == 8 || worldX == 32 || worldZ == 8 || worldZ == 32;
                boolean door = (worldX >= 18 && worldX <= 22 && worldZ == 8) || (worldX >= 18 && worldX <= 22 && worldZ == 32);

                if (wall && !door) {
                    chunkData.setRegion(x, floorY + 1, z, x + 1, ceilingY, z + 1, Material.YELLOW_TERRACOTTA);
                }
                if ((worldX == 14 && worldZ == 14) || (worldX == 26 && worldZ == 14) ||
                    (worldX == 14 && worldZ == 26) || (worldX == 26 && worldZ == 26)) {
                    chunkData.setBlock(x, ceilingY - 1, z, Material.SEA_LANTERN);
                }
                if (worldX == 10 && worldZ == 10) chunkData.setBlock(x, floorY + 1, z, Material.BARREL);
                if (worldX == 11 && worldZ == 10) chunkData.setBlock(x, floorY + 1, z, Material.CHEST);
                if (worldX == 30 && worldZ == 30) chunkData.setBlock(x, floorY + 1, z, Material.CRAFTING_TABLE);
                if (worldX == 20 && worldZ == 9) chunkData.setBlock(x, floorY + 1, z, Material.OAK_SIGN);
            }
        }
    }
}