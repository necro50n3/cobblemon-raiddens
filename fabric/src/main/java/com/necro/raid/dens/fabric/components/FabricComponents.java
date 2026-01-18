package com.necro.raid.dens.fabric.components;

import com.mojang.serialization.Codec;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.data.raid.RaidFeature;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.function.UnaryOperator;

public class FabricComponents {
    public static void registerDataComponents() {
        ModComponents.TIER_COMPONENT = Holder.direct(registerComponent("raid_tier", builder -> builder.persistent(RaidTier.codec())));
        ModComponents.FEATURE_COMPONENT = Holder.direct(registerComponent("raid_feature", builder -> builder.persistent(RaidFeature.codec())));
        ModComponents.TYPE_COMPONENT = Holder.direct(registerComponent("raid_type", builder -> builder.persistent(RaidType.codec())));
        ModComponents.RAID_DEN_KEY = Holder.direct(registerComponent("raid_den_key", builder -> builder.persistent(Codec.BOOL)));
        ModComponents.REMOTE_KEY = Holder.direct(registerComponent("remote_key", builder -> builder.persistent(Codec.BOOL)));
        ModComponents.UNIQUE_KEY = Holder.direct(registerComponent("unique_key", builder -> builder.persistent(Codec.STRING)));
        ModComponents.RAID_ENERGY = Holder.direct(registerComponent("raid_energy", builder -> builder.persistent(Codec.INT)));
    }

    private static <T> DataComponentType<T> registerComponent(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name),
            builderOperator.apply(DataComponentType.builder()).build()
        );
    }
}
