package com.necro.raid.dens.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.raids.RaidCycleMode;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.util.RaidRegistry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RaidDenCommands {
    public static final SuggestionProvider<CommandSourceStack> RAID_BOSSES = (context, builder) -> {
        String remaining = builder.getRemaining().toLowerCase();
        Registry<RaidBoss> registry = context.getSource().getServer().registryAccess().registryOrThrow(RaidRegistry.RAID_BOSS_KEY);

        for (ResourceLocation id : registry.keySet()) {
            if (id.toString().toLowerCase().startsWith(remaining)) {
                builder.suggest(id.toString());
            }
        }
        return builder.buildFuture();
    };

    public static final SuggestionProvider<CommandSourceStack> RAID_TIERS = (context, builder) -> {
        String remaining = builder.getRemaining().toLowerCase();

        for (RaidTier tier : RaidTier.values()) {
            if (tier.toString().toLowerCase().startsWith(remaining)) {
                builder.suggest(tier.name());
            }
        }
        return builder.buildFuture();
    };


    public static final SuggestionProvider<CommandSourceStack> CYCLE_MODE = (context, builder) -> {
        String remaining = builder.getRemaining().toLowerCase();

        for (RaidCycleMode cycleMode : RaidCycleMode.values()) {
            if (cycleMode.toString().toLowerCase().startsWith(remaining)) {
                builder.suggest(cycleMode.name());
            }
        }
        return builder.buildFuture();
    };
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("crd")
            .then(Commands.literal("dens")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("position", BlockPosArgument.blockPos())
                    .executes(context -> createRaidDen(
                        context,
                        BlockPosArgument.getBlockPos(context, "position")
                    ))
                    .then(Commands.argument("cycle_mode", StringArgumentType.word())
                        .suggests(CYCLE_MODE)
                        .executes(context -> createRaidDen(
                            context,
                            BlockPosArgument.getBlockPos(context, "position"),
                            RaidCycleMode.fromString(StringArgumentType.getString(context, "cycle_mode"))
                        ))
                        .then(Commands.argument("can_reset", BoolArgumentType.bool())
                            .executes(context -> createRaidDen(
                                context,
                                BlockPosArgument.getBlockPos(context, "position"),
                                RaidCycleMode.fromString(StringArgumentType.getString(context, "cycle_mode")),
                                BoolArgumentType.getBool(context, "can_reset")
                            ))
                            .then(Commands.literal("tier")
                                .then(Commands.argument("tier", StringArgumentType.word())
                                    .suggests(RAID_TIERS)
                                    .executes(context -> createRaidDen(
                                        context,
                                        BlockPosArgument.getBlockPos(context, "position"),
                                        RaidCycleMode.fromString(StringArgumentType.getString(context, "cycle_mode")),
                                        BoolArgumentType.getBool(context, "can_reset"),
                                        RaidTier.fromString(StringArgumentType.getString(context, "tier").toUpperCase())
                                    ))
                                )
                            )
                            .then(Commands.literal("boss")
                                .then(Commands.argument("boss", ResourceLocationArgument.id())
                                    .suggests(RAID_BOSSES)
                                    .executes(context -> createRaidDen(
                                        context,
                                        BlockPosArgument.getBlockPos(context, "position"),
                                        RaidCycleMode.fromString(StringArgumentType.getString(context, "cycle_mode")),
                                        BoolArgumentType.getBool(context, "can_reset"),
                                        ResourceLocationArgument.getId(context, "boss")
                                    ))
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        register(dispatcher);
    }

    private static int createRaidDenFromExisting(Level level, BlockState blockState, BlockPos blockPos, RaidCycleMode cycleMode, Boolean canReset, RaidBoss raidBoss) {
        if (cycleMode == null) cycleMode = blockState.getValue(RaidCrystalBlock.CYCLE_MODE);
        if (canReset == null) canReset = blockState.getValue(RaidCrystalBlock.CAN_RESET);
        if (raidBoss == null) {
            if (cycleMode == RaidCycleMode.LOCK_TIER) {
                RaidTier tier = blockState.getValue(RaidCrystalBlock.RAID_TIER);
                raidBoss = RaidRegistry.getRandomRaidBoss(level.getRandom(), tier);
            }
            else {
                raidBoss = RaidRegistry.getRandomRaidBoss(level.getRandom(), level);
            }
        }

        level.setBlock(blockPos, blockState
            .setValue(RaidCrystalBlock.ACTIVE, true)
            .setValue(RaidCrystalBlock.CAN_RESET, canReset)
            .setValue(RaidCrystalBlock.CYCLE_MODE, cycleMode)
            .setValue(RaidCrystalBlock.RAID_TYPE, raidBoss.getType())
            .setValue(RaidCrystalBlock.RAID_TIER, raidBoss.getTier()), 2);

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof RaidCrystalBlockEntity raidCrystal) raidCrystal.setRaidBoss(raidBoss, level);
        return 1;
    }

    private static int createRaidDenNew(Level level, BlockPos blockPos, RaidCycleMode cycleMode, Boolean canReset, RaidBoss raidBoss) {
        if (cycleMode == null) cycleMode = RaidCycleMode.fromString(CobblemonRaidDens.CONFIG.cycle_mode);
        if (canReset == null) canReset = CobblemonRaidDens.CONFIG.reset_time > 1;
        if (raidBoss == null) raidBoss = RaidRegistry.getRandomRaidBoss(level.getRandom(), level);

        level.setBlock(blockPos, ModBlocks.INSTANCE.getRaidCrystalBlock().defaultBlockState()
            .setValue(RaidCrystalBlock.ACTIVE, true)
            .setValue(RaidCrystalBlock.CAN_RESET, canReset)
            .setValue(RaidCrystalBlock.CYCLE_MODE, cycleMode)
            .setValue(RaidCrystalBlock.RAID_TYPE, raidBoss.getType())
            .setValue(RaidCrystalBlock.RAID_TIER, raidBoss.getTier()), 2);

        ((RaidCrystalBlockEntity) level.getBlockEntity(blockPos)).setRaidBoss(raidBoss);
        return 1;
    }

    private static int createRaidDen(CommandContext<CommandSourceStack> context, BlockPos blockPos, RaidCycleMode cycleMode, Boolean canReset, RaidBoss raidBoss) {
        ServerLevel level = context.getSource().getLevel();
        if (level.getBiome(blockPos).is(ModDimensions.RAIDDIM_BIOME)) return 0;

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof RaidCrystalBlockEntity raidCrystal) return createRaidDenFromExisting(level, raidCrystal.getBlockState(), blockPos, cycleMode, canReset, raidBoss);
        else return createRaidDenNew(level, blockPos, cycleMode, canReset, raidBoss);
    }

    private static int createRaidDen(CommandContext<CommandSourceStack> context, BlockPos blockPos, RaidCycleMode cycleMode, Boolean canReset, ResourceLocation raidBoss) {
        RaidBoss raidBossObj = context.getSource().getLevel().registryAccess().registryOrThrow(RaidRegistry.RAID_BOSS_KEY).get(raidBoss);
        return createRaidDen(context, blockPos, cycleMode, canReset, raidBossObj);
    }

    private static int createRaidDen(CommandContext<CommandSourceStack> context, BlockPos blockPos, RaidCycleMode cycleMode, Boolean canReset, RaidTier raidTier) {
        RandomSource random = context.getSource().getLevel().getRandom();
        return createRaidDen(context, blockPos, cycleMode, canReset, RaidRegistry.getRandomRaidBoss(random, raidTier));
    }

    private static int createRaidDen(CommandContext<CommandSourceStack> context, BlockPos blockPos, RaidCycleMode cycleMode, Boolean canReset) {
        return createRaidDen(context, blockPos, cycleMode, canReset, (RaidBoss) null);
    }

    private static int createRaidDen(CommandContext<CommandSourceStack> context, BlockPos blockPos, RaidCycleMode cycleMode) {
        return createRaidDen(context, blockPos, cycleMode, null);
    }

    private static int createRaidDen(CommandContext<CommandSourceStack> context, BlockPos blockPos) {
        return createRaidDen(context, blockPos, null);
    }
}