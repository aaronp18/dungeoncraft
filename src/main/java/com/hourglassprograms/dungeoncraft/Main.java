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
import org.bukkit.event.player.PlayerQuitEvent;
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
    // Array of all the arenas loaded into memory from the config
    ArrayList<Arena> _currentArenas = new ArrayList<Arena>();
    // Array of all parties
    ArrayList<Party> _parties = new ArrayList<Party>();

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

        for (Arena arena : _currentArenas) {
            if (tags.contains(arena.arenaID)) {
                // Then means was from this arena

                // Then update remaining num

                updateRemaining();

            }
        }
    }

    // * Listener for when players quit so they can be removed from arenas etc
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Called when a player leaves a server
        Player player = event.getPlayer();
        String quitMessage = event.getQuitMessage();

        // Removes iteself from an arena member list
        for (Arena arena : _currentArenas) {
            boolean toRemove = false;
            if (arena.players != null) {
                for (Player p : arena.players) {
                    if (p.getName().equals(player.getName())) {
                        // Remove from arena
                        toRemove = true;

                    }
                }
                if (toRemove) {
                    arena.players.remove(player);
                }

                // Checks if arena is empty
                if (arena.players.size() == 0) {
                    // Then is empty, so reset to wave 0
                    killAllUnused();
                    arena.currentWave = 0;
                }
            }
        }

        // Removes from party
        partyLeave(player);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // *Must be a player to run commands

        if (sender instanceof Player) {
            // *Test Command

            Player player = (Player) sender;

            // *Create command - requires name for new dungeon
            if (cmd.getName().equalsIgnoreCase("create-dungeon")) {
                if (player.hasPermission("dungeoncraft.dungeon.create")) { // Must have permission
                    // Makes sure correct amount of arguments
                    if (args.length >= 1) {
                        // Checks if a dungeon already exists
                        if (getConfig().contains("dungeons." + args[0])) {
                            // Already exsits
                            player.sendMessage(ChatColor.RED + "This dungeon already exsits...");
                        } else if (args.length == 2) {
                            // Then contains wave count
                            player.sendMessage(ChatColor.GOLD + "Creating dungeon " + args[0]);
                            // Checks if wave count is number
                            if (isInteger(args[1])) {
                                // Creates Dungeon
                                createDungeon(args[0], Integer.parseInt(args[1]));
                            } else {
                                createDungeon(args[0]);
                            }

                        } else {
                            player.sendMessage(ChatColor.GOLD + "Creating dungeon " + args[0]);
                            // Creates Dungeon
                            createDungeon(args[0]);

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

            } // *Create ArenaRoom - Requires dungeon name
            else if (cmd.getName().equalsIgnoreCase("create-arena")) {
                if (player.hasPermission("dungeoncraft.arenas.create")) { // Must have permission
                    // Makes sure correct amount of arguments
                    if (args.length == 1) {

                        createArena(args[0], player);
                        // Creates new dungeon config
                        return true;
                    } else if (args.length >= 2) {
                        // Checks if wave count is number
                        if (isInteger(args[1])) {
                            // Creates Dungeon
                            createArena(args[0], player, Integer.parseInt(args[1]));
                        } else {
                            createArena(args[0], player);
                        }

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
                        // Checks if is in party
                        if (isInParty(player)) {
                            Integer index = getPartyIndex(player);
                            // Checks if is leader
                            if (isPartyLeader(player, index)) {
                                // Is leader
                                startDungeon(args[0], args[1], _parties.get(index).members);
                            } else {
                                player.sendMessage(ChatColor.RED + "Only the party leader can start dungeons...");
                            }
                        } else {
                            // Is just him
                            // So makes his own players
                            ArrayList<Player> players = new ArrayList<Player>();
                            players.add(player);
                            startDungeon(args[0], args[1], players);
                        }

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

                    updateRemaining();
                    if (_currentArenas.size() == 0) {
                        player.sendMessage(ChatColor.RED + "There are currently no created arenas...");
                    } else {
                        player.sendMessage(ChatColor.BOLD + "===== Arenas (" + _currentArenas.size() + ")" + " =====");
                        for (Arena arena : _currentArenas) {
                            player.sendMessage(ChatColor.GOLD + "===== " + arena.arenaID + " =====");
                            player.sendMessage(ChatColor.DARK_AQUA + "- Dungeon Name: " + arena.dungeonName);
                            player.sendMessage(ChatColor.DARK_AQUA + "- Location: ");
                            player.sendMessage(ChatColor.DARK_AQUA + "      World: "
                                    + arena.centerLocation.getWorld().getName() + " X: "
                                    + arena.centerLocation.getBlockX() + " Y: " + arena.centerLocation.getBlockY()
                                    + " Z: " + arena.centerLocation.getBlockZ());

                            player.sendMessage(ChatColor.DARK_AQUA + "- Current Wave: " + arena.currentWave);
                            player.sendMessage(ChatColor.DARK_AQUA + "- Remaining Enemies: " + arena.remainingEnemies);
                            player.sendMessage(ChatColor.DARK_AQUA + "- Spawn Radius: " + arena.spawnRadius);

                        }
                    }

                    return true;

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

            } else if (cmd.getName().equalsIgnoreCase("create-arena")) {
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
            // * Party commands
            // - /party create - creates a party x
            // - /party invite @name - adds player to invited list x
            // - /party accept - moves from invited to joined x
            // - /party leave - removes from joined
            // - /party list - lists members in current party
            else if (cmd.getName().equalsIgnoreCase("party")) {
                if (player.hasPermission("dungeoncraft.party")) { // Must have permission
                    // Makes sure correct amount of arguments
                    if (args.length == 1) {
                        // Creates party
                        if (args[0].equals("create")) {
                            // Checks if player is in a party
                            if (isInParty(player)) {
                                // Already in party
                                player.sendMessage(ChatColor.RED + "You are already in a party...");
                            } else {
                                Party newParty = new Party();
                                newParty.leader = player;
                                newParty.members.add(player);

                                _parties.add(newParty);
                                player.sendMessage(ChatColor.GOLD + "You have successfully create a party.");
                            }
                            return true;

                        } else if (args[0].equals("accept") || args[0].equals("join")) {
                            // Checks if player is in a party
                            if (isInParty(player)) {
                                // Already in party
                                player.sendMessage(ChatColor.RED + "You are already in a party...");

                            } else {
                                // Check if player has been invited
                                Integer inviteIndex = getInvitedIndex(player);
                                if (inviteIndex != null) {
                                    // Been invited
                                    Party party = _parties.get(inviteIndex);
                                    party.invited.remove(player);
                                    party.members.add(player);

                                    player.sendMessage(ChatColor.GOLD + "You have successfully joined the party");
                                    party.leader.sendMessage(
                                            ChatColor.GOLD + player.getName() + " has succesfully joined the party");
                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                                    party.leader.playSound(party.leader.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,
                                            1.0f, 1.0f);

                                } else {
                                    player.sendMessage(ChatColor.RED + "There are no outstanding invites");
                                }

                            }
                            return true;

                        } else if (args[0].equals("leave") || args[0].equals("exit")) {
                            partyLeave(player);
                            return true;

                        }
                        // * Lists all of the invited and current memebers
                        else if (args[0].equals("list")) {
                            if (!_parties.isEmpty()) {
                                player.sendMessage(
                                        ChatColor.BOLD + "===== Parties (" + _parties.size() + ")" + "=====");
                                for (Party party : _parties) {
                                    String members = "";
                                    String invited = "";
                                    for (Player p : party.members) {
                                        members += p.getDisplayName() + " ";
                                    }
                                    for (Player p : party.invited) {
                                        invited += p.getDisplayName() + " ";
                                    }

                                    player.sendMessage(ChatColor.GOLD + "===== " + party.leader.getName() + " =====");
                                    player.sendMessage(ChatColor.DARK_AQUA + " == Members == ");
                                    player.sendMessage(ChatColor.DARK_AQUA + members);
                                    player.sendMessage(ChatColor.DARK_AQUA + " == Invited == ");
                                    player.sendMessage(ChatColor.DARK_AQUA + invited);

                                }
                            } else {
                                // Then parties is empty
                                player.sendMessage(ChatColor.RED + "There are no current parties...");
                            }
                            return true;
                        } else {
                            return false;
                        }

                    } else if (args.length == 2) {
                        if (args[0].equals("invite")) {
                            // If the player is not in a party, then means that they cannot invite anyone to
                            // one
                            if (!isInParty(player)) {
                                player.sendMessage(
                                        ChatColor.RED + "You are not in a party. Do /party create to start one");
                            } else {
                                // Checks if player is online
                                Player invitedPlayer = Bukkit.getPlayer(args[1]);
                                if (invitedPlayer == null) {
                                    // Means the player could not be found
                                    player.sendMessage(
                                            ChatColor.RED + "Player \"" + args[1] + "\" could not be found.");
                                } else {
                                    Integer index = getPartyIndex(player);
                                    // Checks if invited player is already in the invite list
                                    if (isInInviteList(index, invitedPlayer)) {
                                        player.sendMessage(
                                                ChatColor.RED + "Player \"" + args[1] + "\" already invited");
                                    }
                                    // Checks if they are already in the party
                                    else if (isInParty(invitedPlayer)) {
                                        player.sendMessage(
                                                ChatColor.RED + "Player \"" + args[1] + "\" already in the party");
                                    }
                                    // Means that the player isnt in the party or invited yet
                                    else {
                                        // Adds invited player to the invited party
                                        _parties.get(index).invited.add(invitedPlayer);
                                        // Sends notifying message and sound
                                        player.sendMessage(ChatColor.GOLD + "Player \"" + args[1]
                                                + "\" has been invited to the party");
                                        invitedPlayer.sendMessage(ChatColor.GOLD + player.getName()
                                                + " has invited you to join their DungeonCraft party. Do \"/party accept\" to join");
                                        invitedPlayer.playSound(invitedPlayer.getLocation(),
                                                Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                                    }

                                }

                            }
                            return true;
                        }
                    } else {
                        // Incorrect amount of args
                        return false;
                    }

                } else {
                    player.sendMessage(ChatColor.BOLD + "You do not have the perms to do this");
                    return true;
                }

            }
        } else

        {
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
            // Changes the multiplyer depending on number of players
            difficultyMultiplyer = difficultyMultiplyer * (1.0 + (0.5 * (players.size() - 1)));

            for (Player player : players) {
                player.sendMessage(ChatColor.BOLD + "Starting dungeon!");
                player.sendMessage(ChatColor.GOLD + "Teleporting to arena " + arena.arenaID
                        + " on difficulty multiplyer of: " + difficultyMultiplyer.toString() + "...");
                // Tps Players to arena
                player.teleport(arena.centerLocation);
                player.setScoreboard(arena.scoreboard);
            }

            // Updates the current arena with player
            for (Arena a : _currentArenas) {
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
        for (Arena a : _currentArenas) {
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
                    Double spawnRadius = new Double(arena.spawnRadius);
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
        for (Arena a : _currentArenas) {
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
        for (Arena arena : _currentArenas) {
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
        }
        return 1.0;
    }

    private void createArena(String dungeonName, Player player) {
        createArena(dungeonName, player, 10);
    }

    // *Creates arena based on location
    private void createArena(String dungeonName, Player player, Integer radius) {

        FileConfiguration config = this.getConfig();

        // Checks if dungeon exists
        if (config.contains("dungeons." + dungeonName)) {
            String arenaID = generateArenaName(dungeonName);
            String prefix = "arenas." + arenaID + "."; // Make multiple arenas per dungeon
            config.set(prefix + "dungeon-name", dungeonName);
            config.set(prefix + "radius", radius);
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
            newArena.arenaID = arenaID;
            newArena.centerLocation = locale;
            newArena.dungeonName = dungeonName;
            newArena.currentWave = 0;
            newArena.totalWaves = config.getInt("dungeons." + newArena.dungeonName + ".wave-count");
            newArena.remainingEnemies = 0;
            newArena.spawnRadius = radius;

            // Sets up scoreboard for arena
            ScoreboardManager manager = Bukkit.getScoreboardManager();
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

            _currentArenas.add(newArena);

            player.sendMessage(ChatColor.GOLD + "Arena created for " + dungeonName + " with ID: " + arenaID);
        } else {
            player.sendMessage(ChatColor.RED + "Dungeon not found, use /list-dungeons to get the list");
            return;
        }

    }

    // *Creates dungeon and config
    private void createDungeon(String name) {
        createDungeon(name, 5);
    }

    private void createDungeon(String name, Integer waveCount) {
        // Sets the prefix for all of the dungeon settings
        String prefix = "dungeons." + name + ".";
        // Gets config
        FileConfiguration config = this.getConfig();

        // Number of waves to be generated
        ;
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
    public boolean isDouble(String strNum) {
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

    // *Checks if string is a integer
    public boolean isInteger(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Integer d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    // * Checks if the player is already in a party
    public boolean isInParty(Player player) {
        if (!_parties.isEmpty()) {
            // Iterates through parties
            for (Party party : _parties) {
                // Iterates through members
                for (Player p : party.members) {
                    // If one of the members has the same name as the player, then must mean they
                    // are already in a party
                    if (p.getName().equals(player.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // * Returns the current players party index
    public Integer getPartyIndex(Player player) {
        if (!_parties.isEmpty()) {
            // Iterates through parties
            for (Party party : _parties) {
                // Iterates through members
                for (Player p : party.members) {
                    // If one of the members has the same name as the player, then must mean they
                    // are already in a party
                    if (p.getName().equals(player.getName())) {
                        return _parties.indexOf(party);
                    }
                }
            }
        }
        return null;
    }

    // * Checks if player is in the invitelist already
    private boolean isInInviteList(Integer index, Player player) {
        if (!_parties.isEmpty()) {
            Party party = _parties.get(index);
            for (Player p : party.invited) {
                if (p.getName().equals(player.getName())) {
                    // Then is in invite list
                    return true;
                }
            }
            // Otherwise is not in list
        }
        return false;
    }

    // *Returns the index of the party the player has been invited to
    private Integer getInvitedIndex(Player player) {
        if (!_parties.isEmpty()) {
            // Iterates through parties
            for (Party party : _parties) {
                // Iterates through members
                for (Player p : party.invited) {
                    // If one of the members has the same name as the player, then must mean they
                    // are already in a party
                    if (p.getName().equals(player.getName())) {
                        return _parties.indexOf(party);
                    }
                }
            }
        }
        return null;
    }

    // * Checks if the players is the party leader
    private boolean isPartyLeader(Player player, Integer index) {
        if (!_parties.isEmpty()) {
            // Iterates through parties
            for (Party party : _parties) {
                // Checks if the leader is the same as the player
                if (party.leader.getName().equals(player.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    // * Party leave
    public void partyLeave(Player player) {

        // Checks if player is in a party
        if (!isInParty(player)) {
            // Not in a party
            player.sendMessage(ChatColor.RED + "You are not in a party");

        } else {
            Integer index = getPartyIndex(player);
            Party party = _parties.get(index);
            party.members.remove(player);

            if (party.leader.equals(player)) {
                // Then was leader so choose new leader
                if (party.members.size() > 0) {
                    // Atleast one person in party
                    // Assign new leader
                    party.leader = party.members.get(0);

                    party.leader.sendMessage(ChatColor.GOLD + player.getName() + " has left the party");
                    party.leader.sendMessage(ChatColor.GOLD + "You are now the party leader.");
                    party.leader.playSound(party.leader.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                } else {
                    // Empty party so disbandan
                    _parties.remove(party);
                }
            }

            player.sendMessage(ChatColor.GOLD + "You have left the party");

        }
    }

    // *Gets random double
    public static double getRandomDouble(double min, double max) {
        double x = (Math.random() * ((max - min) + 1)) + min;
        return x;
    }

    // * Generates a new arena name that hasnt already been taken
    private String generateArenaName(String dungeonName) {
        FileConfiguration config = this.getConfig();
        String name = dungeonName + "-1";
        Integer counter = 1;
        while (config.contains("arenas." + name)) {
            counter++;
            name = dungeonName + "-" + counter;

        }
        return name;
    }

    // *Loads arenas into global hash map to keep track of waves and avaialblity
    private void loadArenas() {
        getLogger().info("Loading arenas from config...");
        // Clears all current arenas
        _currentArenas.clear();

        FileConfiguration config = this.getConfig();
        // Checks if the config contains "arenas", if not, none in config
        if (config.contains("arenas")) {

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
                newArena.spawnRadius = arenas.getInt(id + ".radius");

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

                _currentArenas.add(newArena);
            }
        }
    }

    // * Updates remaining enemies
    private void updateRemaining() {
        // Get all entities

        // Then counts
        for (Arena arena : _currentArenas) {
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
                } else {
                    // Then has reached the end
                    for (Player player : arena.players) {
                        player.playSound(arena.centerLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Congratulations! You have completed "
                                + arena.dungeonName + "!!!");
                        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Here is your prize, well done");
                        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                    }

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

        for (Arena arena : _currentArenas) {
            // Means if there are players

            if (arena.players != null) {
                // If size = 0, then player has left
                if (arena.players.size() == 0)
                    // Iterates through every entity
                    for (Entity en : arena.centerLocation.getWorld().getEntities()) {

                        if (en.getScoreboardTags().contains(arena.arenaID)) {
                            // Then means was from this arena
                            en.remove();

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

                    }
                }

            }
        }
    }

}
