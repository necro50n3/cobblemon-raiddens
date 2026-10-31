package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidSyncContext;
import com.necro.raid.dens.common.events.RaidEndEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.raids.adventure.AdventureNode;
import com.necro.raid.dens.common.raids.adventure.BossNode;
import com.necro.raid.dens.common.raids.adventure.RaidNode;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class RaidSession extends AbstractRaidSession {
    private final RaidBoss raidBoss;
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

    private RaidSession(@Nullable UUID host, UUID raid, RaidBoss raidBoss, boolean isInDen, @Nullable Vec3 startPos, @Nullable ServerLevel startDimension) {
        super(host, raid, isInDen);
        this.raidBoss = raidBoss;
        this.nodes = raidBoss.copyNodes();
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

    @Override
    public void addPlayer(ServerPlayer player) {
        super.addPlayer(player);
        if (this.activeRaid != null) this.activeRaid.addPlayer(player);
        if (this.currentNode() != null) this.currentNode().onPlayerJoin(player, this);
    }

    @Override
    public void addBattle(PokemonBattle battle) {
        super.addBattle(battle);
        if (this.activeRaid != null) this.activeRaid.addBattle(battle);
    }

    @Override
    public void removePlayer(ServerPlayer player, @Nullable PokemonBattle battle, boolean ignoreLives) {
        super.removePlayer(player, battle, ignoreLives);
        if (this.activeRaid != null) this.activeRaid.removePlayer(player, battle, ignoreLives);
        if (this.currentNode() != null) this.currentNode().onPlayerLeave(player, this);
    }

    public RaidBoss boss() {
        return this.raidBoss;
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

    public void setActiveRaid(@Nullable RaidInstance activeRaid) {
        this.activeRaid = activeRaid;
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

    @Override
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

    @Override
    public void tick() {
        super.tick();
        if (this.isFinished()) return;

        MinecraftServer server = this.getServer();
        if (server == null) return;

        if (!this.started) {
            this.start(server);
            return;
        }

        if (this.activeRaid != null) {
            this.activeRaid.tick();
            if (this.activeRaid.isFinished()) {
                if (this.activeRaid.getRaidState() == RaidState.SUCCESS) {
                    this.activeRaid = null;
                    this.advanceToNextNode(server);
                }
                else this.stopRaid(false);
            }
        }
        else {
            AdventureNode current = this.currentNode();
            if (current != null) {
                current.tick(this, server);
                if (current.isCompleted()) {
                    this.advanceToNextNode(server);
                }
            }
        }
    }

    @Override
    public void stopRaid(boolean raidSuccess) {
        this.raidState = raidSuccess ? RaidState.SUCCESS : RaidState.FAILED;
        this.finished = true;
        if (this.activeRaid != null && !this.activeRaid.isFinished()) this.activeRaid.stopRaid(raidSuccess);
        MinecraftServer server = this.getServer();
        if (server != null) this.closeRaid(server);
    }

    @Override
    public void closeRaid(MinecraftServer server, boolean wasCancelled) {
        super.closeRaid(server, wasCancelled);
        RaidEvents.RAID_END.emit(new RaidEndEvent(this.activePlayers, this.raidBoss, this.raidState == RaidState.SUCCESS));
    }

    @Override
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

    @Override
    public boolean isFinished() {
        return this.finished || this.raidState == RaidState.SUCCESS || this.raidState == RaidState.FAILED || this.raidState == RaidState.CANCELLED;
    }

    @Override
    public @Nullable RaidInstance getActive() {
        return this.activeRaid;
    }

    public boolean isAdventure() {
        return this.raidBoss.isAdventure();
    }

    public RaidSyncContext raidSync() {
        return RaidHelper.getRaidSync(this.raid, this.raidBoss.getId());
    }
}
