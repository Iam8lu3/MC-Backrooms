package com.backrooms;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

public class BackroomsFurnitureGenerator {

    private static final Material[] FURNITURE_COMMON = {
        Material.BARREL,
        Material.CHEST,
        Material.BOOKSHELF,
        Material.CRAFTING_TABLE,
        Material.LECTERN,
        Material.FURNACE
    };

    public static void generateFurniture(ChunkData chunkData, int x, int floorY, int z, int worldX, int worldZ, int region) {
        long hash = ((long) worldX * 92821L) + ((long) worldZ * 37199L) + floorY;
        int roll = (int) Math.abs(hash % 100);

        if (region == BackroomsRegionManager.INDUSTRIAL) {
            if (roll < 40) chunkData.setBlock(x, floorY + 1, z, Material.BARREL);
            else if (roll < 70) chunkData.setBlock(x, floorY + 1, z, Material.CHEST);
            else chunkData.setBlock(x, floorY + 1, z, Material.FURNACE);
            return;
        }

        if (region == BackroomsRegionManager.ARCHIVE) {
            if (roll < 50) chunkData.setBlock(x, floorY + 1, z, Material.BOOKSHELF);
            else if (roll < 75) chunkData.setBlock(x, floorY + 1, z, Material.LECTERN);
            else chunkData.setBlock(x, floorY + 1, z, Material.CHEST);
            return;
        }

        if (roll < 45) {
            // Fix distribution skew using an absolute raw hash modulus
            Material selected = FURNITURE_COMMON[(int) (Math.abs(hash) % FURNITURE_COMMON.length)];
            chunkData.setBlock(x, floorY + 1, z, selected);
        } else if (roll < 55) {
            chunkData.setBlock(x, floorY + 1, z, Material.OAK_STAIRS);
        } else {
            chunkData.setBlock(x, floorY + 1, z, Material.OAK_FENCE);
            chunkData.setBlock(x, floorY + 2, z, Material.OAK_PRESSURE_PLATE);
        }
    }
}