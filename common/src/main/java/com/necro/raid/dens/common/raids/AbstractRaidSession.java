package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.dimension.RaidRegion;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.raids.helpers.RaidJoinHelper;
import com.necro.raid.dens.common.raids.helpers.RaidRegionHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractRaidSession {
    protected final @Nullable UUID host;
    protected final UUID raid;
    protected final boolean isInDen;
    protected boolean isAdventure;

    protected final List<ServerPlayer> activePlayers;
    protected final Set<UUID> failedPlayers;

    protected int sharedLives;
    protected final Map<UUID, Integer> playerLives;

    protected final List<DelayedRunnable> runQueue;

    public AbstractRaidSession(@Nullable UUID host, UUID raid, boolean isInDen) {
        this.host = host;
        this.raid = raid;
        this.isInDen = isInDen;
        this.isAdventure = false;

        this.activePlayers = new ArrayList<>();
        this.failedPlayers = new HashSet<>();

        this.sharedLives = 0;
        this.playerLives = new HashMap<>();

        this.runQueue = new ArrayList<>();
    }

    public void addPlayer(ServerPlayer player) {
        this.activePlayers.add(player);
    }

    public void addBattle(PokemonBattle battle) {}

    public void addPlayerAndBattle(ServerPlayer player, PokemonBattle battle) {
        this.addPlayer(player);
        this.addBattle(battle);
    }

    public void removePlayer(ServerPlayer player, @Nullable PokemonBattle battle, boolean ignoreLives) {}

    public void removePlayer(PokemonBattle battle, boolean ignoreLives) {
        List<ServerPlayer> players = battle.getPlayers();
        if (players.isEmpty()) return;
        this.removePlayer(players.getFirst(), battle, ignoreLives);
    }

    public void removePlayer(ServerPlayer player) {
        PokemonBattle battle = BattleRegistry.getBattleByParticipatingPlayer(player);
        this.removePlayer(player, battle, true);
    }

    public boolean isPlayerIn(ServerPlayer player) {
        return this.activePlayers.contains(player);
    }

    public List<ServerPlayer> getPlayers() {
        return this.activePlayers;
    }

    public int getPlayerCount() {
        return this.activePlayers.size();
    }

    public boolean hasFailed(ServerPlayer player) {
        return this.failedPlayers.contains(player.getUUID());
    }

    public boolean playersShareLives() {
        return false;
    }

    public boolean loseLife(UUID player) {
        if (this.playersShareLives()) return --this.sharedLives <= 0;
        else if (!this.playerLives.containsKey(player)) return true;
        int lives = this.playerLives.compute(player, (uuid, l) -> l == null ? 0 : l - 1);
        return lives <= 0;
    }

    public boolean isAdventure() {
        return this.isAdventure;
    }

    public void setAdventure(boolean adventure) {
        this.isAdventure = adventure;
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
        this.schedule(() -> this.stopRaid(raidSuccess), 60, false);
    }

    public abstract void stopRaid(boolean raidSuccess);

    public @Nullable UUID getHost() {
        return this.host;
    }

    public UUID getRaid() {
        return this.raid;
    }

    public boolean isInDen() {
        return this.isInDen;
    }

    public abstract RaidState getRaidState();

    public abstract boolean isFinished();

    public abstract @Nullable RaidInstance getActive();

    public abstract void start(MinecraftServer server);

    public void closeRaid(MinecraftServer server) {
        this.closeRaid(server, false);
    }

    public void closeRaid(MinecraftServer server, boolean wasCancelled) {
        if (this.shouldClearRaid() && this.isInDen) RaidHelper.clearRaid(this.raid, this.activePlayers);

        RaidRegion region = RaidRegionHelper.getRegion(this.raid);
        if (region != null) server.execute(() -> region.removeRegionTicket(ModDimensions.getRaidDimension(server)));

        if (this.isInDen) RaidHelper.closeRaid(this.raid, wasCancelled ? RaidState.CANCELLED : this.getRaidState(), ModDimensions.getRaidDimension(server));
        else RaidHelper.ACTIVE_RAIDS.remove(this.raid);
        if (this.host != null) RaidHelper.removeRequests(this.host);
        RaidJoinHelper.removeParticipants(this.activePlayers);
    }

    protected boolean shouldClearRaid() {
        if (this.getRaidState() == RaidState.SUCCESS) return true;
        else return this.getRaidState() == RaidState.FAILED && !CobblemonRaidDens.CONFIG.retry_failed_raids;
    }

    public void schedule(Runnable runnable, int delay, boolean repeat) {
        if (this.isFinished()) return;
        this.runQueue.add(new DelayedRunnable(runnable, delay, repeat));
    }

    public class DelayedRunnable {
        private final Runnable runnable;
        protected final int delay;
        protected int tick;
        private final boolean repeat;

        public DelayedRunnable(Runnable runnable, int delay, boolean repeat) {
            this.runnable = runnable;
            this.delay = delay;
            this.tick = 0;
            this.repeat = repeat;
        }

        public boolean tick() {
            if (++this.tick < this.delay) return false;
            else if (AbstractRaidSession.this.isFinished()) return false;
            this.runnable.run();
            this.tick = 0;
            return !this.repeat;
        }
    }
}
