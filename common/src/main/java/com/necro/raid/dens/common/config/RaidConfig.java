package com.necro.raid.dens.common.config;

import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.HashMap;
import java.util.Map;

@Config(name="cobblemonraiddens/common")
public class RaidConfig implements ConfigData {
    @Comment("Enable natural spawning of raid dens. Default: true")
    public boolean enable_spawning = true;
    @Comment("Weighted probability of each raid tier from Tier 1 to Tier 7 per dimension. Leave empty to use overworld/default. Default: {\"minecraft:overworld\": [9.0, 15.0, 25.0, 25.0, 20.0, 5.0, 1.0]}")
    public Map<String, double[]> dimension_tier_weights = new HashMap<>(Map.of(
        "minecraft:overworld", new double[]{9.0, 15.0, 25.0, 25.0, 20.0, 5.0, 1.0}
    ));
    @Comment("How long in seconds until raid dens reset (Set to -1 for no resets). Default: 7200")
    public int reset_time = 7200;
    @Comment("Whether the raid boss and raid tier changes between resets (Options: NONE, LOCK_BOTH, LOCK_TIER, LOCK_TYPE, ALL). Default: ALL")
    public String cycle_mode = "ALL";
    @Comment("Whether failed raids count towards the max clears. Default: false")
    public boolean max_clears_include_fails = false;
    @Comment("Whether the reward Pokemon attributes (IVs/Shiny/etc.) are synced between all players or rolled individually. Default: false")
    public boolean sync_rewards = false;
    @Comment("[EXPERIMENTAL] Caches raid dimensions instead of deleting them after a raid battle. Not recommended for large/public servers. Default: false")
    public boolean cache_dimensions = false;
    @Comment("Whether raid crystals can be broken. Default: true")
    public boolean can_break = true;
}
