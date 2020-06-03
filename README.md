# DungeonCraft
## What is it?
Well in short, this plugin allows you to easily add "dungeons" where you can fight waves of mobs on your own or with friends.
 
Features include:
 - The ability to have multiple of the same **dungeon** running simultaneously in different arenas, ideal for larger servers.
 - The ability to configure how many and what mobs spawn each wave.
 - The ability to spawn mobs with your custom NBT data (you can give them enchanted armour etc)
 - Parties, which allow multiple people to join the same dungeon.
 - Difficulty scaling when more players play (makes it more balanced)
 - Loot rewards can be changed to either spread amongst all the players or to give all of them the same reward.
 - Loot rewards that scale with the difficulty.
 - The ability to run commands when the dungeons are completed, enabling you to give custom items or trigger other plugins.
 
 Firstly, you may be asking whats the difference between a **Dungeon** and an **Arena**. Well simply put, **Dungeons** are the set of waves that spawn a set of mobs, whereas an **Arena** is just the place where this can take place. </p>

This means that you can design 1 **Dungeon** with a set of custom waves which can be played simultaneously in many different **arenas** for many differnt players. These **Arenas** auto provision themselves which means you won't have two games going on in the same **Arena**. Cool right? 

 ## Setup
 Setting up Dungeon Craft is really quite simple.
 #### General Startup
 - Download the plugin into your `pluigin` folder on your Bukkit server.
 - Start up your server
 - Then go into your server and run `/create-dungeon <dungeon name>` to create a new dungeon.
 - Then build a little area for your arena and do `/create-arena <dungeon name> <spawn radius>` with the same name as the last command. 
 The spawn radius should be atleast a couple of blocks smaller than your area to prevent mobs from spawning outside it and making them unkillable.
 #### Dungeon Config
 - Then go open the `config.yml` in the `plugins/dungeoncraft` folder. This will contain all of the information about the waves etc.
 - To add another mob to a wave, simply copy the pre existing "zombie" and paste below it.
 - The `amount` field changes how many mobs spawn with a difficulty of `easy` with only 1 player.
 - To get custom NBT data, visit a [summon command generator](https://www.gamergeeks.nz/apps/minecraft/mob-generator). 
 - Once you have made a mob you want to spawn, copy the curly brackets and everything in it, and place it in the `nbt` field for that mob.
 - To change the rewards given to the players once complete, go to the `rewards` field of the dungeon you want to change.
 - To give them a specific item, simply write its name as the title, the amount you want to give the player and wether you want it spread amongst other players or to give them each the specified amount.
 - To execute a command, simply give the `reward` the name `command` and add the `command` field.
 - This will mean that the game will execute the given command for each player. Simply replace the player name of a command with `$player$` and `$count$` with the number (if needed).
 - Once you have finished with configuring, simply do `/dc-reload` to load the new config onto the server.
 
 ## Commands:
 - `/create-dungeon <dungeon name> [wave count]` - Used to generate a new Dungeon in `config.yml`. The wave count is how many waves are generated. Permission required:  `(dungeoncraft.dungeon.create)`
 - `/create-arena <dungeon name> [spawn radius]` - Used to create a new arena from the players current position. This location will then be linked to the Dungeon inputted. The spawn radius (default: 10) changes the radius at which mobs spawn. Permission required: `(dungeoncraft.arenas.create)`
 - `/arenas` - Simply lists all the arenas that have been made along with their location and current status. Permission required:  `(dungeoncraft.arenas.list)`
 - `/start-dungeon <dungeon name> <difficulty (easy / hard)>` - Starts a dungeon with the current members in the party. Permission required:  `(dungeoncraft.start)`
 - `/dc-reload` - Used to reload the config into the game. This will reset any game in progress so be careful! Permission required:  `(dungeoncraft.reload)`
 
 #### Parties: 
 ###### Permission required:  `(dungeoncraft.party)`
 - `/party create` - Creates a new party
 - `/party invite <player name>` - Invites player to party
 - `/party accept` - Accepts the invite
 - `/party leave` - Leaves the current party
 - `/party list` - Lists current members in the party
 
 
 
