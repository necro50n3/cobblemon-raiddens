package com.necro.raid.dens.common.blocks;

import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class BlockTags {
    public static final TagKey<Block> CAN_INTERACT = createTag("can_interact");
    public static final TagKey<Block> RAID_CRYSTAL = createTag("raid_crystal");

    public static TagKey<Block> createTag(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name));
    }
}
