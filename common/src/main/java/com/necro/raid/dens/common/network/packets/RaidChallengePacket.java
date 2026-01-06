package com.necro.raid.dens.common.network.packets;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonSeenEvent;
import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.config.TierConfig;
import com.necro.raid.dens.common.events.RaidBattleStartEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.network.ServerPacket;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.raids.RaidBuilder;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.RaidUtils;
import kotlin.Unit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public record RaidChallengePacket(int targetedEntityId, UUID selectedPokemonId, BattleFormat battleFormat) implements CustomPacketPayload, ServerPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_challenge");
    public static final Type<RaidChallengePacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, RaidChallengePacket> CODEC = StreamCodec.ofMember(RaidChallengePacket::write, RaidChallengePacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.targetedEntityId);
        buf.writeUUID(this.selectedPokemonId);
        battleFormat.saveToBuffer((RegistryFriendlyByteBuf) buf);
    }

    public static RaidChallengePacket read(FriendlyByteBuf buf) {
        return new RaidChallengePacket(buf.readInt(), buf.readUUID(), BattleFormat.Companion.loadFromBuffer((RegistryFriendlyByteBuf) buf));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    @Override
    public void handleServer(ServerPlayer player) {
        if (!RaidHelper.isAlreadyParticipating(player) && !RaidHelper.isAlreadyHosting(player) && RaidUtils.isRaidDimension(player.level())) {
            player.sendSystemMessage(Component.translatable("message.cobblemonraiddens.raid.not_participating").withStyle(ChatFormatting.RED));
            return;
        }

        Entity entity = player.level().getEntity(this.targetedEntityId);
        if (!(entity instanceof PokemonEntity pokemonEntity) || pokemonEntity.getOwner() != null) return;
        else if (!((IRaidAccessor) pokemonEntity).isRaidBoss()) return;

        RaidBoss boss = ((IRaidAccessor) entity).getRaidBoss();
        TierConfig tierConfig = CobblemonRaidDens.TIER_CONFIG.get(boss.getTier());

        UUID raidId = ((IRaidAccessor) pokemonEntity).getRaidId();
        RaidInstance raid = RaidHelper.ACTIVE_RAIDS.getOrDefault(raidId, null);
        if (raid != null) {
            if (raid.hasFailed(player)) {
                player.sendSystemMessage(Component.translatable("message.cobblemonraiddens.raid.has_fainted").withStyle(ChatFormatting.RED));
                return;
            }
            else if (raid.getPlayers().size() >= tierConfig.maxPlayers()) {
                player.sendSystemMessage(Component.translatable("message.cobblemonraiddens.raid.lobby_is_full").withStyle(ChatFormatting.RED));
                return;
            }
        }

        Pokemon pokemon = PlayerExtensionsKt.party(player).get(this.selectedPokemonId);
        if (pokemon == null) return;
        else if (RaidUtils.isPokemonBlacklisted(pokemon)) {
            player.sendSystemMessage(Component.translatable("message.cobblemonraiddens.raid.forbidden_pokemon", pokemon.getSpecies().getTranslatedName()).withStyle(ChatFormatting.RED));
            return;
        }
        else if (RaidUtils.isAbilityBlacklisted(pokemon.getAbility())) {
            player.sendSystemMessage(Component.translatable("message.cobblemonraiddens.raid.forbidden_ability", Component.translatable(pokemon.getAbility().getDisplayName())).withStyle(ChatFormatting.RED));
            return;
        }
        else if (pokemon.isFainted()) {
            player.sendSystemMessage(Component.translatable("message.cobblemonraiddens.raid.fainted_lead", pokemon.getSpecies().getTranslatedName()).withStyle(ChatFormatting.RED));
            return;
        }

        UUID leadingPokemon = pokemon.getUuid();

        if (PlayerExtensionsKt.canInteractWith(player, pokemonEntity, Cobblemon.config.getBattleWildMaxDistance() * 4.0f) && pokemonEntity.canBattle(player)) {
            RaidBuilder.build(player, pokemonEntity, leadingPokemon, boss, tierConfig)
                .ifSuccessful(battle -> {
                    this.flagAsSeen(battle, pokemonEntity);
                    UUID raidId2 = raidId;
                    if (!RaidHelper.ACTIVE_RAIDS.containsKey(raidId2)) {
                        ((IRaidAccessor) pokemonEntity).setRaidId(battle.getBattleId());
                        raidId2 = ((IRaidAccessor) pokemonEntity).getRaidId();
                        RaidHelper.ACTIVE_RAIDS.put(raidId2, new RaidInstance(pokemonEntity));
                    }
                    RaidInstance raidInstance = RaidHelper.ACTIVE_RAIDS.get(raidId2);
                    raidInstance.addPlayer(battle);
                    RaidEvents.RAID_BATTLE_START.emit(new RaidBattleStartEvent(player, boss, battle));
                    return Unit.INSTANCE;
                })
                .ifErrored(errors -> {
                    errors.sendTo(player, component -> component.withStyle(ChatFormatting.RED));
                    return Unit.INSTANCE;
                });
        }
    }

    private void flagAsSeen(PokemonBattle battle, PokemonEntity entity) {
        PokemonBattleActor actor = (PokemonBattleActor) battle.getActor(entity.getPokemon().getUuid());
        if (actor == null) return;
        Optional<BattlePokemon> battlePokemon = actor.getPokemonList().stream().filter(bp -> bp.getUuid() == entity.getPokemon().getUuid()).findFirst();
        if (battlePokemon.isEmpty()) return;

        battle.getPlayerUUIDs().forEach(uuid ->CobblemonEvents.POKEMON_SEEN.post(
            new PokemonSeenEvent[]{new PokemonSeenEvent(uuid, battlePokemon.get().getEffectedPokemon())},
            (T) -> Unit.INSTANCE
        ));
    }
}
