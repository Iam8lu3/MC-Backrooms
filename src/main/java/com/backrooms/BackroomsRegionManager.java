package com.backrooms;

import org.bukkit.Material;

public class BackroomsRegionManager {
    public static final int CLASSIC = 0;
    public static final int SANDSTONE = 1;
    public static final int FROZEN = 2;
    public static final int FLOODED = 3;
    public static final int INDUSTRIAL = 4;
    public static final int ARCHIVE = 5;
    public static final int DARK = 6;

    public static int getRegionType(int worldX, int worldZ) {
        int regionX = Math.floorDiv(worldX, 240);
        int regionZ = Math.floorDiv(worldZ, 240);
        long hash = ((long) regionX * 92837111L) ^ ((long) regionZ * 194723L);
        hash ^= (hash >>> 33);
        hash *= 0xff51afd7ed558ccdL;
        hash ^= (hash >>> 33);
        return (int) Math.abs(hash % 7);
    }

    public static Material getFloorMaterial(int region, int floorY, int bedrockFloor) {
        if (floorY == bedrockFloor) return Material.BEDROCK;
        switch (region) {
            case SANDSTONE: return Material.SMOOTH_SANDSTONE;
            case FROZEN: return Material.PACKED_ICE;
            case FLOODED: return Material.DARK_PRISMARINE;
            case INDUSTRIAL: return Material.GRAY_CONCRETE;
            case ARCHIVE: return Material.SMOOTH_STONE;
            case DARK: return Material.BLACKSTONE;
            default: return Material.SMOOTH_SANDSTONE;
        }
    }

    public static Material getWallMaterial(int region) {
        switch (region) {
            case SANDSTONE: return Material.SANDSTONE;
            case FROZEN: return Material.SNOW_BLOCK;
            case FLOODED: return Material.PRISMARINE_BRICKS;
            case INDUSTRIAL: return Material.IRON_BLOCK;
            case ARCHIVE: return Material.LIGHT_GRAY_CONCRETE;
            case DARK: return Material.BLACK_CONCRETE;
            default: return Material.YELLOW_TERRACOTTA;
        }
    }

    public static Material getCeilingMaterial(int region) {
        switch (region) {
            case SANDSTONE: return Material.CUT_SANDSTONE;
            case FROZEN: return Material.BLUE_ICE;
            case FLOODED: return Material.SEA_LANTERN;
            case INDUSTRIAL: return Material.IRON_BARS;
            case ARCHIVE: return Material.STRIPPED_OAK_WOOD;
            case DARK: return Material.DEEPSLATE_TILES;
            default: return Material.SMOOTH_SANDSTONE;
        }
    }
}