package com.hourglassprograms.dungeoncraft;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class CommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {

            // * Start dungeon filelr
            if (command.getName().equalsIgnoreCase("start-dungeon") && args.length == 1) {
                List<String> list = new ArrayList<>();
                for (Arena arena : Main._currentArenas) {
                    list.add(arena.dungeonName);
                }
                return list;
            } else if (command.getName().equalsIgnoreCase("start-dungeon") && args.length == 2) {
                List<String> list = new ArrayList<>();
                list.add("easy");
                list.add("hard");
                return list;
            }

            // * Party filler
            else if (command.getName().equalsIgnoreCase("party") && args.length == 1) {
                List<String> list = new ArrayList<>();
                list.add("list");
                list.add("create");
                list.add("invite");
                list.add("accept");
                list.add("leave");
                return list;
            } else if (command.getName().equalsIgnoreCase("party") && args.length == 2) {
                List<String> list = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    list.add(p.getName());
                }
                return list;
            }
        }

        return null;

    }

}