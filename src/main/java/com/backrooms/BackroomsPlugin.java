package com.backrooms;

import com.backrooms.services.SchematicPasteService;
import com.backrooms.services.TileSelectionService;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BackroomsPlugin extends JavaPlugin {

    private static BackroomsPlugin instance;
    private SchematicPasteService schematicPasteService;
    private TileSelectionService tileSelectionService;

    @Override
    public void onEnable() {
        instance = this;
        this.schematicPasteService = new SchematicPasteService(this);
        this.tileSelectionService = new TileSelectionService(this);
    }

    public static BackroomsPlugin getInstance() { return instance; }
    public SchematicPasteService getSchematicPasteService() { return schematicPasteService; }
    public TileSelectionService getTileSelectionService() { return tileSelectionService; }

    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        return new BackroomsGenerator();
    }
}