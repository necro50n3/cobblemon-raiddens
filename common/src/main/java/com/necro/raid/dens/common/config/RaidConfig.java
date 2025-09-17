package com.necro.raid.dens.common.config;

import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.HashMap;
import java.util.Map;

@Config(name="cobbleraiddens-common")
public class RaidConfig implements ConfigData {
    @Comment("Enable natural spawning of raid dens. Default: true")
    public boolean enable_spawning = true;
    @Comment("Weighted probability of each raid tier from Tier 1 to Tier 7 per dimension. Leave empty to use overworld/default. Default: {\"minecraft:overworld\": [9.0, 15.0, 25.0, 25.0, 20.0, 5.0, 1.0]}")
    public Map<String, double[]> dimension_tier_weights = new HashMap<>(Map.of(
        "minecraft:overworld", new double[]{9.0, 15.0, 25.0, 25.0, 20.0, 5.0, 1.0}
    ));
    @Comment("Raids require key items to interact with. Default: false")
    public boolean requires_key = false;
    @Comment("Maximum number of players in a raid (Set to -1 for no limit). Default: 4")
    public int max_players = 4;
    @Comment("Number of clears until the raid den deactivates (Set to -1 for no limit). Default: 3")
    public int max_clears = 3;
    @Comment("How long in seconds until raid dens reset (Set to -1 for no resets). Default: 7200")
    public int reset_time = 7200;
    @Comment("Whether the raid boss and raid tier changes between resets (Options: NONE, LOCK_BOTH, LOCK_TIER, LOCK_TYPE, ALL). Default: ALL")
    public String cycle_mode = "ALL";
//    @Comment("How long in seconds a raid battle lasts. Default: 600")
//    public int raid_duration = 600;
    @Comment("Raid boss HP multipliers for each raid tier from Tier 1 to Tier 7. Default: [5, 5, 8, 12, 20, 25, 30]")
    public int[] tier_health_multiplier = {5, 5, 8, 12, 20, 25, 30};
    @Comment("The default shiny chance for raid bosses (Set to -1 to use the Cobblemon rate). Default: -1.0")
    public float shiny_rate = -1.0f;
    @Comment("The max number of cheers a player can use per raid. Default: 3")
    public int max_cheers = 3;
}
