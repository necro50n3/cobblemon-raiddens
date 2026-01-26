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
import com.necro.raid.dens.common.registry.RaidRegistry;
import com.necro.raid.dens.common.util.ComponentUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class RewardHandler {
    private final ResourceLocation raidBossId;
    private RaidBoss raidBoss;
    private final UUID playerUUID;
    private final boolean isCatchable;

    private final Pokemon cachedReward;

    public RewardHandler(RaidBoss raidBoss, UUID playerUUID, boolean isCatchable, Pokemon cachedReward) {
        this.raidBossId = raidBoss.getId();
        this.raidBoss = raidBoss;
        this.playerUUID = playerUUID;
        this.isCatchable = isCatchable;
        this.cachedReward = cachedReward == null ? null : cachedReward.clone(true, null);
    }

    public RewardHandler(ResourceLocation raidBossId, UUID playerUUID, boolean isCatchable, Pokemon cachedReward) {
        this.raidBossId = raidBossId;
        this.raidBoss = null;
        this.playerUUID = playerUUID;
        this.isCatchable = isCatchable;
        this.cachedReward = cachedReward == null ? null : cachedReward.clone(true, null);
    }

    public RewardHandler(RaidBoss raidBoss, ServerPlayer player, boolean isCatchable, Pokemon cachedReward) {
        this(raidBoss, player.getUUID(), isCatchable, cachedReward);
    }

    public RewardHandler(RaidBoss raidBoss, ServerPlayer player, boolean isCatchable) {
        this(raidBoss, player, isCatchable, null);
    }

    public void sendRewardMessage(ServerPlayer player) {
        if (this.raidBoss == null) this.raidBoss = RaidRegistry.getRaidBoss(this.raidBossId);
        if (this.raidBoss.getDisplaySpecies() == null) this.raidBoss.createDisplayAspects();
        String speciesName = ((TranslatableContents) this.raidBoss.getDisplaySpecies().getTranslatedName().getContents()).getKey();
        RaidDenNetworkMessages.REWARD_PACKET.accept(player, this.isCatchable, speciesName);
        RaidHelper.REWARD_QUEUE.put(player.getUUID(), this);
    }

    public boolean givePokemonToPlayer(ServerPlayer player) {
        if (this.raidBoss == null) this.raidBoss = RaidRegistry.getRaidBoss(this.raidBossId);
        if (!(player.getMainHandItem().getItem() instanceof PokeBallItem pokeBallItem)) {
            player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.reward.reward_not_pokeball"), true);
            return false;
        }

        Pokemon pokemon = this.cachedReward == null ? this.raidBoss.getRewardPokemon(player) : this.cachedReward;
        pokemon.setCaughtBall(pokeBallItem.getPokeBall());

        if (!RaidEvents.REWARD_POKEMON.postWithResult(new RewardPokemonEvent(player, pokemon))) return false;
        else if (!this.giveItemToPlayer(player)) return false;

        PlayerExtensionsKt.party(player).add(pokemon);
        player.getMainHandItem().consume(1, player);

        RaidDenCriteriaTriggers.triggerRaidShiny(player, pokemon);
        return true;
    }

    public boolean giveItemToPlayer(ServerPlayer player) {
        if (this.raidBoss == null) this.raidBoss = RaidRegistry.getRaidBoss(this.raidBossId);
        ItemStack raidPouch = this.buildRaidPouch();
        List<ItemStack> rewards = this.raidBoss.getRandomRewards(player.serverLevel());
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

    public void giveCurrency(ServerPlayer player) {
        if (this.raidBoss == null) this.raidBoss = RaidRegistry.getRaidBoss(this.raidBossId);
        if (!ModCompat.COBBLEDOLLARS.isLoaded()) return;
        else if (this.raidBoss.getCurrency() <= 0) return;
        RaidDensCobbleDollarsCompat.addCurrency(player, this.raidBoss.getCurrency());
    }

    private ItemStack buildRaidPouch() {
        if (this.raidBoss == null) this.raidBoss = RaidRegistry.getRaidBoss(this.raidBossId);
        ItemStack item = ModItems.RAID_POUCH.value().getDefaultInstance();
        item.set(ModComponents.TIER_COMPONENT.value(),  this.raidBoss.getTier());
        item.set(ModComponents.FEATURE_COMPONENT.value(), this.raidBoss.getFeature());
        item.set(ModComponents.TYPE_COMPONENT.value(),  this.raidBoss.getType());
        return item;
    }

    public CompoundTag serialize(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("raid_boss", this.raidBossId.toString());
        tag.putUUID("player", this.playerUUID);
        tag.putBoolean("is_catchable", this.isCatchable);
        if (this.cachedReward != null) {
            tag.put("cached_reward", this.cachedReward.saveToNBT((RegistryAccess) provider, new CompoundTag()));
        }
        return tag;
    }

    public static RewardHandler deserialize(CompoundTag tag, HolderLookup.Provider provider) {
        ResourceLocation raidBossId = ResourceLocation.parse(tag.getString("raid_boss"));
        UUID playerUUID = tag.getUUID("player");
        boolean isCatchable = tag.getBoolean("is_catchable");
        Pokemon cachedReward = null;
        if (tag.contains("cached_reward")) {
            cachedReward = new Pokemon();
            cachedReward.loadFromNBT((net.minecraft.core.RegistryAccess) provider, tag.getCompound("cached_reward"));
        }
        return new RewardHandler(raidBossId, playerUUID, isCatchable, cachedReward);
    }
}
