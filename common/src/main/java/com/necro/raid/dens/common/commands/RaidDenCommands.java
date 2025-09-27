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
import com.necro.raid.dens.common.util.RaidBucket;
import com.necro.raid.dens.common.util.RaidBucketRegistry;
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
import org.jetbrains.annotations.Nullable;

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

    public static final SuggestionProvider<CommandSourceStack> RAID_BUCKETS = (context, builder) -> {
        String remaining = builder.getRemaining().toLowerCase();
        Registry<RaidBucket> registry = context.getSource().getServer().registryAccess().registryOrThrow(RaidBucketRegistry.BUCKET_KEY);

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
                    .then(Commands.literal("bucket")
                        .then(Commands.argument("bucket", ResourceLocationArgument.id())
                            .suggests(RAID_BUCKETS)
                            .executes(context -> createRaidDenWithBucket(
                                context,
                                BlockPosArgument.getBlockPos(context, "position"),
                                context.getSource().getLevel(),
                                ResourceLocationArgument.getId(context, "bucket"),
                                null
                            ))
                            .then(Commands.argument("can_reset", BoolArgumentType.bool())
                                .executes(context -> createRaidDenWithBucket(
                                    context,
                                    BlockPosArgument.getBlockPos(context, "position"),
                                    context.getSource().getLevel(),
                                    ResourceLocationArgument.getId(context, "bucket"),
                                    BoolArgumentType.getBool(context, "can_reset")
                                ))
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
                        .then(Commands.literal("bucket")
                            .then(Commands.argument("bucket", ResourceLocationArgument.id())
                                .suggests(RAID_BUCKETS)
                                .executes(context -> createRaidDenWithBucket(
                                    context,
                                    BlockPosArgument.getBlockPos(context, "position"),
                                    DimensionArgument.getDimension(context, "dimension"),
                                    ResourceLocationArgument.getId(context, "bucket"),
                                    null
                                ))
                                .then(Commands.argument("can_reset", BoolArgumentType.bool())
                                    .executes(context -> createRaidDenWithBucket(
                                        context,
                                        BlockPosArgument.getBlockPos(context, "position"),
                                        DimensionArgument.getDimension(context, "dimension"),
                                        ResourceLocationArgument.getId(context, "bucket"),
                                        BoolArgumentType.getBool(context, "can_reset")
                                    ))
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        register(dispatcher);
    }

    private static void setCrystal(Level level, BlockPos blockPos, BlockState blockState, boolean canReset,
                                   RaidCycleMode cycleMode, ResourceLocation location, @Nullable ResourceLocation bucket) {
        RaidBoss raidBoss = RaidRegistry.getRaidBoss(location);

        level.setBlock(blockPos, blockState
            .setValue(RaidCrystalBlock.ACTIVE, true)
            .setValue(RaidCrystalBlock.CAN_RESET, canReset)
            .setValue(RaidCrystalBlock.CYCLE_MODE, cycleMode)
            .setValue(RaidCrystalBlock.RAID_TYPE, raidBoss.getType())
            .setValue(RaidCrystalBlock.RAID_TIER, raidBoss.getTier()), 2);

        if (level.getBlockEntity(blockPos) instanceof RaidCrystalBlockEntity raidCrystal) {
            raidCrystal.setRaidBoss(location, level.getRandom(), level.getGameTime());
            if (bucket != null) raidCrystal.setRaidBucket(bucket);
        }
    }

    private static int createRaidDenFromExisting(Level level, RaidCrystalBlockEntity blockEntity, BlockPos blockPos, ResourceLocation location, RaidCycleMode cycleMode, Boolean canReset) {
        BlockState blockState = blockEntity.getBlockState();
        if (cycleMode == null) cycleMode = blockState.getValue(RaidCrystalBlock.CYCLE_MODE);
        if (canReset == null) canReset = blockState.getValue(RaidCrystalBlock.CAN_RESET);

        ResourceLocation bucket = null;
        if (cycleMode == RaidCycleMode.BUCKET && location == null) {
            RaidBucket rBucket = blockEntity.getRaidBucket();
            if (rBucket != null) {
                bucket = rBucket.getId();
                location = rBucket.getRandomRaidBoss(level.getRandom(), level);
            }
        }
        if (location == null) {
            RaidTier tier = cycleMode.canCycleTier() ? RaidTier.getWeightedRandom(level.getRandom(), level) : blockState.getValue(RaidCrystalBlock.RAID_TIER);
            RaidType type = cycleMode.canCycleType() ? null : blockState.getValue(RaidCrystalBlock.RAID_TYPE);
            location = RaidRegistry.getRandomRaidBoss(level.getRandom(), level, tier, type, null);
        }

        setCrystal(level, blockPos, blockState, canReset, cycleMode, location, bucket);
        return 1;
    }

    private static int createRaidDenNew(Level level, BlockPos blockPos, ResourceLocation location, RaidCycleMode cycleMode, Boolean canReset) {
        if (cycleMode == null) cycleMode = RaidCycleMode.fromString(CobblemonRaidDens.CONFIG.cycle_mode);
        if (canReset == null) canReset = CobblemonRaidDens.CONFIG.reset_time > 1;

        ResourceLocation bucket = null;
        if (cycleMode == RaidCycleMode.BUCKET && location == null) {
            bucket = RaidBucketRegistry.getRandomBucket(level.getRandom(), level.getBiome(blockPos));
            RaidBucket rBucket = RaidBucketRegistry.getBucket(bucket);
            if (rBucket != null) location = rBucket.getRandomRaidBoss(level.getRandom(), level);
        }
        if (location == null) location = RaidRegistry.getRandomRaidBoss(level.getRandom(), level);

        setCrystal(level, blockPos, ModBlocks.INSTANCE.getRaidCrystalBlock().defaultBlockState(), canReset, cycleMode, location, bucket);
        return 1;
    }

    @SuppressWarnings("unused")
    private static int createRaidDen(CommandContext<CommandSourceStack> context, BlockPos blockPos, ServerLevel level, ResourceLocation raidBoss, RaidCycleMode cycleMode, Boolean canReset) {
        if (level.getBiome(blockPos).is(ModDimensions.RAIDDIM_BIOME)) return 0;

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof RaidCrystalBlockEntity raidCrystal) return createRaidDenFromExisting(level, raidCrystal, blockPos, raidBoss, cycleMode, canReset);
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
        return createRaidDen(context, blockPos, level, RaidRegistry.getRandomRaidBoss(random, level, raidTier, type, null), cycleMode, canReset);
    }

    private static int createRaidDenWithBucketFromExisting(Level level, BlockState blockState, BlockPos blockPos, ResourceLocation bucket, Boolean canReset) {
        if (canReset == null) canReset = blockState.getValue(RaidCrystalBlock.CAN_RESET);
        ResourceLocation location = RaidBucketRegistry.getBucket(bucket).getRandomRaidBoss(level.getRandom(), level);
        if (location == null) {
            RaidTier tier = RaidTier.getWeightedRandom(level.getRandom(), level);
            location = RaidRegistry.getRandomRaidBoss(level.getRandom(), level, tier, null, null);
        }

        setCrystal(level, blockPos, blockState, canReset, RaidCycleMode.BUCKET, location, bucket);
        return 1;
    }

    private static int createRaidDenWithBucketNew(Level level, BlockPos blockPos, ResourceLocation bucket, Boolean canReset) {
        if (canReset == null) canReset = CobblemonRaidDens.CONFIG.reset_time > 1;
        ResourceLocation location = RaidBucketRegistry.getBucket(bucket).getRandomRaidBoss(level.getRandom(), level);
        if (location == null) location = RaidRegistry.getRandomRaidBoss(level.getRandom(), level);

        setCrystal(level, blockPos, ModBlocks.INSTANCE.getRaidCrystalBlock().defaultBlockState(), canReset, RaidCycleMode.BUCKET, location, bucket);
        return 1;
    }

    @SuppressWarnings("unused")
    private static int createRaidDenWithBucket(CommandContext<CommandSourceStack> context, BlockPos blockPos, ServerLevel level, ResourceLocation bucket, Boolean canReset) {
        if (level.getBiome(blockPos).is(ModDimensions.RAIDDIM_BIOME)) return 0;

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof RaidCrystalBlockEntity raidCrystal) return createRaidDenWithBucketFromExisting(level, raidCrystal.getBlockState(), blockPos, bucket, canReset);
        else return createRaidDenWithBucketNew(level, blockPos, bucket, canReset);
    }
}