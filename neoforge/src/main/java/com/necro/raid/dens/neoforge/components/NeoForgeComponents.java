package com.necro.raid.dens.neoforge.components;

import com.mojang.serialization.Codec;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.raids.RaidFeature;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.raids.RaidType;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class NeoForgeComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CobblemonRaidDens.MOD_ID);

    public static void registerDataComponents() {
        ModComponents.TIER_COMPONENT = (Holder<DataComponentType<RaidTier>>) (Object) registerTierComponent(builder -> builder.persistent(RaidTier.codec()));
        ModComponents.FEATURE_COMPONENT = (Holder<DataComponentType<RaidFeature>>) (Object) registerFeatureComponent(builder -> builder.persistent(RaidFeature.codec()));
        ModComponents.TYPE_COMPONENT = (Holder<DataComponentType<RaidType>>) (Object) registerTypeComponent(builder -> builder.persistent(RaidType.codec()));
    }

    public static DeferredHolder<DataComponentType<?>, DataComponentType<RaidTier>> registerTierComponent(UnaryOperator<DataComponentType.Builder<RaidTier>> builderOperator) {
        return DATA_COMPONENT_TYPES.register("raid_tier", () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static DeferredHolder<DataComponentType<?>, DataComponentType<RaidFeature>> registerFeatureComponent(UnaryOperator<DataComponentType.Builder<RaidFeature>> builderOperator) {
        return DATA_COMPONENT_TYPES.register("raid_feature", () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static DeferredHolder<DataComponentType<?>, DataComponentType<RaidType>> registerTypeComponent(UnaryOperator<DataComponentType.Builder<RaidType>> builderOperator) {
        return DATA_COMPONENT_TYPES.register("raid_type", () -> builderOperator.apply(DataComponentType.builder()).build());
    }
}
