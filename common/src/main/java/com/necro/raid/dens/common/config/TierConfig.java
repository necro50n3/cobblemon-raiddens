package com.necro.raid.dens.common.config;

import com.cobblemon.mod.common.api.mark.Mark;
import com.necro.raid.dens.common.data.raid.Script;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public interface TierConfig {
    boolean requiresKey();
    boolean allRequireUniqueKey();
    int maxPlayers();
    int maxClears();
    double haRate();
    int maxCheers();
    int raidPartySize();
    int healthMultiplier();
    float multiplayerHealthMultiplier();
    int bossLevel();
    int rewardLevel();
    int ivs();
    float shinyRate();
    int currency();
    int maxCatches();
    Map<String, Script> defaultScripts();
    ResourceLocation raidAI();
    List<Mark> marks();
    int lives();
    boolean playersShareLives();
    int energy();
    float requiredDamage();
    float catchRate();
}
