package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveSet;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.net.messages.client.battle.BattleApplyPassResponsePacket;
import com.cobblemon.mod.common.net.messages.client.battle.BattleHealthChangePacket;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.asymmetric.battles.common.api.AsymmetricBattleFormats;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.raid.RaidBattleFormat;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.events.ModifyCatchRateEvent;
import com.necro.raid.dens.common.events.RaidBattleEndEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.battle.RaidBattleState;
import com.necro.raid.dens.common.raids.rewards.RewardDistributionContext;
import com.necro.raid.dens.common.raids.scripts.RaidTriggerType;
import com.necro.raid.dens.common.raids.scripts.triggers.RaidTrigger;
import com.necro.raid.dens.common.raids.scripts.triggers.TimerTrigger;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import com.necro.raid.dens.common.showdown.bagitems.CheerBagItem;
import com.necro.raid.dens.common.showdown.events.*;
import com.necro.raid.dens.common.util.ComponentUtils;
import com.necro.raid.dens.common.util.IProperties;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import kotlin.Pair;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RaidInstance {
    private final RaidSession session;
    private final PokemonEntity bossEntity;
    private final RaidBoss boss;
    private final boolean isBoss;
    private final boolean canCatch;

    private float currentHealth;
    private float maxHealth;
    private final float initMaxHealth;
    private final Map<RaidTriggerType, List<RaidTrigger<?>>> triggers;
    private final List<RaidTrigger<?>> triggerAddQueue;

    private boolean failedToStart;

    private final ServerBossEvent bossEvent;
    private final ServerBossEvent timerEvent;
    private final List<PokemonBattle> battles;

    private int time;
    private int maxTime;

    private final Map<UUID, RaidPlayer> playerMap;

    private RaidState raidState;
    private final RaidBattleState battleState;

    public RaidInstance(RaidSession session, PokemonEntity entity, boolean isBoss, boolean canCatch) {
        this.session = session;
        this.bossEntity = entity;
        this.isBoss = isBoss;
        this.canCatch = canCatch;
        this.failedToStart = false;

        RaidBoss boss = ((IRaidAccessor) entity).crd_getRaidBoss();
        if (boss == null) boss = CustomRaidRegistries.RAID_REGISTRY.requestRaidBoss(((IRaidAccessor) entity).crd_getRaidBossId()).resultNow().orElseThrow();
        this.boss = boss;

        this.bossEvent = new ServerBossEvent(
            this.bossBarText(entity, this.boss),
            BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.NOTCHED_10
        );
        this.timerEvent = new ServerBossEvent(
            Component.empty(),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS
        );
        this.timerEvent.setVisible(false);

        this.battles = new ArrayList<>();

        this.maxHealth = this.boss.getHealthMulti() * this.bossEntity.getPokemon().getMaxHealth();
        this.currentHealth = this.maxHealth;
        this.initMaxHealth = this.maxHealth;
        this.applyHealthMulti();

        this.time = 0;
        this.maxTime = 0;

        this.playerMap = new HashMap<>();
        this.triggers = new EnumMap<>(RaidTriggerType.class);
        this.triggerAddQueue = new ArrayList<>();

        this.schedule(this::checkFlee, 20, true);
        this.schedule(this::updateHealthBars, 80, true);

        this.raidState = RaidState.NOT_STARTED;
        this.battleState = new RaidBattleState();

        this.boss.getScript().forEach((key, scripts) -> {
            List<AbstractEvent> functions;
            try {
                functions = scripts.stream()
                    .map(CustomRaidRegistries.SCRIPT_REGISTRY::decode)
                    .filter(Objects::nonNull)
                    .toList();
            }
            catch (Exception e) {
                CobblemonRaidDens.LOGGER.error("Failed to parse script {} for raid boss {}: ", scripts, this.boss.getId(), e);
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
                CobblemonRaidDens.LOGGER.error("Failed to parse trigger {} for raid boss {}: ", key, this.boss.getId(), e);
                this.failedToStart = true;
            }
        });
        if (this.failedToStart) {
            this.closeInstance();
            return;
        }

        this.triggers.computeIfAbsent(RaidTriggerType.TIMER, type -> new ArrayList<>()).forEach(t -> {
            TimerTrigger trigger = (TimerTrigger) t;
            this.schedule(() -> trigger.trigger(this, null), trigger.after() * 20, trigger.repeat());
        });
    }

    public void addPlayer(ServerPlayer player) {
        this.addToBossEvent(player);
        if (this.boss.getMultiplayerHealthMulti() > 1.0F) this.applyHealthMulti();
        this.playerMap.put(player.getUUID(), new RaidPlayer(this.boss));
    }

    public void addBattle(ServerPlayer player, PokemonBattle battle) {
        this.sendHealthPacket(battle);

        if (!this.battles.isEmpty() && this.boss.getMultiplayerHealthMulti() > 1.0F) {
            this.playerJoin(player.getName().getString());
        }

        if (this.boss.getBattleFormat().format() != AsymmetricBattleFormats.GEN_9_RAID) {
            ((IRaidBattle) battle).crd_setRaidSession(this.session);
            this.battles.add(battle);
            this.sendEvent(new StartRaidShowdownEvent(
                this.battleState,
                this.boss.getForceDynamax(),
                this.boss.getFeature().equalsIgnoreCase("tera")
            ), battle);
            this.sendEvent(new ShowdownEvents.DoNothingShowdownEvent(), battle);
        }

        this.runScripts(RaidTriggerType.JOIN, battle, this.battles::size);
        if (this.raidState == RaidState.NOT_STARTED) this.raidState = RaidState.IN_PROGRESS;

        List<Integer> entityIds = this.getEntityIds(this.battles);
        this.session.activePlayers().forEach(p -> RaidDenNetworkMessages.RAID_HEALTH_BAR.accept(p, entityIds, true));
    }

    private void applyHealthMulti() {
        float bonusHealth = this.initMaxHealth * (this.boss.getMultiplayerHealthMulti() - 1F) * this.session.activePlayers().size();
        float currentRatio = this.currentHealth / this.maxHealth;
        this.maxHealth = this.initMaxHealth + bonusHealth;
        this.currentHealth = this.maxHealth * currentRatio;
    }

    public RaidSession session() {
        return this.session;
    }

    public RaidBoss boss() {
        return this.boss;
    }

    private void playerJoin(String newPlayer) {
        this.sendEvent(new ShowdownEvents.PlayerJoinShowdownEvent(newPlayer), null);
    }

    public void removePlayer(ServerPlayer player, @Nullable PokemonBattle battle, boolean ignoreLives) {
        if (battle != null) {
            this.battles.remove(battle);
            ((IRaidBattle) battle).crd_setRaidSession(null);
        }

        if (this.raidState != RaidState.NOT_STARTED) {
            if (ignoreLives || this.session.loseLife(player.getUUID())) this.session.failedPlayers().add(player.getUUID());
            if (this.session.failedPlayers().size() >= this.playerMap.size()) this.stopInstance(false);
            else if (this.session.activePlayers().stream().allMatch(p -> p.level() != this.bossEntity.level())) this.stopInstance(false);
            if (!this.isFinished()) this.runScripts(RaidTriggerType.FAINT, battle);
        }
    }

    public boolean playersShareLives() {
        return this.boss != null && this.boss.getPlayersShareLives();
    }

    public void syncHealth(ServerPlayer player, PokemonBattle battle, float damage) {
        if (!this.session.activePlayers().contains(player) && ((IRaidBattle) battle).crd_isRaid()) this.session.addPlayerAndBattle(player, battle);
        this.session().getDamageRecord(player).addDamage(damage);
        this.runScripts(RaidTriggerType.DAMAGE, battle, () -> new Pair<>(this.session().getDamage(player), player.getUUID()));

        this.currentHealth = Math.clamp(this.currentHealth - damage, 0F, this.maxHealth);
        this.battles.forEach(this::sendHealthPacket);

        this.runScripts(RaidTriggerType.HP, battle, () -> this.currentHealth / this.maxHealth);

        if (this.currentHealth <= 0F) {
            this.bossEvent.setProgress(0F);
            this.queueStopInstance();
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
        this.session.activePlayers().forEach(player -> RaidDenNetworkMessages.RAID_HEALTH_UPDATE.accept(player, entityIds, health));
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

    private void sendHealthPacket(PokemonBattle battle) {
        Set<String> players = new HashSet<>();
        battle.getSide2().getActivePokemon().forEach(pokemon -> players.add(pokemon.getPNX()));
        players.forEach(pnx ->
            battle.sendSidedUpdate(
                battle.getActorAndActiveSlotFromPNX(pnx).getFirst(),
                new BattleHealthChangePacket(pnx, this.getCurrentHealth(), null),
                new BattleHealthChangePacket(pnx, this.getCurrentHealth() / this.getMaxHealth(), null),
                false
            )
        );
    }

    public float getCurrentHealth() {
        return this.currentHealth;
    }

    public float getMaxHealth() {
        return this.maxHealth;
    }

    public boolean failedToStart() {
        return this.failedToStart;
    }

    public void tick() {
        this.actuallyAddTriggers();
    }

    public void schedule(Runnable runnable, int delay, boolean repeat) {
        if (this.isFinished()) return;
        this.session.schedule(runnable, delay, repeat);
    }

    public void queueStopInstance() {
        this.queueStopInstance(true);
    }

    public void queueStopInstance(boolean raidSuccess) {
        this.schedule(() -> this.stopInstance(raidSuccess), 60, false);
    }

    public void stopInstance(boolean raidSuccess) {
        this.bossEvent.setVisible(false);
        this.bossEvent.removeAllPlayers();
        this.timerEvent.setVisible(false);
        this.timerEvent.removeAllPlayers();
        this.session().addTotalHealth(this.getMaxHealth());

        List<Integer> entityIds = this.getEntityIds(this.battles);
        this.session.activePlayers().forEach(player -> RaidDenNetworkMessages.RAID_HEALTH_BAR.accept(player, entityIds, false));

        if (raidSuccess) this.bossEntity.setHealth(0F);
        if (this.boss == null) return;

        if (raidSuccess) this.handleSuccess();
        else this.handleFailed();
    }

    public void closeInstance() {
        if (!this.bossEntity.isRemoved()) {
            ((ServerLevel) this.bossEntity.level()).sendParticles(ParticleTypes.EXPLOSION, this.bossEntity.getX(), this.bossEntity.getY(), this.bossEntity.getZ(), 1, 1.0, 0.0, 0.0, 0.0);
            this.bossEntity.discard();
        }

        if (this.bossEvent != null) this.bossEvent.removeAllPlayers();
        if (this.timerEvent != null) this.timerEvent.removeAllPlayers();
    }

    private void handleSuccess() {
        this.raidState = RaidState.SUCCESS;
        ((IRaidAccessor) this.bossEntity).crd_setRaidState(RaidState.SUCCESS);
        if (!this.canCatch) return;

        int catches = this.boss.getMaxCatches();
        List<ServerPlayer> success;
        List<ServerPlayer> failed;
        List<ServerPlayer> noItems = new ArrayList<>();

        List<ServerPlayer> players = new ArrayList<>(this.session().activePlayers());
        if (this.boss.getRequiredDamage() > 0F) {
            Iterator<ServerPlayer> iter = players.iterator();
            while (iter.hasNext()) {
                ServerPlayer p = iter.next();
                if (this.session().getDamage(p) < this.boss.getRequiredDamage()) {
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
            RewardDistributionContext context = new RewardDistributionContext(players, this.session());
            success = context.success();
            failed = context.fail();
        }

        Pokemon cachedReward;
        if (CobblemonRaidDens.CONFIG.sync_rewards) {
            cachedReward = this.boss.getRewardPokemon(null);
            PokemonProperties bossProperties = this.boss.getBoss();

            cachedReward.setShiny(this.bossEntity.getPokemon().getShiny());
            cachedReward.setGender((bossProperties.getGender() == null ? this.bossEntity.getPokemon() : cachedReward).getGender());
            cachedReward.setNature((bossProperties.getNature() == null ? this.bossEntity.getPokemon() : cachedReward).getNature());

            if (bossProperties.getMoves() == null && ((IProperties) bossProperties).crd_getMovesetBuilder() == null) {
                List<Move> bossMoves = this.bossEntity.getPokemon().getMoveSet().getMoves();
                MoveSet moveSet = cachedReward.getMoveSet();
                for (int i = 0; i < bossMoves.size(); i++) {
                    Move move = bossMoves.get(i).copy();
                    move.setCurrentPp(move.getMaxPp());
                    move.update();
                    moveSet.setMove(i, move);
                    moveSet.update();
                }
            }

            StringSpeciesFeature radiant = new StringSpeciesFeature("radiant", "radiant");
            if (radiant.matches(this.bossEntity.getPokemon())) radiant.apply(cachedReward);
        } else {
            cachedReward = null;
        }

        success.forEach(player -> {
            Pokemon reward = cachedReward == null ? this.boss.getRewardPokemon(player) : cachedReward.clone(true, null);
            float catchRate = this.playerMap.getOrDefault(player.getUUID(), new RaidPlayer()).catchRate();
            ModifyCatchRateEvent event = new ModifyCatchRateEvent(player, reward, catchRate);
            RaidEvents.MODIFY_CATCH_RATE.emit(event);
            RaidEvents.RAID_BATTLE_END.emit(new RaidBattleEndEvent(player, this.boss, reward, this.isBoss, event.catchRate(), true));
        });
        failed.forEach(player ->
            RaidEvents.RAID_BATTLE_END.emit(new RaidBattleEndEvent(player, this.boss, null, this.isBoss, 0F, true))
        );
        noItems.forEach(player -> player.displayClientMessage(Component.translatable("message.cobblemonraiddens.raid.not_enough_damage"), true));
    }

    private void handleFailed() {
        this.raidState = RaidState.FAILED;
        ((IRaidAccessor) this.bossEntity).crd_setRaidState(RaidState.FAILED);

        Component component = ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.raid_fail");
        this.session().activePlayers().forEach(player -> {
            player.displayClientMessage(component, true);
            RaidEvents.RAID_BATTLE_END.emit(new RaidBattleEndEvent(player, this.boss, this.bossEntity.getPokemon(), false, 0F, false));
        });
    }

    public PokemonEntity getBossEntity() {
        return this.bossEntity;
    }

    private List<RaidTrigger<?>> getTriggers(RaidTriggerType type) {
        return this.triggers.computeIfAbsent(type, t -> new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public <T> void runScripts(RaidTriggerType type, @Nullable PokemonBattle battle, @NotNull Supplier<T> predicate) {
        this.getTriggers(type).removeIf(trigger -> ((RaidTrigger<T>) trigger).trigger(this, battle, predicate.get()));
    }

    public void runScripts(RaidTriggerType type, @Nullable PokemonBattle battle) {
        this.runScripts(type, battle, () -> null);
    }

    public void sendEvent(AbstractEvent event, @Nullable PokemonBattle battle) {
        if (this.isFinished()) return;
        RaidContext context = new RaidContext(this, battle);
        event.execute(context);
    }

    private Component bossBarText(PokemonEntity entity, RaidBoss boss) {
        if (boss.getBossBarText() != null) return boss.getBossBarText();

        MutableComponent entityName = (MutableComponent) entity.getName();
        return ComponentUtils.getRaidBossDefault(entityName);
    }

    public void healBoss(float ratio) {
        this.currentHealth = Math.min(this.currentHealth + this.getMaxHealth() * ratio, this.getMaxHealth());
        this.battles.forEach(this::sendHealthPacket);
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

    public void addToBossEvent(ServerPlayer player) {
        this.schedule(() -> {
            if (this.bossEvent != null) this.bossEvent.addPlayer(player);
            if (this.timerEvent != null) this.timerEvent.addPlayer(player);
        }, 2, false);
    }

    public void removeFromBossEvent(ServerPlayer player) {
        if (this.bossEvent != null && this.bossEvent.getPlayers().contains(player)) {
            this.bossEvent.removePlayer(player);
        }
        if (this.timerEvent != null) {
            this.timerEvent.removePlayer(player);
        }
    }

    public RaidState getRaidState() {
        return this.raidState;
    }

    public RaidBattleState getBattleState() {
        return this.battleState;
    }

    public boolean isFinished() {
        return this.raidState == RaidState.SUCCESS || this.raidState == RaidState.FAILED || this.raidState == RaidState.CANCELLED;
    }

    public RaidInstance getActive() {
        return this;
    }

    public List<PokemonBattle> getBattles() {
        return this.battles;
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
        this.sendAction(side1, side2, new BagItemActionResponse(bagItem, target.getFirst().getBattlePokemon(), origin), skipEnemyAction);
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

    public boolean canSync() {
        return this.boss.getBattleFormat() != RaidBattleFormat.RAID && CobblemonRaidDens.CONFIG.max_players_for_support >= this.session.activePlayers().size();
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

    public void initTimer(int time) {
        if (this.maxTime > 0) return;
        this.maxTime = time * 20;
        this.time = this.maxTime;

        this.timerEvent.setVisible(true);
        this.session.schedule(new TimerRunnable());
    }

    public void reduceTimer(float ratio) {
        this.time -= (int) (this.maxTime * ratio);
    }

    public void addCatchRate(ServerPlayer player, float mod) {
        RaidPlayer p = this.playerMap.get(player.getUUID());
        if (p != null) p.addCatchRate(mod);
    }

    public void mulCatchRate(ServerPlayer player, float mod) {
        RaidPlayer p = this.playerMap.get(player.getUUID());
        if (p != null) p.mulCatchRate(mod);
    }

    public class TimerRunnable extends RaidSession.RaidRunnable {
        public TimerRunnable() {
            super(() -> {}, 20, true);
        }

        @Override
        public boolean tick() {
            if (++this.tick < this.delay) return false;
            else if (RaidInstance.this.isFinished()) return false;
            else if (--RaidInstance.this.time <= 0) {
                RaidInstance.this.stopInstance(false);
                return false;
            }
            RaidInstance.this.timerEvent.setProgress((float) RaidInstance.this.time / RaidInstance.this.maxTime);
            return false;
        }
    }

    public static class RaidPlayer {
        private int cheers;
        private float catchRate;

        public RaidPlayer() {
            this.cheers = 0;
            this.catchRate = 0.0F;
        }

        public RaidPlayer(RaidBoss boss) {
            this.cheers = boss.getMaxCheers();
            this.catchRate = boss.getCatchRate();
        }

        public boolean useCheer() {
            if (this.cheers <= 0) return false;
            this.cheers--;
            return true;
        }

        public void addCatchRate(float mod) {
            this.catchRate = Mth.clamp(this.catchRate + mod, 0F, 1F);
        }

        public void mulCatchRate(float mod) {
            this.catchRate = Mth.clamp(this.catchRate * mod, 0F, 1F);
        }

        public float catchRate() {
            return this.catchRate;
        }

        public int getCheers() {
            return this.cheers;
        }
    }
}
