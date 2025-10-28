package com.necro.raid.dens.common.compat.rctapi;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.gitlab.srcmc.rctapi.api.ai.RCTBattleAI;

public class RaidDensRCTCompat {
    public static BattleAI getRctApi() {
        return new RCTBattleAI();
    }
}
