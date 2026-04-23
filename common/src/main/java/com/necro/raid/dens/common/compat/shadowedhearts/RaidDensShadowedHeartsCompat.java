package com.necro.raid.dens.common.compat.shadowedhearts;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.jayemceekay.shadowedhearts.ShadowService;
import com.jayemceekay.shadowedhearts.config.ShadowedHeartsConfigs;
import com.jayemceekay.shadowedhearts.core.ModItems;
import com.jayemceekay.shadowedhearts.server.AuraBroadcastQueue;
import com.jayemceekay.shadowedhearts.server.WildShadowSpawnListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class RaidDensShadowedHeartsCompat {
    public static void setShadowBoss(Pokemon pokemon, PokemonEntity pokemonEntity) {
        ShadowService.setShadow(pokemon, pokemonEntity, true);
        AuraBroadcastQueue.queueBroadcast(pokemonEntity, 2.5F, Integer.MAX_VALUE);
    }

    public static void setShadowReward(Pokemon pokemon) {
        ShadowService.setShadow(pokemon, null, true);
        WildShadowSpawnListener.assignShadowMoves(pokemon);
    }

    public static ItemStack getShadowShard() {
        return ModItems.SHADOW_SHARD.value().getDefaultInstance();
    }

    public static ItemStack getRandomScent() {
        List<Item> scents = new ArrayList<>(List.of(ModItems.JOY_SCENT.value(), ModItems.VIVID_SCENT.value(), ModItems.EXCITE_SCENT.value()));
        if (ShadowedHeartsConfigs.getInstance().getShadowConfig().expandedScentSystemEnabled()) {
            scents.addAll(List.of(
                ModItems.TRANQUIL_SCENT.value(),
                ModItems.MEADOW_SCENT.value(),
                ModItems.SPARK_SCENT.value(),
                ModItems.FOCUS_SCENT.value(),
                ModItems.FAMILIAR_SCENT.value(),
                ModItems.COMFORT_SCENT.value(),
                ModItems.HEARTH_SCENT.value(),
                ModItems.INSIGHT_SCENT.value(),
                ModItems.LUCID_SCENT.value(),
                ModItems.GROUNDING_SCENT.value(),
                ModItems.STEADY_SCENT.value(),
                ModItems.RESOLVE_SCENT.value()
            ));
        }
        Collections.shuffle(scents);
        return scents.getFirst().getDefaultInstance();
    }
}
