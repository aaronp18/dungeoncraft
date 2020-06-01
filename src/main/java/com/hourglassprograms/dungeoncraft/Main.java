package com.hourglassprograms.dungeoncraft;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public class Main extends JavaPlugin {
    public HashMap<String, Integer> currentArenas;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        // Load Current Arenas
        loadArenas();

        getLogger().info("Dungeon Craft has loaded");

    }

    @Override
    public void onDisable() {
        // Shutdown
        // Reloads
        // Plugin reloads

    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // *Must be a player to run commands

        if (sender instanceof Player) {
            // *Test Command

            Player player = (Player) sender;

            if (cmd.getName().equalsIgnoreCase("help")) {
                // Display help for dungeoncraft
                player.sendMessage(ChatColor.BOLD + "Unknown command. Try /dc help");
                return true;
            } // *Create command - requires name for new dungeon
            else if (cmd.getName().equalsIgnoreCase("create-dungeon")) {
                if (player.hasPermission("dungeoncraft.createdungeon")) { // Must have permission
                    // Makes sure correct amount of arguments
                    if (args.length == 1) {
                        player.sendMessage(ChatColor.BOLD + "Creating dungeon...");
                        // Creates Dungeon
                        createDungeon(args[0]);
                        // Creates new dungeon config
                        return true;

                    } else {
                        // Incorrect amount of args
                        return false;
                    }

                } else {
                    player.sendMessage(ChatColor.BOLD + "You do not have the perms to do this");
                    return true;
                }

            } // *Create ArenaRoom - Requires dungeon name
            else if (cmd.getName().equalsIgnoreCase("create-arena")) {
                if (player.hasPermission("dungeoncraft.createarena")) { // Must have permission
                    // Makes sure correct amount of arguments
                    if (args.length == 1) {
                        player.sendMessage(ChatColor.BOLD + "Creating Arena From your postion...");
                        createArena(args[0], player);
                        // Creates new dungeon config
                        return true;
                    } else {
                        // Incorrect amount of args
                        return false;
                    }
                    // Creates Dungeon

                } else {
                    player.sendMessage(ChatColor.BOLD + "You do not have the perms to do this");
                    return true;
                }

            }
            // * Starts dungeon - <dungeon name> <difficulty multiplyer/easy or hard>
            else if (cmd.getName().equalsIgnoreCase("start-dungeon")) {
                if (player.hasPermission("dungeoncraft.start")) { // Must have permission
                    // Makes sure correct amount of arguments
                    if (args.length == 2) {
                        player.sendMessage(ChatColor.BOLD + "Starting dungeon...");
                        startDungeon(args[0], args[1], player);
                        // Creates new dungeon config
                        return true;
                    } else {
                        // Incorrect amount of args
                        return false;
                    }
                    // Creates Dungeon

                } else {
                    player.sendMessage(ChatColor.BOLD + "You do not have the perms to do this");
                    return true;
                }

            }
            // * Reloads config from disk
            else if (cmd.getName().equalsIgnoreCase("dc-reload")) {
                if (player.hasPermission("dungeoncraft.reload")) { // Must have permission

                    player.sendMessage(ChatColor.BOLD + "Reloading the DungeonCraft config...");
                    this.reloadConfig();
                    // Creates new dungeon config
                    return true;

                    // Creates Dungeon

                } else {
                    player.sendMessage(ChatColor.BOLD + "You do not have the perms to do this");
                    return true;
                }

            }

        } else {
            getLogger().info("You must send via a player");
        }

        return false;

    }

    // * Starts dungeon
    private void startDungeon(String dungeonName, String difficulty, Player player) {
        // Checks if theres an avaible Arena
        String arenaID = findAvailableArena(dungeonName);
        if (arenaID != null) {

            // Converts difficulty to multiplyer
            Double difficultyMultiplyer = convertDifficulty(difficulty);
            player.sendMessage(ChatColor.BOLD + "Teleporting to arena " + arenaID + " on difficulty multiplyer of: "
                    + difficultyMultiplyer.toString() + "...");
            // Tps Players to arena
            // Gets location
            player.teleport(getLocation(arenaID));
            // Creates DungeonTask
            BukkitScheduler scheduler = getServer().getScheduler();
            // * After 10 seconds, spawn first wave
            scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                    spawnWave(dungeonName, 1, difficultyMultiplyer, arenaID, player);
                }
            }, 100L);
        } else {
            // Arena not found
            player.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "Available arena not found for dungeon: "
                    + dungeonName + ", please try again later or make a new one...");
        }
    }

    // * Spawns wave
    protected void spawnWave(String dungeonName, int waveNum, Double difficultyMultiplyer, String arenaID,
            Player player) {

        player.sendMessage(ChatColor.BOLD + "Starting wave: " + Integer.toString(waveNum));
        String prefix = "dungeons." + dungeonName + ".waves.wave" + Integer.toString(waveNum);

        // Sets arena to not available anymore with current waveNum
        currentArenas.replace(arenaID, waveNum);

        // Gets location
        Location center = getLocation(arenaID);

        // Iterates through each mob
        FileConfiguration config = this.getConfig();
        // Get all mobs in wave
        ConfigurationSection wave = config.getConfigurationSection(prefix);

        Set<String> mobs = wave.getKeys(false);

        try {
            // Play lightning sound
            player.playSound(center, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
            // Iterate through and comapare dungeon name
            for (String mob : mobs) {
                Integer amount = (int) (config.getInt(prefix + "." + mob + ".amount") * difficultyMultiplyer);
                player.sendMessage("Spawning: " + mob + " x " + amount);
                for (int i = 0; i < amount; i++) {
                    Double spawnRadius = 10.0;
                    // Random += location
                    Double vX = getRandomDouble(-spawnRadius, spawnRadius);
                    Double vZ = getRandomDouble(-spawnRadius, spawnRadius);

                    Double spawnX = center.getX() + vX;
                    Double spawnZ = center.getZ() + vZ;
                    Double spawnY = center.getY() + 3.0;

                    String nbtString = "";
                    // Modifiers
                    // If contains an nbt tag
                    if (config.contains(prefix + "." + mob + ".nbt")) {
                        // Get nbt from config
                        nbtString = config.getString(prefix + "." + mob + ".nbt");
                        // Checking if not empty

                        if (!nbtString.equals("{}")) {
                            // Remove end bracket
                            nbtString = StringUtils.chop(nbtString);
                            // Add on ,data}
                            // Adds death loot table, adds teamId for detection, adds slowfalling effect for
                            // 3 secs
                            nbtString += ",DeathLootTable:\"dungeoncraft:entities/nodrops\",Team:" + arenaID
                                    + ",ActiveEffects:[{Id:28,Amplifier:0,Duration:60f}],PersistenceRequired:1}";

                        } else {
                            // Not correct format
                            // Replace with {data}
                            nbtString = "{DeathLootTable:\"dungeoncraft:entities/nodrops\",Team:" + arenaID
                                    + ",ActiveEffects:[{Id:28,Amplifier:0,Duration:60f}],PersistenceRequired:1}";
                        }
                    }

                    String command = "execute at " + player.getName() + " run summon minecraft:" + mob + " " + spawnX
                            + " " + spawnY + " " + spawnZ + " " + nbtString;

                    // Spawns each mob
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                }

            }
        } catch (

        Exception e) {
            // TO DO: handle exception
            player.sendMessage("An error has occured... Perhaps the config is broken?");
            getLogger().info("An error has occured spawning wave: " + e.getMessage());
        }
        // +- random amount

        // Spawns each mob

    }

    // * Gets the center location of the arena
    private Location getLocation(String arenaID) {
        FileConfiguration config = this.getConfig();
        String prefix = "arenas." + arenaID + ".";

        // config.getString(prefix + "world"),
        return new Location(Bukkit.getServer().getWorld(config.getString(prefix + "world")),
                config.getDouble(prefix + "x"), config.getDouble(prefix + "y"), config.getDouble(prefix + "z"));

    }

    // * Finds any availabe arena for dungeon
    private String findAvailableArena(String dungeonName) {
        FileConfiguration config = this.getConfig();
        // Get all arenas
        ConfigurationSection arenas = config.getConfigurationSection("arenas");

        Set<String> ids = arenas.getKeys(false);

        // Iterate through and comapare dungeon name
        for (String id : ids) {
            if (dungeonName.equals(config.getString("arenas." + id + ".dungeon-name"))) {
                // Must be not in use
                if (currentArenas.get(id) == 0) {
                    // Then is usable
                    return id;
                    // ! Checks if its not being used TO DO
                }
            }
        }
        return null;

    }

    // *Converts "Easy" "Hard" to difficulty values
    private Double convertDifficulty(String difficulty) {
        if (difficulty.equalsIgnoreCase("easy")) {
            return 1.0;
        } else if (difficulty.equalsIgnoreCase("hard")) {
            return 2.0;
        } else if (isNumeric(difficulty)) {
            return Double.parseDouble(difficulty);

        }
        return 1.0;
    }

    // *Creates arena based on location
    private void createArena(String dungeonName, Player player) {

        FileConfiguration config = this.getConfig();

        // Checks if dungeon exists
        if (config.contains("dungeons." + dungeonName)) {
            String ID = UUID.randomUUID().toString();
            String prefix = "arenas." + ID + "."; // Make multiple arenas per dungeon
            config.set(prefix + "dungeon-name", dungeonName);
            // Gets location
            Location locale = player.getLocation();

            // Saves Location
            config.set(prefix + "world", locale.getWorld().getName());
            config.set(prefix + "x", locale.getX());
            config.set(prefix + "y", locale.getY());
            config.set(prefix + "z", locale.getZ());
            // Creates new arena in config

            this.saveConfig();
            player.sendMessage(ChatColor.BOLD + "Arena created (" + dungeonName + ") with ID: " + ID);
        } else {
            player.sendMessage(ChatColor.BOLD + "Dungeon not found, use /list-dungeons to get the list");
            return;
        }

    }

    // *Creates dungeon and config
    private void createDungeon(String name) {
        // Sets the prefix for all of the dungeon settings
        String prefix = "dungeons." + name + ".";
        // Gets config
        FileConfiguration config = this.getConfig();
        // Number of waves to be generated
        Integer waveCount = 5;
        config.set(prefix + "wave-count", waveCount);
        config.set(prefix + "dungeon-id", UUID.randomUUID().toString());

        for (int i = 1; i < waveCount + 1; i++) {
            String wavePrefix = prefix + "waves." + "wave" + i + ".zombie.";
            config.set(wavePrefix + "amount", 5);
            config.set(wavePrefix + "nbt",
                    "{HandItems:[{id:iron_sword,Count:1}],HandDropChances:[0.00F],ArmorItems:[{id:chainmail_boots,Count:1},{id:chainmail_leggings,Count:1},{id:chainmail_chestplate,Count:1},{id:chainmail_helmet,Count:1}],Attributes:[{Name:\"generic.maxHealth\",Base:20.0F}],ArmorDropChances:[0F,0F,0F,0F]}");
        }
        this.saveConfig();

    }

    // *Checks if string is a number
    public boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    // *Gets random double
    public static double getRandomDouble(double min, double max) {
        double x = (Math.random() * ((max - min) + 1)) + min;
        return x;
    }

    // *Loads arenas into global hash map to keep track of waves and avaialblity
    private void loadArenas() {
        // Clears all current arenas
        currentArenas.clear();

        FileConfiguration config = this.getConfig();
        // Get all arenas
        ConfigurationSection arenas = config.getConfigurationSection("arenas");

        Set<String> ids = arenas.getKeys(false);

        // Iterate through and add to current arenas with value = 0 (available)
        for (String id : ids) {
            currentArenas.put(id, 0);
        }
    }
}