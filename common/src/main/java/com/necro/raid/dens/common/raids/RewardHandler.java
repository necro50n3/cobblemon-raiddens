package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.raid.dens.common.advancements.RaidDenCriteriaTriggers;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.cobbledollars.RaidDensCobbleDollarsCompat;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.items.ModItems;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RewardHandler {
    private final RaidBoss raidBoss;
    private final ServerPlayer player;
    private final boolean isCatchable;

    public RewardHandler(RaidBoss raidBoss, ServerPlayer player, boolean isCatchable) {
        this.raidBoss = raidBoss;
        this.player = player;
        this.isCatchable = isCatchable;
    }

    public void sendRewardMessage() {
        if (this.raidBoss.getDisplaySpecies() == null) this.raidBoss.createDisplayAspects();
        String speciesName = ((TranslatableContents) this.raidBoss.getDisplaySpecies().getTranslatedName().getContents()).getKey();
        RaidDenNetworkMessages.REWARD_PACKET.accept(this.player, this.isCatchable, speciesName);
        RaidHelper.REWARD_QUEUE.put(this.player, this);
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

    public void giveCurrency() {
        if (!ModCompat.COBBLEDOLLARS.isLoaded()) return;
        RaidDensCobbleDollarsCompat.addCurrency(player, this.raidBoss.getCurrency());
    }

    private ItemStack buildRaidPouch() {
        ItemStack item = ModItems.RAID_POUCH.value().getDefaultInstance();
        item.set(ModComponents.TIER_COMPONENT.value(),  this.raidBoss.getTier());
        item.set(ModComponents.FEATURE_COMPONENT.value(), this.raidBoss.getFeature());
        item.set(ModComponents.TYPE_COMPONENT.value(),  this.raidBoss.getType());
        return item;
    }
}
