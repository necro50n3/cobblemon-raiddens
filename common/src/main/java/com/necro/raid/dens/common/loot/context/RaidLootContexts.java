package com.necro.raid.dens.common.loot.context;

import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class RaidLootContexts {
    public static final LootContextParam<ItemStack> RAID_POUCH = new LootContextParam<>(ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_pouch"));
    public static final LootContextParamSet RAID_POUCH_USE = new LootContextParamSet.Builder()
        .required(RAID_POUCH)
        .optional(LootContextParams.THIS_ENTITY)
        .build();
}
