
# ProPack
Modern solution for minecraft resource pack development

[ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) is required for bukkit version


## Features

- [Models generating](#meshes--models)
- [Auto CustomModelData](#auto-custommodeldata)
- [Auto sounds FFmpeg conversion](#sounds)
- [Auto fancyPants (custom armor)](#armor-textures)
- [Obfuscation](#obfuscation)
- ItemsAdder integration


## Example Project

Look at the [example project](https://github.com/Nelonn/ProPack/blob/master/propack-core/src/main/resources/resources/example)


## Json specification

Json is used in lenient mode, possible file extensions:
- json
- [json5](https://json5.org/)
- jsonc

Some features of json5 will not work due to [GSON](https://github.com/google/gson) flaws


## Path to content

Thanks to this table, you can clearly see how content paths work

Context is always important in this way, right now we are in the 'foo:bar/baz' folder



| What the user wrote | What does it turn into |
|---------------------|------------------------|
| `foo:bar/baz`       | `foo:bar/baz`          |
| `:bar/baz`          | `foo:bar/baz`          |
| `:bar/../bar/baz`   | `foo:bar/baz`          |
| `:bar/./baz`        | `foo:bar/baz`          |
| `./`                | `foo:bar/baz`          |
| `../baz`            | `foo:bar/baz`          |


## Meshes & Models

[Java Block/Item models](https://minecraft.fandom.com/wiki/Model) in the ProPack are called **meshes**.
Their files should end with `.mesh.json`.

In order for your **mesh** to be applied to an item, you need to create a model configuration

Models configuration file name should end with `.model.json`. 

**WARNING**: Offset works wrong, not recommended to use

<br>

### Currently implemented model types:

**DefaultItemModel**, example:

```json
{
  "Type": "DefaultItemModel",
  "Mesh": "./mesh",
  "Target": "minecraft:paper"
}
```

<br>

**CombinedItemModel**, example:

```json
{
  "Type": "CombinedItemModel",
  "Mesh": "./mesh",
  "Elements": {
    "element1": "./mesh1",
    "element2": {
      "Mesh": "./mesh2"
    },
    "element3": {
      "Mesh": "./mesh3",
      "Offset": [0.1, 0.2, 0.3],
      "Scale": {
        "Origin": [8.0, 0.0, 8.0],
        "Size": 2.0
      }
    }
  },
  "Target": "minecraft:paper"
}
```

`Offset` simply move whole mesh relative

`Scale` is representation of BlockBench's _Transform -> Scale_ tool

Rotation at the generation stage not supported, sorry

<br>

**SlotItemModel**, example:

```json
{
  "Type": "SlotItemModel",
  "Mesh": "./awp",
  "Slots": {
    "scope": {
      "collimator": "../scopes/collimator",
      "holographic": {
      	"Mesh": "../scopes/holographic",
      	"Offset": [0.0, 3.0, 0.0]
      }
    },
    "grip": {
      "vertical": "../grips/vertical",
      "bipods": "../grips/bipods"
    },
    "magazine": {
      "exists": "../magazine"
    }
  },
  "Target": [
    "minecraft:paper",
    "minecraft:crossbow"
  ]
}
```

Working with sub-meshes is the same as in CombinedItemModel

### Fields

- `Type` - the type of model presented above
- `Mesh` - basic **mesh**, the path can be specified relative to (`./`)
- `Target` - this is a list of items for which you want to [Auto CustomModelData](#auto-custommodeldata) of the specified model.


## Auto CustomModelData

When building resource pack ProPack takes the default model from the folder `include/assets/minecraft/models/item/<item>.json`
and adds the necessary elements to override.

Using ProtocolLib, it takes the model path from the NBT tag `CustomModel` and automatically specifies its CustomModelData.

Example NBT tag for DefaultItemModel: 
- `{CustomModel:"example:models/example_defaultmodel"}`

Example NBT tag for CombinedItemModel:
- `{CustomModel:"example:models/example_combinedmodel",CombinedItemModel:["element1","element3"]}`

Example NBT tag for SlotItemModel:
- `{CustomModel:"example:models/example_slotmodel",SlotItemModel:[scope:"holographic",magazine:"exists"]}`


## Sounds

Sound files must ends with `.sound.json`, eg. `scream.sound.json`

Sound file paths can be relative, example `../folder/sound`.

FFmpeg auto conversion is supported. Optionally, use `-Dpropack.ffmpeg=path` to define its location,
by default it `ffmpeg`


## Armor Textures
Added an automatic builder of custom textures for leather armor for the [shader `fancyPants`](https://github.com/Ancientkingg/fancyPants)

Armor file must ends with `.armor.json`, eg. `emerald.armor.json`

[Example file]():
```json
{
  "Color": {
    "r": 255,
    "g": 0,
    "b": 0
  },
  "Layer1": "./emerald_armor_layer_1.png",
  "Layer2": "./emerald_armor_layer_2.png"
}
```
The second animated version:
```json
{
  "Color": "#00ff00", 
  "Layer1": "./test_layer_1.png",
  "Layer2": {
    "Image": "./test_layer_2.png",
    "SaveImage": false,
    "Frames": 4,
    "Speed": 24,
    "Interpolation": true,
    "Emissivity": 0
  }
}
```
`.png` is not required to be specified.

For more information, see [README.md of fancyPants](https://github.com/Ancientkingg/fancyPants/blob/master/README.md)

`SaveImage` allows you to determine whether to save 
the specified `Image` in the output resource pack


## Languages
Language file must ends with `.lang.json`, eg. `en_us.lang.json`.

Path in content doesn't matter.

Multiple languages in the same namespace will be merged into one.

There is also a placeholder `<namespace>`.


## Fonts
Fonts work unchanged except for the file extension `.font.json`

It is recommended to work with custom fonts from another plugin working with ProPack API


## Obfuscation
It just obfuscates the entire resource pack, except for translations.
The settings for this function are in [`config/build.json5`](https://github.com/Nelonn/ProPack/blob/master/propack-core/src/main/resources/example/config/build.json5)


## Planned in the future
- [ ] **(W.I.P.)** Rewrite builder to **Golang** as binary executable for all platforms and architectures.
- [ ] Fonts generating (auto symbol mapping, etc.)
- [ ] **(ALMOST DONE)** Global player resource pack memory using [Redis](https://redis.io/) and CI/CD. (for multi server)
- [ ] Improve the quality of the code and API.
- [ ] Vanilla minecraft support for [Fabric](https://fabricmc.net/) and [Quilt](https://quiltmc.org/).


## License
*Click here to read [the entire license](https://github.com/Nelonn/ProPack/blob/master/LICENSE.txt).*