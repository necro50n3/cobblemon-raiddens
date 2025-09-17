package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.raid.dens.common.advancements.RaidDenCriteriaTriggers;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.items.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RewardHandler {
    private final RaidBoss raidBoss;
    private final ServerPlayer player;

    public RewardHandler(RaidBoss raidBoss, ServerPlayer player) {
        this.raidBoss = raidBoss;
        this.player = player;
    }

    public void sendRewardMessage() {
        MutableComponent component = Component.translatable("message.cobblemonraiddens.raid.raid_success", player.getName());
        if (this.raidBoss.isCatchable()) {
            component.append(Component.translatable("message.cobblemonraiddens.raid.raid_catch")
                .withStyle(Style.EMPTY.applyFormat(ChatFormatting.GREEN).withClickEvent(
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, this.getPokemonAndItems())
                )));
        }
        component.append(Component.translatable("message.cobblemonraiddens.raid.raid_leave")
            .withStyle(Style.EMPTY.applyFormat(ChatFormatting.RED).withClickEvent(
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, this.getItemsOnly())
            )));
        this.player.sendSystemMessage(component);
        RaidHelper.REWARD_QUEUE.put(this.player, this);
    }

    private String getPokemonAndItems() {
        return "/crd_rewards claim";
    }

    private String getItemsOnly() {
        return "/crd_rewards claim itemonly";
    }

    public boolean givePokemonToPlayer() {
        if (!(player.getMainHandItem().getItem() instanceof PokeBallItem pokeBallItem)) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.reward.reward_not_pokeball"));
            return false;
        }
        else if (!this.giveItemToPlayer()) return false;

        Pokemon pokemon = this.raidBoss.getRewardPokemon(player);
        pokemon.setCaughtBall(pokeBallItem.getPokeBall());
        PlayerExtensionsKt.party(player).add(pokemon);
        player.getMainHandItem().consume(1, player);

        RaidDenCriteriaTriggers.triggerRaidShiny(player, pokemon);
        return true;
    }

    public boolean giveItemToPlayer() {
        ItemStack raidPouch = this.buildRaidPouch();
        List<ItemStack> rewards = this.raidBoss.getRandomRewards(this.player.serverLevel());
        rewards.addFirst(raidPouch);
        for (ItemStack item : rewards) {
            if (!player.getInventory().add(item)) {
                ItemEntity itemEntity = player.drop(item, false);
                if (itemEntity == null) continue;
                itemEntity.setNoPickUpDelay();
                itemEntity.setTarget(player.getUUID());
            }
        }
        return true;
    }

    private ItemStack buildRaidPouch() {
        ItemStack item = ModItems.RAID_POUCH.value().getDefaultInstance();
        item.set(ModComponents.TIER_COMPONENT.value(),  this.raidBoss.getTier());
        item.set(ModComponents.FEATURE_COMPONENT.value(), this.raidBoss.getFeature());
        item.set(ModComponents.TYPE_COMPONENT.value(),  this.raidBoss.getType());
        return item;
    }
}
