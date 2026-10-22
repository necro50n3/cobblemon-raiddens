package com.necro.raid.dens.common.raids;

import com.necro.raid.dens.common.data.adventure.RaidAdventure;
import com.necro.raid.dens.common.events.RaidEndEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.raids.adventure.AdventureNode;
import com.necro.raid.dens.common.raids.adventure.RaidNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class RaidAdventureInstance extends AbstractRaidSession {
    private final RaidAdventure adventure;
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

    private RaidAdventureInstance(@Nullable UUID host, UUID raid, RaidAdventure adventure, boolean isInDen, @Nullable Vec3 startPos, @Nullable ServerLevel startDimension) {
        super(host, raid, isInDen);
        this.adventure = adventure;
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

    public RaidAdventureInstance(@Nullable UUID host, UUID raid, RaidAdventure adventure) {
        this(host, raid, adventure, true, null, null);
    }

    public RaidAdventureInstance(@Nullable UUID host, UUID raid, RaidAdventure adventure, @NotNull Vec3 startPos, @NotNull ServerLevel startDimension) {
        this(host, raid, adventure, false, startPos, startDimension);
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
        if (this.getCurrentNode() != null) this.getCurrentNode().onPlayerJoin(player, this);
    }

    public RaidAdventure adventure() {
        return this.adventure;
    }

    public List<AdventureNode> getNodes() {
        return this.adventure.nodes();
    }

    public int getCurrentNodeIndex() {
        return this.currentNodeIndex;
    }

    public Vec3 startPos() {
        return this.startPos;
    }

    public ServerLevel startDimension() {
        return this.startDimension;
    }

    public @Nullable AdventureNode getCurrentNode() {
        if (this.currentNodeIndex >= 0 && this.currentNodeIndex < this.getNodes().size()) {
            return this.getNodes().get(this.currentNodeIndex);
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
        AdventureNode current = this.getCurrentNode();
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
        this.schedule(() -> this.advanceToNextNode(server), 60, false);
    }

    public void advanceToNextNode(@Nullable MinecraftServer server) {
        if (server == null) server = this.getServer();
        if (server == null) return;
        if (this.getCurrentNode() != null && !this.getCurrentNode().isCompleted()) return;

        while (true) {
            this.currentNodeIndex++;
            if (this.currentNodeIndex >= this.getNodes().size()) {
                this.stopRaid(true);
                return;
            }

            AdventureNode nextNode = this.getNodes().get(this.currentNodeIndex);
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
            AdventureNode current = this.getCurrentNode();
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
        RaidEvents.RAID_END.emit(new RaidEndEvent(this.activePlayers, this.adventure, this.raidState == RaidState.SUCCESS));
    }

    @Override
    public RaidState getRaidState() {
        return this.raidState;
    }

    @Override
    public boolean isFinished() {
        return this.finished || this.raidState == RaidState.SUCCESS || this.raidState == RaidState.FAILED || this.raidState == RaidState.CANCELLED;
    }

    @Override
    public @Nullable RaidInstance getActive() {
        return this.activeRaid;
    }
}
