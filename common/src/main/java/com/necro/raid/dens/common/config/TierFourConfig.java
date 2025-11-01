package com.necro.raid.dens.common.config;

import com.necro.raid.dens.common.raids.RaidAI;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.HashMap;
import java.util.Map;

@Config(name="cobblemonraiddens/tier_four")
public class TierFourConfig implements ConfigData, TierConfig {
    @Comment("Raids require key items to interact with. Default: false")
    public boolean requires_key = false;
    @Comment("Whether all players require the unique key or just the host. Default: true")
    public boolean all_require_unique = true;
    @Comment("Maximum number of players in a raid (Set to -1 for no limit). Default: 4")
    public int max_players = 4;
    @Comment("Number of clears until the raid den deactivates (Set to -1 for no limit). Default: 3")
    public int max_clears = 3;
    @Comment("The max number of cheers a player can use per raid. Default: 3")
    public int max_cheers = 3;
    @Comment("The chance for raid bosses to have their hidden ability. Default: 0.20")
    public double ha_rate = 0.20;
    @Comment("The max number Pokemon a player can use in a raid. Default: 1")
    public int raid_party_size = 1;

    @Comment("Raid boss HP multiplier. Default: 12")
    public int health_multiplier = 12;
    @Comment("Bonus raid boss HP multiplier for each extra player that joins the raid battle. Default: 1.0")
    public float multiplayer_health_multiplier = 1.0f;
    @Comment("Raid boss level. Default: 45")
    public int boss_level = 45;
    @Comment("Reward Pokemon level. Default: 45")
    public int reward_level = 45;
    @Comment("Reward Pokemon number of max IVs. Default: 3")
    public int ivs = 3;
    @Comment("The default shiny chance for raid bosses (Set to -1 to use the Cobblemon rate). Default: -1.0")
    public float shiny_rate = -1.0f;
    @Comment("How much currency is rewarded for clearing a raid boss (Requires CobbleDollars). Default: 10000")
    public int currency = 10000;
    @Comment("The max number of Pokemon that can be caught from a raid battle. Default -1.")
    public int max_catches = -1;
    @Comment("The default script to add to raid bosses without a script. Default: {}")
    public Map<String, String> default_scripts = new HashMap<>();
    @Comment("The battle AI used by the raid boss (Options: RANDOM, STRONG, RCT). Default: RANDOM")
    public RaidAI raid_ai = RaidAI.RANDOM;

    public boolean requiresKey() {
        return this.requires_key;
    }
    public boolean allRequireUniqueKey() {
        return this.all_require_unique;
    }
    public int maxPlayers() {
        return this.max_players;
    }
    public int maxClears() {
        return this.max_clears;
    }
    public double haRate() {
        return this.ha_rate;
    }
    public int maxCheers() {
        return this.max_cheers;
    }
    public int raidPartySize() {
        return this.raid_party_size;
    }
    public int healthMultiplier() {
        return this.health_multiplier;
    }
    public float multiplayerHealthMultiplier() {
        return this.multiplayer_health_multiplier;
    }
    public int bossLevel() {
        return this.boss_level;
    }
    public int rewardLevel() {
        return this.reward_level;
    }
    public int ivs() {
        return this.ivs;
    }
    public float shinyRate() {
        return this.shiny_rate;
    }
    public int currency() {
        return this.currency;
    }
    public int maxCatches() {
        return this.max_catches;
    }
    public Map<String, String> defaultScripts() {
        return this.default_scripts;
    }
    public RaidAI raidAI() {
        return this.raid_ai;
    }
}
