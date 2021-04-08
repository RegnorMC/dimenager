# Dimenager
Dimenager is a [Fabric](https://fabricmc.net/) server-side mod that lets you easily manage Minecraft dimensions in-game using simple commands.  
The mod depends on the [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api), but because it only uses a single FAPI module, I decided to include it in the mods jar. This means you don't need Fabric API or any other libraries installed.  
## Contributing
Contributions to the mod are welcome, but the source code is currently unfriendly for coders. Some classes use strange names that may be confusing for you and I recommend reading the entire README file for better understanding.  
Also, the mod uses official Minecraft mappings.  
## The concept
### Dimensions
Every dimension is represented with an identifier and is linked to generator settings, and a dimension type. To avoid any confusion `/dimension worlds` is used to manage the dimensions, and is called "worlds" because `/dimension dimensions` would not sound too well.  
A dimension with an identifier `namespace:name` will be stored in `generated/namespace/dimensions/name.json` file and its json will contain identifiers of a dimension type, and a generator as well as a boolean value telling if the dimension is enabled or not  
Disabled dimensions will not be loaded on server startup, but can still be loaded manually.  
Removing dimensions won't really delete their worlds but will remove the information stored about them in the `generated` directory. You can remove the world manually later.  
### Dimension types
The dimension types are a collection of properties for your dimensions. I recommend using the `minecraft:overworld` dimension types in most cases. New dimension types will be created with Overworld dimension settings, and can be modified later. For the properties list (with comments), see [this section of the ***Custom dimension*** article on the **Official Minecraft Wiki**](https://minecraft.gamepedia.com/Custom_dimension#Syntax). You can also find the settings of the vanilla dimension types there.  
A dimension type with an identifier `namespace:name` will be stored in `generated/namespace/dimension_types/name.json` file.  
### Generators
**Unlike vanilla, Dimenager's dimensions are linked with a generator instead of having an exact copy of the generator it was created with. If you want to use the same generator for multiple dimensions, note that any changes in that generator will also change the way all the linked dimensions are generated. In some cases you will want to copy that generator and add the changes to the cloned one.**  
The generators simply tell the game how to generate a dimension. You can consider generators as settings for the generator type, and the generator type as something that reads those settings and generates the world.  
A generator with an identifier `namespace:name` will be stored in `generated/namespace/generators/name.json` file.  
The only 'configured' (hardcoded) generators are reflecting generators of configured dimensions (e.g. the generator `minecraft:overworld` will be a copy of Overworld's generator) and also the `dimenager:void` generator which can be used to create void/empty worlds.  
### Generator types
Every generator is based on a generator type, and contains its properties. The types can only be added by Minecraft or mods. The vanilla generator types are: `minecraft:noise`, `minecraft:flat` and `minecraft:debug`, but the Dimenager also adds `dimenager:void`, so you don't need to create flat worlds with one air layer (there is also a build-in generator that is based on that generator type).  
There are no 'generated' generator types  
### Configured and Generated
In code, dimensions, dimension types, generators and generator types divide into configured and generated. Configured ones are created by Minecraft, mods or datapacks and the generated group is the ones created with Dimenager.  
You cannot modify or remove anything that belongs to configured group using Dimenager.  
All generator types are in the configured group because you can only create them in mods.  
Generated dimensions, dimension types and generators are located in the world's `generated` directory, just like structures created with structure blocks.  
## The `/dimension` command
Most of the mod's features can be used with the vanilla styled `/dimension` command.  
This command splits into three sub commands: `/dimension worlds`, `/dimension types` and `/dimension generators`.
### Syntax tree
```
/dimension
├── worlds
│   ├──	add <identifier> <type> <generator>    Creates a new dimension
│   ├──	remove <dimension>                     Removes a dimension
│   └──	list                                   Lists dimensions
├── types
│   ├──	add <identifier>                       Creates a new dimension type with defualt settings
│   │	└── copy <other>                       Copies settings of a dimension type to a new one
│   ├──	remove <type>                          Removes a dimension type
│   ├──	list                                   Lists available dimension types
│   └──	set <type> <property> <value>          Changes a property of a dimension type
└── generators
    ├──	add <identifier>
    │   ├── new <type>                         Creates a generator of given generator type
    │   │   └── <seed>                         Creates a generator of given generator type with a given seed
    │   └── copy <other>                       Creates a new generator from another one
    ├──	remove <generator>                     Removes a generator
    ├──	data
    │	└── get <generator>                    Prints the JSON data of the generator
    ├──	list                                   Lists available generators
    └──	types                                  Lists available generator types
```
## Changes in the `/tp` command
**You can disable this feature in the mod's configuration by setting the `modify_tp_command` option to `false` and restarting the game.**  
The mod also modifies Minecraft's `/teleport` (aka `/tp`) command. It allows you to simply teleport to other dimensions using the `/tp` command instead of long `/execute` commands.  
Another minor change it brings to `/tp` is `/teleport <position> <rotation>`. Vanilla doesn't add that command, and you can set the rotation only if the entity argument is there too.  
### Examples  
| Vanilla | Dimenager  |
|---|---|
| `/execute in minecraft:the_nether run tp ~ ~ ~` | `/tp minecraft:the_nether` |
| `/execute in minecraft:the_end run tp 118 65 224` | `/tp minecraft:the_end 118 65 224` |
| `/execute in example:dimension1 run tp OtherPlayer ~ ~ ~` | `/tp OtherPlayer example:dimension1` |
| `/tp @s ~ ~ ~ 90 0` | `/tp ~ ~ ~ 90 0` |
### Downsides
There are some downsides of those `/tp` changes. The biggest one is the ambiguity it creates for the arguments. For example when you try to use command `/tp overworld` to teleport to the Overworld dimension, the game will think you are trying to teleport to a player called `overworld`, even if it doesn't exist. You can simply use the namespaces and autocompletion adds them for you, but it can cause some problems or misunderstanding anyways. Minecraft also spams 10 lines to the console every time you start the server about the ambiguities in the command. Vanilla `/tp` also causes some of those warnings by itself, but the dimension argument adds a few more lines of ambiguity warnings.  
To disable the `/tp` modifications set `modify_tp_command` to false in the mod configuration located in `config/dimenager.json`. That will make all the ambiguities added by Dimenager disappear.  
If you want to have the modifications but also hate any useless warnings, you can disable all command ambiguity warnings using the `remove_command_ambiguity_warns` option in the mod configuration.  
<details>
  <summary>The console spam comparison</summary>

  Vanilla
  ```
  [hh:mm:ss] [main/WARN] (Minecraft) Ambiguity between arguments [teleport, destination] and [teleport, targets] with inputs: [Player, 0123, @e, dd12be42-52a9-4a91-a8a1-11c01849e498]
  [hh:mm:ss] [main/WARN] (Minecraft) Ambiguity between arguments [teleport, location] and [teleport, destination] with inputs: [0.1 -0.5 .9, 0 0 0]
  [hh:mm:ss] [main/WARN] (Minecraft) Ambiguity between arguments [teleport, location] and [teleport, targets] with inputs: [0.1 -0.5 .9, 0 0 0]
  [hh:mm:ss] [main/WARN] (Minecraft) Ambiguity between arguments [teleport, targets] and [teleport, destination] with inputs: [Player, 0123, dd12be42-52a9-4a91-a8a1-11c01849e498]
  [hh:mm:ss] [main/WARN] (Minecraft) Ambiguity between arguments [teleport, targets, location] and [teleport, targets, destination] with inputs: [0.1 -0.5 .9, 0 0 0]
  ```

  Dimenager
  ```
  [hh:mm:ss] [main/WARN] (Minecraft) Ambiguity between arguments [teleport, destination] and [teleport, dimension] with inputs: [0123, dd12be42-52a9-4a91-a8a1-11c01849e498]
  [hh:mm:ss] [main/WARN] (Minecraft) Ambiguity between arguments [teleport, destination] and [teleport, targets] with inputs: [Player, 0123, @e, dd12be42-52a9-4a91-a8a1-11c01849e498]
  [hh:mm:ss] [main/WARN] (Minecraft) Ambiguity between arguments [teleport, location] and [teleport, destination] with inputs: [0.1 -0.5 .9, 0 0 0]
  [hh:mm:ss] [main/WARN] (Minecraft) Ambiguity between arguments [teleport, location] and [teleport, dimension] with inputs: [0.1 -0.5 .9, 0 0 0]
  [hh:mm:ss] [main/WARN] (Minecraft) Ambiguity between arguments [teleport, location] and [teleport, targets] with inputs: [0.1 -0.5 .9, 0 0 0]
  [hh:mm:ss] [main/WARN] (Minecraft) Ambiguity between arguments [teleport, targets] and [teleport, destination] with inputs: [Player, 0123, dd12be42-52a9-4a91-a8a1-11c01849e498]
  [hh:mm:ss] [main/WARN] (Minecraft) Ambiguity between arguments [teleport, targets] and [teleport, dimension] with inputs: [0123, dd12be42-52a9-4a91-a8a1-11c01849e498]
  [hh:mm:ss] [main/WARN] (Minecraft) Ambiguity between arguments [teleport, targets, destination] and [teleport, targets, dimension] with inputs: [0123, dd12be42-52a9-4a91-a8a1-11c01849e498]
  [hh:mm:ss] [main/WARN] (Minecraft) Ambiguity between arguments [teleport, targets, location] and [teleport, targets, destination] with inputs: [0.1 -0.5 .9, 0 0 0]
  [hh:mm:ss] [main/WARN] (Minecraft) Ambiguity between arguments [teleport, targets, location] and [teleport, targets, dimension] with inputs: [0.1 -0.5 .9, 0 0 0]
  ```

  :(
</details>
