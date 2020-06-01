package com.hourglassprograms.dungeoncraft;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Arena {
    String dungeonName;
    String arenaID;
    Location centerLocation;
    Integer currentWave = 0;
    Integer remainingEnemies = 0;
    Double difficultyMultiplyer = 1.0;
    Player player; // Add multiple
}