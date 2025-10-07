package com.necro.raid.dens.common.config;

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
    Map<String, String> defaultScripts();
}
