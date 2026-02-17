package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BagItemActionResponse;
import com.cobblemon.mod.common.battles.PassActionResponse;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
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
import com.necro.raid.dens.common.util.ComponentUtils;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
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
    private final ServerBossEvent timerEvent;
    private final List<PokemonBattle> battles;
    private final Map<UUID, Float> damageTracker;
    private final List<ServerPlayer> activePlayers;
    private final Set<UUID> failedPlayers;

    private float currentHealth;
    private float maxHealth;
    private final float initMaxHealth;
    private int sharedLives;
    private final Map<Integer, List<ShowdownEvent>> scriptByTurn;
    private final NavigableMap<Double, List<ShowdownEvent>> scriptByHp;

    private final Map<UUID, RaidPlayer> playerMap;
    private final List<DelayedRunnable> runQueue;

    private RaidState raidState;
    private final RaidBattleState battleState;
    private final boolean isInDen;

    public RaidInstance(PokemonEntity entity, UUID host, boolean isInDen) {
        this.bossEntity = entity;
        this.host = host;
        this.raid = ((IRaidAccessor) entity).crd_getRaidId();
        this.raidBoss = ((IRaidAccessor) entity).crd_getRaidBoss();
        this.bossEvent = new ServerBossEvent(
            this.bossBarName(entity, raidBoss),
            BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.NOTCHED_10
        );
        this.timerEvent = new ServerBossEvent(
            Component.empty(),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS
        );
        this.timerEvent.setVisible(false);

        this.battles = new ArrayList<>();
        this.damageTracker = new HashMap<>();

        this.activePlayers = new ArrayList<>();
        this.failedPlayers = new HashSet<>();

        this.maxHealth = this.raidBoss.getHealthMulti() * this.bossEntity.getPokemon().getMaxHealth();
        this.currentHealth = this.maxHealth;
        this.initMaxHealth = this.maxHealth;
        this.sharedLives = this.raidBoss.getLives();

        this.playerMap = new HashMap<>();
        this.runQueue = new ArrayList<>();
        this.runQueue.add(new DelayedRunnable(() -> {
            if (this.bossEntity.isDeadOrDying()) return;
            List<PokemonBattle> battleCache = new ArrayList<>(this.battles);
            for (PokemonBattle battle : battleCache) {
                if (!battle.getEnded()) battle.checkFlee();
            }
        }, 20, true));

        this.raidState = RaidState.NOT_STARTED;
        this.battleState = new RaidBattleState();
        this.isInDen = isInDen;

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
                        new DelayedRunnable(() -> {
                            if (this.isFinished()) return;
                            functions.forEach(event -> this.sendEvent(event, null));
                        }, time, key.startsWith("repeat:"))
                    );
                }
            }
            catch (Exception ignored) {}
        });
    }

    public RaidInstance(PokemonEntity entity, UUID host) {
        this(entity, host, true);
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

        this.playerMap.put(player.getUUID(), new RaidPlayer(this.raidBoss));
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

    public void removePlayer(ServerPlayer player, @Nullable PokemonBattle battle, boolean ignoreLives) {
        this.removeFromBossEvent(player);
        if (this.raidState != RaidState.NOT_STARTED) {
            if (ignoreLives || this.loseLife(player.getUUID())) this.failedPlayers.add(player.getUUID());
            if (this.failedPlayers.size() >= this.playerMap.size()) this.stopRaid(false);
        }

        if (battle == null) return;
        this.battles.remove(battle);
        ((IRaidBattle) battle).crd_setRaidBattle(null);
    }

    public void removePlayer(PokemonBattle battle, boolean ignoreLives) {
        List<ServerPlayer> players = battle.getPlayers();
        if (players.isEmpty()) return;
        this.removePlayer(players.getFirst() , battle, ignoreLives);
    }

    public void removePlayer(ServerPlayer player) {
        this.removePlayer(player, null, true);
    }

    private boolean loseLife(UUID player) {
        if (this.raidBoss.getPlayersShareLives()) return --this.sharedLives <= 0;
        else if (!this.playerMap.containsKey(player)) return true;
        else return this.playerMap.get(player).loseLife();
    }

    @SuppressWarnings("unused")
    public int getLife(UUID player) {
        if (this.raidBoss.getPlayersShareLives()) return this.sharedLives;
        RaidPlayer raidPlayer = this.playerMap.get(player);
        return raidPlayer == null ? 0 : raidPlayer.lives;
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
                if (this.isFinished()) return;
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

    public List<PokemonBattle> getBattles() {
        return this.battles;
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
        if (this.isFinished()) return;
        try { this.runQueue.removeIf(DelayedRunnable::tick); }
        catch (ConcurrentModificationException ignored) {}
    }

    public void queueStopRaid() {
        this.queueStopRaid(true);
    }

    public void queueStopRaid(boolean raidSuccess) {
        this.runQueue.add(new DelayedRunnable(() -> {
            if (this.isFinished()) return;
            this.stopRaid(raidSuccess);
        }, 60));
    }

    public void stopRaid(boolean raidSuccess) {
        this.bossEvent.setVisible(false);
        this.bossEvent.removeAllPlayers();
        this.timerEvent.setVisible(false);
        this.timerEvent.removeAllPlayers();

        if (raidSuccess) this.bossEntity.setHealth(0f);
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
        List<ServerPlayer> noItems = new ArrayList<>();

        List<ServerPlayer> players = new ArrayList<>(this.activePlayers);
        Iterator<ServerPlayer> iter = players.iterator();
        if (this.raidBoss.getRequiredDamage() > 0f) {
            while (iter.hasNext()) {
                ServerPlayer p = iter.next();
                if (this.damageTracker.getOrDefault(p.getUUID(), 0f) / this.maxHealth < this.raidBoss.getRequiredDamage()) {
                    noItems.add(p);
                    iter.remove();
                }
            }
        }

        if (catches == 0) {
            success = List.of();
            failed = players;
        }
        else if (CobblemonRaidDens.CONFIG.reward_distribution == RewardDistribution.SURVIVOR) {
            List<ServerPlayer> survivors = new ArrayList<>();
            failed = new ArrayList<>();
            for (ServerPlayer player : players) {
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
        else if (catches < 0 || players.size() < catches) {
            success = players;
            failed = List.of();
        }
        else {
            this.sortPlayers();
            success = players.subList(0, catches);
            failed = players.subList(catches, players.size());
        }

        Pokemon cachedReward;
        if (CobblemonRaidDens.CONFIG.sync_rewards) {
            cachedReward = this.raidBoss.getRewardPokemon(null);
            cachedReward.setShiny(this.bossEntity.getPokemon().getShiny());
            cachedReward.setGender(this.bossEntity.getPokemon().getGender());
            cachedReward.setNature(this.bossEntity.getPokemon().getNature());
            StringSpeciesFeature radiant = new StringSpeciesFeature("radiant", "radiant");
            if (radiant.matches(this.bossEntity.getPokemon())) radiant.apply(cachedReward);
        } else {
            cachedReward = null;
        }

        success.forEach(player ->
            RaidEvents.RAID_END.emit(new RaidEndEvent(player, this.raidBoss, cachedReward == null ? this.raidBoss.getRewardPokemon(player) : cachedReward.clone(true, null), true))
        );
        failed.forEach(player ->
            RaidEvents.RAID_END.emit(new RaidEndEvent(player, this.raidBoss, null, true))
        );
        noItems.forEach(player -> player.displayClientMessage(Component.translatable("message.cobblemonraiddens.raid.not_enough_damage"), true));
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

        Component component = ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.raid_fail");
        this.activePlayers.forEach(player -> {
            player.displayClientMessage(component, true);
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
        this.runQueue.add(new DelayedRunnable(() -> {
            if (this.isFinished()) return;
            this.bossEvent.addPlayer(player);
            this.timerEvent.addPlayer(player);
        }, 2));
    }

    public void removeFromBossEvent(ServerPlayer player) {
        this.bossEvent.removePlayer(player);
        this.timerEvent.removePlayer(player);
    }

    public RaidState getRaidState() {
        return this.raidState;
    }

    public boolean isFinished() {
        return this.raidState == RaidState.SUCCESS || this.raidState == RaidState.FAILED || this.raidState == RaidState.CANCELLED;
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
        RaidPlayer raidPlayer = this.playerMap.getOrDefault(player.getUUID(), new RaidPlayer());
        if (!raidPlayer.useCheer()) return false;

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

    private void initTimer(int time) {
        this.timerEvent.setVisible(true);
        this.runQueue.add(new TimerRunnable(this, time));
    }

    public void sendEvent(ShowdownEvent event, @Nullable PokemonBattle battle) {
        if (this.isFinished()) return;
        if (event instanceof BroadcastingShowdownEvent broadcast) broadcast.broadcast(this.battles);
        else if (event instanceof TimerRaidEvent(int time)) this.initTimer(time * 20);
        else event.send(battle == null ? this.battles.getFirst() : battle);
    }

    public boolean canSync() {
        return CobblemonRaidDens.CONFIG.max_players_for_support >= this.activePlayers.size();
    }

    public void broadcastToOthers(ShowdownEvent event, @Nullable PokemonBattle battle) {
        if (!this.canSync() || this.isFinished()) return;
        for (PokemonBattle b : this.battles) {
            if (battle != null && b == battle) continue;
            event.send(b);
        }
    }

    public void updateBattleState(PokemonBattle battle, Function<RaidBattleState, Optional<ShowdownEvent>> function) {
        if (!this.canSync() || this.isFinished()) return;
        Optional<ShowdownEvent> optional = function.apply(this.battleState);
        optional.ifPresent(event -> {
            for (PokemonBattle b : this.battles) {
                if (b == battle) continue;
                event.send(b);
            }
        });
    }

    public void updateBattleContext(PokemonBattle battle, Consumer<PokemonBattle> consumer) {
        if (!this.canSync() || this.isFinished()) return;
        for (PokemonBattle b : this.battles) {
            if (b == battle) continue;
            b.dispatch(() -> {
                if (!this.isFinished()) consumer.accept(b);
                return DispatchResultKt.getGO();
            });
        }
    }

    public void closeRaid(MinecraftServer server) {
        this.closeRaid(server, false);
    }

    public void closeRaid(MinecraftServer server, boolean wasCancelled) {
        if (this.raidState == RaidState.SUCCESS && this.isInDen) RaidHelper.clearRaid(this.raid, this.activePlayers);
        if (!this.bossEntity.isRemoved()) {
            ((ServerLevel) this.bossEntity.level()).sendParticles(ParticleTypes.EXPLOSION, this.bossEntity.getX(), this.bossEntity.getY(), this.bossEntity.getZ(), 1, 1.0, 0.0, 0.0, 0.0);
            this.bossEntity.discard();
        }

        RaidRegion region = RaidRegionHelper.getRegion(this.raid);
        if (region != null) region.removeRegionTicket(ModDimensions.getRaidDimension(server));

        if (this.isInDen) RaidHelper.closeRaid(this.raid, wasCancelled ? RaidState.CANCELLED : this.raidState, ModDimensions.getRaidDimension(server));
        else RaidHelper.ACTIVE_RAIDS.remove(this.raid);
        RaidHelper.removeRequests(this.host);
        RaidJoinHelper.removeParticipants(this.activePlayers);
    }

    private Component bossBarName(PokemonEntity entity, RaidBoss raidBoss) {
        MutableComponent entityName = (MutableComponent) entity.getName();
        if (raidBoss.getBossBarName() != null) {
            entityName = (MutableComponent) Component.translatable(raidBoss.getBossBarName(), entity.getName());
        }
        return entityName.withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.WHITE);
    }

    private static class DelayedRunnable {
        private final Runnable runnable;
        final int delay;
        int tick;
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

    private static class TimerRunnable extends DelayedRunnable {
        private final RaidInstance raid;
        private int time;
        private final int maxTime;

        public TimerRunnable(RaidInstance raid, int time) {
            super(() -> {}, 20, true);
            this.raid = raid;
            this.time = time;
            this.maxTime = time;
        }

        @Override
        public boolean tick() {
            if (++this.tick < this.delay) return false;
            else if (this.raid.isFinished()) return false;
            else if (--this.time <= 0) {
                raid.stopRaid(false);
                return false;
            }
            this.raid.timerEvent.setProgress((float) this.time / this.maxTime);
            return false;
        }
    }

    private static class RaidPlayer {
        private int cheers;
        private int lives;

        private RaidPlayer() {
            this.cheers = 0;
            this.lives = 0;
        }

        private RaidPlayer(RaidBoss boss) {
            this.cheers = boss.getMaxCheers();
            this.lives = boss.getLives();
        }

        private boolean useCheer() {
            if (this.cheers <= 0) return false;
            this.cheers--;
            return true;
        }

        private boolean loseLife() {
            return --this.lives <= 0;
        }
    }
}
