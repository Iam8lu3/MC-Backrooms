package com.backrooms;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class BackroomsMobPopulator extends BlockPopulator {

    private static final int[] FLOORS = {40, 55, 70, 85, 100, 115, 130, 145, 160, 175, 190, 205, 220, 235};

    public void populate(Object worldInfo, Random random, int chunkX, int chunkZ, Chunk chunk) {
        World world = chunk.getWorld();

        Random r = new Random(
                ((long) chunk.getX() * 341873128712L)
                        ^ ((long) chunk.getZ() * 132897987541L)
        );

        if (r.nextDouble() > 0.15) return;

        int blockX = (chunkX << 4) + r.nextInt(16);
        int blockZ = (chunkZ << 4) + r.nextInt(16);
        int floorY = FLOORS[r.nextInt(FLOORS.length)];

        Block feet = world.getBlockAt(blockX, floorY + 1, blockZ);
        Block head = world.getBlockAt(blockX, floorY + 2, blockZ);
        Block ground = world.getBlockAt(blockX, floorY, blockZ);

        if (!feet.getType().isAir()) return;
        if (!head.getType().isAir()) return;
        if (!ground.getType().isSolid()) return;

        int region = BackroomsRegionManager.getRegionType(blockX, blockZ);

        world.spawnEntity(
                new Location(world, blockX + 0.5, floorY + 1, blockZ + 0.5),
                randomMob(r, region)
        );
    }

    private EntityType randomMob(Random r, int region) {
        double roll = r.nextDouble();

        if (region == BackroomsRegionManager.FLOODED) {
            return roll < 0.70 ? EntityType.DROWNED : EntityType.GUARDIAN;
        }

        if (region == BackroomsRegionManager.DARK) {
            return EntityType.ENDERMAN;
        }

        if (roll < 0.60) {
            EntityType[] atmospheric = {
                    EntityType.CAT,
                    EntityType.FOX,
                    EntityType.BAT
            };

            return atmospheric[r.nextInt(atmospheric.length)];
        }

        return EntityType.VILLAGER;
    }
}