package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BagItemActionResponse;
import com.cobblemon.mod.common.battles.PassActionResponse;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.net.messages.client.battle.BattleApplyPassResponsePacket;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.events.RaidEndEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.items.ModItems;
import com.necro.raid.dens.common.showdown.CheerBagItem;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

public class RaidInstance {
    private static final Map<String, BiConsumer<RaidInstance, PokemonBattle>> INSTRUCTION_MAP = new HashMap<>();

    private final PokemonEntity bossEntity;
    private final RaidBoss raidBoss;
    private final ServerBossEvent bossEvent;
//    private final ServerBossEvent timer;
    private final List<PokemonBattle> battles;
    private final Map<ServerPlayer, Float> damageCache;
    private final List<ServerPlayer> activePlayers;
    private final List<ServerPlayer> failedPlayers;

    private float currentHealth;
    private final float maxHealth;
    private final Map<Integer, String> scriptByTurn;
    private final NavigableMap<Double, String> scriptByHp;

//    private final int maxDuration;
//    private int durationTick;

    private final Map<ServerPlayer, Integer> cheersLeft;
    private final List<DelayedRunnable> runQueue;

    public RaidInstance(PokemonEntity entity) {
        this.bossEntity = entity;
        this.raidBoss = ((IRaidAccessor) entity).getRaidBoss();
        this.bossEvent = new ServerBossEvent(
            ((MutableComponent) entity.getName()).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.WHITE),
            BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.NOTCHED_10
        );
//        this.timer = new ServerBossEvent(
//            Component.empty(),
//            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS
//        );
        this.battles = new ArrayList<>();
        this.damageCache = new HashMap<>();

        this.activePlayers = new ArrayList<>();
        this.failedPlayers = new ArrayList<>();

        this.currentHealth = entity.getPokemon().getCurrentHealth();
        this.maxHealth = entity.getPokemon().getMaxHealth();

