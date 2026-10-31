package com.necro.raid.dens.common.blocks.entity;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.CobblemonRaidDensClient;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.client.block.RaidCrystalSparkleRenderer;
import com.necro.raid.dens.common.data.dimension.RaidRegion;
import com.necro.raid.dens.common.data.raid.*;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.events.RaidDenSpawnEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.events.SetRaidEvent;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.*;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.raids.helpers.RaidJoinHelper;
import com.necro.raid.dens.common.raids.helpers.RaidRegionHelper;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import com.necro.raid.dens.common.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RaidCrystalBlockEntity extends BlockEntity implements GeoBlockEntity {
    private int clears;
    private int inactiveTicks;
    private int soundTicks;

    private int beamHeight;
    private int lastCheckedY;
    private int checkingHeight;

    private UUID uuid;
    private ResourceLocation raidBucket;
    private ResourceLocation raidBoss;

    private RaidResetContext lastReset;
    private boolean isOpen;
    private Consumer<ServerPlayer> aspectSync;

    private boolean isGenerating;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RaidCrystalBlockEntity(BlockEntityType<? extends RaidCrystalBlockEntity> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        this.soundTicks = 0;
        this.beamHeight = 0;
        this.lastCheckedY = -1;
        this.checkingHeight = 0;
        this.uuid = UUID.randomUUID();
        this.isOpen = false;
        this.isGenerating = false;
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (level.isClientSide()) {
            this.clientTick(blockState);
            return;
        }

        boolean isIdle = this.isIdle();
        if (RaidHelper.hasRaidState(this.getUuid()) && isIdle) this.closeRaid();

        if (this.raidBoss != null && this.exists() && (!CustomRaidRegistries.RAID_REGISTRY.cacheHas(this.raidBoss) || level.getGameTime() % 400 == 0)) {
            CustomRaidRegistries.RAID_REGISTRY.requestRaidBoss(this.raidBoss);
        }
        if (this.canGenerateBoss(blockState) && (this.raidBoss == null || !this.exists())) this.generateRaid(level, blockPos, blockState);

        if (this.isInProgress() && isIdle) {
            if (++this.inactiveTicks > 2400) this.closeRaid();
        }
        else this.inactiveTicks = 0;

        if (this.isActive(blockState) && ++this.soundTicks % 120 == 0) {
            level.playSound(null, blockPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.5f, 1.0f);
            this.soundTicks = 0;
        }

        if (!blockState.getValue(RaidCrystalBlock.CAN_RESET)) return;
        else if (CobblemonRaidDens.CONFIG.reset_time <= 0) return;
        else if (this.isInProgress()) return;

        long gameTime = level.getGameTime();
        if (this.lastReset == null) this.lastReset = new RaidResetContext(gameTime);
        else if (this.lastReset.shouldReset(gameTime)) this.generateRaid(level, blockPos, blockState);
    }

    private boolean exists() {
        return CustomRaidRegistries.RAID_REGISTRY.exists(this.raidBoss);
    }

    private void clientTick(BlockState blockState) {
        if (!this.isActive(blockState)) return;
        this.calculateBeamHeight(this.getLevel());
    }

    private void calculateBeamHeight(Level level) {
        BlockPos blockPos = this.getBlockPos();
        int maxHeight = 320 - blockPos.getY();
        boolean hasSkyAccess = RaidUtils.hasSkyAccess(level, blockPos);
        if (!hasSkyAccess) {
            if (this.lastCheckedY < blockPos.getY()) {
                this.lastCheckedY = blockPos.getY();
                this.checkingHeight = 0;
            }

            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(blockPos.getX(), this.lastCheckedY + 1, blockPos.getZ());

            for (int i = 0; i < 10 && this.checkingHeight < maxHeight; i++) {
                if (level.getBlockState(mutableBlockPos).getLightBlock(level, mutableBlockPos) >= 15) {
                    this.beamHeight = this.checkingHeight;
                    this.lastCheckedY = blockPos.getY() - 1;
                    return;
                }

                this.checkingHeight++;
                this.lastCheckedY++;
                mutableBlockPos.move(0, 1, 0);
            }
        }

        if (hasSkyAccess || this.checkingHeight >= maxHeight){
            this.beamHeight = maxHeight + 192;
            this.lastCheckedY = blockPos.getY() - 1;
            this.checkingHeight = 0;
        }
    }

    public int getBeamHeight() {
        return this.beamHeight;
    }

    public void generateRaid(Level level, BlockPos blockPos, BlockState blockState) {
        if (this.isGenerating) return;

        RaidCycleMode cycleMode = blockState.getValue(RaidCrystalBlock.CYCLE_MODE);
        if (cycleMode == RaidCycleMode.CONFIG) cycleMode = CobblemonRaidDens.CONFIG.cycle_mode;
        ResourceLocation bossLocation = switch (cycleMode) {
            case RaidCycleMode.NONE -> this.generateFromExisting(level, blockState, cycleMode);
            case RaidCycleMode.BUCKET -> this.generateFromBucket(level, blockState, cycleMode);
            default -> this.generateRandom(level, blockState, cycleMode);
        };
        if (bossLocation == null) return;
        this.isGenerating = true;

        CustomRaidRegistries.RAID_REGISTRY.requestRaidBoss(bossLocation).whenCompleteAsync((optional, error) -> {
            this.isGenerating = false;
            if (error != null || optional.isEmpty() || this.isRemoved()) return;
            this.generateRaid(optional.get(), level, blockPos, blockState);
        }, level.getServer());
    }

    private void generateRaid(RaidBoss boss, Level level, BlockPos blockPos, BlockState blockState) {
        SetRaidEvent event = new SetRaidEvent(boss);
        RaidEvents.SET_RAID.emit(event);
        boss = event.getRaid();
        if (boss == null) {
            this.inactiveTicks = 0;
            this.lastReset = new RaidResetContext(level.getGameTime());
            return;
        }

        this.setRaidBoss(boss.getId(), level.getGameTime());

        level.setBlock(blockPos, blockState
            .setValue(RaidCrystalBlock.RAID_TIER, boss.getTier())
            .setValue(RaidCrystalBlock.RAID_TYPE, boss.getType())
            .setValue(RaidCrystalBlock.ACTIVE, true)
            .setValue(RaidCrystalBlock.IS_ADVENTURE, boss.isAdventure()), 2);

        RaidEvents.RAID_DEN_SPAWN.emit(new RaidDenSpawnEvent((ServerLevel) level, blockPos, boss));
    }

    private ResourceLocation generateFromBucket(Level level, BlockState blockState, RaidCycleMode cycleMode) {
        if (this.raidBucket == null) this.raidBucket = CustomRaidRegistries.RAID_BUCKET_REGISTRY.getRandom(level.getRandom(), level.getBiome(this.getBlockPos()), level.registryAccess());
        if (this.raidBucket == null) return this.generateRandom(level, blockState, cycleMode);
        RaidBucket bucket = CustomRaidRegistries.RAID_BUCKET_REGISTRY.get(this.raidBucket);
        if (bucket == null) {
            this.raidBucket = null;
            return this.generateFromBucket(level, blockState, cycleMode);
        }
        ResourceLocation boss = bucket.getRandomRaidBoss(level.getRandom(), level);
        return boss == null ? this.generateRandom(level, blockState, cycleMode) : boss;
    }

    private ResourceLocation generateRandom(Level level, BlockState blockState, RaidCycleMode cycleMode) {
        RaidTier tier = cycleMode.canCycleTier() ? RaidTier.getWeightedRandom(level.getRandom(), level) : blockState.getValue(RaidCrystalBlock.RAID_TIER);
        RaidType type = cycleMode.canCycleType() ? null : blockState.getValue(RaidCrystalBlock.RAID_TYPE);
        return CustomRaidRegistries.RAID_REGISTRY.getRandomRaidBoss(level.getRandom(), level, tier, type, null, null);
    }

    private ResourceLocation generateFromExisting(Level level, BlockState blockState, RaidCycleMode cycleMode) {
        return this.raidBoss == null ? this.generateRandom(level, blockState, cycleMode) : this.raidBoss;
    }

    public boolean spawnRaid(UUID playerId, RaidRegion region) {
        if (this.getLevel() == null) return false;
        RaidBoss raid = this.getRaidBoss();
        ServerLevel level = ModDimensions.getRaidDimension(this.getLevel().getServer());
        if (raid == null || region == null || level == null) {
            CobblemonRaidDens.LOGGER.error("Could not load Raid {}", this.raidBoss);
            this.setRaidBoss(null, 0);
            return false;
        }
        region.placeStructure(level);
        return this.spawnRaid(playerId, raid, level);
    }

    public boolean spawnRaid(UUID playerId, RaidBoss boss, ServerLevel level) {
        RaidSession raid = new RaidSession(playerId, this.getUuid(), boss);
        raid.start(level.getServer());
        if (raid.isFinished()) return false;
        RaidHelper.ACTIVE_RAIDS.put(this.getUuid(), raid);

        if (raid.isAdventure() || raid.getActive() == null) return true;
        PokemonEntity bossEntity = raid.getActive().getBossEntity();

        if (bossEntity.getPokemon().getAbility().getName().equalsIgnoreCase("imposter")) {
            this.setAspectSync(player -> RaidDenNetworkMessages.RAID_ASPECT.accept(player, bossEntity));
        }
        this.setChanged();
        return true;
    }

    public void clearRaid(boolean wasWin) {
        this.clears++;
        if (wasWin) {
            RaidHelper.clearRaidSync(this.getUuid());
            this.setChanged();
        }
        if (this.isAtMaxClears()) {
            RaidHelper.resetClearedRaids(this.getUuid());
            if (this.getLevel() != null) this.getLevel().setBlock(
                this.getBlockPos(),
                this.getBlockState().setValue(RaidCrystalBlock.ACTIVE, false),
                2
            );
        }
    }

    private boolean shouldClear(RaidState raidState) {
        if (raidState == null) return false;
        else if (raidState == RaidState.SUCCESS) return true;
        return CobblemonRaidDens.CONFIG.max_clears_include_fails && raidState != RaidState.CANCELLED;
    }

    public void closeRaid() {
        if (this.getLevel() == null) return;
        ServerLevel level = ModDimensions.getRaidDimension(this.getLevel().getServer());
        if (level == null) return;

        RaidState raidState = RaidHelper.getRaidState(this.getUuid());
        if (this.shouldClear(raidState)) this.clearRaid(raidState == RaidState.SUCCESS);

        RaidRegionHelper.clearRegion(this.getUuid(), level);

        this.inactiveTicks = 0;
        this.getLevel().getChunkAt(this.getBlockPos()).setUnsaved(true);
        this.setChanged();
        this.isOpen = false;
        this.setAspectSync(null);
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public RaidBucket getRaidBucket() {
        return CustomRaidRegistries.RAID_BUCKET_REGISTRY.get(this.raidBucket);
    }

    public RaidBoss getRaidBoss() {
        return CustomRaidRegistries.RAID_REGISTRY.getRaidBoss(this.raidBoss);
    }

    public ResourceLocation getRaidBossLocation() {
        return this.raidBoss;
    }

    public int getClears() {
        return this.clears;
    }

    public int getPlayerCount() {
        AbstractRaidSession raid = RaidHelper.ACTIVE_RAIDS.get(this.getUuid());
        if (raid == null) return 0;
        return raid.getPlayerCount();
    }

    public long getTicksUntilNextReset() {
        if (this.getLevel() == null) return 0;
        else if (!this.getBlockState().getValue(RaidCrystalBlock.CAN_RESET)) return 0;
        else if (CobblemonRaidDens.CONFIG.reset_time <= 0) return 0;
        else if (this.lastReset == null) return 0;
        return this.lastReset.nextReset(this.getLevel().getGameTime());
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setRaidBucket(ResourceLocation bucket) {
        this.raidBucket = bucket;
    }

    public void setRaidBoss(ResourceLocation raidBoss, long gameTime) {
        RaidHelper.resetClearedRaids(this.getUuid());
        this.resetClears();
        this.inactiveTicks = 0;
        this.lastReset = new RaidResetContext(gameTime);
        this.raidBoss = raidBoss;
        RaidHelper.clearRaidSync(this.getUuid());
        this.setChanged();
        if (this.getLevel() != null) this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public void setClears(int clears) {
        this.clears = clears;
    }

    public void setOpen() {
        this.isOpen = true;
    }

    public boolean isPlayerParticipating(Player player) {
        return RaidJoinHelper.isParticipating(player, this.getUuid());
    }

    public boolean isInProgress() {
        return RaidHelper.ACTIVE_RAIDS.containsKey(this.getUuid());
    }

    public boolean isIdle() {
        if (this.getLevel() == null) return false;
        RaidRegion region = RaidRegionHelper.getRegion(this.getUuid());
        if (region == null) return true;

        ServerLevel level = ModDimensions.getRaidDimension(this.getLevel().getServer());
        if (level == null) return false;

        int activePlayers = level.getEntitiesOfClass(Player.class, region.bound()).size();
        return activePlayers == 0;
    }

    public boolean isActive(BlockState blockState) {
        return blockState.getValue(RaidCrystalBlock.ACTIVE) && blockState.getValue(RaidCrystalBlock.RAID_TYPE) != RaidType.NONE && this.raidBoss != null;
    }

    public boolean canGenerateBoss(BlockState blockState) {
        return blockState.getValue(RaidCrystalBlock.ACTIVE) && blockState.getValue(RaidCrystalBlock.RAID_TYPE) != RaidType.NONE;
    }

    public void resetClears() {
        this.clears = 0;
    }

    public boolean isAtMaxClears() {
        RaidBoss boss = this.getRaidBoss();
        if (boss == null) return true;
        int maxClears = boss.getMaxClears();
        return maxClears != -1 && this.clears >= maxClears;
    }

    public boolean isFull() {
        RaidBoss boss = this.getRaidBoss();
        if (boss == null) return true;
        int maxPlayers = boss.getMaxPlayers();
        return maxPlayers != -1 && this.getPlayerCount() >= maxPlayers;
    }

    public boolean hasStartedAdventure() {
        RaidBoss boss = this.getRaidBoss();
        if (boss == null || !boss.isAdventure()) return false;
        AbstractRaidSession raid = RaidHelper.ACTIVE_RAIDS.get(this.getUuid());
        if (raid == null) return false;
        return ((RaidSession) raid).currentNodeIndex() > 1;
    }

    public void syncAspects(ServerPlayer player) {
        if (this.aspectSync == null) return;
        CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS).execute(() -> this.aspectSync.accept(player));
    }

    public void setAspectSync(Consumer<ServerPlayer> sync) {
        this.aspectSync = sync;
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
        this.clears = compoundTag.getInt("raid_cleared");
        this.lastReset = RaidResetContext.load(compoundTag.getCompound("reset_context"));

        if (compoundTag.contains("uuid")) this.uuid = UUID.fromString(compoundTag.getString("uuid"));
        else this.uuid = UUID.randomUUID();
        if (compoundTag.contains("raid_bucket")) this.raidBucket = ResourceLocation.parse(compoundTag.getString("raid_bucket"));
        if (compoundTag.contains("raid_boss")) this.raidBoss = ResourceLocation.parse(compoundTag.getString("raid_boss"));
        if (compoundTag.contains("is_open")) this.isOpen = true;

        if (this.getLevel() instanceof ServerLevel serverLevel && this.raidBoss != null) {
            Consumer<RaidBoss> consumer = boss -> {
                if (this.isRemoved()) return;
                RaidEvents.RAID_DEN_LOAD.emit(new RaidDenSpawnEvent(serverLevel, this.getBlockPos(), boss));
            };
            RaidBoss boss = this.getRaidBoss();
            if (boss == null) CustomRaidRegistries.RAID_REGISTRY.requestRaidBoss(this.raidBoss, consumer, serverLevel.getServer());
            else consumer.accept(boss);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
        compoundTag.putInt("raid_cleared", this.clears);
        if (this.lastReset != null) compoundTag.put("reset_context", this.lastReset.save(new CompoundTag()));

        if (this.uuid != null) compoundTag.putString("uuid", this.uuid.toString());
        if (this.raidBucket != null) compoundTag.putString("raid_bucket", this.raidBucket.toString());
        if (this.raidBoss != null) compoundTag.putString("raid_boss", this.raidBoss.toString());
        if (this.isOpen) compoundTag.putBoolean("is_open", true);

        if (this.getLevel() instanceof ServerLevel serverLevel && this.raidBoss != null) {
            Consumer<RaidBoss> consumer = boss -> {
                if (this.isRemoved()) return;
                RaidEvents.RAID_DEN_SAVE.emit(new RaidDenSpawnEvent(serverLevel, this.getBlockPos(), boss));
            };
            RaidBoss boss = this.getRaidBoss();
            if (boss == null) CustomRaidRegistries.RAID_REGISTRY.requestRaidBoss(this.raidBoss, consumer, serverLevel.getServer());
            else consumer.accept(boss);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        if (this.raidBoss != null) tag.putString("raid_boss", this.raidBoss.toString());
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GeoAnimatable>(this, "controller", state ->
            state.setAndContinue(RawAnimation.begin().thenLoop("animation.raid_den.sparkle")))
            .setCustomInstructionKeyframeHandler(event -> {
                RaidCrystalBlockEntity blockEntity = (RaidCrystalBlockEntity) event.getAnimatable();
                if (!CobblemonRaidDensClient.CLIENT_CONFIG.show_particles) return;
                else if (!blockEntity.isActive(blockEntity.getBlockState())) return;
                else if (!blockEntity.canGenerateBoss(blockEntity.getBlockState())) return;
                RaidCrystalSparkleRenderer.sparkle(this);
            })
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}
