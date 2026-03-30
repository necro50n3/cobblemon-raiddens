package com.necro.raid.dens.common.compat.vocalized;

import com.cobblemon.vocalized.common.api_impl.VoiceRegistryImpl;
import com.necro.raid.dens.common.items.ModItems;

import java.util.List;

public class RaidDensVocalizedCompat {
    public static void init() {
        VoiceRegistryImpl.INSTANCE.registerAll(List.of(
            new CheerController("AttackCheer", ModItems.ATTACK_CHEER, "go all out"),
            new CheerController("DefenseCheer", ModItems.DEFENSE_CHEER, List.of("hang tough", "hang tuff")),
            new CheerController("HealCheer", ModItems.HEAL_CHEER, List.of("heal up", "he'll up", "hell up"))
        ));
    }
}
