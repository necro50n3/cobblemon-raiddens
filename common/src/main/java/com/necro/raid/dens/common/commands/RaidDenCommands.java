package com.necro.raid.dens.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.raids.RaidBoss;
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

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("crd")
            .then(Commands.literal("dens")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("position", BlockPosArgument.blockPos())
                    .executes(context -> createRaidDen(
                        context,
                        BlockPosArgument.getBlockPos(context, "position")
                    ))
                    .then(Commands.argument("can_cycle", BoolArgumentType.bool())
                        .executes(context -> createRaidDen(
                            context,
                            BlockPosArgument.getBlockPos(context, "position"),
                            BoolArgumentType.getBool(context, "can_cycle")
                        ))
                        .then(Commands.argument("can_reset", BoolArgumentType.bool())
                            .executes(context -> createRaidDen(
                                context,
                                BlockPosArgument.getBlockPos(context, "position"),
                                BoolArgumentType.getBool(context, "can_cycle"),
                                BoolArgumentType.getBool(context, "can_reset")
                            ))
                            .then(Commands.literal("tier")
                                .then(Commands.argument("tier", StringArgumentType.word())
                                    .suggests(RAID_TIERS)
                                    .executes(context -> createRaidDen(
                                        context,
                                        BlockPosArgument.getBlockPos(context, "position"),
                                        BoolArgumentType.getBool(context, "can_cycle"),
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
                                        BoolArgumentType.getBool(context, "can_cycle"),
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

    private static int createRaidDen(CommandContext<CommandSourceStack> context, BlockPos blockPos, boolean canCycle, boolean canReset, RaidBoss raidBoss) throws CommandSyntaxException {
        if (raidBoss == null) return 0;

        ServerLevel level = context.getSource().getLevel();
        if (level.getBiome(blockPos).is(ModDimensions.RAIDDIM_BIOME)) return 0;
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        BlockState blockState;

        if (blockEntity instanceof RaidCrystalBlockEntity raidCrystal) blockState = raidCrystal.getBlockState();
        else blockState = ModBlocks.INSTANCE.getRaidCrystalBlock().defaultBlockState();

        level.setBlock(blockPos, blockState
            .setValue(RaidCrystalBlock.ACTIVE, true)
            .setValue(RaidCrystalBlock.CAN_RESET, canReset)
            .setValue(RaidCrystalBlock.CAN_CYCLE, canCycle)
            .setValue(RaidCrystalBlock.RAID_TYPE, raidBoss.getType())
            .setValue(RaidCrystalBlock.RAID_TIER, raidBoss.getTier()), 2);

        ((RaidCrystalBlockEntity) level.getBlockEntity(blockPos)).setRaidBoss(raidBoss);
        return 1;
    }

    private static int createRaidDen(CommandContext<CommandSourceStack> context, BlockPos blockPos, boolean canCycle, boolean canReset, ResourceLocation raidBoss) throws CommandSyntaxException {
        RaidBoss raidBossObj = context.getSource().getLevel().registryAccess().registryOrThrow(RaidRegistry.RAID_BOSS_KEY).get(raidBoss);
        return createRaidDen(context, blockPos, canCycle, canReset, raidBossObj);
    }

    private static int createRaidDen(CommandContext<CommandSourceStack> context, BlockPos blockPos, boolean canCycle, boolean canReset, RaidTier raidTier) throws CommandSyntaxException {
        RandomSource random = context.getSource().getLevel().getRandom();
        return createRaidDen(context, blockPos, canCycle, canReset, RaidRegistry.getRandomRaidBoss(random, raidTier));
    }

    private static int createRaidDen(CommandContext<CommandSourceStack> context, BlockPos blockPos, boolean canCycle, boolean canReset) throws CommandSyntaxException {
        Level level = context.getSource().getLevel();
        return createRaidDen(context, blockPos, canCycle, canReset, RaidRegistry.getRandomRaidBoss(level.getRandom(), level));
    }

    private static int createRaidDen(CommandContext<CommandSourceStack> context, BlockPos blockPos, boolean canCycle) throws CommandSyntaxException {
        return createRaidDen(context, blockPos, canCycle, true);
    }

    private static int createRaidDen(CommandContext<CommandSourceStack> context, BlockPos blockPos) throws CommandSyntaxException {
        return createRaidDen(context, blockPos, true);
    }
}