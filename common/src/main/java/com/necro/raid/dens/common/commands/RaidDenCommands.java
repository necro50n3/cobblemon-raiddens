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
import com.necro.raid.dens.common.raids.RaidType;
import com.necro.raid.dens.common.util.RaidRegistry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
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
                        BlockPosArgument.getBlockPos(context, "position"),
                        context.getSource().getLevel(),
                        (ResourceLocation) null, null, null
                    ))
                    .then(Commands.literal("tier")
                        .then(Commands.argument("tier", StringArgumentType.word())
                            .suggests(RAID_TIERS)
                            .executes(context -> createRaidDen(
                                context,
                                BlockPosArgument.getBlockPos(context, "position"),
                                context.getSource().getLevel(),
                                RaidTier.fromString(StringArgumentType.getString(context, "tier").toUpperCase()),
                                null, null
                            ))
                            .then(Commands.argument("cycle_mode", StringArgumentType.word())
                                .suggests(CYCLE_MODE)
                                .executes(context -> createRaidDen(
                                    context,
                                    BlockPosArgument.getBlockPos(context, "position"),
                                    context.getSource().getLevel(),
                                    RaidTier.fromString(StringArgumentType.getString(context, "tier").toUpperCase()),
                                    RaidCycleMode.fromString(StringArgumentType.getString(context, "cycle_mode")),
                                    null
                                ))
                                .then(Commands.argument("can_reset", BoolArgumentType.bool())
                                    .executes(context -> createRaidDen(
                                        context,
                                        BlockPosArgument.getBlockPos(context, "position"),
                                        context.getSource().getLevel(),
                                        RaidTier.fromString(StringArgumentType.getString(context, "tier").toUpperCase()),
                                        RaidCycleMode.fromString(StringArgumentType.getString(context, "cycle_mode")),
                                        BoolArgumentType.getBool(context, "can_reset")
                                    ))
                                )
                            )
                        )
                    )
                    .then(Commands.literal("boss")
                        .then(Commands.argument("boss", ResourceLocationArgument.id())
                            .suggests(RAID_BOSSES)
                            .executes(context -> createRaidDen(
                                context,
                                BlockPosArgument.getBlockPos(context, "position"),
                                context.getSource().getLevel(),
                                ResourceLocationArgument.getId(context, "boss"),
                                null, null
                            ))
                            .then(Commands.argument("cycle_mode", StringArgumentType.word())
                                .suggests(CYCLE_MODE)
                                .executes(context -> createRaidDen(
                                    context,
                                    BlockPosArgument.getBlockPos(context, "position"),
                                    context.getSource().getLevel(),
                                    ResourceLocationArgument.getId(context, "boss"),
                                    RaidCycleMode.fromString(StringArgumentType.getString(context, "cycle_mode")),
                                    null
                                ))
                                .then(Commands.argument("can_reset", BoolArgumentType.bool())
                                    .executes(context -> createRaidDen(
                                        context,
                                        BlockPosArgument.getBlockPos(context, "position"),
                                        context.getSource().getLevel(),
                                        ResourceLocationArgument.getId(context, "boss"),
                                        RaidCycleMode.fromString(StringArgumentType.getString(context, "cycle_mode")),
                                        BoolArgumentType.getBool(context, "can_reset")
                                    ))
                                )
                            )
                        )
                    )
                    .then(Commands.argument("dimension", DimensionArgument.dimension())
                        .requires(context -> !context.isPlayer())
                        .executes(context -> createRaidDen(
                            context,
                            BlockPosArgument.getBlockPos(context, "position"),
                            DimensionArgument.getDimension(context, "dimension"),
                            (ResourceLocation) null, null, null
                        ))
                        .then(Commands.literal("tier")
                            .then(Commands.argument("tier", StringArgumentType.word())
                                .suggests(RAID_TIERS)
                                .executes(context -> createRaidDen(
                                    context,
                                    BlockPosArgument.getBlockPos(context, "position"),
                                    DimensionArgument.getDimension(context, "dimension"),
                                    RaidTier.fromString(StringArgumentType.getString(context, "tier").toUpperCase()),
                                    null, null
                                ))
                                .then(Commands.argument("cycle_mode", StringArgumentType.word())
                                    .suggests(CYCLE_MODE)
                                    .executes(context -> createRaidDen(
                                        context,
                                        BlockPosArgument.getBlockPos(context, "position"),
                                        DimensionArgument.getDimension(context, "dimension"),
                                        RaidTier.fromString(StringArgumentType.getString(context, "tier").toUpperCase()),
                                        RaidCycleMode.fromString(StringArgumentType.getString(context, "cycle_mode")),
                                        null
                                    ))
                                    .then(Commands.argument("can_reset", BoolArgumentType.bool())
                                        .executes(context -> createRaidDen(
                                            context,
                                            BlockPosArgument.getBlockPos(context, "position"),
                                            DimensionArgument.getDimension(context, "dimension"),
                                            RaidTier.fromString(StringArgumentType.getString(context, "tier").toUpperCase()),
                                            RaidCycleMode.fromString(StringArgumentType.getString(context, "cycle_mode")),
                                            BoolArgumentType.getBool(context, "can_reset")
                                        ))
                                    )
                                )
                            )
                        )
                        .then(Commands.literal("boss")
                            .then(Commands.argument("boss", ResourceLocationArgument.id())
                                .suggests(RAID_BOSSES)
                                .executes(context -> createRaidDen(
                                    context,
                                    BlockPosArgument.getBlockPos(context, "position"),
                                    DimensionArgument.getDimension(context, "dimension"),
                                    ResourceLocationArgument.getId(context, "boss"),
                                    null, null
                                ))
                                .then(Commands.argument("cycle_mode", StringArgumentType.word())
                                    .suggests(CYCLE_MODE)
                                    .executes(context -> createRaidDen(
                                        context,
                                        BlockPosArgument.getBlockPos(context, "position"),
                                        DimensionArgument.getDimension(context, "dimension"),
                                        ResourceLocationArgument.getId(context, "boss"),
                                        RaidCycleMode.fromString(StringArgumentType.getString(context, "cycle_mode")),
                                        null
                                    ))
                                    .then(Commands.argument("can_reset", BoolArgumentType.bool())
                                        .executes(context -> createRaidDen(
                                            context,
                                            BlockPosArgument.getBlockPos(context, "position"),
                                            DimensionArgument.getDimension(context, "dimension"),
                                            ResourceLocationArgument.getId(context, "boss"),
                                            RaidCycleMode.fromString(StringArgumentType.getString(context, "cycle_mode")),
                                            BoolArgumentType.getBool(context, "can_reset")
                                        ))
                                    )
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

    private static int createRaidDenFromExisting(Level level, BlockState blockState, BlockPos blockPos, ResourceLocation location, RaidCycleMode cycleMode, Boolean canReset) {
        if (cycleMode == null) cycleMode = blockState.getValue(RaidCrystalBlock.CYCLE_MODE);
        if (canReset == null) canReset = blockState.getValue(RaidCrystalBlock.CAN_RESET);
        if (location == null) {
            RaidTier tier = cycleMode.canCycleTier() ? RaidTier.getWeightedRandom(level.getRandom(), level) : blockState.getValue(RaidCrystalBlock.RAID_TIER);
            RaidType type = cycleMode.canCycleType() ? null : blockState.getValue(RaidCrystalBlock.RAID_TYPE);
            location = RaidRegistry.getRandomRaidBoss(level.getRandom(), tier, type, null);
        }

        RaidBoss raidBoss = RaidRegistry.getRaidBoss(location);

        level.setBlock(blockPos, blockState
            .setValue(RaidCrystalBlock.ACTIVE, true)
            .setValue(RaidCrystalBlock.CAN_RESET, canReset)
            .setValue(RaidCrystalBlock.CYCLE_MODE, cycleMode)
            .setValue(RaidCrystalBlock.RAID_TYPE, raidBoss.getType())
            .setValue(RaidCrystalBlock.RAID_TIER, raidBoss.getTier()), 2);

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof RaidCrystalBlockEntity raidCrystal) raidCrystal.setRaidBoss(location, level);
        return 1;
    }

    private static int createRaidDenNew(Level level, BlockPos blockPos, ResourceLocation location, RaidCycleMode cycleMode, Boolean canReset) {
        if (cycleMode == null) cycleMode = RaidCycleMode.fromString(CobblemonRaidDens.CONFIG.cycle_mode);
        if (canReset == null) canReset = CobblemonRaidDens.CONFIG.reset_time > 1;
        if (location == null) location = RaidRegistry.getRandomRaidBoss(level.getRandom(), level);

        RaidBoss raidBoss = RaidRegistry.getRaidBoss(location);

        level.setBlock(blockPos, ModBlocks.INSTANCE.getRaidCrystalBlock().defaultBlockState()
            .setValue(RaidCrystalBlock.ACTIVE, true)
            .setValue(RaidCrystalBlock.CAN_RESET, canReset)
            .setValue(RaidCrystalBlock.CYCLE_MODE, cycleMode)
            .setValue(RaidCrystalBlock.RAID_TYPE, raidBoss.getType())
            .setValue(RaidCrystalBlock.RAID_TIER, raidBoss.getTier()), 2);

        ((RaidCrystalBlockEntity) level.getBlockEntity(blockPos)).setRaidBoss(location);
        return 1;
    }

    private static int createRaidDen(CommandContext<CommandSourceStack> context, BlockPos blockPos, ServerLevel level, ResourceLocation raidBoss, RaidCycleMode cycleMode, Boolean canReset) {
        if (level.getBiome(blockPos).is(ModDimensions.RAIDDIM_BIOME)) return 0;

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof RaidCrystalBlockEntity raidCrystal) return createRaidDenFromExisting(level, raidCrystal.getBlockState(), blockPos, raidBoss, cycleMode, canReset);
        else return createRaidDenNew(level, blockPos, raidBoss, cycleMode, canReset);
    }

    private static int createRaidDen(CommandContext<CommandSourceStack> context, BlockPos blockPos, ServerLevel level, RaidTier raidTier, RaidCycleMode cycleMode, Boolean canReset) {
        BlockState blockState = level.getBlockState(blockPos);
        if (cycleMode == null) cycleMode = blockState.hasProperty(RaidCrystalBlock.CYCLE_MODE)
            ? level.getBlockState(blockPos).getValue(RaidCrystalBlock.CYCLE_MODE)
            : RaidCycleMode.fromString(CobblemonRaidDens.CONFIG.cycle_mode);

        RandomSource random = context.getSource().getLevel().getRandom();
        RaidType type;
        if (!blockState.hasProperty(RaidCrystalBlock.RAID_TYPE) || cycleMode.canCycleType()) type = null;
        else type = level.getBlockState(blockPos).getValue(RaidCrystalBlock.RAID_TYPE);
        return createRaidDen(context, blockPos, level, RaidRegistry.getRandomRaidBoss(random, raidTier, type, null), cycleMode, canReset);
    }
}