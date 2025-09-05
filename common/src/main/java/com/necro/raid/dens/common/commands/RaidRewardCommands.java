package com.necro.raid.dens.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.raids.RewardHandler;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class RaidRewardCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        dispatcher.register(Commands.literal("crd_rewards")
            .requires(source -> source.isPlayer() && RaidHelper.REWARD_QUEUE.containsKey(source.getPlayer()))
            .then(Commands.literal("claim")
                .executes(RaidRewardCommands::claimPokemonAndItems)
                .then(Commands.literal("itemonly")
                    .executes(RaidRewardCommands::claimItemsOnly)
                )
            )
        );
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        register(dispatcher, commandBuildContext);
    }

    private static int claimPokemonAndItems(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        RewardHandler handler = RaidHelper.REWARD_QUEUE.get(player);
        if (handler == null) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.reward.already_received_reward"));
            return 0;
        }
        else if (handler.givePokemonToPlayer()) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.reward.reward_pokemon"));
            RaidHelper.REWARD_QUEUE.remove(player);
            player.getServer().getCommands().sendCommands(player);
            return 1;
        }
        else return 0;
    }

    private static int claimItemsOnly(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        RewardHandler handler = RaidHelper.REWARD_QUEUE.get(player);
        if (handler == null) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.reward.already_received_reward"));
            return 0;
        }
        else if (handler.giveItemToPlayer()) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.reward.reward_item"));
            RaidHelper.REWARD_QUEUE.remove(player);
            player.getServer().getCommands().sendCommands(player);
            return 1;
        }
        else return 0;
    }
}
