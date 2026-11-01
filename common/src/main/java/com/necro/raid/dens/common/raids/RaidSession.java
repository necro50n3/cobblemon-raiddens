package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.dimension.RaidRegion;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidSyncContext;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.events.RaidEndEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.party.RaidStoreFactory;
import com.necro.raid.dens.common.raids.adventure.AdventureNode;
import com.necro.raid.dens.common.raids.adventure.BossNode;
import com.necro.raid.dens.common.raids.adventure.RaidNode;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.raids.helpers.RaidJoinHelper;
import com.necro.raid.dens.common.raids.helpers.RaidRegionHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RaidSession {
    private final @Nullable UUID host;
    private final UUID raid;
    private final RaidBoss boss;
    private final boolean isInDen;

    private final List<ServerPlayer> activePlayers;
    private final Set<UUID> failedPlayers;
    private int sharedLives;
    private final Map<UUID, Integer> playerLives;
    private final Map<UUID, DamageRecord> damageTracker;
    private final ConcurrentLinkedQueue<RaidRunnable> runQueue;

    private final List<AdventureNode> nodes;
    private int currentNodeIndex;
    private @Nullable RaidInstance activeRaid;

    private RaidState raidState;
    private final Vec3 startPos;
    private final ServerLevel startDimension;
    private boolean started;
    private boolean finished;

    private boolean noAI;
    private boolean isInvulnerable;
    private boolean isPersistent;

    private RaidSession(@Nullable UUID host, UUID raid, RaidBoss boss, boolean isInDen, @Nullable Vec3 startPos, @Nullable ServerLevel startDimension) {
        this.host = host;
        this.raid = raid;
        this.boss = boss;
        this.isInDen = isInDen;

        this.activePlayers = new ArrayList<>();
        this.failedPlayers = new HashSet<>();
        this.sharedLives = this.boss.getLives();
        this.playerLives = new HashMap<>();
        this.damageTracker = new HashMap<>();
        this.runQueue = new ConcurrentLinkedQueue<>();

        this.nodes = this.boss.copyNodes();
        this.nodes.add(new BossNode());
        this.currentNodeIndex = -1;
        this.activeRaid = null;

        this.raidState = RaidState.NOT_STARTED;
        this.startPos = startPos;
        this.startDimension = startDimension;
        this.started = false;
        this.finished = false;

        this.noAI = true;
        this.isInvulnerable = true;
        this.isPersistent = false;
    }

    public RaidSession(@Nullable UUID host, UUID raid, RaidBoss raidBoss) {
        this(host, raid, raidBoss, true, null, null);
    }

    public RaidSession(@Nullable UUID host, UUID raid, RaidBoss raidBoss, @NotNull Vec3 startPos, @NotNull ServerLevel startDimension) {
        this(host, raid, raidBoss, false, startPos, startDimension);
    }

    public void setFlags(boolean noAI, boolean isInvulnerable, boolean isPersistent) {
        this.noAI = noAI;
        this.isInvulnerable = isInvulnerable;
        this.isPersistent = isPersistent;
    }

    public boolean noAI() {
        return this.noAI;
    }

    public boolean isInvulnerable() {
        return this.isInvulnerable;
    }

    public boolean isPersistent() {
        return this.isPersistent;
    }

    public @Nullable UUID getHost() {
        return this.host;
    }

    public UUID getRaid() {
        return this.raid;
    }

    public RaidBoss boss() {
        return this.boss;
    }

    public boolean isAdventure() {
        return this.boss.isAdventure();
    }

    public List<ServerPlayer> activePlayers() {
        return this.activePlayers;
    }

    public Set<UUID> failedPlayers() {
        return this.failedPlayers;
    }

    public List<AdventureNode> nodes() {
        return this.nodes;
    }

    public Vec3 startPos() {
        return this.startPos;
    }

    public ServerLevel startDimension() {
        return this.startDimension;
    }

    public AdventureNode currentNode() {
        if (this.currentNodeIndex >= 0 && this.currentNodeIndex < this.nodes().size()) {
            return this.nodes().get(this.currentNodeIndex);
        }
        return null;
    }

    public void addPlayer(ServerPlayer player) {
        this.activePlayers.add(player);
        this.playerLives.putIfAbsent(player.getUUID(), this.boss().getLives());
        this.damageTracker.put(player.getUUID(), new DamageRecord(0F, 0F));
        if (this.activeRaid != null) this.activeRaid.addPlayer(player);
        if (this.currentNode() != null) this.currentNode().onPlayerJoin(player, this);
    }

    public void addBattle(ServerPlayer player, PokemonBattle battle) {
        if (this.activeRaid != null) this.activeRaid.addBattle(player, battle);
    }

    public void addPlayerAndBattle(ServerPlayer player, PokemonBattle battle) {
        this.addPlayer(player);
        this.addBattle(player, battle);
    }

    public void removePlayer(ServerPlayer player, @Nullable PokemonBattle battle, boolean ignoreLives) {
        if (this.activeRaid != null) this.activeRaid.removePlayer(player, battle, ignoreLives);
        if (this.currentNode() != null) this.currentNode().onPlayerLeave(player, this);
    }

    public void removePlayer(PokemonBattle battle, boolean ignoreLives) {
        List<ServerPlayer> players = battle.getPlayers();
        if (players.isEmpty()) return;
        if (this.activeRaid != null) this.activeRaid.removePlayer(players.getFirst(),battle, ignoreLives);
        if (this.currentNode() != null) this.currentNode().onPlayerLeave(players.getFirst(), this);
    }

    public void removePlayer(ServerPlayer player) {
        PokemonBattle battle = BattleRegistry.getBattleByParticipatingPlayer(player);
        this.removePlayer(player, battle, true);
    }

    public boolean isPlayerIn(ServerPlayer player) {
        return this.activePlayers.contains(player);
    }

    public void setActiveRaid(@NotNull RaidInstance activeRaid) {
        this.activeRaid = activeRaid;
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
        return this.boss.getPlayersShareLives();
    }

    public boolean loseLife(UUID player) {
        if (this.playersShareLives()) return --this.sharedLives <= 0;
        else if (!this.playerLives.containsKey(player)) return true;
        int lives = this.playerLives.compute(player, (uuid, l) -> l == null ? 0 : l - 1);
        return lives <= 0;
    }

    public @Nullable ServerPlayer getSelectingPlayer(MinecraftServer server) {
        if (this.host != null) {
            ServerPlayer hostPlayer = server.getPlayerList().getPlayer(this.host);
            if (hostPlayer != null && this.isPlayerIn(hostPlayer)) return hostPlayer;
        }
        for (ServerPlayer player : this.activePlayers) {
            if (player != null && !player.hasDisconnected()) return player;
        }
        return null;
    }

    public void onBossSelected(ServerPlayer player, ResourceLocation selectedBoss) {
        AdventureNode current = this.currentNode();
        if (current instanceof RaidNode raidNode) {
            MinecraftServer server = this.getServer();
            if (server != null) raidNode.onBossSelected(player, selectedBoss, this, server);
        }
    }

    public @Nullable MinecraftServer getServer() {
        for (ServerPlayer player : this.activePlayers) {
            if (player != null && player.getServer() != null) {
                return player.getServer();
            }
        }
        return null;
    }

    public void start(MinecraftServer server) {
        if (this.started || this.isFinished()) return;
        this.started = true;
        this.raidState = RaidState.IN_PROGRESS;
        if (this.isAdventure()) this.schedule(() -> this.advanceToNextNode(server), 20, false);
        else this.advanceToNextNode(server);

    }

    public void advanceToNextNode(@Nullable MinecraftServer server) {
        if (server == null) server = this.getServer();
        if (server == null) return;
        if (this.currentNode() != null && !this.currentNode().isCompleted()) return;

        while (true) {
            this.currentNodeIndex++;
            if (this.currentNodeIndex >= this.nodes().size()) {
                this.stopRaid(true);
                return;
            }

            AdventureNode nextNode = this.nodes().get(this.currentNodeIndex);
            nextNode.run(this, server);
            if (!nextNode.isCompleted()) break;
        }
    }

    public void schedule(Runnable runnable, int delay, boolean repeat) {
        if (this.isFinished()) return;
        this.runQueue.add(new DelayedRunnable(runnable, delay, repeat));
    }

    public void schedule(RaidRunnable runnable) {
        if (this.isFinished()) return;
        this.runQueue.add(runnable);
    }

    public void tick() {
        if (this.isFinished()) return;

        try { this.runQueue.removeIf(RaidRunnable::tick); }
        catch (ConcurrentModificationException ignored) {}

        MinecraftServer server = this.getServer();
        if (server == null) return;

        if (!this.started) {
            this.start(server);
            return;
        }

        if (this.activeRaid != null) {
            this.activeRaid.tick();
            if (this.activeRaid.isFinished()) {
                this.activeRaid.closeInstance();
                if (this.activeRaid.getRaidState() == RaidState.SUCCESS) {
                    this.activeRaid = null;
                    this.advanceToNextNode(server);
                }
                else this.stopRaid(false);
            }
        }
        else {
            AdventureNode current = this.currentNode();
            if (current != null && current.isCompleted()) {
                this.advanceToNextNode(server);
            }
        }
    }

    public void stopRaid(boolean raidSuccess) {
        this.raidState = raidSuccess ? RaidState.SUCCESS : RaidState.FAILED;
        this.finished = true;
        if (this.activeRaid != null && !this.activeRaid.isFinished()) this.activeRaid.stopInstance(raidSuccess);
        MinecraftServer server = this.getServer();
        if (server != null) this.closeRaid(server);
    }

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
        this.activePlayers.forEach(RaidStoreFactory::removePlayerParty);
        RaidEvents.RAID_END.emit(new RaidEndEvent(this.activePlayers, this.boss, this.raidState == RaidState.SUCCESS));
    }

    public boolean shouldClearRaid() {
        if (this.getRaidState() == RaidState.SUCCESS) return true;
        else return this.getRaidState() == RaidState.FAILED && !CobblemonRaidDens.CONFIG.retry_failed_raids;
    }

    public RaidState getRaidState() {
        return this.raidState;
    }

    public boolean isInProgress() {
        if (this.currentNodeIndex == -1) return false;
        else if (this.currentNodeIndex > 0) return true;
        else if (this.activeRaid == null) return true;
        else return this.activeRaid.getRaidState() != RaidState.NOT_STARTED;
    }

    public boolean canUsePC(ServerPlayer player) {
        return this.currentNodeIndex == -1 || !this.playerLives.containsKey(player.getUUID()) || this.raidState == RaidState.NOT_STARTED;
    }

    public boolean canUseHealingItems(ServerPlayer player) {
        if (!this.isAdventure() || this.currentNodeIndex == -1 || !this.playerLives.containsKey(player.getUUID()) || this.raidState == RaidState.NOT_STARTED) return true;
        else if (this.boss().getLives() <= 1) return false;
        return PlayerExtensionsKt.party(player).toGappyList().stream().allMatch(pokemon -> pokemon == null || pokemon.isFainted());
    }

    public boolean isFinished() {
        return this.finished || this.raidState == RaidState.SUCCESS || this.raidState == RaidState.FAILED || this.raidState == RaidState.CANCELLED;
    }

    public void addTotalHealth(float health) {
        for (ServerPlayer player : this.activePlayers) {
            this.damageTracker.computeIfAbsent(player.getUUID(), uuid -> new DamageRecord(0F, 0F)).addTotalHealth(health);
        }
    }

    public DamageRecord getDamageRecord(ServerPlayer player) {
        return this.damageTracker.computeIfAbsent(player.getUUID(), uuid -> new DamageRecord(0F, 0F));
    }

    public float getDamage(ServerPlayer player) {
        if (!this.damageTracker.containsKey(player.getUUID())) return 0;
        return this.getDamageRecord(player).ratio();
    }

    public @Nullable RaidInstance getActive() {
        return this.activeRaid;
    }

    public RaidSyncContext raidSync() {
        return RaidHelper.getRaidSync(this.raid, this.boss.getId());
    }

    public abstract static class RaidRunnable {
        protected final Runnable runnable;
        protected final int delay;
        protected int tick;
        protected final boolean repeat;

        public RaidRunnable(Runnable runnable, int delay, boolean repeat) {
            this.runnable = runnable;
            this.delay = delay;
            this.tick = 0;
            this.repeat = repeat;
        }

        public abstract boolean tick();
    }

    public class DelayedRunnable extends RaidRunnable {
        public DelayedRunnable(Runnable runnable, int delay, boolean repeat) {
            super(runnable, delay, repeat);
        }

        @Override
        public boolean tick() {
            if (++this.tick < this.delay) return false;
            else if (RaidSession.this.isFinished()) return false;
            this.runnable.run();
            this.tick = 0;
            return !this.repeat;
        }
    }

    public static class DamageRecord {
        private float damage;
        private float totalHealth;

        public DamageRecord(float damage, float totalHealth) {
            this.damage = damage;
            this.totalHealth = totalHealth;
        }

        public void addDamage(float damage) {
            this.damage += damage;
        }

        public void addTotalHealth(float totalHealth) {
            this.totalHealth += totalHealth;
        }

        public float ratio() {
            return Math.clamp(this.damage / this.totalHealth, 0F, 1F);
        }
    }
}
