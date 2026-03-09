package com.necro.raid.dens.neoforge.components;

import com.mojang.serialization.Codec;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.data.raid.RaidFeature;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class NeoForgeComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CobblemonRaidDens.MOD_ID);

    public static void registerDataComponents() {
        ModComponents.TIER_COMPONENT = registerComponent("raid_tier", RaidTier.codec());
        ModComponents.FEATURE_COMPONENT = registerComponent("raid_feature", RaidFeature.codec());
        ModComponents.TYPE_COMPONENT = registerComponent("raid_type", RaidType.codec());
        ModComponents.BOSS_COMPONENT = registerComponent("raid_boss", ResourceLocation.CODEC);
        ModComponents.BONUS_LOOT_COMPONENT = registerComponent("bonus_loot", Codec.BOOL);

        ModComponents.RAID_DEN_KEY = registerComponent("raid_den_key", Codec.BOOL);
        ModComponents.REMOTE_KEY = registerComponent("remote_key", Codec.BOOL);
        ModComponents.UNIQUE_KEY = registerComponent("unique_key", Codec.STRING);

        ModComponents.RAID_ENERGY = registerComponent("raid_energy", Codec.INT);
        ModComponents.CATCH_BOOST = registerComponent("catch_boost", Codec.floatRange(0F, Float.POSITIVE_INFINITY));
    }

    @SuppressWarnings("unchecked")
    private static <T> Holder<DataComponentType<T>> registerComponent(String name, Codec<T> codec) {
        UnaryOperator<DataComponentType.Builder<T>> builderOperator = builder -> builder.persistent(codec);
        return (Holder<DataComponentType<T>>) (Object) DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }
}
