package com.backrooms;

import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.loot.Lootable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BackroomsGenerator extends ChunkGenerator {

    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull ChunkData chunkData) { }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull ChunkData chunkData) { }

    @Override
    public void generateBedrock(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull ChunkData chunkData) { }

    @Override
    public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        return Collections.singletonList(new BlockPopulator() {
            @Override
            public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull LimitedRegion limitedRegion) {
                BackroomsPlugin plugin = BackroomsPlugin.getInstance();
                int rotation = plugin.getTileSelectionService().getRandomRotation();
                
                org.bukkit.World bukkitWorld = org.bukkit.Bukkit.getWorld(worldInfo.getUID());
                if (bukkitWorld == null) return;

                // 1. Paste the physical tile layout
                plugin.getSchematicPasteService().pasteChunkTile(bukkitWorld, x, z, rotation);

                // 2. Scan the chunk snapshot for containers to apply loot tables
                int minX = x << 4;
                int minZ = z << 4;
                int maxX = minX + 15;
                int maxZ = minZ + 15;

                for (int blockX = minX; blockX <= maxX; blockX++) {
                    for (int blockZ = minZ; blockZ <= maxZ; blockZ++) {
                        for (int blockY = bukkitWorld.getMinHeight(); blockY < bukkitWorld.getMaxHeight(); blockY++) {
                            
                            BlockState state = limitedRegion.getBlockState(blockX, blockY, blockZ);
                            
                            if (state instanceof Container && state instanceof Lootable lootable) {
                                String typeStr = state.getType().toString();
                                NamespacedKey lootKey;

                                // Separate food cookers from standard storage units
                                if (typeStr.contains("SMOKER") || typeStr.contains("FURNACE")) {
                                    lootKey = new NamespacedKey(plugin, "backrooms/kitchen");
                                } else {
                                    lootKey = new NamespacedKey(plugin, "backrooms/containers");
                                }

                                lootable.setLootTable(org.bukkit.Bukkit.getLootTable(lootKey));
                                state.update(true, false);
                            }
                        }
                    }
                }

                // 3. Low-frequency neutral mob spawning (Ominous passive environment)
                if (random.nextInt(100) < 15) { // 15% chance per chunk execution zone
                    int spawnX = minX + random.nextInt(16);
                    int spawnZ = minZ + random.nextInt(16);
                    int spawnY = bukkitWorld.getMinHeight() + random.nextInt(bukkitWorld.getMaxHeight() - bukkitWorld.getMinHeight());

                    if (limitedRegion.getType(spawnX, spawnY, spawnZ).isAir() && 
                        limitedRegion.getType(spawnX, spawnY - 1, spawnZ).isSolid()) {
                        
                        EntityType type = random.nextBoolean() ? EntityType.SPIDER : EntityType.ENDERMAN;
                        bukkitWorld.spawnEntity(new org.bukkit.Location(bukkitWorld, spawnX, spawnY, spawnZ), type);
                    }
                }
            }
        });
    }
}