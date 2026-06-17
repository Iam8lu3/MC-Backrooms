package com.backrooms;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.loot.LootTables;
import org.bukkit.loot.Lootable;

import java.util.Random;

public class BackroomsLootPopulator extends BlockPopulator {

    private static final int[] FLOORS = {40, 55, 70, 85, 100, 115, 130, 145, 160, 175, 190, 205, 220, 235};

    public void populate(Object worldInfo, Random random, int chunkX, int chunkZ, Chunk chunk) {

        for (int x = 0; x < 16; x++) {
            int worldX = (chunkX << 4) + x;

            for (int z = 0; z < 16; z++) {
                int worldZ = (chunkZ << 4) + z;
                int region = BackroomsRegionManager.getRegionType(worldX, worldZ);

                for (int floorY : FLOORS) {
                    Block block = chunk.getBlock(x, floorY + 1, z);
                    Material type = block.getType();

                    if (type == Material.CHEST || type == Material.BARREL) {
                        BlockState state = block.getState(false);

                        if (state instanceof Lootable lootable) {
                            lootable.setSeed(random.nextLong());

                            if (region == BackroomsRegionManager.FLOODED) {
                                lootable.setLootTable(Bukkit.getLootTable(LootTables.BURIED_TREASURE.getKey()));
                            } else if (region == BackroomsRegionManager.DARK) {
                                lootable.setLootTable(Bukkit.getLootTable(LootTables.STRONGHOLD_CORRIDOR.getKey()));
                            } else if (floorY <= 70) {
                                lootable.setLootTable(Bukkit.getLootTable(LootTables.ABANDONED_MINESHAFT.getKey()));
                            } else if (floorY <= 130) {
                                lootable.setLootTable(Bukkit.getLootTable(LootTables.SIMPLE_DUNGEON.getKey()));
                            } else {
                                lootable.setLootTable(Bukkit.getLootTable(LootTables.END_CITY_TREASURE.getKey()));
                            }

                            state.update(true, false);
                        }
                    }
                }
            }
        }
    }
}