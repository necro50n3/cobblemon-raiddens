package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BagItemActionResponse;
import com.cobblemon.mod.common.battles.PassActionResponse;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.net.messages.client.battle.BattleApplyPassResponsePacket;
import com.cobblemon.mod.common.net.messages.client.battle.BattleHealthChangePacket;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.dimension.RaidRegion;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.events.RaidEndEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.raids.battle.RaidBattleState;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.raids.helpers.RaidJoinHelper;
import com.necro.raid.dens.common.raids.helpers.RaidRegionHelper;
import com.necro.raid.dens.common.raids.helpers.RaidScriptHelper;
import com.necro.raid.dens.common.showdown.bagitems.CheerBagItem;
import com.necro.raid.dens.common.showdown.events.*;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class RaidInstance {
    private final PokemonEntity bossEntity;
    private final UUID host;
    private final UUID raid;
    private final RaidBoss raidBoss;
    private final ServerBossEvent bossEvent;
    private final List<PokemonBattle> battles;
    private final Map<UUID, Float> damageTracker;
    private final List<ServerPlayer> activePlayers;
    private final Set<UUID> failedPlayers;

    private float currentHealth;
    private float maxHealth;
    private final float initMaxHealth;
    private final Map<Integer, List<ShowdownEvent>> scriptByTurn;
    private final NavigableMap<Double, List<ShowdownEvent>> scriptByHp;

    private final Map<UUID, Integer> cheersLeft;
    private final List<DelayedRunnable> runQueue;

    private RaidState raidState;
    private final RaidBattleState battleState;

    public RaidInstance(PokemonEntity entity, UUID host) {
        this.bossEntity = entity;
        this.host = host;
        this.raid = ((IRaidAccessor) entity).crd_getRaidId();
        this.raidBoss = ((IRaidAccessor) entity).crd_getRaidBoss();
        this.bossEvent = new ServerBossEvent(
            ((MutableComponent) entity.getName()).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.WHITE),
            BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.NOTCHED_10
        );

        this.battles = new ArrayList<>();
        this.damageTracker = new HashMap<>();

        this.activePlayers = new ArrayList<>();
        this.failedPlayers = new HashSet<>();

        this.maxHealth = this.raidBoss.getHealthMulti() * this.bossEntity.getPokemon().getMaxHealth();
        this.currentHealth = this.maxHealth;
        this.initMaxHealth = this.maxHealth;

        this.cheersLeft = new HashMap<>();
        this.runQueue = new ArrayList<>();
        this.runQueue.add(new DelayedRunnable(() -> {
            if (this.bossEntity.isDeadOrDying()) return;
            List<PokemonBattle> battleCache = new ArrayList<>(this.battles);
            for (PokemonBattle battle : battleCache) battle.checkFlee();
        }, 20, true));

        this.raidState = RaidState.NOT_STARTED;
        this.battleState = new RaidBattleState();

        this.scriptByTurn = new HashMap<>();
        this.scriptByHp = new TreeMap<>();
        raidBoss.getScript().forEach((key, scripts) -> {
            List<ShowdownEvent> functions = scripts.functions().stream()
                .map(this::getInstructions)
                .filter(Objects::nonNull)
                .toList();
            if (functions.isEmpty()) return;

            try {
                if (key.startsWith("turn:")) {
                    this.scriptByTurn.put(Integer.parseInt(key.split(":")[1]), functions);
                }
                else if (key.startsWith("hp:")) {
                    double threshold = Double.parseDouble(key.split(":")[1]);
                    if ((this.currentHealth / this.maxHealth) < threshold) return;
                    this.scriptByHp.put(threshold, functions);
                }
                else if (key.startsWith("after:") || key.startsWith("repeat:")) {
                    int time = Integer.parseInt(key.split(":")[1]) * 20;
                    this.runQueue.add(
                        new DelayedRunnable(() -> functions.forEach(event -> this.sendEvent(event, null)),
                            time, key.startsWith("repeat:"))
                    );
                }
            }
            catch (Exception ignored) {}
        });
    }

    public void addPlayerAndBattle(ServerPlayer player, PokemonBattle battle) {
        this.addPlayer(player);
        this.addBattle(battle);
    }

    public void addPlayer(ServerPlayer player) {
        this.addToBossEvent(player);

        this.damageTracker.put(player.getUUID(), 0f);
        if (!this.activePlayers.isEmpty() && this.raidBoss.getMultiplayerHealthMulti() > 1.0f) {
            this.applyHealthMulti();
        }

        this.cheersLeft.put(player.getUUID(), this.raidBoss.getMaxCheers());
        this.activePlayers.add(player);
    }

    public void addBattle(PokemonBattle battle) {
        ((IRaidBattle) battle).crd_setRaidBattle(this);
        this.sendHealthPacket(battle);

        if (!this.battles.isEmpty() && this.raidBoss.getMultiplayerHealthMulti() > 1.0f) {
            this.playerJoin(battle.getPlayers().getFirst().getName().getString());
        }

        this.battles.add(battle);
        new StartRaidShowdownEvent(this.battleState).send(battle);
        this.runScriptByTurn(0, battle);
        if (this.raidState == RaidState.NOT_STARTED) this.raidState = RaidState.IN_PROGRESS;
    }

    private void applyHealthMulti() {
        float bonusHealth = this.initMaxHealth * (this.raidBoss.getMultiplayerHealthMulti() - 1f) * this.activePlayers.size();
        float currentRatio = this.currentHealth / this.maxHealth;
        this.maxHealth = this.initMaxHealth + bonusHealth;
        this.currentHealth = this.maxHealth * currentRatio;
    }

    private void playerJoin(String newPlayer) {
        this.sendEvent(new PlayerJoinShowdownEvent(newPlayer), null);
    }

    public void removePlayer(ServerPlayer player, @Nullable PokemonBattle battle) {
        this.removeFromBossEvent(player);
        if (this.raidState != RaidState.NOT_STARTED) this.failedPlayers.add(player.getUUID());
        if (this.failedPlayers.size() >= this.activePlayers.size()) this.stopRaid(false);

        if (battle == null) return;
        this.battles.remove(battle);
        ((IRaidBattle) battle).crd_setRaidBattle(null);
    }

    public void removePlayer(PokemonBattle battle) {
        this.removePlayer(battle.getPlayers().getFirst(), battle);
    }

    public void removePlayer(ServerPlayer player) {
        this.removePlayer(player, null);
    }

    public void syncHealth(ServerPlayer player, PokemonBattle battle, float damage) {
        if (!this.activePlayers.contains(player) && ((IRaidBattle) battle).crd_isRaidBattle()) this.addPlayerAndBattle(player, battle);
        this.damageTracker.computeIfPresent(player.getUUID(), (uuid, totalDamage) -> totalDamage + damage);

        this.currentHealth = Math.clamp(this.currentHealth - damage, 0f, this.maxHealth);
        this.battles.forEach(this::sendHealthPacket);

        if (this.currentHealth == 0f) {
            this.bossEvent.setProgress(this.currentHealth / this.maxHealth);
            this.queueStopRaid();
        }
        else {
            this.runQueue.add(new DelayedRunnable(() -> {
                this.bossEvent.setProgress(this.currentHealth / this.maxHealth);
                this.runScriptByHp((double) this.currentHealth / this.maxHealth);
            }, 20));
        }
    }

    private void sendHealthPacket(PokemonBattle battle) {
        String pnx = battle.getSide2().getActivePokemon().getFirst().getPNX();
        BattleActor actor = battle.getActorAndActiveSlotFromPNX(pnx).getFirst();
        battle.sendSidedUpdate(
            actor,
            new BattleHealthChangePacket(pnx, this.getRemainingHealth(), null),
            new BattleHealthChangePacket(pnx, this.getRemainingHealth() / this.getMaxHealth(), null),
            false
        );
    }

    public List<ServerPlayer> getPlayers() {
        return this.activePlayers;
    }

    public float getRemainingHealth() {
        return this.currentHealth;
    }

    public float getMaxHealth() {
        return this.maxHealth;
    }

    public boolean hasFailed(ServerPlayer player) {
        return this.failedPlayers.contains(player.getUUID());
    }

    public void tick() {
        this.runQueue.removeIf(DelayedRunnable::tick);
    }

    public void queueStopRaid() {
        this.queueStopRaid(true);
    }

    public void queueStopRaid(boolean raidSuccess) {
        this.runQueue.add(new DelayedRunnable(() -> this.stopRaid(raidSuccess), 60));
    }

    public void stopRaid(boolean raidSuccess) {
        this.bossEvent.setVisible(false);
        this.bossEvent.removeAllPlayers();
        if (raidSuccess) this.bossEntity.setHealth(0f);
        this.battles.forEach(PokemonBattle::stop);
        if (this.raidBoss == null) return;

        if (raidSuccess) this.handleSuccess();
        else this.handleFailed();

        this.closeRaid(this.bossEntity.getServer());
    }

    private void handleSuccess() {
        this.raidState = RaidState.SUCCESS;
        ((IRaidAccessor) this.bossEntity).crd_setRaidState(RaidState.SUCCESS);

        int catches = this.raidBoss.getMaxCatches();
        List<ServerPlayer> success;
        List<ServerPlayer> failed;
        if (catches == 0) {
            success = List.of();
            failed = this.activePlayers;
        }
        else if (CobblemonRaidDens.CONFIG.reward_distribution == RewardDistribution.SURVIVOR) {
            List<ServerPlayer> survivors = new ArrayList<>();
            failed = new ArrayList<>();
            for (ServerPlayer player : this.activePlayers) {
                if (this.failedPlayers.contains(player.getUUID())) failed.add(player);
                else survivors.add(player);
            }

            if (catches > 0 && survivors.size() > catches) {
                Collections.shuffle(survivors);
                success = survivors.subList(0, catches);
                failed.addAll(survivors.subList(catches, survivors.size()));
            }
            else success = survivors;
        }
        else if (catches < 0 || this.activePlayers.size() < catches) {
            success = this.activePlayers;
            failed = List.of();
        }
        else {
            this.sortPlayers();
            success = this.activePlayers.subList(0, catches);
            failed = this.activePlayers.subList(catches, this.activePlayers.size());
        }

        Pokemon cachedReward;
        if (CobblemonRaidDens.CONFIG.sync_rewards) {
            cachedReward = this.raidBoss.getRewardPokemon(null);
            cachedReward.setShiny(this.bossEntity.getPokemon().getShiny());
            cachedReward.setGender(this.bossEntity.getPokemon().getGender());
            cachedReward.setNature(this.bossEntity.getPokemon().getNature());
            StringSpeciesFeature radiant = new StringSpeciesFeature("radiant", "radiant");
            if (radiant.matches(this.bossEntity)) radiant.apply(cachedReward);
        } else {
            cachedReward = null;
        }

        success.forEach(player -> {
            new RewardHandler(this.raidBoss, player, true, cachedReward).sendRewardMessage();
            RaidEvents.RAID_END.emit(new RaidEndEvent(player, this.raidBoss, this.bossEntity.getPokemon(), true));
        });
        failed.forEach(player -> {
            new RewardHandler(this.raidBoss, player, false).sendRewardMessage();
            RaidEvents.RAID_END.emit(new RaidEndEvent(player, this.raidBoss, this.bossEntity.getPokemon(), true));
        });
    }

    private void sortPlayers() {
        if (CobblemonRaidDens.CONFIG.reward_distribution == RewardDistribution.DAMAGE) {
            this.activePlayers.sort((a, b) -> Float.compare(
                this.damageTracker.getOrDefault(b.getUUID(), 0f),
                this.damageTracker.getOrDefault(a.getUUID(), 0f)
            ));
        }
        else {
            Collections.shuffle(this.activePlayers);
        }
    }

    private void handleFailed() {
        this.raidState = RaidState.FAILED;
        ((IRaidAccessor) this.bossEntity).crd_setRaidState(RaidState.FAILED);

        this.activePlayers.forEach(player -> {
            player.sendSystemMessage(Component.translatable("message.cobblemonraiddens.raid.raid_fail"));
            RaidEvents.RAID_END.emit(new RaidEndEvent(player, this.raidBoss, this.bossEntity.getPokemon(), false));
        });
    }

    public PokemonEntity getBossEntity() {
        return this.bossEntity;
    }

    public RaidBoss getRaidBoss() {
        return this.raidBoss;
    }

    public void addToBossEvent(ServerPlayer player) {
        this.bossEvent.addPlayer(player);
    }

    public void removeFromBossEvent(ServerPlayer player) {
        this.bossEvent.removePlayer(player);
    }

    public RaidState getRaidState() {
        return this.raidState;
    }

    private ShowdownEvent getInstructions(@NotNull String function) {
        return RaidScriptHelper.decode(function);
    }

    public void runScriptByTurn(int turn, PokemonBattle battle) {
        List<ShowdownEvent> functions = this.scriptByTurn.remove(turn);
        if (functions == null) return;
        functions.forEach(event -> this.sendEvent(event, battle));
    }

    public void runScriptByHp(double hpRatio) {
        this.scriptByHp.tailMap(hpRatio, true)
            .values()
            .forEach(events -> events.forEach(event -> this.sendEvent(event, null)));

        this.scriptByHp.keySet().removeIf(hp -> hp >= hpRatio);
    }

    public boolean runCheer(ServerPlayer player, PokemonBattle oBattle, CheerBagItem bagItem, String origin) {
        int cheersLeft = this.cheersLeft.getOrDefault(player.getUUID(), 0);
        if (cheersLeft <= 0) return false;
        this.cheersLeft.put(player.getUUID(), --cheersLeft);

        this.cheer(oBattle, bagItem, origin, false);

        Consumer<PokemonBattle> cheer = switch (bagItem.cheerType()) {
            case CheerBagItem.CheerType.ATTACK -> battle -> new CheerAttackShowdownEvent(origin).send(battle);
            case CheerBagItem.CheerType.DEFENSE -> battle -> new CheerDefenseShowdownEvent(origin).send(battle);
            case CheerBagItem.CheerType.HEAL -> battle -> new CheerHealShowdownEvent(origin).send(battle);
        };

        for (PokemonBattle b : this.battles) {
            if (b == oBattle) continue;
            cheer.accept(b);
        }

        return true;
    }

    public void cheer(PokemonBattle battle, BagItem bagItem, String origin, boolean skipEnemyAction) {
        BattleActor side1 = battle.getSide1().getActors()[0];
        BattleActor side2 = battle.getSide2().getActors()[0];
        List<ActiveBattlePokemon> target = side1.getActivePokemon();
        if (side1.getRequest() == null || side2.getRequest() == null || target.isEmpty() || target.getFirst().getBattlePokemon() == null) return;
        if (bagItem instanceof CheerBagItem(CheerBagItem.CheerType cheerType) && cheerType == CheerBagItem.CheerType.HEAL && target.getFirst().getBattlePokemon().getEntity() instanceof PokemonEntity entity) {
            entity.playSound(CobblemonSounds.MEDICINE_HERB_USE, 1f, 1f);
        }
        this.sendAction(side1, side2,new BagItemActionResponse(bagItem, target.getFirst().getBattlePokemon(), origin), skipEnemyAction);
    }

    private void sendAction(BattleActor side1, BattleActor side2, ShowdownActionResponse response, boolean skipEnemyAction) {
        side1.getResponses().add(response);
        side1.setMustChoose(false);
        if (skipEnemyAction) {
            side2.getResponses().addFirst(PassActionResponse.INSTANCE);
            side2.setMustChoose(false);
        }
        side1.getBattle().checkForInputDispatch();
        side1.sendUpdate(new BattleApplyPassResponsePacket());
    }

    public void sendEvent(ShowdownEvent event, @Nullable PokemonBattle battle) {
        if (event instanceof BroadcastingShowdownEvent broadcast) broadcast.broadcast(this.battles);
        else event.send(battle == null ? this.battles.getFirst() : battle);
    }

    public boolean canSync() {
        return CobblemonRaidDens.CONFIG.max_players_for_support >= this.activePlayers.size();
    }

    public void broadcastToOthers(ShowdownEvent event, @Nullable PokemonBattle battle) {
        if (!this.canSync()) return;
        for (PokemonBattle b : this.battles) {
            if (battle != null && b == battle) continue;
            event.send(b);
        }
    }

    public void updateBattleState(PokemonBattle battle, Function<RaidBattleState, Optional<ShowdownEvent>> function) {
        if (!this.canSync()) return;
        Optional<ShowdownEvent> optional = function.apply(this.battleState);
        optional.ifPresent(event -> {
            for (PokemonBattle b : this.battles) {
                if (b == battle) continue;
                event.send(b);
            }
        });
    }

    public void updateBattleContext(PokemonBattle battle, Consumer<PokemonBattle> consumer) {
        if (!this.canSync()) return;
        for (PokemonBattle b : this.battles) {
            if (b == battle) continue;
            consumer.accept(b);
        }
    }

    public void closeRaid(MinecraftServer server) {
        this.closeRaid(server, false);
    }

    public void closeRaid(MinecraftServer server, boolean wasCancelled) {
        if (this.raidState == RaidState.SUCCESS) RaidHelper.clearRaid(this.raid, this.activePlayers);
        if (!this.bossEntity.isRemoved()) this.bossEntity.discard();

        RaidRegion region = RaidRegionHelper.getRegion(this.raid);
        if (region != null) region.removeRegionTicket(ModDimensions.getRaidDimension(server));

        RaidHelper.closeRaid(this.raid, wasCancelled ? RaidState.CANCELLED : this.raidState, ModDimensions.getRaidDimension(server));
        RaidHelper.removeRequests(this.host);
        RaidJoinHelper.removeParticipants(this.activePlayers);
    }

    private static class DelayedRunnable {
        private final Runnable runnable;
        private final int delay;
        private int tick;
        private final boolean repeat;

        public DelayedRunnable(Runnable runnable, int delay, boolean repeat) {
            this.runnable = runnable;
            this.delay = delay;
            this.tick = 0;
            this.repeat = repeat;
        }

        public DelayedRunnable(Runnable runnable, int delay) {
            this(runnable, delay, false);
        }

        public boolean tick() {
            if (++this.tick < this.delay) return false;
            this.runnable.run();
            if (this.repeat) this.tick = 0;
            return !this.repeat;
        }
    }
}
