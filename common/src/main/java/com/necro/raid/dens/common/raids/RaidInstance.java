package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BagItemActionResponse;
import com.cobblemon.mod.common.battles.PassActionResponse;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.item.interactive.PotionType;
import com.necro.raid.dens.common.events.RaidEndEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.util.IRaidAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;

import java.util.*;

public class RaidInstance {
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

//    private final int maxDuration;
//    private int durationTick;

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

//        this.maxDuration = CobblemonRaidDens.CONFIG.raid_duration * 20;
//        this.durationTick = this.maxDuration;

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
        this.bossEvent.addPlayer(player);
//        this.timer.addPlayer(player);
        this.damageCache.put(player, this.currentHealth);
        this.activePlayers.add(player);
        RaidBuilder.SYNC_HEALTH.accept(player, this.currentHealth / this.maxHealth);
    }

    public void addPlayer(PokemonBattle battle) {
        this.addPlayer(battle.getPlayers().getFirst(), battle);
    }

    public void removePlayer(ServerPlayer player, PokemonBattle battle) {
        // Originally raids were timed before pivoting to one-life system.
//        this.durationTick -= (int) (this.maxDuration * 0.1);

        this.battles.remove(battle);
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
        this.bossEvent.getPlayers().forEach(p -> RaidBuilder.SYNC_HEALTH.accept(p, this.currentHealth / this.maxHealth));

        if (this.currentHealth == 0f) {
            this.bossEvent.setProgress(this.currentHealth / this.maxHealth);
            this.queueStopRaid();
        }
        else {
            this.runQueue.add(new DelayedRunnable(() -> this.bossEvent.setProgress(this.currentHealth / this.maxHealth), 60));
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

    public void attackCheer() {
        this.cheer(CobblemonItems.X_ATTACK.getBagItem());
    }

    public void defenseCheer() {
        this.cheer(CobblemonItems.X_DEFENSE.getBagItem());
    }

    public void healCheer() {
        this.cheer(CobblemonItems.POTION.getBagItem());
    }

    private void cheer(BagItem bagItem) {
        for (PokemonBattle battle : this.battles) {
            BattleActor battleActor = battle.getSide1().getActors()[0];
            BattleActor battleActor2 = battle.getSide2().getActors()[0];

            List<ActiveBattlePokemon> activePokemon = battleActor.getActivePokemon();
            if (activePokemon.isEmpty()) continue;
            BattlePokemon battlePokemon = activePokemon.getFirst().getBattlePokemon();
            if (battlePokemon == null) continue;

            String value = bagItem instanceof PotionType ? String.valueOf(battlePokemon.getMaxHealth() / 2) : "1";

            battleActor.getResponses().add(new BagItemActionResponse(bagItem, battlePokemon, value));
            battleActor.setMustChoose(false);
            battleActor2.getResponses().addFirst(PassActionResponse.INSTANCE);
            battleActor2.setMustChoose(false);
            battle.checkForInputDispatch();
        }
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
            return !this.repeat;
        }
    }
}
