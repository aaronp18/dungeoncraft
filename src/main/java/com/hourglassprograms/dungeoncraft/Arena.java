package com.hourglassprograms.dungeoncraft;

import java.util.ArrayList;

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
    Integer spawnRadius = 10;
    Boolean isWaiting = false;
    Boolean keepInv = true;
    Double difficultyMultiplyer = 1.0;
    ArrayList<Player> players = null;
    Scoreboard scoreboard;
}