package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.net.messages.client.battle.BattleApplyPassResponsePacket;
import com.cobblemon.mod.common.net.messages.client.battle.BattleHealthChangePacket;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.dimension.RaidRegion;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidFeature;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.events.ModifyCatchRateEvent;
import com.necro.raid.dens.common.events.RaidEndEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.battle.RaidBattleState;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.raids.helpers.RaidJoinHelper;
import com.necro.raid.dens.common.raids.helpers.RaidRegionHelper;
import com.necro.raid.dens.common.raids.rewards.RewardDistributionContext;
import com.necro.raid.dens.common.raids.scripts.RaidTriggerType;
import com.necro.raid.dens.common.raids.scripts.triggers.RaidTrigger;
import com.necro.raid.dens.common.raids.scripts.triggers.TimerTrigger;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import com.necro.raid.dens.common.showdown.bagitems.CheerBagItem;
import com.necro.raid.dens.common.showdown.events.*;
import com.necro.raid.dens.common.util.ComponentUtils;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import kotlin.Pair;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RaidInstance {
    private final PokemonEntity bossEntity;
    private final @Nullable UUID host;
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
    private final Map<RaidTriggerType, List<RaidTrigger<?>>> triggers;
    private final List<RaidTrigger<?>> triggerAddQueue;

    private int time;
    private int maxTime;

    private final Map<UUID, RaidPlayer> playerMap;
    private final List<DelayedRunnable> runQueue;

    private RaidState raidState;
    private final RaidBattleState battleState;
    private final boolean isInDen;

    private boolean failedToStart;

    public RaidInstance(PokemonEntity entity, @Nullable UUID host, boolean isInDen) {
        this.failedToStart = false;
        this.bossEntity = entity;
        this.host = host;
        this.raid = ((IRaidAccessor) entity).crd_getRaidId();
        this.raidBoss = ((IRaidAccessor) entity).crd_getRaidBoss();
        this.bossEvent = new ServerBossEvent(
            this.bossBarText(entity, raidBoss),
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

        this.time = 0;
        this.maxTime = 0;

        this.playerMap = new HashMap<>();
        this.runQueue = new ArrayList<>();
        this.schedule(this::checkFlee, 20, true);
        this.schedule(this::updateHealthBars, 80, true);

        this.raidState = RaidState.NOT_STARTED;
        this.battleState = new RaidBattleState();
        this.isInDen = isInDen;

        this.triggers = new EnumMap<>(RaidTriggerType.class);
        this.triggerAddQueue = new ArrayList<>();

        raidBoss.getScript().forEach((key, scripts) -> {
            List<AbstractEvent> functions;
            try {
                functions = scripts.stream()
                    .map(CustomRaidRegistries.SCRIPT_REGISTRY::decode)
                    .filter(Objects::nonNull)
                    .toList();
            }
            catch (Exception e) {
                CobblemonRaidDens.LOGGER.error("Failed to parse script {} for raid boss {}: ", scripts, this.raidBoss.getId(), e);
                this.failedToStart = true;
                return;
            }
            if (functions.isEmpty()) return;

            try {
                RaidTrigger<?> trigger = RaidTriggerType.decode(key, functions);
                if (trigger == null) return;
                this.triggers.computeIfAbsent(trigger.type(), type -> new ArrayList<>()).add(trigger);
            }
            catch (Exception e) {
                CobblemonRaidDens.LOGGER.error("Failed to parse trigger {} for raid boss {}: ", key, this.raidBoss.getId(), e);
                this.failedToStart = true;
            }
        });
        if (this.failedToStart) {
            this.closeRaid(this.bossEntity.getServer(), true);
            return;
        }

        this.triggers.computeIfAbsent(RaidTriggerType.TIMER, type -> new ArrayList<>()).forEach(t -> {
            TimerTrigger trigger = (TimerTrigger) t;
            this.schedule(() -> trigger.trigger(this, null), trigger.after() * 20, trigger.repeat());
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

        this.damageTracker.put(player.getUUID(), 0F);
        if (!this.activePlayers.isEmpty() && this.raidBoss.getMultiplayerHealthMulti() > 1.0F) {
            this.applyHealthMulti();
        }

        this.playerMap.put(player.getUUID(), new RaidPlayer(this.raidBoss));
        this.activePlayers.add(player);
    }

    public boolean isPlayerIn(ServerPlayer player) {
        return this.activePlayers.contains(player);
    }

    public void addBattle(PokemonBattle battle) {
        ((IRaidBattle) battle).crd_setRaidBattle(this);
        this.sendHealthPacket(battle);

        if (!this.battles.isEmpty() && this.raidBoss.getMultiplayerHealthMulti() > 1.0F) {
            this.playerJoin(battle.getPlayers().getFirst().getName().getString());
        }

        this.battles.add(battle);
        List<RaidFeature> effects = new ArrayList<>();
        if (this.raidBoss.getForceDynamax()) effects.add(RaidFeature.DYNAMAX);
        if (this.raidBoss.isTera()) effects.add(RaidFeature.TERA);
        this.sendEvent(new StartRaidShowdownEvent(this.battleState, effects), battle);
        this.sendEvent(new ShowdownEvents.DoNothingShowdownEvent(), battle);
        this.runScripts(RaidTriggerType.TURN, battle, () -> 0);
        this.runScripts(RaidTriggerType.JOIN, battle, this.activePlayers::size);
        if (this.raidState == RaidState.NOT_STARTED) this.raidState = RaidState.IN_PROGRESS;

        List<Integer> entityIds = this.getEntityIds(this.battles);
        this.activePlayers.forEach(player -> RaidDenNetworkMessages.RAID_HEALTH_BAR.accept(player, entityIds, true));
    }

    private List<Integer> getEntityIds(List<PokemonBattle> battles) {
        List<Integer> entityIds = new ArrayList<>();
        for (PokemonBattle battle : battles) {
            BattlePokemon battlePokemon = battle.getSide1().getActivePokemon().getFirst().getBattlePokemon();
            if (battlePokemon == null) continue;
            PokemonEntity leadingPokemon = battlePokemon.getEntity();
            if (leadingPokemon == null) continue;
            entityIds.add(leadingPokemon.getId());
        }
        return entityIds;
    }

    private void applyHealthMulti() {
        float bonusHealth = this.initMaxHealth * (this.raidBoss.getMultiplayerHealthMulti() - 1F) * this.activePlayers.size();
        float currentRatio = this.currentHealth / this.maxHealth;
        this.maxHealth = this.initMaxHealth + bonusHealth;
        this.currentHealth = this.maxHealth * currentRatio;
    }

    private void playerJoin(String newPlayer) {
        this.sendEvent(new ShowdownEvents.PlayerJoinShowdownEvent(newPlayer), null);
    }

    public void removePlayer(ServerPlayer player, @Nullable PokemonBattle battle, boolean ignoreLives) {
        if (battle != null) {
            this.battles.remove(battle);
            ((IRaidBattle) battle).crd_setRaidBattle(null);
        }

        if (this.raidState != RaidState.NOT_STARTED) {
            if (ignoreLives || this.loseLife(player.getUUID())) this.failedPlayers.add(player.getUUID());
            if (this.failedPlayers.size() >= this.playerMap.size()) this.stopRaid(false);
            if (!this.isFinished()) this.runScripts(RaidTriggerType.FAINT, battle, () -> (Void) null);
        }
    }

    public void removePlayer(PokemonBattle battle, boolean ignoreLives) {
        List<ServerPlayer> players = battle.getPlayers();
        if (players.isEmpty()) return;
        this.removePlayer(players.getFirst() , battle, ignoreLives);
    }

    public void removePlayer(ServerPlayer player) {
        PokemonBattle battle = BattleRegistry.getBattleByParticipatingPlayer(player);
        this.removePlayer(player, battle, true);
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
        this.damageTracker.computeIfPresent(player.getUUID(), (uuid, totalDamage) -> totalDamage + (Math.max(damage, 0F) / this.maxHealth));
        this.runScripts(RaidTriggerType.DAMAGE, battle, () -> new Pair<>(this.getDamage(player), player.getUUID()));

        this.currentHealth = Math.clamp(this.currentHealth - damage, 0F, this.maxHealth);
        this.battles.forEach(this::sendHealthPacket);

        this.runScripts(RaidTriggerType.HP, battle, () -> this.currentHealth / this.maxHealth);

        if (this.currentHealth <= 0F) {
            this.bossEvent.setProgress(this.currentHealth / this.maxHealth);
            this.queueStopRaid();
        }
        else {
            this.schedule(() -> this.bossEvent.setProgress(this.currentHealth / this.maxHealth), 20, false);
        }
    }

    private void checkFlee() {
        if (this.bossEntity.isDeadOrDying()) return;
        List<PokemonBattle> battleCache = new ArrayList<>(this.battles);
        for (PokemonBattle battle : battleCache) {
            if (!battle.getEnded()) battle.checkFlee();
        }
    }

    private void updateHealthBars() {
        List<Integer> entityIds = new ArrayList<>();
        List<Float> health = new ArrayList<>();
        for (PokemonBattle battle : this.battles) {
            BattlePokemon battlePokemon = battle.getSide1().getActivePokemon().getFirst().getBattlePokemon();
            if (battlePokemon == null || battlePokemon.getEntity() == null) continue;
            entityIds.add(battlePokemon.getEntity().getId());
            float currentHealth = battlePokemon.getEffectedPokemon().getCurrentHealth();
            float maxHealth = battlePokemon.getEffectedPokemon().getMaxHealth();
            health.add(currentHealth / maxHealth);
        }
        this.activePlayers.forEach(player -> RaidDenNetworkMessages.RAID_HEALTH_UPDATE.accept(player, entityIds, health));
    }

    private void sendHealthPacket(PokemonBattle battle) {
        String pnx = battle.getSide2().getActivePokemon().getFirst().getPNX();
        BattleActor actor = battle.getActorAndActiveSlotFromPNX(pnx).getFirst();
        battle.sendSidedUpdate(
            actor,
            new BattleHealthChangePacket(pnx, this.getCurrentHealth(), null),
            new BattleHealthChangePacket(pnx, this.getCurrentHealth() / this.getMaxHealth(), null),
            false
        );
    }

    public List<ServerPlayer> getPlayers() {
        return this.activePlayers;
    }

    public List<PokemonBattle> getBattles() {
        return this.battles;
    }

    public float getCurrentHealth() {
        return this.currentHealth;
    }

    public float getMaxHealth() {
        return this.maxHealth;
    }

    public boolean hasFailed(ServerPlayer player) {
        return this.failedPlayers.contains(player.getUUID());
    }

    public float getDamage(ServerPlayer player) {
        return this.damageTracker.getOrDefault(player.getUUID(), 0F);
    }

    public boolean failedToStart() {
        return this.failedToStart;
    }

    public void tick() {
        if (this.isFinished()) return;
        try { this.runQueue.removeIf(DelayedRunnable::tick); }
        catch (ConcurrentModificationException ignored) {}
        this.actuallyAddTriggers();
    }

    public void queueStopRaid() {
        this.queueStopRaid(true);
    }

    public void queueStopRaid(boolean raidSuccess) {
        this.schedule(() -> this.stopRaid(raidSuccess), 60, false);
    }

    public void stopRaid(boolean raidSuccess) {
        this.bossEvent.setVisible(false);
        this.bossEvent.removeAllPlayers();
        this.timerEvent.setVisible(false);
        this.timerEvent.removeAllPlayers();

        List<Integer> entityIds = this.getEntityIds(this.battles);
        this.activePlayers.forEach(player -> RaidDenNetworkMessages.RAID_HEALTH_BAR.accept(player, entityIds, false));

        if (raidSuccess) this.bossEntity.setHealth(0F);
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
        if (this.raidBoss.getRequiredDamage() > 0F) {
            Iterator<ServerPlayer> iter = players.iterator();
            while (iter.hasNext()) {
                ServerPlayer p = iter.next();
                if (this.getDamage(p) < this.raidBoss.getRequiredDamage()) {
                    noItems.add(p);
                    iter.remove();
                }
            }
        }

        if (catches == 0) {
            success = List.of();
            failed = players;
        }
        else {
            RewardDistributionContext context = new RewardDistributionContext(players, this);
            success = context.success();
            failed = context.fail();
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

        success.forEach(player -> {
            Pokemon reward = cachedReward == null ? this.raidBoss.getRewardPokemon(player) : cachedReward.clone(true, null);
            float catchRate = this.playerMap.getOrDefault(player.getUUID(), new RaidPlayer()).catchRate();
            ModifyCatchRateEvent event = new ModifyCatchRateEvent(player, reward, catchRate);
            RaidEvents.MODIFY_CATCH_RATE.emit(event);
            RaidEvents.RAID_END.emit(new RaidEndEvent(player, this.raidBoss, reward, event.catchRate(), true));
        });
        failed.forEach(player ->
            RaidEvents.RAID_END.emit(new RaidEndEvent(player, this.raidBoss, null, 0F, true))
        );
        noItems.forEach(player -> player.displayClientMessage(Component.translatable("message.cobblemonraiddens.raid.not_enough_damage"), true));
    }

    private void handleFailed() {
        this.raidState = RaidState.FAILED;
        ((IRaidAccessor) this.bossEntity).crd_setRaidState(RaidState.FAILED);

        Component component = ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.raid_fail");
        this.activePlayers.forEach(player -> {
            player.displayClientMessage(component, true);
            RaidEvents.RAID_END.emit(new RaidEndEvent(player, this.raidBoss, this.bossEntity.getPokemon(), 0F, false));
        });
    }

    public @Nullable UUID getHost() {
        return this.host;
    }

    public PokemonEntity getBossEntity() {
        return this.bossEntity;
    }

    public RaidBoss getRaidBoss() {
        return this.raidBoss;
    }

    public void addToBossEvent(ServerPlayer player) {
        this.schedule(() -> {
            this.bossEvent.addPlayer(player);
            this.timerEvent.addPlayer(player);
        }, 2, false);
    }

    public void removeFromBossEvent(ServerPlayer player) {
        if (!this.bossEvent.getPlayers().contains(player)) return;
        this.bossEvent.removePlayer(player);
        this.timerEvent.removePlayer(player);
    }

    public RaidState getRaidState() {
        return this.raidState;
    }

    public boolean isFinished() {
        return this.raidState == RaidState.SUCCESS || this.raidState == RaidState.FAILED || this.raidState == RaidState.CANCELLED;
    }

    private List<RaidTrigger<?>> getTriggers(RaidTriggerType type) {
        return this.triggers.computeIfAbsent(type, t -> new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public <T> void runScripts(RaidTriggerType type, @Nullable PokemonBattle battle, Supplier<T> predicate) {
        this.getTriggers(type).removeIf(trigger -> ((RaidTrigger<T>) trigger).trigger(this, battle, predicate.get()));
    }

    public boolean runCheer(ServerPlayer player, PokemonBattle oBattle, CheerBagItem bagItem, String origin) {
        RaidPlayer raidPlayer = this.playerMap.getOrDefault(player.getUUID(), new RaidPlayer());
        if (!raidPlayer.useCheer()) return false;

        this.cheer(oBattle, bagItem, origin, false);

        Consumer<PokemonBattle> cheer = switch (bagItem.cheerType()) {
            case CheerBagItem.CheerType.ATTACK -> battle -> new ShowdownEvents.CheerAttackShowdownEvent(origin).send(battle);
            case CheerBagItem.CheerType.DEFENSE -> battle -> new ShowdownEvents.CheerDefenseShowdownEvent(origin).send(battle);
            case CheerBagItem.CheerType.HEAL -> battle -> new ShowdownEvents.CheerHealShowdownEvent(origin).send(battle);
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
            entity.playSound(CobblemonSounds.MEDICINE_HERB_USE, 1F, 1F);
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

    public void sendEvent(AbstractEvent event, @Nullable PokemonBattle battle) {
        if (this.isFinished()) return;
        RaidContext context = new RaidContext(this, battle);
        event.execute(context);
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

        this.bossEvent.removeAllPlayers();
        this.timerEvent.removeAllPlayers();

        RaidRegion region = RaidRegionHelper.getRegion(this.raid);
        if (region != null) region.removeRegionTicket(ModDimensions.getRaidDimension(server));

        if (this.isInDen) RaidHelper.closeRaid(this.raid, wasCancelled ? RaidState.CANCELLED : this.raidState, ModDimensions.getRaidDimension(server));
        else RaidHelper.ACTIVE_RAIDS.remove(this.raid);
        if (this.host != null) RaidHelper.removeRequests(this.host);
        RaidJoinHelper.removeParticipants(this.activePlayers);
    }

    private Component bossBarText(PokemonEntity entity, RaidBoss raidBoss) {
        if (raidBoss.getBossBarText() != null) return raidBoss.getBossBarText();
    
        MutableComponent entityName = (MutableComponent) entity.getName();
        return ComponentUtils.getRaidBossDefault(entityName);
    }

    public void initTimer(int time) {
        if (this.maxTime > 0) return;
        this.maxTime = time * 20;
        this.time = this.maxTime;

        this.timerEvent.setVisible(true);
        this.runQueue.add(new TimerRunnable());
    }

    public void reduceTimer(float ratio) {
        this.time -= (int) (this.maxTime * ratio);
    }

    public void healBoss(float ratio) {
        this.currentHealth = Math.min(this.currentHealth + this.getMaxHealth() * ratio, this.getMaxHealth());
        this.battles.forEach(this::sendHealthPacket);
    }

    public void addCatchRate(ServerPlayer player, float mod) {
        RaidPlayer p = this.playerMap.get(player.getUUID());
        if (p != null) p.addCatchRate(mod);
    }

    public void mulCatchRate(ServerPlayer player, float mod) {
        RaidPlayer p = this.playerMap.get(player.getUUID());
        if (p != null) p.mulCatchRate(mod);
    }

    public void schedule(Runnable runnable, int delay, boolean repeat) {
        if (this.isFinished()) return;
        this.runQueue.add(new DelayedRunnable(runnable, delay, repeat));
    }

    public void addTrigger(RaidTrigger<?> trigger) {
        this.triggerAddQueue.add(trigger);
    }

    public void actuallyAddTriggers() {
        this.triggerAddQueue.forEach(trigger -> {
            if (trigger.type() == RaidTriggerType.TIMER && trigger instanceof TimerTrigger timerTrigger) {
                this.schedule(() -> timerTrigger.trigger(this, null), timerTrigger.after() * 20, timerTrigger.repeat());
            }
            else this.triggers.get(trigger.type()).add(trigger);
        });
        this.triggerAddQueue.clear();
    }

    private class DelayedRunnable {
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

        public boolean tick() {
            if (++this.tick < this.delay) return false;
            else if (RaidInstance.this.isFinished()) return false;
            this.runnable.run();
            this.tick = 0;
            return !this.repeat;
        }
    }

    private class TimerRunnable extends DelayedRunnable {
        public TimerRunnable() {
            super(() -> {}, 20, true);
        }

        @Override
        public boolean tick() {
            if (++this.tick < this.delay) return false;
            else if (RaidInstance.this.isFinished()) return false;
            else if (--RaidInstance.this.time <= 0) {
                RaidInstance.this.stopRaid(false);
                return false;
            }
            RaidInstance.this.timerEvent.setProgress((float) RaidInstance.this.time / RaidInstance.this.maxTime);
            return false;
        }
    }

    private static class RaidPlayer {
        private int cheers;
        private int lives;
        private float catchRate;

        private RaidPlayer() {
            this.cheers = 0;
            this.lives = 0;
            this.catchRate = 0.0F;
        }

        private RaidPlayer(RaidBoss boss) {
            this.cheers = boss.getMaxCheers();
            this.lives = boss.getLives();
            this.catchRate = boss.getCatchRate();
        }

        private boolean useCheer() {
            if (this.cheers <= 0) return false;
            this.cheers--;
            return true;
        }

        private boolean loseLife() {
            return --this.lives <= 0;
        }

        private void addCatchRate(float mod) {
            this.catchRate = Mth.clamp(this.catchRate + mod, 0F, 1F);
        }

        private void mulCatchRate(float mod) {
            this.catchRate = Mth.clamp(this.catchRate * mod, 0F, 1F);
        }

        private float catchRate() {
            return this.catchRate;
        }
    }
}
