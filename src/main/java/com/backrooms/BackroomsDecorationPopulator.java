package com.backrooms;

import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BackroomsDecorationPopulator extends BlockPopulator {

    private static final int[] FLOORS = {40, 55, 70, 85, 100, 115, 130, 145, 160, 175, 190, 205, 220, 235};

    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull Chunk chunk) {
        World world = chunk.getWorld();

        long seed = ((long) chunkX * 341873128712L) ^ ((long) chunkZ * 132897987541L) ^ worldInfo.getSeed();
        Random r = new Random(seed);

        int itemsToPlace = 4 + r.nextInt(4);
        for (int i = 0; i < itemsToPlace; i++) {
            int blockX = (chunkX << 4) + r.nextInt(16);
            int blockZ = (chunkZ << 4) + r.nextInt(16);
            int floorY = FLOORS[r.nextInt(FLOORS.length)];
            tryPlaceDecoration(world, blockX, floorY + 2, blockZ, r);
        }
    }

    private void tryPlaceDecoration(World world, int x, int y, int z, Random r) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        for (BlockFace face : faces) {
            Block air = world.getBlockAt(x, y, z);
            Block wall = air.getRelative(face);

            if (!air.getType().isAir()) continue;
            if (!wall.getType().isSolid()) continue;

            double roll = r.nextDouble();

            if (roll < 0.40) {
                placeSign(air, face.getOppositeFace(), r);
                return;
            }

            if (roll < 0.65) {
                scheduleEntitySpawn(world, air.getLocation(), face.getOppositeFace(), r, false);
                return;
            }

            if (roll < 0.85) {
                scheduleEntitySpawn(world, air.getLocation(), face.getOppositeFace(), r, true);
                return;
            }

            placeFalseDoor(air, face.getOppositeFace(), r);
            return;
        }
    }

    private void placeSign(Block block, BlockFace facing, Random r) {
        block.setType(Material.OAK_WALL_SIGN, false);
        BlockData data = block.getBlockData();

        if (data instanceof Directional directional) {
            directional.setFacing(facing);
            block.setBlockData(directional, false);
        }

        BlockState state = block.getState(false);
        if (state instanceof Sign sign) {
            String[][] pairedMessages = {
                    {"DON'T LOOK BACK", "IT SEES YOU"},
                    {"ROOM SEALED", "DO NOT OPEN"},
                    {"NO EXIT", "TURN AROUND"},
                    {"KEEP MOVING", "DON'T SLEEP"},
                    {"THE LIGHTS LIE", "FOLLOW THE HUM"},
                    {"SUPPLIES LOW", "RUNNING OUT"},
                    {"STAIRS MOVE", "THEY SHIFT"},
                    {"NOT ALONE", "BE QUIET"},
                    {"WAKE UP", "PLEASE WAKE UP"},
                    {"LEVEL ERROR", "SYSTEM FAILURE"}
            };

            int index = r.nextInt(pairedMessages.length);
            sign.setLine(0, pairedMessages[index][0]);
            state.update(true, false);

            Block adjacent = block.getRelative(facing == BlockFace.EAST || facing == BlockFace.WEST ? BlockFace.NORTH : BlockFace.EAST);
            if (adjacent.getType().isAir() && adjacent.getRelative(facing.getOppositeFace()).getType().isSolid()) {
                adjacent.setType(Material.OAK_WALL_SIGN, false);
                BlockData adjData = adjacent.getBlockData();

                if (adjData instanceof Directional adjDir) {
                    adjDir.setFacing(facing);
                    adjacent.setBlockData(adjDir, false);
                }

                BlockState adjState = adjacent.getState(false);
                if (adjState instanceof Sign adjSign) {
                    adjSign.setLine(0, pairedMessages[index][1]);
                    adjState.update(true, false);
                }
            }
        }
    }

    private void scheduleEntitySpawn(World world, Location loc, BlockFace facing, Random r, boolean isPainting) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("backrooms");
        if (plugin == null) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!loc.getBlock().getType().isAir()) return;

            try {
                Location center = loc.clone().add(0.5, 0.5, 0.5);

                if (isPainting) {
                    Painting painting = world.spawn(center, Painting.class);
                    painting.setFacingDirection(facing, true);
                    Art[] art = Art.values();
                    painting.setArt(art[r.nextInt(art.length)], true);
                } else {
                    ItemFrame frame = world.spawn(center, ItemFrame.class);
                    frame.setFacingDirection(facing, true);

                    Material[] items = {
                            Material.PAPER,
                            Material.MAP,
                            Material.COMPASS,
                            Material.CLOCK,
                            Material.BOOK
                    };

                    if (r.nextDouble() < 0.70) {
                        frame.setItem(new ItemStack(items[r.nextInt(items.length)]));
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

    private void placeFalseDoor(Block block, BlockFace facing, Random r) {
        if (!block.getRelative(BlockFace.DOWN).getType().isSolid()) return;
        if (!block.getRelative(BlockFace.UP).getType().isAir()) return;

        Material mat = r.nextDouble() < 0.85 ? Material.OAK_DOOR : Material.IRON_DOOR;

        Door bottom = (Door) Bukkit.createBlockData(mat);
        bottom.setHalf(Bisected.Half.BOTTOM);
        bottom.setFacing(facing);

        Door top = (Door) Bukkit.createBlockData(mat);
        top.setHalf(Bisected.Half.TOP);
        top.setFacing(facing);

        block.setBlockData(bottom, false);
        block.getRelative(BlockFace.UP).setBlockData(top, false);
    }
}