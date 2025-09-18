package com.necro.raid.dens.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.common.raids.RaidHelper;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class RaidAdminCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("crd")
            .then(Commands.literal("resetclears")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .then(Commands.argument("dimension", DimensionArgument.dimension())
                        .executes(context -> resetClearsForAll(
                            context,
                            BlockPosArgument.getBlockPos(context, "pos"),
                            DimensionArgument.getDimension(context, "dimension")
                        ))
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(RaidAdminCommands::resetClearsForPlayerAndPos)
                        )
                    )
                    .requires(CommandSourceStack::isPlayer)
                    .executes(context -> resetClearsForAll(
                        context, BlockPosArgument.getBlockPos(context, "pos")
                    ))
                )
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(RaidAdminCommands::resetClearsForPlayer)
                )
            )
            .then(Commands.literal("remove")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("dimension", DimensionArgument.dimension())
                    .executes(RaidAdminCommands::removeDimension)
                )
            )
            .then(Commands.literal("refresh")
                .then(Commands.argument("player", EntityArgument.player())
                    .requires(source -> source.hasPermission(2))
                    .executes(context -> refreshPlayer(context, EntityArgument.getPlayer(context, "player")))
                )
                .requires(CommandSourceStack::isPlayer)
                .executes(RaidAdminCommands::refreshPlayer)
            )
        );
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        register(dispatcher);
    }

    private static int refreshPlayer(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        if (DimensionHelper.isCustomDimension(player.serverLevel())) {
            context.getSource().sendFailure(Component.translatable("error.cobblemonraiddens.player_in_raid"));
            return 0;
        }

        RaidHelper.removeHost(player.getUUID());
        RaidHelper.removeParticipant(player.getUUID());
        if (RaidHelper.JOIN_QUEUE.containsKey(player)) {
            RaidHelper.JOIN_QUEUE.get(player).refundItem();
            RaidHelper.JOIN_QUEUE.remove(player);
        }
        context.getSource().sendSystemMessage(
            RaidHelper.getSystemMessage(Component.translatable("message.cobblemonraiddens.command.refresh_player", player.getName()))
        );
        return 1;
    }

    private static int refreshPlayer(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        return refreshPlayer(context, player);
    }

    private static int resetClearsForPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        RaidHelper.resetPlayerAllClearedRaids(player.getUUID());
        context.getSource().sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.command.reset_clears"));
        return 1;
    }

    private static int resetClearsForPlayerAndPos(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        BlockPos blockPos = BlockPosArgument.getBlockPos(context, "pos");
        ServerLevel dimension = DimensionArgument.getDimension(context, "dimension");
        RaidHelper.resetPlayerClearedRaid(dimension, blockPos, player.getUUID());
        context.getSource().sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.command.reset_clears"));

        if (dimension.getBlockEntity(blockPos) instanceof RaidCrystalBlockEntity raidCrystal) raidCrystal.resetClears();
        return 1;
    }

    private static int resetClearsForAll(CommandContext<CommandSourceStack> context, BlockPos blockPos) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        ServerLevel dimension = player.serverLevel();
        return resetClearsForAll(context, blockPos, dimension);
    }

    private static int resetClearsForAll(CommandContext<CommandSourceStack> context, BlockPos blockPos, ServerLevel dimension) {
        RaidHelper.resetClearedRaids(dimension, blockPos);
        context.getSource().sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.command.reset_clears"));

        if (dimension.getBlockEntity(blockPos) instanceof RaidCrystalBlockEntity raidCrystal) raidCrystal.resetClears();
        return 1;
    }

    private static int removeDimension(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerLevel level = DimensionArgument.getDimension(context, "dimension");
        if (!DimensionHelper.isCustomDimension(level)) {
            context.getSource().sendFailure(Component.translatable("error.cobblemonraiddens.invalid_dimension"));
            return 0;
        }
        else if (!level.players().isEmpty()) {
            context.getSource().sendFailure(Component.translatable("error.cobblemonraiddens.players_in_dimension"));
            return 0;
        }
        DimensionHelper.queueForRemoval(level.dimension(), level);
        DimensionHelper.SYNC_DIMENSIONS.accept(context.getSource().getServer(), level.dimension(), false);
        context.getSource().sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.command.remove_dimension"));
        return 1;
    }
}