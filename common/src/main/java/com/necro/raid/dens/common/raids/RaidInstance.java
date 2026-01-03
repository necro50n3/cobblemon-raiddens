package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BagItemActionResponse;
import com.cobblemon.mod.common.battles.PassActionResponse;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.net.messages.client.battle.BattleApplyPassResponsePacket;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.config.TierConfig;
import com.necro.raid.dens.common.events.RaidEndEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.showdown.bagitems.CheerBagItem;
import com.necro.raid.dens.common.showdown.events.*;
import com.necro.raid.dens.common.util.IHealthSetter;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class RaidInstance {
    private static final Map<String, Consumer<PokemonBattle>> INSTRUCTION_MAP = new HashMap<>();

    private final PokemonEntity bossEntity;
    private final RaidBoss raidBoss;
    private final ServerBossEvent bossEvent;
    private final List<PokemonBattle> battles;
    private final Map<UUID, Float> damageCache;
    private final Map<UUID, Float> damageTracker;
    private final List<ServerPlayer> activePlayers;
    private final List<UUID> failedPlayers;

    private float currentHealth;
    private float maxHealth;
    private final float initMaxHealth;
    private final Map<Integer, String> scriptByTurn;
    private final NavigableMap<Double, String> scriptByHp;

    private final Map<UUID, Integer> cheersLeft;
    private final List<DelayedRunnable> runQueue;

    public RaidInstance(PokemonEntity entity) {
        this.bossEntity = entity;
        this.raidBoss = ((IRaidAccessor) entity).getRaidBoss();
        this.bossEvent = new ServerBossEvent(
            ((MutableComponent) entity.getName()).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.WHITE),
            BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.NOTCHED_10
        );

        this.battles = new ArrayList<>();
        this.damageCache = new HashMap<>();
        this.damageTracker = new HashMap<>();

        this.activePlayers = new ArrayList<>();
        this.failedPlayers = new ArrayList<>();

        this.currentHealth = entity.getPokemon().getCurrentHealth();
        this.maxHealth = entity.getPokemon().getMaxHealth();
        this.initMaxHealth = this.maxHealth;

        this.scriptByTurn = new HashMap<>();
        this.scriptByHp = new TreeMap<>();
        raidBoss.getScript().forEach((key, func) -> {
            if (!INSTRUCTION_MAP.containsKey(func) && !func.startsWith("USE_MOVE")) return;
            try {
                if (key.startsWith("turn:")) {
                    this.scriptByTurn.put(Integer.parseInt(key.split(":")[1]), func);
                }
                else if (key.startsWith("hp:")) {
                    double threshold = Double.parseDouble(key.split(":")[1]);
                    if ((this.currentHealth / this.maxHealth) < threshold) return;
                    this.scriptByHp.put(threshold, func);
                }
            }
            catch (Exception ignored) {}
        });

        this.cheersLeft = new HashMap<>();
        this.runQueue = new ArrayList<>();
        this.runQueue.add(new DelayedRunnable(() -> {
            if (this.bossEntity.isDeadOrDying()) return;
            for (ServerPlayer player : this.activePlayers) {
                if (player.level() != this.bossEntity.level()) this.removePlayer(player);
            }
        }, 20, true));
    }

    public void addPlayer(ServerPlayer player, PokemonBattle battle) {
        TierConfig tierConfig = CobblemonRaidDens.TIER_CONFIG.get(this.raidBoss.getTier());
        ((IRaidBattle) battle).setRaidBattle(this);
        this.battles.add(battle);
        this.bossEvent.addPlayer(player);

        this.damageCache.put(player.getUUID(), this.currentHealth);
        this.damageTracker.put(player.getUUID(), 0f);
        if (!this.activePlayers.isEmpty() && tierConfig.multiplayerHealthMultiplier() > 1.0f) this.applyHealthMulti(player);
        if (this.scriptByTurn.containsKey(0)) {
            Consumer<PokemonBattle> script = this.getInstructions(this.scriptByTurn.get(0));
            if (script != null) script.accept(battle);
        }

        this.cheersLeft.put(player.getUUID(), tierConfig.maxCheers());
        this.activePlayers.add(player);
        RaidDenNetworkMessages.SYNC_HEALTH.accept(player, this.currentHealth / this.maxHealth);
    }

    public void addPlayer(PokemonBattle battle) {
        this.addPlayer(battle.getPlayers().getFirst(), battle);
    }

    private void applyHealthMulti(ServerPlayer newPlayer) {
        float bonusHealth = this.initMaxHealth * (CobblemonRaidDens.TIER_CONFIG.get(this.raidBoss.getTier()).multiplayerHealthMultiplier() - 1f) * this.activePlayers.size();
        float currentRatio = this.currentHealth / this.maxHealth;
        this.maxHealth = this.initMaxHealth + bonusHealth;
        this.currentHealth = this.maxHealth * currentRatio;

        ((IHealthSetter) this.bossEntity.getPokemon()).setMaxHealth((int) this.maxHealth, false);
        this.bossEntity.getPokemon().setCurrentHealth((int) this.currentHealth);

        this.battles.forEach(b -> {
            ServerPlayer player = b.getPlayers().getFirst();
            this.playerJoin(b, player, newPlayer);
        });
    }

    public void removePlayer(ServerPlayer player, PokemonBattle battle) {
        this.battles.remove(battle);
        ((IRaidBattle) battle).setRaidBattle(null);
        this.bossEvent.removePlayer(player);
        this.damageCache.remove(player.getUUID());
        this.failedPlayers.add(player.getUUID());
    }

    public void removePlayer(PokemonBattle battle) {
        this.removePlayer(battle.getPlayers().getFirst(), battle);
    }

    public void removePlayer(ServerPlayer player) {
        this.bossEvent.removePlayer(player);
        this.damageCache.remove(player.getUUID());
    }

    public void syncHealth(ServerPlayer player, PokemonBattle battle, float remainingHealth) {
        if (!this.activePlayers.contains(player) && ((IRaidBattle) battle).isRaidBattle()) this.addPlayer(player, battle);

        float damage = this.damageCache.get(player.getUUID()) - remainingHealth;
        this.damageCache.put(player.getUUID(), remainingHealth);
        this.damageTracker.computeIfPresent(player.getUUID(), (uuid, totalDamage) -> totalDamage + damage);

        this.currentHealth = Math.clamp(this.currentHealth - damage, 0f, this.maxHealth);
        this.activePlayers.forEach(p -> RaidDenNetworkMessages.SYNC_HEALTH.accept(p, this.currentHealth / this.maxHealth));

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

    public List<ServerPlayer> getPlayers() {
        return this.activePlayers;
    }

    public float getRemainingHealth() {
        return this.currentHealth;
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
        RaidHelper.ACTIVE_RAIDS.remove(((IRaidAccessor) this.bossEntity).getRaidId());
        this.battles.forEach(PokemonBattle::stop);
        if (this.raidBoss == null) return;

        if (raidSuccess) this.handleSuccess();
        else this.handleFailed();
    }

    private void handleSuccess() {
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

        if (this.bossEntity != null && !this.bossEntity.isRemoved()) this.bossEntity.discard();
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
        this.activePlayers.forEach(player -> {
            player.sendSystemMessage(Component.translatable("message.cobblemonraiddens.raid.raid_fail"));
            RaidEvents.RAID_END.emit(new RaidEndEvent(player, this.raidBoss, this.bossEntity.getPokemon(), false));
        });
    }

    public RaidBoss getRaidBoss() {
        return this.raidBoss;
    }

    private Consumer<PokemonBattle> getInstructions(@NotNull String func) {
        if (func.startsWith("USE_MOVE")) {
            String[] params = func.split("_");
            if (params.length != 4) return null;
            String move = params[2].toLowerCase();
            int target = Integer.parseInt(params[3]);
            
            return battle -> new UseMoveShowdownEvent(move, target).send(battle);
        }
        else {
            return INSTRUCTION_MAP.get(func);
        }
    }

    public void runScriptByTurn(PokemonBattle battle, int turn) {
        String func = this.scriptByTurn.remove(turn);
        if (func == null) return;
        Consumer<PokemonBattle> script = this.getInstructions(func);
        if (script != null) script.accept(battle);
    }

    public void runScriptByHp(double hpRatio) {
        this.scriptByHp.tailMap(hpRatio, true)
            .values()
            .forEach(func -> this.battles.forEach(battle -> {
                if (func == null) return;
                Consumer<PokemonBattle> script = this.getInstructions(func);
                if (script != null) script.accept(battle);
            }));

        this.scriptByHp.keySet().removeIf(hp -> hp >= hpRatio);
    }

    public boolean runCheer(ServerPlayer player, PokemonBattle oBattle, CheerBagItem bagItem, String data) {
        int cheersLeft = this.cheersLeft.getOrDefault(player.getUUID(), 0);
        if (cheersLeft <= 0) return false;
        this.cheersLeft.put(player.getUUID(), --cheersLeft);

        this.cheer(oBattle, bagItem, data, false);

        Consumer<PokemonBattle> cheer = switch (bagItem.cheerType()) {
            case CheerBagItem.CheerType.ATTACK -> battle -> new CheerAttackShowdownEvent(Integer.parseInt(bagItem.param()), "").send(battle);
            case CheerBagItem.CheerType.DEFENSE -> battle -> new CheerDefenseShowdownEvent(Integer.parseInt(bagItem.param()), "").send(battle);
            case CheerBagItem.CheerType.HEAL -> battle -> new CheerHealShowdownEvent(Double.parseDouble(bagItem.param()), "").send(battle);
        };

        for (PokemonBattle b : this.battles) {
            if (b == oBattle) continue;
            cheer.accept(b);
        }

        return true;
    }

    public void playerJoin(PokemonBattle battle, ServerPlayer player, Player newPlayer) {
        new PlayerJoinShowdownEvent(this.currentHealth, newPlayer.getName().getString()).send(battle);
        this.damageCache.put(player.getUUID(), (float) Math.floor(this.currentHealth));
    }

    public void cheer(PokemonBattle battle, BagItem bagItem, String data, boolean skipEnemyAction) {
        BattleActor side1 = battle.getSide1().getActors()[0];
        BattleActor side2 = battle.getSide2().getActors()[0];
        List<ActiveBattlePokemon> target = side1.getActivePokemon();
        if (side1.getRequest() == null || side2.getRequest() == null || target.isEmpty() || target.getFirst().getBattlePokemon() == null) return;
        if (bagItem instanceof CheerBagItem cheerBagItem && cheerBagItem.cheerType() == CheerBagItem.CheerType.HEAL && target.getFirst().getBattlePokemon().getEntity() instanceof PokemonEntity entity) {
            entity.playSound(CobblemonSounds.MEDICINE_HERB_USE, 1f, 1f);
        }
        this.sendAction(side1, side2,new BagItemActionResponse(bagItem, target.getFirst().getBattlePokemon(), data), skipEnemyAction);
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

    static {
        INSTRUCTION_MAP.put("RESET_BOSS", battle -> new ClearBossShowdownEvent().send(battle));
        INSTRUCTION_MAP.put("RESET_PLAYER", battle -> new ClearPlayerShowdownEvent().send(battle));

        INSTRUCTION_MAP.put("BOSS_ATK_1", battle -> new StatBoostShowdownEvent(Stats.ATTACK, 1, 2).send(battle));
        INSTRUCTION_MAP.put("BOSS_ATK_2", battle -> new StatBoostShowdownEvent(Stats.ATTACK, 2, 2).send(battle));
        INSTRUCTION_MAP.put("BOSS_DEF_1", battle -> new StatBoostShowdownEvent(Stats.DEFENCE, 1, 2).send(battle));
        INSTRUCTION_MAP.put("BOSS_DEF_2", battle -> new StatBoostShowdownEvent(Stats.DEFENCE, 2, 2).send(battle));
        INSTRUCTION_MAP.put("BOSS_SPA_1", battle -> new StatBoostShowdownEvent(Stats.SPECIAL_ATTACK, 1, 2).send(battle));
        INSTRUCTION_MAP.put("BOSS_SPA_2", battle -> new StatBoostShowdownEvent(Stats.SPECIAL_ATTACK, 2, 2).send(battle));
        INSTRUCTION_MAP.put("BOSS_SPD_1", battle -> new StatBoostShowdownEvent(Stats.SPECIAL_DEFENCE, 1, 2).send(battle));
        INSTRUCTION_MAP.put("BOSS_SPD_2", battle -> new StatBoostShowdownEvent(Stats.SPECIAL_DEFENCE, 2, 2).send(battle));
        INSTRUCTION_MAP.put("BOSS_SPE_1", battle -> new StatBoostShowdownEvent(Stats.SPEED, 1, 2).send(battle));
        INSTRUCTION_MAP.put("BOSS_SPE_2", battle -> new StatBoostShowdownEvent(Stats.SPEED, 2, 2).send(battle));
        INSTRUCTION_MAP.put("BOSS_ACC_1", battle -> new StatBoostShowdownEvent(Stats.ACCURACY, 1, 2).send(battle));
        INSTRUCTION_MAP.put("BOSS_ACC_2", battle -> new StatBoostShowdownEvent(Stats.ACCURACY, 2, 2).send(battle));
        INSTRUCTION_MAP.put("BOSS_EVA_1", battle -> new StatBoostShowdownEvent(Stats.EVASION, 1, 2).send(battle));
        INSTRUCTION_MAP.put("BOSS_EVA_2", battle -> new StatBoostShowdownEvent(Stats.EVASION, 2, 2).send(battle));

        INSTRUCTION_MAP.put("PLAYER_ATK_1", battle -> new StatBoostShowdownEvent(Stats.ATTACK, -1, 1).send(battle));
        INSTRUCTION_MAP.put("PLAYER_ATK_2", battle -> new StatBoostShowdownEvent(Stats.ATTACK, -2, 1).send(battle));
        INSTRUCTION_MAP.put("PLAYER_DEF_1", battle -> new StatBoostShowdownEvent(Stats.DEFENCE, -1, 1).send(battle));
        INSTRUCTION_MAP.put("PLAYER_DEF_2", battle -> new StatBoostShowdownEvent(Stats.DEFENCE, -2, 1).send(battle));
        INSTRUCTION_MAP.put("PLAYER_SPA_1", battle -> new StatBoostShowdownEvent(Stats.SPECIAL_ATTACK, -1, 1).send(battle));
        INSTRUCTION_MAP.put("PLAYER_SPA_2", battle -> new StatBoostShowdownEvent(Stats.SPECIAL_ATTACK, -2, 1).send(battle));
        INSTRUCTION_MAP.put("PLAYER_SPD_1", battle -> new StatBoostShowdownEvent(Stats.SPECIAL_DEFENCE, -1, 1).send(battle));
        INSTRUCTION_MAP.put("PLAYER_SPD_2", battle -> new StatBoostShowdownEvent(Stats.SPECIAL_DEFENCE, -2, 1).send(battle));
        INSTRUCTION_MAP.put("PLAYER_SPE_1", battle -> new StatBoostShowdownEvent(Stats.SPEED, 1, -1).send(battle));
        INSTRUCTION_MAP.put("PLAYER_SPE_2", battle -> new StatBoostShowdownEvent(Stats.SPEED, 2, -1).send(battle));
        INSTRUCTION_MAP.put("PLAYER_ACC_1", battle -> new StatBoostShowdownEvent(Stats.ACCURACY, -1, 1).send(battle));
        INSTRUCTION_MAP.put("PLAYER_ACC_2", battle -> new StatBoostShowdownEvent(Stats.ACCURACY, -2, 1).send(battle));
        INSTRUCTION_MAP.put("PLAYER_EVA_1", battle -> new StatBoostShowdownEvent(Stats.EVASION, -1, 1).send(battle));
        INSTRUCTION_MAP.put("PLAYER_EVA_2", battle -> new StatBoostShowdownEvent(Stats.EVASION, -2, 1).send(battle));

        INSTRUCTION_MAP.put("SET_RAIN", battle -> new SetWeatherShowdownEvent("raindance").send(battle));
        INSTRUCTION_MAP.put("SET_SANDSTORM", battle -> new SetWeatherShowdownEvent("sandstorm").send(battle));
        INSTRUCTION_MAP.put("SET_SNOW", battle -> new SetWeatherShowdownEvent("snow").send(battle));
        INSTRUCTION_MAP.put("SET_SUN", battle -> new SetWeatherShowdownEvent("sunnyday").send(battle));

        INSTRUCTION_MAP.put("SET_ELECTRIC_TERRAIN", battle -> new SetTerrainShowdownEvent("electricterrain").send(battle));
        INSTRUCTION_MAP.put("SET_GRASSY_TERRAIN", battle -> new SetTerrainShowdownEvent("grassyterrain").send(battle));
        INSTRUCTION_MAP.put("SET_MISTY_TERRAIN", battle -> new SetTerrainShowdownEvent("mistyterrain").send(battle));
        INSTRUCTION_MAP.put("SET_PSYCHIC_TERRAIN", battle -> new SetTerrainShowdownEvent("psychicterrain").send(battle));
    }
}
