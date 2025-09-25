package com.necro.raid.dens.common.blocks.entity;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.raids.*;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.RaidBucket;
import com.necro.raid.dens.common.util.RaidBucketRegistry;
import com.necro.raid.dens.common.util.RaidRegistry;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
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
    private int age;
    private int inactiveTicks;
    private int soundTicks;

    private UUID uuid;
    private ResourceLocation raidBucket;
    private ResourceLocation raidBoss;
    private ServerLevel dimension;

    private boolean queueFindDimension;
    private int queueTimeout;
    private boolean queueClose;

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
            this.dimension = level.getServer().getLevel(ModDimensions.createLevelKey(this.raidHost.toString()));
            if (this.hasDimension()) this.queueFindDimension = false;
            else this.queueTimeout++;

            if (this.queueTimeout > 200) {
                RaidHelper.removeHost(this.raidHost);
                RaidHelper.finishRaid(this.playerQueue);
                this.raidHost = null;
                this.playerQueue.clear();
                this.queueFindDimension = false;
            }
        }
        else if (this.queueClose && (this.dimension == null || this.dimension.players().isEmpty())) {
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

        if (this.raidHost != null && this.hasDimension() && this.dimension.players().isEmpty()) {
            if (++this.inactiveTicks > 2400) this.closeRaid(blockPos);
        }
        else this.inactiveTicks = 0;

        if (!blockState.getValue(RaidCrystalBlock.CAN_RESET)) return;
        else if (CobblemonRaidDens.CONFIG.reset_time <= 0) return;
        else if (this.isInProgress()) return;

        if (++this.age % (CobblemonRaidDens.CONFIG.reset_time * 20)  == 0) {
            this.playerQueue.clear();
            RaidHelper.resetClearedRaids(this.getUuid());
            this.resetClears();
            this.inactiveTicks = 0;
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
        this.setRaidBoss(bossLocation);

        level.setBlock(blockPos, blockState
            .setValue(RaidCrystalBlock.RAID_TIER, raidBoss.getTier())
            .setValue(RaidCrystalBlock.RAID_TYPE, raidBoss.getType())
            .setValue(RaidCrystalBlock.ACTIVE, true), 2);
    }

    public void spawnRaidBoss() {
        if (this.dimension == null) return;
        RaidBoss raidBoss = this.getRaidBoss();
        if (raidBoss == null) {
            this.setRaidBoss(null);
            return;
        }

        PokemonEntity pokemonEntity = raidBoss.getBossEntity(this.dimension);
        pokemonEntity.moveTo(0.5, 0, -14.5);
        this.dimension.addFreshEntity(pokemonEntity);
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

        if (this.getLevel() == null) return;
        MinecraftServer server = this.getLevel().getServer();
        ServerPlayer p = null;
        if (server != null && this.raidHost != null) p = server.getPlayerList().getPlayer(this.raidHost);
        if (p != null) server.getCommands().sendCommands(p);

        if (!this.hasDimension()) return;
        this.dimension.players().forEach((player) ->
            player.teleportTo((ServerLevel) this.getLevel(), blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() - 0.5, 0, 0)
        );
        this.dimension
            .getEntitiesOfClass(PokemonEntity.class, new AABB(BlockPos.ZERO).inflate(32), p1 -> ((IRaidAccessor) p1).isRaidBoss())
            .forEach(p1 -> {
                RaidInstance raidInstance = RaidHelper.ACTIVE_RAIDS.remove(((IRaidAccessor) p1).getRaidId());
                if (raidInstance != null) raidInstance.stopRaid(false);
            });

        this.removeDimension();
        this.dimension = null;
        this.raidHost = null;
        this.playerQueue.clear();
        this.inactiveTicks = 0;
        this.getLevel().getChunkAt(blockPos).setUnsaved(true);
    }

    protected void removeDimension() {
        ResourceKey<Level> levelKey = ModDimensions.createLevelKey(this.raidHost.toString());
        DimensionHelper.queueForRemoval(levelKey, this.dimension);
        if (this.getLevel() != null) DimensionHelper.SYNC_DIMENSIONS.accept(this.getLevel().getServer(), levelKey, false);
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

    public void addPlayer(Player player) {
        this.playerQueue.add(player.getUUID());
        this.setChanged();
    }

    public boolean isPlayerParticipating(Player player) {
        return this.playerQueue.contains(player.getUUID());
    }

    public boolean isInProgress() {
        return !this.playerQueue.isEmpty() && this.dimension != null && !this.dimension.players().isEmpty();
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
        return this.dimension;
    }

    public void setDimension(ServerLevel level) {
        this.dimension = level;
    }

    public boolean hasDimension() {
        return this.dimension != null;
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
        this.age = compoundTag.getInt("age");
        this.inactiveTicks = compoundTag.getInt("raid_inactive_for");

        if (compoundTag.contains("uuid")) this.uuid = UUID.fromString(compoundTag.getString("uuid"));
        else this.uuid = UUID.randomUUID();
        if (compoundTag.contains("raid_bucket")) this.raidBucket = ResourceLocation.parse(compoundTag.getString("raid_bucket"));
        if (compoundTag.contains("raid_boss")) this.raidBoss = ResourceLocation.parse(compoundTag.getString("raid_boss"));
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        if (this.raidHost != null) compoundTag.putString("raid_host_uuid", this.raidHost.toString());
        else if (this.dimension != null) compoundTag.putString("raid_host_uuid", this.dimension.dimension().location().getPath());

        ListTag playerQueueTag = new ListTag();
        this.playerQueue.forEach(uuid -> playerQueueTag.add(StringTag.valueOf(uuid.toString())));
        compoundTag.put("raid_player_queue", playerQueueTag);

        compoundTag.putInt("raid_cleared", this.clears);
        compoundTag.putInt("age", this.age);
        compoundTag.putInt("raid_inactive_for", this.inactiveTicks);

        if (this.uuid != null) compoundTag.putString("uuid", this.uuid.toString());
        if (this.raidBucket != null) compoundTag.putString("raid_bucket", this.raidBucket.toString());
        if (this.raidBoss != null) compoundTag.putString("raid_boss", this.raidBoss.toString());
    }

    public void setRaidBoss(ResourceLocation raidBoss) {
        RaidHelper.resetClearedRaids(this.getUuid());
        this.resetClears();
        this.inactiveTicks = 0;
        this.raidBoss = raidBoss;
        this.setChanged();
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
