package com.backrooms.services;

import com.backrooms.BackroomsPlugin;
import java.util.Random;

public class TileSelectionService {

    private final BackroomsPlugin plugin;
    private final Random random;

    public TileSelectionService(BackroomsPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    /**
     * Determines a random rotation for the chunk to maximize disorientation.
     * @return 0, 90, 180, or 270 degrees
     */
    public int getRandomRotation() {
        return random.nextInt(4) * 90;
    }
}