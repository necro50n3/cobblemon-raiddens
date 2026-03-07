package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.raid.dens.common.advancements.RaidDenCriteriaTriggers;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.cobbledollars.RaidDensCobbleDollarsCompat;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.events.ModifyCatchRateEvent;
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
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RewardHandler {
    private final ResourceLocation raidBossId;
    private RaidBoss raidBoss;
    private final UUID playerUUID;
    private final @Nullable Pokemon pokemonReward;
    private final float catchRate;

    public RewardHandler(RaidBoss raidBoss, UUID playerUUID, @Nullable Pokemon pokemonReward, float catchRate) {
        this.raidBossId = raidBoss.getId();
        this.raidBoss = raidBoss;
        this.playerUUID = playerUUID;
        this.pokemonReward = pokemonReward;
        this.catchRate = catchRate;
    }

    public RewardHandler(ResourceLocation raidBossId, UUID playerUUID, @Nullable Pokemon pokemonReward, float catchRate) {
        this.raidBossId = raidBossId;
        this.raidBoss = null;
        this.playerUUID = playerUUID;
        this.pokemonReward = pokemonReward;
        this.catchRate = catchRate;
    }

    public void sendRewardMessage(ServerPlayer player) {
        if (this.raidBoss == null) this.raidBoss = RaidRegistry.getRaidBoss(this.raidBossId);
        if (this.raidBoss.getDisplaySpecies() == null) this.raidBoss.createDisplayAspects();
        String speciesName = ((TranslatableContents) this.raidBoss.getDisplaySpecies().getTranslatedName().getContents()).getKey();
        RaidDenNetworkMessages.REWARD_PACKET.accept(player, this.pokemonReward != null, speciesName);
        RaidHelper.REWARD_QUEUE.put(player.getUUID(), this);
    }

    public boolean givePokemonToPlayer(ServerPlayer player) {
        if (this.raidBoss == null) this.raidBoss = RaidRegistry.getRaidBoss(this.raidBossId);

        boolean success = true;
        if (this.pokemonReward != null) {
            if (!(player.getMainHandItem().getItem() instanceof PokeBallItem pokeBallItem)) {
                player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.reward.reward_not_pokeball"), true);
                return false;
            }
            success = this.rollPokemon(player);
            if (success) {
                this.pokemonReward.setCaughtBall(pokeBallItem.getPokeBall());
                if (!RaidEvents.REWARD_POKEMON.postWithResult(new RewardPokemonEvent(player, this.pokemonReward))) return false;

                PlayerExtensionsKt.party(player).add(this.pokemonReward);
                player.getMainHandItem().consume(1, player);
                RaidDenCriteriaTriggers.triggerRaidShiny(player, this.pokemonReward);
            }
        }

        return this.giveItemToPlayer(player, !success);
    }

    private boolean rollPokemon(ServerPlayer player) {
        ModifyCatchRateEvent event = new ModifyCatchRateEvent(player, this.pokemonReward, this.catchRate);
        RaidEvents.MODIFY_CATCH_RATE.emit(event);
        if (player.getRandom().nextFloat() < event.catchRate()) return true;
        player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.reward.failed_catch_rate"), true);
        return false;
    }

    public boolean giveItemToPlayer(ServerPlayer player, boolean applyBonus) {
        if (this.raidBoss == null) this.raidBoss = RaidRegistry.getRaidBoss(this.raidBossId);
        ItemStack raidPouch = this.buildRaidPouch(applyBonus);
        if (raidPouch == null) {
            player.displayClientMessage(ComponentUtils.getErrorMessage("error.cobblemonraiddens.raid_boss_not_found"), true);
            return true;
        }

        if (!player.getInventory().add(raidPouch)) {
            ItemEntity itemEntity = player.drop(raidPouch, false);
            if (itemEntity == null) return true;
            itemEntity.setNoPickUpDelay();
            itemEntity.setTarget(player.getUUID());
        }
        return true;
    }

    public void giveCurrency(ServerPlayer player) {
        if (this.raidBoss == null) this.raidBoss = RaidRegistry.getRaidBoss(this.raidBossId);
        if (!ModCompat.COBBLEDOLLARS.isLoaded()) return;
        else if (this.raidBoss.getCurrency() <= 0) return;
        RaidDensCobbleDollarsCompat.addCurrency(player, this.raidBoss.getCurrency());
    }

    private ItemStack buildRaidPouch(boolean applyBonus) {
        if (this.raidBoss == null) this.raidBoss = RaidRegistry.getRaidBoss(this.raidBossId);
        if (this.raidBoss == null) return null;

        ItemStack item = ModItems.RAID_POUCH.value().getDefaultInstance();
        item.set(ModComponents.TIER_COMPONENT.value(), this.raidBoss.getTier());
        item.set(ModComponents.FEATURE_COMPONENT.value(), this.raidBoss.getFeature());
        item.set(ModComponents.TYPE_COMPONENT.value(), this.raidBoss.getType());
        if (this.raidBoss.getId() != null) item.set(ModComponents.BOSS_COMPONENT.value(), this.raidBoss.getId());
        item.set(ModComponents.BONUS_LOOT_COMPONENT.value(), applyBonus);
        return item;
    }

    public CompoundTag serialize(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("raid_boss", this.raidBossId.toString());
        tag.putUUID("player", this.playerUUID);
        if (this.pokemonReward != null) {
            tag.put("pokemon_reward", this.pokemonReward.saveToNBT((RegistryAccess) provider, new CompoundTag()));
        }
        tag.putFloat("catch_rate", this.catchRate);
        return tag;
    }

    public static RewardHandler deserialize(CompoundTag tag, HolderLookup.Provider provider) {
        ResourceLocation raidBossId = ResourceLocation.parse(tag.getString("raid_boss"));
        UUID playerUUID = tag.getUUID("player");
        Pokemon pokemonReward = null;
        if (tag.contains("pokemon_reward")) {
            pokemonReward = new Pokemon();
            pokemonReward.loadFromNBT((net.minecraft.core.RegistryAccess) provider, tag.getCompound("cached_reward"));
        }
        float catchRate = tag.getFloat("catch_rate");
        return new RewardHandler(raidBossId, playerUUID, pokemonReward, catchRate);
    }
}
