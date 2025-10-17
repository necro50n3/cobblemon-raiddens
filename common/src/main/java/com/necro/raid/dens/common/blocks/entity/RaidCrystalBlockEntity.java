package com.necro.raid.dens.common.blocks.entity;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.events.RaidDenSpawnEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.events.SetRaidBossEvent;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.*;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.common.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class RaidCrystalBlockEntity extends BlockEntity implements GeoBlockEntity {
    private UUID raidHost;
    private final Set<UUID> playerQueue;
    private int clears;
    private int inactiveTicks;
    private int soundTicks;

    private UUID uuid;
    private ResourceLocation raidBucket;
    private ResourceLocation raidBoss;
    private ResourceLocation raidStructure;

    private ResourceKey<Level> dimensionKey;
    private ServerLevel dimensionLevel;

    private boolean queueFindDimension;
    private int queueTimeout;
    private boolean queueClose;

    private long lastReset;
    private boolean isOpen;
    private Consumer<ServerPlayer> aspectSync;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RaidCrystalBlockEntity(BlockEntityType<? extends RaidCrystalBlockEntity> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        this.playerQueue = new HashSet<>();
        this.soundTicks = 0;
        this.uuid = UUID.randomUUID();
        this.queueFindDimension = false;
        this.queueTimeout = 0;
        this.queueClose = false;
        this.isOpen = false;
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (this.queueFindDimension && this.raidHost != null && level.getServer() != null) {
            ResourceKey<Level> key = ModDimensions.createLevelKey(this.raidHost.toString());
            if (level.getServer().getLevel(key) != null) {
                this.setDimension(level.getServer().getLevel(key));
                this.queueFindDimension = false;
            }
            else this.queueTimeout++;

            if (this.queueTimeout > 200) {
                RaidHelper.removeHost(this.raidHost);
                RaidHelper.finishRaid(this.playerQueue);
                this.raidHost = null;
                this.playerQueue.clear();
                this.queueFindDimension = false;
            }
        }
        else if (this.queueClose && (this.getDimension() == null || this.getDimension().players().isEmpty())) {
            this.closeRaid(blockPos);
            if (this.isAtMaxClears()) level.setBlock(blockPos, blockState.setValue(RaidCrystalBlock.ACTIVE, false), 2);
            this.queueClose = false;
        }

        if (level.isClientSide()) return;

        if (this.canGenerateBoss(blockState) && (this.raidBoss == null || !RaidRegistry.exists(this.raidBoss))) {
            this.generateRaidBoss(level, blockPos, blockState);
        }

        if (this.raidHost != null && this.hasDimension() && this.getDimension().players().isEmpty()) {
            if (++this.inactiveTicks > 2400) this.closeRaid(blockPos);
        }
        else this.inactiveTicks = 0;

        if (!blockState.getValue(RaidCrystalBlock.CAN_RESET)) return;
        else if (CobblemonRaidDens.CONFIG.reset_time <= 0) return;
        else if (this.isInProgress()) return;

        long gameTime = level.getGameTime();
        if (this.lastReset == 0) this.lastReset = gameTime;
        else if (gameTime - this.lastReset > CobblemonRaidDens.CONFIG.reset_time * 20L) {
            this.playerQueue.clear();
            this.generateRaidBoss(level, blockPos, blockState);
        }

        if (!this.isActive(blockState)) return;
        if (++this.soundTicks % 60 == 0) {
            level.playSound(null, blockPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.0f);
            this.soundTicks = 0;
        }
    }

    public void generateRaidBoss(Level level, BlockPos blockPos, BlockState blockState) {
        RaidCycleMode cycleMode = blockState.getValue(RaidCrystalBlock.CYCLE_MODE);
        ResourceLocation bossLocation = null;

        if (cycleMode == RaidCycleMode.NONE) return;

        if (this.raidBucket != null) {
            bossLocation = RaidBucketRegistry.getBucket(this.raidBucket).getRandomRaidBoss(level.getRandom(), level);
        }

        if (bossLocation == null) {
            RaidTier tier = cycleMode.canCycleTier() ? RaidTier.getWeightedRandom(level.getRandom(), level) : blockState.getValue(RaidCrystalBlock.RAID_TIER);
            RaidType type = cycleMode.canCycleType() ? null : blockState.getValue(RaidCrystalBlock.RAID_TYPE);
            bossLocation = RaidRegistry.getRandomRaidBoss(level.getRandom(), level, tier, type, null);
        }

        RaidBoss raidBoss = RaidRegistry.getRaidBoss(bossLocation);
        if (raidBoss == null) return;

        SetRaidBossEvent event = new SetRaidBossEvent(raidBoss);
        RaidEvents.SET_RAID_BOSS.emit(event);
        raidBoss = event.getRaidBoss();
        if (raidBoss == null) {
            this.inactiveTicks = 0;
            this.lastReset = level.getGameTime();
            return;
        }

        this.setRaidBoss(raidBoss.getId(), level.getRandom(), level.getGameTime());

        level.setBlock(blockPos, blockState
            .setValue(RaidCrystalBlock.RAID_TIER, raidBoss.getTier())
            .setValue(RaidCrystalBlock.RAID_TYPE, raidBoss.getType())
            .setValue(RaidCrystalBlock.ACTIVE, true), 2);

        RaidEvents.RAID_DEN_SPAWN.emit(new RaidDenSpawnEvent((ServerLevel) level, blockPos, raidBoss));
    }

    public boolean spawnRaidBoss() {
        if (this.getDimension() == null) return false;
        RaidBoss raidBoss = this.getRaidBoss();
        if (raidBoss == null) {
            CobblemonRaidDens.LOGGER.error("Could not load Raid Boss {}", this.raidBoss);
            this.setRaidBoss(null, null, 0);
            return false;
        }

        PokemonEntity pokemonEntity = raidBoss.getBossEntity(this.getDimension());
        pokemonEntity.moveTo(RaidDenRegistry.getBossPos(this.getRaidStructure()));
        this.getDimension().addFreshEntity(pokemonEntity);

        if (pokemonEntity.getPokemon().getAbility().getName().equals("imposter") ||
            pokemonEntity.getPokemon().getMoveSet().getMoves().stream().anyMatch(move -> move.getName().equals("transform"))) {
            this.setAspectSync(player -> RaidDenNetworkMessages.RAID_ASPECT.accept(player, pokemonEntity));
        }

        return true;
    }

    public void clearRaid() {
        this.clears++;
        if (this.isAtMaxClears()) RaidHelper.resetClearedRaids(this.getUuid());
        else RaidHelper.clearRaid(this.getUuid(), this.playerQueue);
        this.setQueueClose();
    }

    public void closeRaid(BlockPos blockPos) {
        this.removeChunkTicket();

        RaidHelper.removeHost(this.raidHost);
        RaidHelper.finishRaid(this.playerQueue);
        RaidHelper.removeRequests(this.raidHost);

        if (this.getLevel() == null || !this.hasDimension()) return;

        this.getDimension().players().forEach((player) ->
            RaidUtils.teleportPlayerSafe(player, (ServerLevel) this.getLevel(), blockPos, player.getYHeadRot(), player.getXRot())
        );
        this.getDimension()
            .getEntitiesOfClass(PokemonEntity.class, new AABB(BlockPos.ZERO).inflate(32), p1 -> ((IRaidAccessor) p1).isRaidBoss())
            .forEach(p1 -> {
                RaidInstance raidInstance = RaidHelper.ACTIVE_RAIDS.remove(((IRaidAccessor) p1).getRaidId());
                if (raidInstance != null) {
                    raidInstance.stopRaid(false);
                    if (CobblemonRaidDens.CONFIG.max_clears_include_fails) this.clears++;
                }
            });

        this.removeDimension();
        this.setDimension(null);
        this.raidHost = null;
        this.playerQueue.clear();
        this.inactiveTicks = 0;
        this.getLevel().getChunkAt(blockPos).setUnsaved(true);
        this.setChanged();
        this.isOpen = false;
        this.setAspectSync(null);
    }

    protected void removeDimension() {
        if (CobblemonRaidDens.CONFIG.cache_dimensions) {
            DimensionHelper.addToCache(this.getDimension());
        }
        else {
            ResourceKey<Level> levelKey = ModDimensions.createLevelKey(this.raidHost.toString());
            DimensionHelper.queueForRemoval(levelKey, this.getDimension());
            if (this.getLevel() != null) DimensionHelper.SYNC_DIMENSIONS.accept(this.getLevel().getServer(), levelKey, false);
        }
    }

    public void addChunkTicket() {
        if (this.getLevel() == null) return;
        ChunkPos chunkPos = new ChunkPos(this.getBlockPos());
        ((ServerLevel) this.getLevel()).getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 2, "raid".hashCode());
    }

    private void removeChunkTicket() {
        if (this.getLevel() == null) return;
        ChunkPos chunkPos = new ChunkPos(this.getBlockPos());
        try {((ServerLevel) this.getLevel()).getChunkSource().removeRegionTicket(TicketType.POST_TELEPORT, chunkPos, 2, "raid".hashCode()); }
        catch (Throwable ignored) {}
    }

    public UUID getRaidHost() {
        return this.raidHost;
    }

    public boolean canSetRaidHost() {
        return this.getRaidHost() == null;
    }

    public void setRaidHost(Player player) {
        this.raidHost = player.getUUID();
        this.playerQueue.add(player.getUUID());
        this.setChanged();
    }

    public void clearRaidHost() {
        this.playerQueue.remove(this.raidHost);
        this.raidHost = null;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public RaidBucket getRaidBucket() {
        return RaidBucketRegistry.getBucket(this.raidBucket);
    }

    public RaidBoss getRaidBoss() {
        return RaidRegistry.getRaidBoss(this.raidBoss);
    }

    public ResourceLocation getRaidBossLocation() {
        return this.raidBoss;
    }

    public ResourceLocation getRaidStructure() {
        return this.raidStructure;
    }

    public void addPlayer(Player player) {
        this.playerQueue.add(player.getUUID());
        this.setChanged();
    }

    public int getPlayerCount() {
        return this.playerQueue.size();
    }

    public long getTicksUntilNextReset() {
        if (this.getLevel() == null) return 0;
        else if (!this.getBlockState().getValue(RaidCrystalBlock.CAN_RESET)) return 0;
        else if (CobblemonRaidDens.CONFIG.reset_time <= 0) return 0;
        return CobblemonRaidDens.CONFIG.reset_time * 20L - (this.getLevel().getGameTime() - this.lastReset);
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    public void setOpen() {
        this.isOpen = true;
    }

    public boolean isPlayerParticipating(Player player) {
        return this.playerQueue.contains(player.getUUID());
    }

    public boolean isInProgress() {
        return !this.playerQueue.isEmpty() && this.getDimension() != null && !this.getDimension().players().isEmpty();
    }

    public boolean isActive(BlockState blockState) {
        return blockState.getValue(RaidCrystalBlock.ACTIVE)
            && blockState.getValue(RaidCrystalBlock.RAID_TYPE) != RaidType.NONE
            && this.raidBoss != null;
    }

    public boolean canGenerateBoss(BlockState blockState) {
        return blockState.getValue(RaidCrystalBlock.ACTIVE)
            && blockState.getValue(RaidCrystalBlock.RAID_TYPE) != RaidType.NONE;
    }

    public boolean renderBeacon(BlockState blockState) {
        return blockState.getValue(RaidCrystalBlock.ACTIVE) && blockState.getValue(RaidCrystalBlock.RAID_TYPE) != RaidType.NONE;
    }

    public void resetClears() {
        this.clears = 0;
    }

    public boolean isAtMaxClears() {
        RaidBoss boss = this.getRaidBoss();
        if (boss == null) return true;
        int maxClears = CobblemonRaidDens.TIER_CONFIG.get(boss.getTier()).maxClears();
        return maxClears != -1 && this.clears >= maxClears;
    }

    public ServerLevel getDimension() {
        if (this.dimensionLevel != null) return this.dimensionLevel;
        else if (this.getLevel() == null || this.getLevel().getServer() == null) return null;
        else this.dimensionLevel = this.getLevel().getServer().getLevel(this.dimensionKey);
        return this.dimensionLevel;
    }

    public void setDimension(ServerLevel level) {
        this.dimensionKey = level == null ? null : level.dimension();
        this.dimensionLevel = level;
    }

    public boolean hasDimension() {
        return this.getDimension() != null;
    }

    public boolean isFull() {
        RaidBoss boss = this.getRaidBoss();
        if (boss == null) return true;
        int maxPlayers = CobblemonRaidDens.TIER_CONFIG.get(boss.getTier()).maxPlayers();
        return maxPlayers != -1 && this.playerQueue.size() >= maxPlayers;
    }

    public boolean isBusy() {
        return this.queueClose || this.queueFindDimension;
    }

    public void setQueueClose() {
        this.queueClose = true;
    }

    public void setRaidBucket(ResourceLocation bucket) {
        this.raidBucket = bucket;
    }

    public void syncAspects(ServerPlayer player) {
        if (this.aspectSync == null) return;
        CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS).execute(() -> this.aspectSync.accept(player));
    }

    public void setAspectSync(Consumer<ServerPlayer> sync) {
        this.aspectSync = sync;
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        if (compoundTag.contains("raid_host_uuid")) {
            this.raidHost = UUID.fromString(compoundTag.getString("raid_host_uuid"));
            this.queueFindDimension = true;
        }
        if (compoundTag.contains("raid_player_queue")) {
            compoundTag.getList("raid_player_queue", Tag.TAG_STRING).forEach(tag -> this.playerQueue.add(UUID.fromString(tag.getAsString())));
        }
        this.clears = compoundTag.getInt("raid_cleared");
        this.lastReset = compoundTag.getLong("last_reset");
        this.inactiveTicks = compoundTag.getInt("raid_inactive_for");

        if (compoundTag.contains("uuid")) this.uuid = UUID.fromString(compoundTag.getString("uuid"));
        else this.uuid = UUID.randomUUID();
        if (compoundTag.contains("raid_bucket")) this.raidBucket = ResourceLocation.parse(compoundTag.getString("raid_bucket"));
        if (compoundTag.contains("raid_boss")) this.raidBoss = ResourceLocation.parse(compoundTag.getString("raid_boss"));
        if (compoundTag.contains("raid_structure")) this.raidStructure = ResourceLocation.parse(compoundTag.getString("raid_structure"));
        else this.raidStructure = RaidDenRegistry.DEFAULT;
        if (compoundTag.contains("is_open")) this.isOpen = true;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        if (this.raidHost != null) compoundTag.putString("raid_host_uuid", this.raidHost.toString());
        else if (this.getDimension() != null) compoundTag.putString("raid_host_uuid", this.dimensionKey.location().getPath());

        ListTag playerQueueTag = new ListTag();
        this.playerQueue.forEach(uuid -> playerQueueTag.add(StringTag.valueOf(uuid.toString())));
        compoundTag.put("raid_player_queue", playerQueueTag);

        compoundTag.putInt("raid_cleared", this.clears);
        compoundTag.putLong("last_reset", this.lastReset);
        compoundTag.putInt("raid_inactive_for", this.inactiveTicks);

        if (this.uuid != null) compoundTag.putString("uuid", this.uuid.toString());
        if (this.raidBucket != null) compoundTag.putString("raid_bucket", this.raidBucket.toString());
        if (this.raidBoss != null) compoundTag.putString("raid_boss", this.raidBoss.toString());
        if (this.raidStructure != null) compoundTag.putString("raid_structure", this.raidStructure.toString());
        if (this.isOpen) compoundTag.putBoolean("is_open", true);
    }

    public void setRaidBoss(ResourceLocation raidBoss, RandomSource random, long gameTime) {
        RaidHelper.resetClearedRaids(this.getUuid());
        this.resetClears();
        this.inactiveTicks = 0;
        this.lastReset = gameTime;
        this.raidBoss = raidBoss;
        if (raidBoss == null) this.raidStructure = null;
        else this.raidStructure = this.getRaidBoss().getRandomDen(random);
        this.setChanged();
    }

    public void setRaidStructure(ResourceLocation structure) {
        this.raidStructure = structure;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        if (this.raidBoss != null) tag.putString("raid_boss", this.raidBoss.toString());
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}
