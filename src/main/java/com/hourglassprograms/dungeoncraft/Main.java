package com.hourglassprograms.dungeoncraft;

import java.util.ArrayList;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

public class Main extends JavaPlugin implements Listener {
    ArrayList<Arena> currentArenas = new ArrayList<Arena>();

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        // Load Current Arenas
        loadArenas();

        getLogger().info("Dungeon Craft has loaded");
        getServer().getPluginManager().registerEvents(this, this);

        killAllUnused();

    }

    @Override
    public void onDisable() {
        // Shutdown
        // Reloads
        // Plugin reloads

    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // If team = currentArena
        Entity entity = event.getEntity();
        Set<String> tags = entity.getScoreboardTags();

        for (Arena arena : currentArenas) {
            if (tags.contains(arena.arenaID)) {
                // Then means was from this arena
                // ToDo not upadting on kill
                // Then update remaining num

                updateRemaining();

            }
        }
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
                if (player.hasPermission("dungeoncraft.dungeon.create")) { // Must have permission
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
                if (player.hasPermission("dungeoncraft.arenas.create")) { // Must have permission
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
                    if (args.length >= 2) {
                        player.sendMessage(ChatColor.BOLD + "Starting dungeon...");

                        ArrayList<Player> players = new ArrayList<Player>();
                        players.add(player);
                        try {

                            for (Integer i = 2; i < args.length; i++) {

                                players.add(Bukkit.getPlayer(args[i]));
                            }
                        } catch (Exception e) {
                            player.sendMessage("An error occured getting other players... Make sure they are online!");
                        }

                        startDungeon(args[0], args[1], players);
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
            // * Lists the arenas
            else if (cmd.getName().equalsIgnoreCase("arenas")) {
                if (player.hasPermission("dungeoncraft.arenas.list")) { // Must have permission
                    // Makes sure correct amount of arguments
                    if (args.length == 0) {
                        updateRemaining();
                        player.sendMessage(ChatColor.BOLD + "===== Arenas (" + currentArenas.size() + ")" + "=====");
                        for (Arena arena : currentArenas) {
                            player.sendMessage(ChatColor.GOLD + "===== " + arena.arenaID + "=====");
                            player.sendMessage(ChatColor.DARK_AQUA + "- Dungeon Name: " + arena.dungeonName);
                            player.sendMessage(ChatColor.DARK_AQUA + "- Location: ");
                            player.sendMessage(ChatColor.DARK_AQUA + "      World: "
                                    + arena.centerLocation.getWorld().getName() + " X: "
                                    + arena.centerLocation.getBlockX() + " Y: " + arena.centerLocation.getBlockY()
                                    + " Z: " + arena.centerLocation.getBlockZ());

                            player.sendMessage(ChatColor.DARK_AQUA + "- Current Wave: " + arena.currentWave);
                            player.sendMessage(ChatColor.DARK_AQUA + "- Remaining Enemies: " + arena.remainingEnemies);

                        }
                        return true;
                    } else {
                        // Incorrect amount of args
                        return false;
                    }

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
    private void startDungeon(String dungeonName, String difficulty, ArrayList<Player> players) {
        // Checks if theres an avaible Arena
        Arena arena = findAvailableArena(dungeonName);
        if (arena != null) {

            // Converts difficulty to multiplyer
            Double difficultyMultiplyer = convertDifficulty(difficulty);

            for (Player player : players) {
                player.sendMessage(ChatColor.GOLD + "Teleporting to arena " + arena.arenaID
                        + " on difficulty multiplyer of: " + difficultyMultiplyer.toString() + "...");
                // Tps Players to arena
                player.teleport(arena.centerLocation);
                player.setScoreboard(arena.scoreboard);
            }

            // Updates the current arena with player
            for (Arena a : currentArenas) {
                if (a.arenaID == arena.arenaID) {
                    // Then is current
                    a.players = players;
                    a.difficultyMultiplyer = difficultyMultiplyer;
                    a.dungeonName = dungeonName;
                    a.currentWave = 1;
                    a.isWaiting = true;
                    // Displays scoreboard

                    updateScoreboard(a);

                }
            }

            // Creates DungeonTask
            BukkitScheduler scheduler = getServer().getScheduler();

            // * After 10 seconds, spawn first wave
            scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {

                    spawnWave(arena.arenaID);
                }
            }, 100L);
        } else {
            // Arena not found
            for (Player player : players) {
                player.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "Available arena not found for dungeon: "
                        + dungeonName + ", please try again later or make a new one...");
            }

        }
    }

    // * Spawns wave
    protected void spawnWave(String arenaID) {
        Arena arena = getArena(arenaID);

        for (Player player : arena.players) {
            player.sendMessage(ChatColor.BOLD + "Starting wave: " + Integer.toString(arena.currentWave));
        }
        String prefix = "dungeons." + arena.dungeonName + ".waves.wave" + Integer.toString(arena.currentWave);

        // Gets arena
        for (Arena a : currentArenas) {
            if (arena.arenaID.equals(arenaID)) {
                a.isWaiting = false;

            }
        }
        // Iterates through each mob
        FileConfiguration config = this.getConfig();
        // Get all mobs in wave
        ConfigurationSection wave = config.getConfigurationSection(prefix);

        Set<String> mobs = wave.getKeys(false);

        try {
            // Play lightning sound
            for (Player player : arena.players) {
                player.playSound(arena.centerLocation, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
            }

            // Iterate through and comapare dungeon name
            for (String mob : mobs) {
                Integer amount = (int) (config.getInt(prefix + "." + mob + ".amount") * arena.difficultyMultiplyer);

                for (int i = 0; i < amount; i++) {
                    Double spawnRadius = 10.0;
                    // Random += location
                    Double vX = getRandomDouble(-spawnRadius, spawnRadius);
                    Double vZ = getRandomDouble(-spawnRadius, spawnRadius);

                    Double spawnX = arena.centerLocation.getX() + vX;
                    Double spawnZ = arena.centerLocation.getZ() + vZ;
                    Double spawnY = arena.centerLocation.getY() + 3.0;

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
                            nbtString += ",DeathLootTable:\"dungeoncraft:entities/nodrops\",ActiveEffects:[{Id:28,Amplifier:0,Duration:60f}],PersistenceRequired:1,Tags:[\""
                                    + arenaID + "\"]}";

                        } else {
                            // Not correct format
                            // Replace with {data}
                            nbtString = "{DeathLootTable:\"dungeoncraft:entities/nodrops\",ActiveEffects:[{Id:28,Amplifier:0,Duration:60f}],PersistenceRequired:1,Tags:[\""
                                    + arenaID + "\"]}";
                        }
                    }

                    String command = "execute at " + arena.players.get(0).getName() + " run summon minecraft:" + mob
                            + " " + spawnX + " " + spawnY + " " + spawnZ + " " + nbtString;

                    // Spawns each mob
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                    updateRemaining();
                }

            }
        } catch (

        Exception e) {
            // TO DO: handle exception
            for (Player player : arena.players) {
                player.sendMessage("An error has occured... Perhaps the config is broken?");
            }

            getLogger().info("An error has occured spawning wave: " + e.getMessage());
        }
        // +- random amount

        // Spawns each mob

    }

    // Returns updated Arena
    private Arena getArena(String arenaID) {
        for (Arena a : currentArenas) {
            if (arenaID == a.arenaID) {
                return a;
            }
        }
        return null;
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
    private Arena findAvailableArena(String dungeonName) {
        for (Arena arena : currentArenas) {
            if (dungeonName.equals(arena.dungeonName)) {
                // Then is for that dungeon
                if (arena.currentWave == 0) {
                    // Then is avialable
                    return arena;
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

            // Load into object
            Arena newArena = new Arena();
            newArena.arenaID = ID;
            newArena.centerLocation = locale;
            newArena.dungeonName = dungeonName;
            newArena.currentWave = 0;

            currentArenas.add(newArena);

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
        // currentArenas.clear();

        FileConfiguration config = this.getConfig();
        // Get all arenas
        ConfigurationSection arenas = config.getConfigurationSection("arenas");

        Set<String> ids = arenas.getKeys(false);

        // Gets scoreboard manager
        ScoreboardManager manager = Bukkit.getScoreboardManager();

        // Iterate through and add to current arenas with value = 0 (available)
        for (String id : ids) {
            Arena newArena = new Arena();
            newArena.arenaID = id;
            newArena.centerLocation = getLocation(id);
            newArena.dungeonName = arenas.getString("." + id + ".dungeon-name");
            newArena.currentWave = 0;
            newArena.totalWaves = config.getInt("dungeons." + newArena.dungeonName + ".wave-count");
            newArena.remainingEnemies = 0;

            // Sets up scoreboard for arena
            newArena.scoreboard = manager.getNewScoreboard();

            Objective objective = newArena.scoreboard.registerNewObjective(newArena.arenaID, "dummy",
                    ChatColor.GOLD + "===== " + newArena.dungeonName + " =====");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            Score waveText = objective.getScore(ChatColor.DARK_AQUA + "Wave:");
            waveText.setScore(10);

            Team waveCounter = newArena.scoreboard.registerNewTeam("waveCounter");
            waveCounter.addEntry(ChatColor.DARK_AQUA + "");
            waveCounter.setPrefix(ChatColor.GOLD + "0");
            objective.getScore(ChatColor.DARK_AQUA + "").setScore(9);

            Score remainingText = objective.getScore(ChatColor.DARK_AQUA + "Remaining: ");
            remainingText.setScore(8);

            Team remainingCounter = newArena.scoreboard.registerNewTeam("remainingCounter");
            remainingCounter.addEntry(ChatColor.RED + "");
            remainingCounter.setPrefix(ChatColor.GOLD + "0");
            objective.getScore(ChatColor.RED + "").setScore(7);

            Score difficultyText = objective.getScore(ChatColor.DARK_AQUA + "Dificulty: ");
            difficultyText.setScore(6);

            Team diffCounter = newArena.scoreboard.registerNewTeam("diffCounter");
            diffCounter.addEntry(ChatColor.DARK_RED + "");
            diffCounter.setPrefix(ChatColor.GOLD + "0");
            objective.getScore(ChatColor.DARK_RED + "").setScore(5);

            currentArenas.add(newArena);
        }
    }

    // * Updates remaining enemies
    private void updateRemaining() {
        // Get all entities

        // Then counts
        for (Arena arena : currentArenas) {
            // Reset remaining
            arena.remainingEnemies = 0;
            // Iterates through every entity
            for (Entity en : arena.centerLocation.getWorld().getEntities()) {

                if (en.getScoreboardTags().contains(arena.arenaID)) {
                    // Then means was from this arena
                    arena.remainingEnemies += 1;
                    // Then can add to count

                }
            }
            updateScoreboard(arena);
            // If 0, and running, then start next wave
            if (arena.remainingEnemies == 0 && arena.currentWave != 0 && !arena.isWaiting) {
                // The wave ended
                if (arena.currentWave < arena.totalWaves) {
                    for (Player player : arena.players) {
                        player.playSound(arena.centerLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        player.sendMessage(
                                ChatColor.GOLD + "" + ChatColor.BOLD + "Wave " + arena.currentWave + " complete.");
                    }

                    BukkitScheduler scheduler = getServer().getScheduler();
                    // * After 5 seconds, spawn next wave

                    // Adds 1 to current wave
                    arena.currentWave++;
                    arena.isWaiting = true;

                    scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                        @Override
                        public void run() {
                            arena.isWaiting = false;
                            spawnWave(arena.arenaID);
                        }
                    }, 100L);
                }
            }

        }

    }

    private void updateScoreboard(Arena arena) {
        arena.scoreboard.getTeam("waveCounter").setPrefix(
                ChatColor.GOLD + Integer.toString(arena.currentWave) + " / " + Integer.toString(arena.totalWaves));
        arena.scoreboard.getTeam("remainingCounter")
                .setPrefix(ChatColor.GOLD + Integer.toString(arena.remainingEnemies));
        arena.scoreboard.getTeam("diffCounter").setPrefix(ChatColor.GOLD + Double.toString(arena.difficultyMultiplyer));
    }

    // * Kills all of the unused mobs with no players in that arena
    private void killAllUnused() {
        // Gets all mobs

        for (Arena arena : currentArenas) {
            // Means if there are players

            if (arena.players != null) {
                // If size = 0, then player has left
                if (arena.players.size() == 0)
                    // Iterates through every entity
                    for (Entity en : arena.centerLocation.getWorld().getEntities()) {

                        if (en.getScoreboardTags().contains(arena.arenaID)) {
                            // Then means was from this arena
                            en.remove();

                            // Then can add to count

                        }
                    }
            }
            // Has reloaded
            else {
                // Iterates through every entity
                for (Entity en : arena.centerLocation.getWorld().getEntities()) {

                    if (en.getScoreboardTags().contains(arena.arenaID)) {
                        // Then means was from this arena
                        en.remove();

                        // Then can add to count

                    }
                }

            }
        }
    }

}
