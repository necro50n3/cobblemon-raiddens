package com.necro.raid.dens.neoforge.blocks;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.neoforge.blocks.entity.RaidCrystalBlockEntityNeoForge;
import com.necro.raid.dens.neoforge.blocks.entity.RaidHomeBlockEntityNeoForge;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NeoForgeBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
        BuiltInRegistries.BLOCK_ENTITY_TYPE, CobblemonRaidDens.MOD_ID
    );

    public static final Supplier<BlockEntityType<RaidCrystalBlockEntityNeoForge>> RAID_CRYSTAL_BLOCK_ENTITY =
        BLOCK_ENTITIES.register("raid_crystal_block_entity", () ->
            BlockEntityType.Builder.of(RaidCrystalBlockEntityNeoForge::new, NeoForgeBlocks.RAID_CRYSTAL_BLOCK.get()).build(null)
        );

    public static final Supplier<BlockEntityType<RaidHomeBlockEntityNeoForge>> RAID_HOME_BLOCK_ENTITY =
        BLOCK_ENTITIES.register("raid_home_block_entity", () ->
            BlockEntityType.Builder.of(RaidHomeBlockEntityNeoForge::new, NeoForgeBlocks.RAID_HOME_BLOCK.get()).build(null)
        );
}
