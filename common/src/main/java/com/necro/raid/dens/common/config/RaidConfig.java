package com.necro.raid.dens.common.config;

import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name="cobbleraiddens-common")
public class RaidConfig implements ConfigData {
    @Comment("Enable natural spawning of raid dens. Default: true")
    public boolean enable_spawning = true;
    @Comment("Raids require key items to interact with. Default: false")
    public boolean requires_key = false;
    @Comment("Maximum number of players in a raid (Set to -1 for no limit). Default: 4")
    public int max_players = 4;
    @Comment("Number of clears until the raid den deactivates (Set to -1 for no limit). Default: 3")
    public int max_clears = 3;
    @Comment("How long in seconds until raid dens reset (Set to -1 for no resets). Default: 7200")
    public int reset_time = 7200;
    @Comment("Whether the raid boss changes between resets. Default: true")
    public boolean can_cycle = true;
//    @Comment("How long in seconds a raid battle lasts. Default: 600")
//    public int raid_duration = 600;
    @Comment("Weighted probability of each raid tier from Tier 1 to Tier 7. Default: [9.0, 15.0, 25.0, 25.0, 20.0, 5.0, 1.0]")
    public double[] tier_weights = {9.0, 15.0, 25.0, 25.0, 20.0, 5.0, 1.0};
    @Comment("Raid boss HP multipliers for each raid tier from Tier 1 to Tier 7. Default: [5, 5, 8, 12, 20, 25, 30]")
    public int[] tier_health_multiplier = {5, 5, 8, 12, 20, 25, 30};
}
