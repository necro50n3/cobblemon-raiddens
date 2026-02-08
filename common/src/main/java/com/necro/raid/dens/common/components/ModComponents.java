package com.necro.raid.dens.common.components;

import com.necro.raid.dens.common.data.raid.RaidFeature;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ModComponents {
    public static Holder<DataComponentType<RaidTier>> TIER_COMPONENT;
    public static Holder<DataComponentType<RaidFeature>> FEATURE_COMPONENT;
    public static Holder<DataComponentType<RaidType>> TYPE_COMPONENT;

    public static Holder<DataComponentType<String>> UUID_COMPONENT;
    public static Holder<DataComponentType<ResourceLocation>> BUCKET_COMPONENT;
    public static Holder<DataComponentType<ResourceLocation>> BOSS_COMPONENT;
    public static Holder<DataComponentType<Long>> LAST_RESET_COMPONENT;
    public static Holder<DataComponentType<List<String>>> ASPECTS_COMPONENT;

    public static Holder<DataComponentType<Boolean>> RAID_DEN_KEY;
    public static Holder<DataComponentType<Boolean>> REMOTE_KEY;
    public static Holder<DataComponentType<String>> UNIQUE_KEY;

    public static Holder<DataComponentType<Integer>> RAID_ENERGY;
}
