package com.hourglassprograms.dungeoncraft;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
            if (label.equalsIgnoreCase("dg") || label.equalsIgnoreCase("dungeoncraft")
                    || label.equalsIgnoreCase("dc")) {
                Player player = (Player) sender;
                if (args.length == 0) {
                    // Display help for dungeoncraft
                    player.sendMessage(ChatColor.BOLD + "Unknown command. Try /dc help");
                    return false;
                } // *Create command - requires name for new dungeon
                else if (args[0].equalsIgnoreCase("create-dungeon")) {
                    if (player.hasPermission("dungeoncraft.createdungeon")) { // Must have permission
                        if (args.length == 2) { // Makes sure correct amount of arguments
                            player.sendMessage(ChatColor.BOLD + "Creating dungeon...");
                            createDungeon(args[1]);
                            // Creates new dungeon config

                        } else {
                            player.sendMessage(ChatColor.RED + "Incorrect arguments...");
                            return false;
                        }
                        // Creates Dungeon

                    } else {
                        player.sendMessage(ChatColor.BOLD + "You do not have the perms to do this");
                    }

                }

                if (player.hasPermission("hello.use")) {
                    player.sendMessage(ChatColor.BOLD + "Hello and welcome :P");

                } else {
                    player.sendMessage(ChatColor.BOLD + "You do not have the perms to do this");
                }
                return true;
            }
            return false;
        }
        return false;
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
        config.set(prefix + "dungeon-id", ((int) (Math.random() * ((1000) + 1))));

        for (int i = 1; i < waveCount + 1; i++) {
            String wavePrefix = prefix + "waves." + "wave" + i + ".zombie.";
            config.set(wavePrefix + "health", 20);
            config.set(wavePrefix + "amount", 5);
            config.set(wavePrefix + "damage", 20);
        }
        this.saveConfig();
    }

}