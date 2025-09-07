package com.necro.raid.dens.common.blocks.entity;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.raids.*;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.RaidRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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

    private RaidBoss raidBoss;
    private ServerLevel dimension;

    private boolean queueFindDimension;
    private int queueTimeout;
    private boolean queueClose;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RaidCrystalBlockEntity(BlockEntityType<? extends RaidCrystalBlockEntity> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        this.playerQueue = new HashSet<>();
        this.soundTicks = 0;
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
                CobblemonRaidDens.LOGGER.info("Could not find dimension {}:{}", CobblemonRaidDens.MOD_ID, this.raidHost.toString());
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

        if (this.raidBoss == null && blockState.getValue(RaidCrystalBlock.CAN_CYCLE)) {
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
        else if (this.isInProgress()) return;

        if (++this.age % (CobblemonRaidDens.CONFIG.reset_time * 20)  == 0) {
            this.playerQueue.clear();
            RaidHelper.resetClearedRaids(blockPos);
            this.clears = 0;
            this.inactiveTicks = 0;
            if (blockState.getValue(RaidCrystalBlock.CAN_CYCLE)) this.generateRaidBoss(level, blockPos, blockState);
        }
    }

    public void generateRaidBoss(Level level, BlockPos blockPos, BlockState blockState) {
        RaidTier newRaidTier = RaidTier.getWeightedRandom(level.getRandom());
        this.raidBoss = RaidRegistry.getRandomRaidBoss(level.getRandom(), newRaidTier);
        if (this.raidBoss == null) return;
        RaidType newRaidType = this.raidBoss.getType();

        level.setBlock(blockPos, blockState
            .setValue(RaidCrystalBlock.RAID_TIER, newRaidTier)
            .setValue(RaidCrystalBlock.RAID_TYPE, newRaidType)
            .setValue(RaidCrystalBlock.ACTIVE, true), 2);
    }

    public void spawnRaidBoss() {
        if (this.dimension == null) return;
        PokemonEntity pokemonEntity = this.raidBoss.getBossEntity(this.dimension);
        pokemonEntity.moveTo(0.5, 0, -14.5);
        ((IRaidAccessor) pokemonEntity).setRaidBossData(this.raidBoss);
        float height = pokemonEntity.getExposedSpecies().getHeight();
        CobblemonRaidDens.LOGGER.info(String.valueOf(height));
        float scale;
        if (height > 80.0f) scale = 1.5f;
        else if (height > 40.f) scale = 2.0f;
        else scale = 2.5f;
        pokemonEntity.getPokemon().setScaleModifier(scale);
        this.dimension.addFreshEntity(pokemonEntity);
    }

    public void clearRaid(BlockPos blockPos) {
        this.clears++;
        RaidHelper.clearRaid(blockPos, this.playerQueue);
        this.queueClose = true;
    }

    public void closeRaid(BlockPos blockPos) {
        RaidHelper.removeHost(this.raidHost);
        RaidHelper.finishRaid(this.playerQueue);

        try {
            MinecraftServer server = this.getLevel().getServer();
            ServerPlayer player = server.getPlayerList().getPlayer(this.raidHost);
            if (player != null) server.getCommands().sendCommands(player);
        }
        catch (NullPointerException ignored) {}

        if (!this.hasDimension()) return;
        this.dimension.players().forEach((player) ->
            player.teleportTo((ServerLevel) this.level, blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() - 0.5, 0, 0)
        );

        this.removeDimension();
        this.dimension = null;
        this.raidHost = null;
        this.playerQueue.clear();
        this.inactiveTicks = 0;
        this.getLevel().getChunkAt(blockPos).setUnsaved(true);
    }

    protected void removeDimension() {
        DimensionHelper.queueForRemoval(ModDimensions.createLevelKey(this.raidHost.toString()), this.dimension);
    }

    public UUID getRaidHost() {
        return this.raidHost;
    }

    public boolean setRaidHost(Player player) {
        if (this.getRaidHost() != null) return false;
        this.raidHost = player.getUUID();
        this.playerQueue.add(player.getUUID());
        return true;
    }

    public RaidBoss getRaidBoss() {
        return this.raidBoss;
    }

    public void addPlayer(Player player) {
        this.playerQueue.add(player.getUUID());
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

    public boolean hasPlayerCleared(BlockPos blockPos, Player player) {
        Set<UUID> cleared = RaidHelper.CLEARED_RAIDS.getOrDefault(blockPos, new HashSet<>());
        return cleared.contains(player.getUUID());
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

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        if (compoundTag.contains("raid_host_uuid")) {
            this.raidHost = UUID.fromString(compoundTag.getString("raid_host_uuid"));
            this.queueFindDimension = true;
        }
        if (compoundTag.contains("raid_player_queue")) {
            ((ListTag) compoundTag.get("raid_player_queue")).forEach(tag -> this.playerQueue.add(UUID.fromString(tag.getAsString())));
        }
        this.clears = compoundTag.getInt("raid_cleared");
        this.age = compoundTag.getInt("age");
        this.inactiveTicks = compoundTag.getInt("raid_inactive_for");

        if (compoundTag.contains("raid_boss")) {
            this.raidBoss = RaidBoss.loadNbt(compoundTag.getCompound("raid_boss"));
        }
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
        if (this.raidBoss != null) {
            compoundTag.put("raid_boss", this.raidBoss.saveNbt(new CompoundTag()));
        }
    }

    public void setRaidBoss(RaidBoss raidBoss) {
        this.raidBoss = raidBoss;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}
