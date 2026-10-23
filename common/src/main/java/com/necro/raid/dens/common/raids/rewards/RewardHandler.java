package com.necro.raid.dens.common.raids.rewards;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.raid.dens.common.advancements.RaidDenCriteriaTriggers;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.data.RaidSession;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.events.RewardPokemonEvent;
import com.necro.raid.dens.common.items.ModItems;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.registry.RaidRegistry;
import com.necro.raid.dens.common.util.ComponentUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class RewardHandler {
    private final ResourceLocation raidSessionId;
    private final CompoundTag serializedPokemonReward;

    private RaidSession raidSession;
    private final UUID playerUUID;
    private @Nullable Pokemon pokemonReward;
    private final float catchRate;

    public RewardHandler(RaidSession raidSession, UUID playerUUID, @Nullable Pokemon pokemonReward, float catchRate) {
        this.raidSessionId = raidSession.instance().getId();
        this.serializedPokemonReward = null;
        this.raidSession = raidSession;
        this.playerUUID = playerUUID;
        this.pokemonReward = pokemonReward;
        this.catchRate = catchRate;
    }

    public RewardHandler(ResourceLocation raidSessionId, UUID playerUUID, @Nullable CompoundTag pokemonReward, float catchRate) {
        this.raidSessionId = raidSessionId;
        this.serializedPokemonReward = pokemonReward;
        this.raidSession = null;
        this.playerUUID = playerUUID;
        this.pokemonReward = null;
        this.catchRate = catchRate;
    }

    public void sendRewardMessage(ServerPlayer player) {
        this.raidSession(session -> {
            if (session.instance().getDisplaySpecies() == null) session.instance().createDisplayAspects();
            String speciesName = ((TranslatableContents) session.instance().getDisplaySpecies().getTranslatedName().getContents()).getKey();
            RaidDenNetworkMessages.REWARD_PACKET.accept(player, this.catchRate, speciesName);
            RaidHelper.REWARD_QUEUE.put(player.getUUID(), this);
        }, player.getServer());
    }

    public CompletableFuture<Boolean> givePokemonToPlayer(ServerPlayer player) {
        return this.raidSession(session -> {
            if (this.pokemonReward == null && this.serializedPokemonReward != null) {
                this.pokemonReward = new Pokemon().loadFromNBT(player.registryAccess(), this.serializedPokemonReward);
            }

            boolean success = true;
            if (this.pokemonReward != null) {
                if (!(player.getMainHandItem().getItem() instanceof PokeBallItem pokeBallItem)) {
                    player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.reward.reward_not_pokeball"), true);
                    return false;
                }
                success = this.checkCatchingCharm(player);
                if (success) {
                    this.pokemonReward.setCaughtBall(pokeBallItem.getPokeBall());
                    if (!RaidEvents.REWARD_POKEMON.postWithResult(new RewardPokemonEvent(player, this.pokemonReward))) return false;

                    PlayerExtensionsKt.party(player).add(this.pokemonReward);
                    player.getMainHandItem().consume(1, player);
                    RaidDenCriteriaTriggers.triggerRaidShiny(player, this.pokemonReward);
                    player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.reward.reward_pokemon"), true);
                    player.playNotifySound(SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Cobblemon.MODID, "poke_ball.capture_succeeded")), SoundSource.PLAYERS, 1F,1F);
                }
                else {
                    player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.reward.failed_catch_rate"), true);
                }
            }

            return this.giveItemToPlayer(player, !success).join();
        }, player.getServer());
    }

    private boolean checkCatchingCharm(ServerPlayer player) {
        ItemStack charm = player.getOffhandItem();
        float catchRate = this.catchRate;
        if (charm.is(ModItems.CATCHING_CHARM)) {
            float mod = charm.getOrDefault(ModComponents.CATCH_BOOST.value(), 0F);
            catchRate = Mth.clamp(catchRate * (1F + mod), 0F, 1F);
            charm.consume(1, player);
            player.awardStat(Stats.ITEM_USED.get(ModItems.CATCHING_CHARM.value()));
            CriteriaTriggers.CONSUME_ITEM.trigger(player, charm);
        }
        return player.getRandom().nextFloat() < catchRate;
    }

    public CompletableFuture<Boolean> giveItemToPlayer(ServerPlayer player, boolean applyBonus) {
        return this.raidSession(session -> {
            ItemStack raidPouch = this.buildRaidPouch(session, applyBonus);
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
        }, player.getServer());
    }

    private ItemStack buildRaidPouch(RaidSession session, boolean applyBonus) {
        if (session == null) return null;

        ItemStack item = ModItems.RAID_POUCH.value().getDefaultInstance();
        item.set(ModComponents.TIER_COMPONENT.value(), session.instance().getTier());
        item.set(ModComponents.FEATURE_COMPONENT.value(), session.instance().getFeature());
        item.set(ModComponents.TYPE_COMPONENT.value(), session.instance().getType());
        if (session.instance().getId() != null) item.set(ModComponents.BOSS_COMPONENT.value(), session.instance().getId());
        item.set(ModComponents.BONUS_LOOT_COMPONENT.value(), applyBonus);
        item.set(ModComponents.ADVENTURE_COMPONENT.value(), session.isAdventure());
        return item;
    }

    public CompoundTag serialize(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("raid_boss", this.raidSessionId.toString());
        tag.putUUID("player", this.playerUUID);
        if (this.serializedPokemonReward != null) {
            tag.put("pokemon_reward", this.serializedPokemonReward);
        }
        else if (this.pokemonReward != null) {
            tag.put("pokemon_reward", this.pokemonReward.saveToNBT((RegistryAccess) provider, new CompoundTag()));
        }
        tag.putFloat("catch_rate", this.catchRate);
        return tag;
    }

    @SuppressWarnings("unused")
    public static RewardHandler deserialize(CompoundTag tag, HolderLookup.Provider provider) {
        ResourceLocation raidSessionId = ResourceLocation.parse(tag.getString("raid_boss"));
        UUID playerUUID = tag.getUUID("player");
        CompoundTag pokemonReward = null;
        if (tag.contains("pokemon_reward")) pokemonReward = tag.getCompound("cached_reward");
        float catchRate = tag.getFloat("catch_rate");
        return new RewardHandler(raidSessionId, playerUUID, pokemonReward, catchRate);
    }

    public <T> CompletableFuture<T> raidSession(Function<RaidSession, T> function, MinecraftServer server) {
        if (this.raidSession != null) return CompletableFuture.completedFuture(function.apply(this.raidSession));

        return RaidRegistry.requestRaidSession(this.raidSessionId).thenApplyAsync(session -> {
            if (session.isEmpty()) return null;
            this.raidSession = session.get();
            return function.apply(session.get());
        }, server);
    }

    public void raidSession(Consumer<RaidSession> consumer, MinecraftServer server) {
        if (this.raidSession != null) {
            consumer.accept(this.raidSession);
            return;
        }

        RaidRegistry.requestRaidSession(this.raidSessionId, session -> {
            this.raidSession = session;
            consumer.accept(session);
        }, server);
    }
}