        this.scriptByTurn = new HashMap<>();
        this.scriptByHp = new TreeMap<>();
        raidBoss.getScript().forEach((key, func) -> {
            if (!INSTRUCTION_MAP.containsKey(func)) return;
            try {
                if (key.startsWith("turn:")) {
                    this.scriptByTurn.put(Integer.parseInt(key.split(":")[1]), func);
                }
                else if (key.startsWith("hp:")) {
                    this.scriptByHp.put(Double.parseDouble(key.split(":")[1]), func);
                }
            }
            catch (Exception ignored) {}
        });

//        this.maxDuration = CobblemonRaidDens.CONFIG.raid_duration * 20;
//        this.durationTick = this.maxDuration;

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
        this.battles.add(battle);
        ((IRaidBattle) battle).setRaidBattle(this);
        this.bossEvent.addPlayer(player);
//        this.timer.addPlayer(player);
        this.damageCache.put(player, this.currentHealth);
        this.activePlayers.add(player);
        this.cheersLeft.put(player, CobblemonRaidDens.CONFIG.max_cheers);
        RaidBuilder.SYNC_HEALTH.accept(player, this.currentHealth / this.maxHealth);
    }

    public void addPlayer(PokemonBattle battle) {
        this.addPlayer(battle.getPlayers().getFirst(), battle);
    }

    public void removePlayer(ServerPlayer player, PokemonBattle battle) {
        // Originally raids were timed before pivoting to one-life system.
//        this.durationTick -= (int) (this.maxDuration * 0.1);

        this.battles.remove(battle);
        ((IRaidBattle) battle).setRaidBattle(null);
        this.bossEvent.removePlayer(player);
//        this.timer.removePlayer(player);
        this.damageCache.remove(player);
        this.failedPlayers.add(player);
    }

    public void removePlayer(PokemonBattle battle) {
        this.removePlayer(battle.getPlayers().getFirst(), battle);
    }

    public void removePlayer(ServerPlayer player) {
        this.bossEvent.removePlayer(player);
        this.damageCache.remove(player);
    }

    public void syncHealth(ServerPlayer player, float remainingHealth) {
        float damage = this.damageCache.get(player) - remainingHealth;
        this.damageCache.put(player, remainingHealth);

        this.currentHealth = Math.clamp(this.currentHealth - damage, 0f, this.maxHealth);
        this.activePlayers.forEach(p -> RaidBuilder.SYNC_HEALTH.accept(p, this.currentHealth / this.maxHealth));

        if (this.currentHealth == 0f) {
            this.bossEvent.setProgress(this.currentHealth / this.maxHealth);
            this.queueStopRaid();
        }
        else {
            this.runQueue.add(new DelayedRunnable(() -> {
                this.bossEvent.setProgress(this.currentHealth / this.maxHealth);
                this.runScriptByHp((double) this.currentHealth / this.maxHealth);
            }, 60));
        }
    }

    public float getRemainingHealth() {
        return this.currentHealth;
    }

    public boolean hasFailed(ServerPlayer player) {
        return this.failedPlayers.contains(player);
    }

    public void tick() {
//        if (--this.durationTick == 0) this.queueStopRaid(false);
//        if (this.durationTick % 40 == 0) this.timer.setProgress(this.durationTick / (float) this.maxDuration);
        this.runQueue.removeIf(DelayedRunnable::tick);
    }

    public void queueStopRaid() {
        this.queueStopRaid(true);
    }

    private void queueStopRaid(boolean raidSuccess) {
        this.runQueue.add(new DelayedRunnable(() -> this.stopRaid(raidSuccess), 60));
    }

    public void stopRaid(boolean raidSuccess) {
        this.bossEvent.setVisible(false);
        this.bossEvent.removeAllPlayers();
//        this.timer.setVisible(false);
//        this.timer.removeAllPlayers();
        if (raidSuccess) this.bossEntity.setHealth(0f);
        RaidHelper.ACTIVE_RAIDS.remove(((IRaidAccessor) this.bossEntity).getRaidId());
        this.battles.forEach(PokemonBattle::stop);
        if (this.raidBoss == null) return;
        this.activePlayers.forEach(player -> {
            if (raidSuccess) new RewardHandler(this.raidBoss, player).sendRewardMessage();
            else player.sendSystemMessage(Component.translatable("message.cobblemonraiddens.raid.raid_fail"));
            RaidEvents.RAID_END.emit(new RaidEndEvent(player, this.raidBoss, raidSuccess));
        });
    }

    public RaidBoss getRaidBoss() {
        return this.raidBoss;
    }

    public void runScriptByTurn(PokemonBattle battle, int turn) {
        String func = this.scriptByTurn.remove(turn);
        if (func == null) return;
        ((IRaidBattle) battle).addToQueue(INSTRUCTION_MAP.get(func));
    }

    public void runScriptByHp(double hpRatio) {
        this.scriptByHp.tailMap(hpRatio, true)
            .values()
            .forEach(func -> this.battles.forEach(battle -> ((IRaidBattle) battle).addToQueue(INSTRUCTION_MAP.get(func))));

        this.scriptByHp.keySet().removeIf(hp -> hp >= hpRatio);
    }

    public boolean runCheer(ServerPlayer player, PokemonBattle oBattle, BagItem bagItem, String data) {
        int cheersLeft = this.cheersLeft.getOrDefault(player, 0);
        if (cheersLeft <= 0) return false;
        this.cheersLeft.put(player, --cheersLeft);

        this.cheer(oBattle, bagItem, data, false);
        for (PokemonBattle b : this.battles) {
            if (b == oBattle) continue;
            ((IRaidBattle) b).addToQueue((raid, battle) -> raid.cheer(battle, bagItem, data, true));
        }
        return true;
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

    private void clearBossStats(@NotNull PokemonBattle battle) {
        BattleActor side1 = battle.getSide1().getActors()[0];
        BattleActor side2 = battle.getSide2().getActors()[0];
        List<ActiveBattlePokemon> target = side2.getActivePokemon();
        if (side1.getRequest() == null || side2.getRequest() == null || target.isEmpty() || target.getFirst().getBattlePokemon() == null) return;
        BattlePokemon bp = target.getFirst().getBattlePokemon();
        String key = bp.getName().getContents() instanceof TranslatableContents t ? t.getKey() : bp.getName().toString();
        this.sendAction(side1, side2, new BagItemActionResponse(ModItems.CLEAR_BOSS, bp, key));
    }

    private void clearPlayerStats(@NotNull PokemonBattle battle) {
        BattleActor side1 = battle.getSide1().getActors()[0];
        BattleActor side2 = battle.getSide2().getActors()[0];
        List<ActiveBattlePokemon> target = side1.getActivePokemon();
        if (side1.getRequest() == null || side2.getRequest() == null || target.isEmpty() || target.getFirst().getBattlePokemon() == null) return;
        BattlePokemon bp = target.getFirst().getBattlePokemon();
        String key = bp.getName().getContents() instanceof TranslatableContents t ? t.getKey() : bp.getName().toString();
        this.sendAction(side1, side2, new BagItemActionResponse(ModItems.CLEAR_PLAYER, bp, key));
    }

    private void sendAction(BattleActor side1, BattleActor side2, ShowdownActionResponse response) {
        this.sendAction(side1, side2, response, true);
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
        INSTRUCTION_MAP.put("RESET_BOSS", RaidInstance::clearBossStats);
        INSTRUCTION_MAP.put("RESET_PLAYER", RaidInstance::clearPlayerStats);
    }
}
