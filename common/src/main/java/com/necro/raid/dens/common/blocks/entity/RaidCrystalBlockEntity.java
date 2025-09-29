package com.necro.raid.dens.common.blocks.entity;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.dimensions.ModDimensions;
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

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RaidCrystalBlockEntity(BlockEntityType<? extends RaidCrystalBlockEntity> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        this.playerQueue = new HashSet<>();
        this.soundTicks = 0;
        this.uuid = UUID.randomUUID();
        this.queueFindDimension = false;
        this.queueTimeout = 0;
        this.queueClose = false;
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
        else if (!this.canTick(blockState)) return;

        if (this.raidBoss == null || !RaidRegistry.exists(this.raidBoss)) {
            this.generateRaidBoss(level, blockPos, blockState);
        }

        if (++this.soundTicks % 60 == 0) {
            level.playSound(null, blockPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.0f);
            this.soundTicks = 0;
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
        if (bossLocation == null || raidBoss == null) return;
        this.setRaidBoss(bossLocation, level.getRandom(), level.getGameTime());

        level.setBlock(blockPos, blockState
            .setValue(RaidCrystalBlock.RAID_TIER, raidBoss.getTier())
            .setValue(RaidCrystalBlock.RAID_TYPE, raidBoss.getType())
            .setValue(RaidCrystalBlock.ACTIVE, true), 2);
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
        return true;
    }

    public void clearRaid() {
        this.clears++;
        if (this.isAtMaxClears()) RaidHelper.resetClearedRaids(this.getUuid());
        else RaidHelper.clearRaid(this.getUuid(), this.playerQueue);
        this.setQueueClose();
    }

    public void closeRaid(BlockPos blockPos) {
        RaidHelper.removeHost(this.raidHost);
        RaidHelper.finishRaid(this.playerQueue);
        RaidHelper.removeRequests(this.raidHost);

        if (this.getLevel() == null || !this.hasDimension()) return;
        this.removeChunkTicket();

        this.getDimension().players().forEach((player) ->
            player.teleportTo((ServerLevel) this.getLevel(), blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() - 0.5, 0, 0)
        );
        this.getDimension()
            .getEntitiesOfClass(PokemonEntity.class, new AABB(BlockPos.ZERO).inflate(32), p1 -> ((IRaidAccessor) p1).isRaidBoss())
            .forEach(p1 -> {
                RaidInstance raidInstance = RaidHelper.ACTIVE_RAIDS.remove(((IRaidAccessor) p1).getRaidId());
                if (raidInstance != null) raidInstance.stopRaid(false);
            });

        this.removeDimension();
        this.setDimension(null);
        this.raidHost = null;
        this.playerQueue.clear();
        this.inactiveTicks = 0;
        this.getLevel().getChunkAt(blockPos).setUnsaved(true);
        this.setChanged();
    }

    protected void removeDimension() {
        ResourceKey<Level> levelKey = ModDimensions.createLevelKey(this.raidHost.toString());
        DimensionHelper.queueForRemoval(levelKey, this.getDimension());
        if (this.getLevel() != null) DimensionHelper.SYNC_DIMENSIONS.accept(this.getLevel().getServer(), levelKey, false);
    }

    public void addChunkTicket() {
        if (this.getLevel() == null) return;
        ChunkPos chunkPos = new ChunkPos(this.getBlockPos());
        ((ServerLevel) this.getLevel()).getChunkSource().addRegionTicket(TicketType.FORCED, chunkPos, 1, chunkPos);
    }

    private void removeChunkTicket() {
        if (this.getLevel() == null) return;
        ChunkPos chunkPos = new ChunkPos(this.getBlockPos());
        ((ServerLevel) this.getLevel()).getChunkSource().removeRegionTicket(TicketType.FORCED, chunkPos, 1, chunkPos);
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

    public boolean canTick(BlockState blockState) {
        return blockState.getValue(RaidCrystalBlock.ACTIVE);
    }

    public boolean renderBeacon(BlockState blockState) {
        return blockState.getValue(RaidCrystalBlock.ACTIVE) && blockState.getValue(RaidCrystalBlock.RAID_TYPE) != RaidType.NONE;
    }

    public void resetClears() {
        this.clears = 0;
    }

    public boolean isAtMaxClears() {
        return CobblemonRaidDens.CONFIG.max_clears != -1 && this.clears >= CobblemonRaidDens.CONFIG.max_clears;
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
        return CobblemonRaidDens.CONFIG.max_players != -1 && this.playerQueue.size() >= CobblemonRaidDens.CONFIG.max_players;
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
