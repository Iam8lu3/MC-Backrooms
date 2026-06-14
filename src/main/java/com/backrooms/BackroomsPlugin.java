package com.backrooms;

import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BackroomsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("The Backrooms system is online and stable.");
    }

    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        return new BackroomsGenerator();
    }
}
