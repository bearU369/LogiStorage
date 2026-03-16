## About

In how Computercraft and AE2 is bridged as such, **LogiStorage** follows an *event-driven architectural* approach of automation to achieve better performance and minimal overhead.
This approach also allow both mods to scale well as your factory expands, enabling Computercraft to utilize new additional signal events introduced by this mod to avoid expensive iterative checks with the AE2 system.

This architecture is built by injecting additional codes into the AE2's crafting system, making it deeply integrated in the AE2's crafting processes. It would allow to detect new crafting events (*starting crafting events, completed crafting events, and cancelled crafting events*) and detect the kind of items being crafted, amount of requested item, and to detect the requesters (*Terminals, Interfaces, Exporters, etc.*) that initiated a crafting event.
After receiving these events, the mod would *translate* these AE2 events into Computercraft's signal events, passing the recent actions occurred from AE2 system into the Computercraft networks. This is to provide more flexibility and use for users to integrate both mods well.

## Blocks & Items

This mod only provide few blocks available for users to use, due to the mod's focus on being minimal and only providing its intended features.
The blocks can be mined via an Iron pickaxe or better.

| Blocks | Recipe (Shapeless) | Purpose |
| --- | --- | --- |
| **ME Modem Interface** | ME Interface (AE2) + Computer (CC) + Wired Modem (CC) | [See more details](#me-modem-interface) |
| **ME Processing Modem Interface** | ME Interface (AE2) + Advanced Computer (CC) + Wired Modem (CC) | [See more details](#me-processing-modem-interface) |

---

### ME Modem Interface

This block provides Lua commands for interactions with the AE2 network.
| Commands | Arguments | Purposes |
| --- | --- | --- |
| `getNetworkInfo` | N/A | Check the status of the AE2 network. |
| `listItems` | N/A | List all items stored within the AE2 network. | 
| `listFluids` | N/A | List all fluids stored within the AE2 network. |
| `findItem` | **itemID** (String) | Find specific items in AE2 network based on the given itemID. | 
| `findFluid` | **fluidID** (String) | Find specific fluid in AE2 network based on the given fluidID. |
| `pullItem` | **targetPeripheral** (String), **slot** (Integer; default to **1**), **count** (Integer; default to **64**) | Pull items from `targetPeripheral` into AE2 network. | 
| `pullFluid` | **targetPeripheral** (String), **count** (Integer; default to max capacity) | Pull fluids from `targetPeripheral` into AE2 network. |
| `pushItem` | **targetPerpheral** (String), **targetItem** (String), **slot** (Integer; default to **1**), **count** (Integer; default to slot's max count) | Push items from AE2 network to `targetPeripheral`. |
| `pushFluid` | **targetPeripheral** (String), **targetFluid** (String), **count** (Integer; default to max capacity) | Push fluids from AE2 network to `targetPeripheral`. |

This block also provide item/fluid containers for immediate flushing of items/fluids into the AE2 network, allowing for ducts and pipes to connect to this block.

### ME Processing Modem Interface

An advanced variant of *ME Modem Interface*. This block acts as an **ME Pattern Provider** in the AE2 network, allowing to contain encoded patterns for the network to send recipe items into this block.
It also listens for crafting events in the AE2 network to transmit these events into signal events for the Computers to read via `os.pullEvent`.

Capable of transmitting signal events, this block provide specific events as follow:
| Event Name | Data | Triggered by |
| --- | --- | --- |
| `crafting_start` | **itemID** (String), **count** (Integer), **triggeringSrc** (String) | AE2 network initiated a crafting job. |
| `crafting_complete` | **itemID** (String), **count** (Integer), **triggeringSrc** (String) | AE2 network finished a crafting job. |
| `crafting_cancel` | **itemID** (String), **count** (Integer), **triggeringSrc** (String) | AE2 network cancelled a crafting job. |
| `crafting_process_preinsert` | **craftingOutputs** (Key - Array) | The expected crafting output/s (in its current batch) before this block received the recipe items. |
| `crafting_process_insert` | **recipeItems** (Key - Array) | Received the recipe items in this block. |
| `crafting_process_postinsert` | **craftingOutputs** (Key - Array) | After all recipe items in the batch is sent, repeating the crafting outputs from `crafting_process_preinsert`. |
| `crafting_process_start` | **itemID** (String), **count** (Integer), **triggeringSrc** (String) | AE2 network picked up this block as part of the crafting job. |
| `crafting_process_complete` | **itemID** (String), **count** (Integer), **triggeringSrc** (String) | AE2 network finished a crafting job where this block is involved in. |
| `crafting_process_cancel` | **itemID** (String), **count** (Integer), **triggeringSrc** (String) | AE2 network cancelled a crafting job where this block is involved in. |

How the `Key` data is structured as:
```lua
{
    id = 'minecraft:dirt', -- The name of key's id.
    amount = 1, -- The amount of key.
    isFluid = false, -- Whether the received key is item or fluid.
    slot = 1, -- Where the key is inserted. (NOTE: only 'crafting_process_insert' append this value! )
}
```

This block also provide item/fluid containers for immediate flushing of items/fluids into the AE2 network. However, the block provides two distinct container systems:
- **Recipe containers** - Item/Fluid containers where the AE2 network would insert the recipes and for peripherals in Computercraft network would able to fetch.
- **Return containers** - Item/Fluid containers where the block's Item and Fluid capabilities is default to. These are the only containers that pipes and ducts can only reach. These containers would also flush the items/fluids stored here into the AE2 network.