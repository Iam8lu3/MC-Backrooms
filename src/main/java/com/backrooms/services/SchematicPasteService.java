package com.backrooms.services;

import com.backrooms.BackroomsPlugin;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SchematicPasteService {

    private final BackroomsPlugin plugin;
    private Clipboard cachedClipboard;

    public SchematicPasteService(BackroomsPlugin plugin) {
        this.plugin = plugin;
        loadSchematic();
    }

    private void loadSchematic() {
        File file = new File(plugin.getDataFolder(), "backrooms.schem");
        if (!file.exists()) {
            plugin.getLogger().warning("backrooms.schem not found in plugin data folder!");
            return;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) return;

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            this.cachedClipboard = reader.read();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load backrooms.schem: " + e.getMessage());
        }
    }

    /**
     * Pastes the schematic at a specific chunk coordinate with a given rotation.
     */
    public void pasteChunkTile(World world, int chunkX, int chunkY, int rotationAngle) {
        if (cachedClipboard == null) return;

        // Calculate world coordinates based on chunk alignment (assuming bedrock starts at world min height)
        int blockX = chunkX << 4;
        int blockZ = chunkY << 4;
        int blockY = world.getMinHeight(); 

        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
            ClipboardHolder holder = new ClipboardHolder(cachedClipboard);
            
            // Apply the 2D horizontal rotation around the center
            if (rotationAngle != 0) {
                AffineTransform transform = new AffineTransform();
                transform = transform.rotateY(rotationAngle);
                holder.setTransform(transform);
            }

            // Paste aligned to the minimum corner of the chunk
            Operation operation = holder.createPaste(editSession)
                    .to(BlockVector3.at(blockX, blockY, blockZ))
                    .ignoreAirBlocks(false)
                    .build();
            
            Operations.complete(operation);
        } catch (Exception e) {
            plugin.getLogger().severe("Error pasting chunk at [" + chunkX + ", " + chunkY + "]: " + e.getMessage());
        }
    }
}