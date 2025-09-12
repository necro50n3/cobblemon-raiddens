package com.necro.raid.dens.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.events.RaidJoinEvent;
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

import java.util.HashSet;

public class RaidRequestCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("crd_raids")
            .requires(source -> source.isPlayer() && RaidHelper.RAID_HOSTS.contains(source.getPlayer().getUUID()))
            .then(Commands.literal("acceptrequest")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("origin", DimensionArgument.dimension())
                        .then(Commands.argument("position", BlockPosArgument.blockPos())
                            .executes(RaidRequestCommands::acceptRequest)
                        )
                    )
                )
            )
            .then(Commands.literal("denyrequest")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(RaidRequestCommands::denyRequest)
                )
            )
        );
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        register(dispatcher);
    }

    private static int acceptRequest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer host = context.getSource().getPlayer();
        assert host != null;
        if (!RaidHelper.RAID_HOSTS.contains(host.getUUID())) return 0;
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ServerLevel originalLevel = DimensionArgument.getDimension(context, "origin");
        BlockPos blockPos = BlockPosArgument.getBlockPos(context, "position");
        RaidCrystalBlockEntity blockEntity = (RaidCrystalBlockEntity) originalLevel.getBlockEntity(blockPos);
        if (blockEntity == null) return 0;
        else if (blockEntity.isFull()) {
            host.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.lobby_is_full"));
            return 0;
        }

        boolean success = RaidEvents.RAID_JOIN.postWithResult(new RaidJoinEvent(player, false, blockEntity.getRaidBoss()));
        if (!success) return 0;

        if (RaidHelper.JOIN_QUEUE.containsKey(player) && !RaidHelper.isAlreadyParticipating(player)) {
            RaidHelper.JOIN_QUEUE.remove(player);
            RaidHelper.addParticipant(player);
            blockEntity.addPlayer(player);
            player.teleportTo(blockEntity.getDimension(), 0.5, 0, -0.5, new HashSet<>(), 180f, 0f);
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.accepted_request"));
            host.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.confirm_accept_request"));
            return 1;
        }
        else {
            host.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.request_time_out"));
            return 0;
        }
    }

    private static int denyRequest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer host = context.getSource().getPlayer();
        assert host != null;
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        if (RaidHelper.JOIN_QUEUE.containsKey(player)) {
            RaidHelper.JOIN_QUEUE.get(player).refundItem();
            RaidHelper.JOIN_QUEUE.remove(player);
            player.sendSystemMessage(RaidHelper.getSystemMessage(
                Component.translatable("message.cobblemonraiddens.raid.rejected_request", host.getName()))
            );
            host.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.confirm_deny_request"));
            return 1;
        }
        else {
            host.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.request_time_out"));
            return 0;
        }
    }
}
