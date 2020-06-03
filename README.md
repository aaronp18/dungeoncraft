<hr>
# DungeonCraft
<hr>
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
 <hr>

 
 Firstly, you may be asking whats the difference between a **Dungeon** and an **Arena**. Well simply put, **Dungeons** are the set of waves that spawn a set of mobs, whereas an **Arena** is just the place where this can take place. </p>

This means that you can design 1 **Dungeon** with a set of custom waves which can be played simultaneously in many different **arenas** for many differnt players. These **Arenas** auto provision themselves which means you won't have two games going on in the same **Arena**. Cool right? 
 

 ### Commands:
 - `/create-dungeon <dungeon name> [wave count]` - Used to generate a new Dungeon in `config.yml`. The wave count is how many waves are generated.
 - `/create-arena <dungeon name> [spawn radius]` - Used to create a new arena from the players current position. This location will then be linked to the Dungeon inputted. The spawn radius (default: 10) changes the radius at which mobs spawn.
 - `/arenas` - Simply lists all the arenas that have been made along with their location and current status. 
 - `/start-dungeon <dungeon name> <difficulty (easy / hard)>` - Starts a dungeon with the current members in the party. 
 - `/dc-reload` - Used to reload the config into the game. This will reset any game in progress so be careful!
 
 #### Parties:
 - `/party create` - Creates a new party
 - `/party invite <player name>` - Invites player to party
 - `/party accept` - Accepts the invite
 - `/party leave` - Leaves the current party
 - `/party list` - Lists current members in the party
 
 
 
