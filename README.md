# Cobblemon Raid Dens
Pokemon's raid dens have finally made their way to Cobblemon! Face the MIGHTY raid Pokemon in an epic battle just like in the main series.

![Raid Crystal](https://i.imgur.com/cM3615S.png)

## Features
- Raid crystals are generated in the overworld ranging from Tier 1 to Tier 7.
- Base mod contains all Cobblemon from Tier 1 to Tier 5.
- Host a raid, or join one that has already started.
- The raid boss' health pool is SHARED between all battlers.
- Obtain the Pokemon as well as receive extra rewards for completing the raid.
- For map makers/server owners: An optional item tag/data component and config for raid key requirements.
- Fully configurable!

![Raid Boss](https://i.imgur.com/u5zD078.png)

## Raiding
- The first person to join a raid will become the host.
- All raid participants will be teleported into a separate dimension containing the raid Pokemon.
- The host can accept and deny future join requests while the raid is active.
- All players can start their own battles simultaneously.
- The boss' health is synced across all battlers.
- Once your Pokemon has fainted, you cannot rejoin the battle, but you will still receive the rewards if the raid is cleared.
- Once the boss is defeated, you can receive the Pokemon in your chosen Pokeball, as well as a Raid Pouch with random item rewards.

## Dependencies
- GeckoLib

## Optional Compatibilities
- Mega Showdown: Tera, Dynamax and Mega raids supported! (Addons required)
- Jade: See raid details, including the raid boss!
![Jade Compat](https://i.imgur.com/Zw6mWA2.png)

## Future Updates
- Custom UI for raid elements

## For Map Makers/Server Owners
### Raid Crystals
- Raid crystals have the following properties:
  - ACTIVE: Whether the raid is currently active.
  - CAN_RESET: Whether the raid crystal can reset after a configurable amount of time.
  - CYCLE_MODE: Whether the raid boss, tier and/or type can change between resets.
  - RAID_TYPE: The elemental type of the raid.
  - RAID_TIER: The tier of the raid.
- Raid crystals can be changed or created using /crd dens.
- There is an option to require a key to enter a raid using the "cobblemonraiddens:raid_den_key" item tag or data component.

### Raid Bosses
- Raid bosses have the following properties:
  - pokemon*
    - species*: The Pokemon species
    - gender: The Pokemon gender
    - ability: The Pokemon ability
    - nature: The Pokemon nature
    - moves: The list of moves the Pokemon has
  - raid_form: The list of custom form data for the raid boss only
    - name*: The name of the form data (i.e. mega_evolution)
    - value*: The value of the form data (i.e. mega_x)
  - base_form: The list of custom form data for both the raid boss and reward Pokemon. Same format as raid_form
  - raid_tier*: The tier of the raid
  - raid_type*: The type of the raid
  - raid_feature: Flags the raid as Tera, Dynamax or Mega
  - loot_table: The loot table location for raid boss-specific loot
  - weight: The frequency the raid boss appears
  - is_catchable: Whether the raid boss is catchable
  - health_multi: The raid boss health multiplier
  - shiny_rate: The shiny rate of the raid boss
  - script: Custom raid boss triggers. This will be explained in the future
- *Required fields
- See example below

```
{
    "pokemon": {
        "species": "charizard",
        "moves": [
            "flareblitz",
            "slash",
            "dragonclaw",
            "airslash"
        ]
    },
    "raid_form": [
        {
            "name": "mega_evolution",
            "value": "mega_x"
        }
    ],
    "raid_tier": "TIER_SEVEN",
    "raid_type": "FIRE",
    "raid_feature": "MEGA",
    "loot_table": "cobblemonraiddens:raid/boss/charizard_mega_x",
    "weight": 10.0
}
```

### Data-Driven
- Raid bosses are completely data-driven! New raid bosses can be added with custom data packs.
- Raid spawning locations are dependent on the "cobblemonraiddens:raid_spawnable" biome tag.