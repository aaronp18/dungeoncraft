package com.hourglassprograms.dungeoncraft;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class Arena {
    String dungeonName;
    String arenaID;
    Location centerLocation;
    Integer currentWave = 0;
    Integer totalWaves;
    Integer remainingEnemies = 0;
    Boolean isWaiting = false;
    Double difficultyMultiplyer = 1.0;
    Player player; // Add multiple
    Scoreboard scoreboard;
}