package com.necro.raid.dens.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("reset")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(RaidAdminCommands::resetPlayer)
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(RaidAdminCommands::resetClearsForPlayer)
                    )
                )
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .executes(RaidAdminCommands::resetClearsForAll)
                )
            )
            .then(Commands.literal("remove")
                .then(Commands.argument("dimension", DimensionArgument.dimension())
                    .executes(RaidAdminCommands::removeDimension)
                )
            )
        );
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        register(dispatcher);
    }

    private static int resetPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        RaidHelper.RAID_HOSTS.remove(player.getUUID());
        RaidHelper.RAID_PARTICIPANTS.remove(player.getUUID());
        if (RaidHelper.JOIN_QUEUE.containsKey(player)) {
            RaidHelper.JOIN_QUEUE.get(player).refundItem();
            RaidHelper.JOIN_QUEUE.remove(player);
        }
        return 1;
    }

    private static int resetClearsForPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        BlockPos blockPos = BlockPosArgument.getBlockPos(context, "pos");

        if (!RaidHelper.CLEARED_RAIDS.containsKey(blockPos)) return 0;
        RaidHelper.CLEARED_RAIDS.get(blockPos).remove(player.getUUID());
        return 1;
    }

    private static int resetClearsForAll(CommandContext<CommandSourceStack> context) {
        BlockPos blockPos = BlockPosArgument.getBlockPos(context, "pos");
        RaidHelper.CLEARED_RAIDS.remove(blockPos);
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
        return 1;
    }
}