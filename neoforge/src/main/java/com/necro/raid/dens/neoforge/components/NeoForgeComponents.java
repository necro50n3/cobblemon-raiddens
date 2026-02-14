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
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class NeoForgeComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CobblemonRaidDens.MOD_ID);

    @SuppressWarnings("unchecked")
    public static void registerDataComponents() {
        ModComponents.TIER_COMPONENT = (Holder<DataComponentType<RaidTier>>) (Object) registerComponent("raid_tier", RaidTier.codec());
        ModComponents.FEATURE_COMPONENT = (Holder<DataComponentType<RaidFeature>>) (Object) registerComponent("raid_feature", RaidFeature.codec());
        ModComponents.TYPE_COMPONENT = (Holder<DataComponentType<RaidType>>) (Object) registerComponent("raid_type", RaidType.codec());
        ModComponents.BOSS_COMPONENT = (Holder<DataComponentType<ResourceLocation>>) (Object) registerComponent("raid_boss", ResourceLocation.CODEC);

        ModComponents.RAID_DEN_KEY = (Holder<DataComponentType<Boolean>>) (Object) registerComponent("raid_den_key", Codec.BOOL);
        ModComponents.REMOTE_KEY = (Holder<DataComponentType<Boolean>>) (Object) registerComponent("remote_key", Codec.BOOL);
        ModComponents.UNIQUE_KEY = (Holder<DataComponentType<String>>) (Object) registerComponent("unique_key", Codec.STRING);

        ModComponents.RAID_ENERGY = (Holder<DataComponentType<Integer>>) (Object) registerComponent("raid_energy", Codec.INT);
    }

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> registerComponent(String name, Codec<T> codec) {
        UnaryOperator<DataComponentType.Builder<T>> builderOperator = builder -> builder.persistent(codec);
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }
}
