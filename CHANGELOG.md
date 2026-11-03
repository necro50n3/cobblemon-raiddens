This update makes some sweeping changes that may affect mod compatibilities. Datapack addons should be unaffected.

### Asymmetric Battles API
- Asymmetric Battles API is now a required dependency.
- Added the ability to set the battle format for raids (Singles to Sextuples).
- Added an experimental Raid battle format that allows up to four players to join the same battle, just like in Gen 8.
- Reimplemented battle sync and raid scripts using Asymmetric Battles API's engine.

### Raidite
- Added a new resource, Raidite.
- Raidite can be found from Raidite Ore, most common in the deepest parts of the Overworld.
- Raidite can also be received from raid pouches.
- More uses for Raidite will be added in future updates.

### Raid Adventures
- Added Raid Adventures, a type of multiphase raid battles inspired by Dynamax Adventures.
- Raid Adventures can be identified by an especially powerful beam emitting from the Raid Crystal.
- Raid Adventures can be defined by the `adventure` keyword when adding Raid Bosses. Refer to the WIki for formatting.
- When entering a Raid Adventure, the host is presented with two choices to select for each phase before reaching the final boss. All raid bosses must be defeated to clear the Raid Adventure.
- By default, one Raidite is required to enter a Raid Adventure.
- Promoted some raids to Raid Adventures.

### Other Changes
- Added raids for the new Pokemon added in Cobblemon 1.8.2.
- Added limited Genesis Forms compatibility.
- To balance raid adventures, certain changes to raid battles were made:
  - Players can no longer access their PC or using a Healing Machine once the raid has started.
  - Players can only use healing items when their entire team is fainted once the raid has started.
  - Players are now restricted to their raid party once the raid has started.
  - The reward Pokemon from raids are now added to the player's PC instead of party.