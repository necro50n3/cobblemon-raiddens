package com.necro.raid.dens.common.commands;

import com.cobblemon.mod.common.api.permission.Permission;
import com.cobblemon.mod.common.api.permission.PermissionLevel;
import com.cobblemon.mod.common.util.PermissionUtilsKt;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.commands.permission.RaidDenPermission;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.raids.helpers.RaidJoinHelper;
import com.necro.raid.dens.common.util.RaidUtils;
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
import net.minecraft.world.level.block.state.BlockState;

public class RaidAdminCommands {
    private static final Permission RESET_CLEARS = new RaidDenPermission("command.resetclears", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS);
    private static final Permission REMOVE = new RaidDenPermission("command.remove", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS);
    private static final Permission REFRESH = new RaidDenPermission("command.refreshother", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("crd")
            .then(PermissionUtilsKt.permission(
                Commands.literal("resetclears")
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
                    ),
                RESET_CLEARS, true
            ))
            .then(PermissionUtilsKt.permission(
                Commands.literal("remove")
                    .then(Commands.argument("dimension", DimensionArgument.dimension())
                        .executes(RaidAdminCommands::removeDimension)
                    ),
                REMOVE, true
            ))
            .then(Commands.literal("refresh")
                .then(PermissionUtilsKt.permission(
                    Commands.argument("player", EntityArgument.player())
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> refreshPlayer(context, EntityArgument.getPlayer(context, "player"))),
                    REFRESH, true
                ))
                .requires(CommandSourceStack::isPlayer)
                .executes(RaidAdminCommands::refreshPlayer)
            )
        );
    }

    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        register(dispatcher);
    }

    private static int refreshPlayer(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        if (RaidUtils.isRaidDimension(player.serverLevel())) {
            context.getSource().sendFailure(Component.translatable("error.cobblemonraiddens.player_in_raid"));
            return 0;
        }

        RaidJoinHelper.removeParticipant(player);
        RaidJoinHelper.removeFromQueue(player);

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

        if (dimension.getBlockEntity(blockPos) instanceof RaidCrystalBlockEntity raidCrystal) {
            RaidHelper.resetPlayerClearedRaid(raidCrystal.getUuid(), player.getUUID());
            context.getSource().sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.command.reset_clears"));
            raidCrystal.resetClears();
            return 1;
        }
        else return 0;
    }

    private static int resetClearsForAll(CommandContext<CommandSourceStack> context, BlockPos blockPos) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        ServerLevel dimension = player.serverLevel();
        return resetClearsForAll(context, blockPos, dimension);
    }

    private static int resetClearsForAll(CommandContext<CommandSourceStack> context, BlockPos blockPos, ServerLevel dimension) {
        if (dimension.getBlockEntity(blockPos) instanceof RaidCrystalBlockEntity raidCrystal) {
            RaidHelper.resetClearedRaids(raidCrystal.getUuid());
            context.getSource().sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.command.reset_clears"));
            raidCrystal.resetClears();

            BlockState blockState = raidCrystal.getBlockState();
            dimension.setBlock(blockPos, blockState.setValue(RaidCrystalBlock.ACTIVE, true), 2);
            return 1;
        }
        else return 0;
    }

    private static int removeDimension(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerLevel level = DimensionArgument.getDimension(context, "dimension");
        if (!RaidUtils.isRaidDimension(level)) {
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