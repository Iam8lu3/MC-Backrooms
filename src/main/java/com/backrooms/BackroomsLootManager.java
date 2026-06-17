package com.backrooms;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BackroomsLootManager {

    public static List<ItemStack> generateRandomLoot(Random r, int floorY, int region) {
        List<ItemStack> loot = new ArrayList<>();
        if (r.nextDouble() < 0.30) return loot;

        int maxItems = 2 + r.nextInt(3); 
        for (int i = 0; i < maxItems; i++) {
            loot.add(getRandomItem(r, floorY, region));
        }
        return loot;
    }

    private static ItemStack getRandomItem(Random r, int floorY, int region) {
        double roll = r.nextDouble();

        if (region == BackroomsRegionManager.FLOODED && r.nextDouble() < 0.40) {
            return new ItemStack(Material.PRISMARINE_SHARD, 1 + r.nextInt(2));
        }
        if (region == BackroomsRegionManager.INDUSTRIAL && r.nextDouble() < 0.40) {
            return new ItemStack(Material.IRON_INGOT, 1 + r.nextInt(3));
        }

        if (roll < 0.35) return createAlmondWater();
        if (roll < 0.50) return createBattery();
        if (roll < 0.60) return createFlashlight();
        if (roll < 0.70) return createExplorerJournal(r);

        if (roll < 0.85) {
            Material[] food = {Material.BREAD, Material.APPLE, Material.COOKED_CHICKEN, Material.CARROT};
            return new ItemStack(food[r.nextInt(food.length)], 1 + r.nextInt(2));
        }

        if (floorY <= 70) return new ItemStack(Material.STONE_PICKAXE);
        else if (floorY <= 130) return new ItemStack(Material.IRON_PICKAXE);
        else return new ItemStack(Material.DIAMOND_PICKAXE);
    }

    private static ItemStack createAlmondWater() {
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "Almond Water");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A sweet, nutty substance.");
            lore.add(ChatColor.GRAY + "Restores sanity and health.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createFlashlight() {
        // Artifact resolved: Directly creating the Golden Carrot utility object
        ItemStack item = new ItemStack(Material.GOLDEN_CARROT); 
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "Flashlight");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Illuminates dark corridors.");
            lore.add(ChatColor.GRAY + "Requires batteries to run.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createBattery() {
        ItemStack item = new ItemStack(Material.COPPER_INGOT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Battery");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Power source for electronic tools.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createExplorerJournal(Random r) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta != null) {
            String[][] entries = {
                {"Day 4", "The yellow walls don't stop. The humming of the lights is driving me crazy. I swear I heard footsteps behind me, but when I turned around, there was nothing there."},
                {"Log Entry", "Found a stairwell leading down today. The next floor looks identical, but it's freezing cold. If anyone finds this, don't go down into the dark."},
                {"Warning", "They are in the walls. Don't look at them. Just run if the lights start flickering."}
            };
            int index = r.nextInt(entries.length);
            meta.setTitle("Mangled Journal");
            meta.setAuthor("Lost Explorer");
            meta.addPage(entries[index][1]);
            book.setItemMeta(meta);
        }
        return book;
    }
}