package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.raid.dens.common.advancements.RaidDenCriteriaTriggers;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.cobbledollars.RaidDensCobbleDollarsCompat;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.events.RewardPokemonEvent;
import com.necro.raid.dens.common.items.ModItems;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.util.ComponentUtils;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record RewardHandler(RaidBoss raidBoss, ServerPlayer player, Pokemon pokemonReward) {
    public RewardHandler(RaidBoss raidBoss, ServerPlayer player, @Nullable Pokemon pokemonReward) {
        this.raidBoss = raidBoss;
        this.player = player;
        this.pokemonReward = pokemonReward;
    }

    public void sendRewardMessage() {
        if (this.raidBoss.getDisplaySpecies() == null) this.raidBoss.createDisplayAspects();
        String speciesName = ((TranslatableContents) this.raidBoss.getDisplaySpecies().getTranslatedName().getContents()).getKey();
        RaidDenNetworkMessages.REWARD_PACKET.accept(this.player, this.pokemonReward != null, speciesName);
        RaidHelper.REWARD_QUEUE.put(this.player.getUUID(), this);
    }

    public boolean givePokemonToPlayer() {
        if (this.pokemonReward != null) {
            if (!(this.player.getMainHandItem().getItem() instanceof PokeBallItem pokeBallItem)) {
                this.player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.reward.reward_not_pokeball"), true);
                return false;
            }
            this.pokemonReward.setCaughtBall(pokeBallItem.getPokeBall());
            if (!RaidEvents.REWARD_POKEMON.postWithResult(new RewardPokemonEvent(this.player, this.pokemonReward))) return false;

            RaidDenCriteriaTriggers.triggerRaidShiny(this.player, this.pokemonReward);
            PlayerExtensionsKt.party(player).add(this.pokemonReward);
            this.player.getMainHandItem().consume(1, this.player);
        }

        return this.giveItemToPlayer();
    }

    public boolean giveItemToPlayer() {
        ItemStack raidPouch = this.buildRaidPouch();
        List<ItemStack> rewards = this.raidBoss.getRandomRewards(this.player.serverLevel());
        rewards.addFirst(raidPouch);
        for (ItemStack item : rewards) {
            if (!this.player.getInventory().add(item)) {
                ItemEntity itemEntity = this.player.drop(item, false);
                if (itemEntity == null) continue;
                itemEntity.setNoPickUpDelay();
                itemEntity.setTarget(this.player.getUUID());
            }
        }
        return true;
    }

    public void giveCurrency() {
        if (!ModCompat.COBBLEDOLLARS.isLoaded()) return;
        else if (this.raidBoss.getCurrency() <= 0) return;
        RaidDensCobbleDollarsCompat.addCurrency(player, this.raidBoss.getCurrency());
    }

    private ItemStack buildRaidPouch() {
        ItemStack item = ModItems.RAID_POUCH.value().getDefaultInstance();
        item.set(ModComponents.TIER_COMPONENT.value(), this.raidBoss.getTier());
        item.set(ModComponents.FEATURE_COMPONENT.value(), this.raidBoss.getFeature());
        item.set(ModComponents.TYPE_COMPONENT.value(), this.raidBoss.getType());
        return item;
    }
}
