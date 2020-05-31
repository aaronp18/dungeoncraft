package com.hourglassprograms.dungeoncraft;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
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
                    player.sendMessage(ChatColor.BOLD + "Creating dungeon...");
                    createDungeon(args[0]);
                    // Creates new dungeon config
                    return true;

                    // Creates Dungeon

                } else {
                    player.sendMessage(ChatColor.BOLD + "You do not have the perms to do this");
                    return true;
                }

            } // *Create ArenaRoom - Requires dungeon name
            else if (cmd.getName().equalsIgnoreCase("create-arena")) {
                if (player.hasPermission("dungeoncraft.createarena")) { // Must have permission
                    // Makes sure correct amount of arguments
                    player.sendMessage(ChatColor.BOLD + "Creating Arena From your postion...");
                    createArena(args[0], player);
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

    // *Creates arena based on location
    private void createArena(String name, Player player) {

        FileConfiguration config = this.getConfig();

        // Checks if dungeon exits
        if (config.contains("dungeons." + name)) {
            String ID = UUID.randomUUID().toString();
            String prefix = "arenas." + ID + "."; // Make multiple arenas per dungeon
            config.set(prefix + "dungeon-name", name);
            // Gets location
            Location locale = player.getLocation();

            // Saves Location
            config.set(prefix + "x", locale.getX());
            config.set(prefix + "y", locale.getY());
            config.set(prefix + "z", locale.getZ());
            // Creates new arena in config
            this.saveConfig();
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
            config.set(wavePrefix + "health", 20);
            config.set(wavePrefix + "amount", 5);
            config.set(wavePrefix + "damage", 20);
        }
        this.saveConfig();
    }

}