
# ProPack
Modern solution for minecraft resource pack development

## Features
- [Resource pack obfuscation](#obfuscation)
- [Auto CustomModelData Mapping](#autocmd)
- [Auto fancyPants custom armor](#customarmor)
- And much more...

## Example Project
Look at the [example project](https://github.com/Nelonn/ProPack/blob/master/propack-core/src/main/resources/example)

## Json specification
Json is used in lenient mode, possible file extensions:
- json
- [json5](https://json5.org/)
- jsonc

Some futures from json 5 will not work due to [GSON](https://github.com/google/gson) flaws

## Meshes & Models
[Java Block/Item models](https://minecraft.fandom.com/wiki/Model) in the ProPack are called meshes.
Their files should end with `.mesh.json`

Models file name should end with `.model.json`. 
Currently implemented model types:

<br>
DefaultItemModel, example:

```json
{
  "Type": "DefaultItemModel",
  "Mesh": "./mesh",
  "Target": "minecraft:paper"
}
```

<br>
CombinedItemModel, example:

```json
{
  "Type": "CombinedItemModel",
  "Mesh": "./mesh",
  "Elements": {
    "element1": "./mesh1",
    "element2": {
      "Mesh": "./mesh2",
      "Offset": [0.1, 0.2, 0.3]
    }
  },
  "Target": "minecraft:paper"
}
```

<br>
SlotItemModel, example:

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

`Target` - this is an indication of the items for which you need to `Auto CustomModelData Mapping` of the specified model

## <a name="autocmd"></a> Auto CustomModelData Mapping
When building resource pack ProPack takes the default model from the folder `include/assets/minecraft/models/item/<item>.json`
and adds the necessary elements to override.

Using ProtocolLib, it takes the model path from the NBT tag `CustomModel` and automatically specifies its CustomModelData.
<br>

Example NBT tag for DefaultItemModel: 
`{CustomModel:"example:models/example_defaultmodel"}`
<br>

Example NBT tag for CombinedItemModel:
`{CustomModel:"example:models/example_combinedmodel",ModelElements:["element1","element2"]}`
<br>

Example NBT tag for SlotItemModel: `{CustomModel:"example:models/example_slotmodel",ModelSlots:{scope:"holographic"}}`

## Sounds

Sound files must ends with `.sound.json`
- Example: `scream.sound.json`

Ogg paths can be relative, example `../folder/sound.ogg`.

`.ogg` is not required to be specified

## <a name="armortextures"></a> Armor Textures
Added an automatic builder of custom textures for leather armor for the [shader `fancyPants`](https://github.com/Ancientkingg/fancyPants)

Armor file must ends with `.armor.json`

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
`.png` is not required to be specified
For more information, see [README.md of fancyPants](https://github.com/Ancientkingg/fancyPants/blob/master/README.md)

`SaveImage` allows you to determine whether to save 
the specified `Image` in the output resource pack

## Languages
Language file must ends with `.lang.json`
- Example: `en_us.lang.json`

There is also a placeholder `<namespace>`.
Multiple languages in the same namespace will be merged into one.

## Fonts
Fonts work unchanged except for the file extension `.font.json`

## <a name="obfuscation"></a> Obfuscation
It just obfuscates the entire resource pack, except for translations.
The settings for this function are in [`config/build.json5`](https://github.com/Nelonn/ProPack/blob/master/propack-core/src/main/resources/example/config/build.json5)

## Planned in the future
- [ ] Integration into [ItemsAdder](https://www.spigotmc.org/resources/%E2%9C%A8itemsadder%E2%AD%90emotes-mobs-items-armors-hud-gui-emojis-blocks-wings-hats-liquids.73355/), [Oraxen](https://www.spigotmc.org/resources/%E2%98%84%EF%B8%8F-oraxen-add-items-blocks-armors-hats-food-furnitures-plants-and-gui.72448/), [Model Engine](https://www.spigotmc.org/resources/conxeptworks-model-engine—ultimate-custom-entity-model-manager-1-16-5-1-19-3.79477/), etc.
- [ ] Fonts generating
- [ ] CI/CD using [Redis](https://redis.io/), etc.
- [ ] Improve the quality of the code and API
- [ ] Mod for [Fabric](https://fabricmc.net/)

## License
*Click here to read [the entire license](https://github.com/Nelonn/ProPack/blob/master/LICENSE.txt).*