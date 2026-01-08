package com.necro.raid.dens.common.blocks.entity;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.data.dimension.RaidRegion;
import com.necro.raid.dens.common.data.raid.*;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.events.RaidDenSpawnEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.events.SetRaidBossEvent;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.*;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.raids.helpers.RaidJoinHelper;
import com.necro.raid.dens.common.raids.helpers.RaidRegionHelper;
import com.necro.raid.dens.common.registry.RaidBucketRegistry;
import com.necro.raid.dens.common.registry.RaidRegistry;
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
    private int clears;
    private int inactiveTicks;
    private int soundTicks;

    private UUID uuid;
    private ResourceLocation raidBucket;
    private ResourceLocation raidBoss;

    private long lastReset;
    private boolean isOpen;
    private Boolean isShiny;
    private Consumer<ServerPlayer> aspectSync;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RaidCrystalBlockEntity(BlockEntityType<? extends RaidCrystalBlockEntity> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        this.soundTicks = 0;
        this.uuid = UUID.randomUUID();
        this.isOpen = false;
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (level.isClientSide()) return;

        boolean isIdle = this.isIdle();
        if (RaidHelper.hasRaidState(this.getUuid()) && isIdle) this.closeRaid();

        if (this.canGenerateBoss(blockState) && (this.raidBoss == null || !RaidRegistry.exists(this.raidBoss))) {
            this.generateRaidBoss(level, blockPos, blockState);
        }

        if (this.raidHost != null && isIdle) {
            if (++this.inactiveTicks > 2400) this.closeRaid();
        }
        else this.inactiveTicks = 0;

        if (!blockState.getValue(RaidCrystalBlock.CAN_RESET)) return;
        else if (CobblemonRaidDens.CONFIG.reset_time <= 0) return;
        else if (this.isInProgress()) return;

        long gameTime = level.getGameTime();
        if (this.lastReset == 0) this.lastReset = gameTime;
        else if (gameTime - this.lastReset > CobblemonRaidDens.CONFIG.reset_time * 20L) {
            this.generateRaidBoss(level, blockPos, blockState);
        }

        if (!this.isActive(blockState)) return;
        if (++this.soundTicks % 120 == 0) {
            level.playSound(null, blockPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.5f, 1.0f);
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

        this.setRaidBoss(raidBoss.getId(), level.getGameTime());

        level.setBlock(blockPos, blockState
            .setValue(RaidCrystalBlock.RAID_TIER, raidBoss.getTier())
            .setValue(RaidCrystalBlock.RAID_TYPE, raidBoss.getType())
            .setValue(RaidCrystalBlock.ACTIVE, true), 2);

        RaidEvents.RAID_DEN_SPAWN.emit(new RaidDenSpawnEvent((ServerLevel) level, blockPos, raidBoss));
    }

    public boolean spawnRaidBoss() {
        if (this.getLevel() == null) return false;
        RaidBoss raidBoss = this.getRaidBoss();
        RaidRegion region = RaidRegionHelper.getRegion(this.getUuid());
        ServerLevel level = ModDimensions.getRaidDimension(this.getLevel().getServer());
        if (raidBoss == null || region == null || level == null) {
            CobblemonRaidDens.LOGGER.error("Could not load Raid Boss {}", this.raidBoss);
            this.setRaidBoss(null, 0);
            return false;
        }

        region.placeStructure(level);

        PokemonEntity pokemonEntity = raidBoss.getBossEntity(level);
        pokemonEntity.setNoAi(true);
        pokemonEntity.setInvulnerable(true);
        ((IRaidAccessor) pokemonEntity).crd_setRaidId(this.getUuid());

        pokemonEntity.moveTo(region.getBossPos());
        level.addFreshEntity(pokemonEntity);

        if (pokemonEntity.getPokemon().getAbility().getName().equals("imposter") ||
            pokemonEntity.getPokemon().getMoveSet().getMoves().stream().anyMatch(move -> move.getName().equals("transform"))) {
            this.setAspectSync(player -> RaidDenNetworkMessages.RAID_ASPECT.accept(player, pokemonEntity));
        }

        if (CobblemonRaidDens.CONFIG.sync_rewards) {
            if (this.isShiny == null) this.isShiny = pokemonEntity.getPokemon().getShiny();
            else pokemonEntity.getPokemon().setShiny(this.isShiny);
        }

        RaidHelper.ACTIVE_RAIDS.put(this.getUuid(), new RaidInstance(pokemonEntity, this.raidHost));
        return true;
    }

    public void clearRaid() {
        this.clears++;
        this.isShiny = null;
        if (this.isAtMaxClears()) RaidHelper.resetClearedRaids(this.getUuid());
    }

    public void closeRaid() {
        if (this.getLevel() == null) return;
        ServerLevel level = ModDimensions.getRaidDimension(this.getLevel().getServer());
        if (level == null) return;

        RaidState raidState = RaidHelper.getRaidState(this.getUuid());
        if (raidState == RaidState.SUCCESS || CobblemonRaidDens.CONFIG.max_clears_include_fails) this.clearRaid();

        RaidRegion region = RaidRegionHelper.getRegion(this.getUuid());
        if (region != null) {
            level.getEntitiesOfClass(ServerPlayer.class, region.bound())
                .forEach(player -> {
                    RaidUtils.leaveRaid(player);
                    ((IRaidTeleporter) player).crd_returnHome();
                });
        }
        RaidRegionHelper.clearRegion(this.getUuid(), level);

        this.clearRaidHost();
        this.inactiveTicks = 0;
        this.getLevel().getChunkAt(this.getBlockPos()).setUnsaved(true);
        this.setChanged();
        this.isOpen = false;
        this.setAspectSync(null);
    }

    public UUID getRaidHost() {
        return this.raidHost;
    }

    public boolean canSetRaidHost() {
        return this.getRaidHost() == null;
    }

    public void setRaidHost(Player player) {
        this.raidHost = player.getUUID();
        this.setChanged();
    }

    public void clearRaidHost() {
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

    public int getPlayerCount() {
        RequestHandler handler = RaidHelper.REQUEST_QUEUE.get(this.raidHost);
        if (handler == null) return 0;
        return handler.getPlayerCount();
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

    public boolean isFull() {
        RaidBoss boss = this.getRaidBoss();
        if (boss == null) return true;
        int maxPlayers = CobblemonRaidDens.TIER_CONFIG.get(boss.getTier()).maxPlayers();
        return maxPlayers != -1 && this.getPlayerCount() >= maxPlayers;
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
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
        if (compoundTag.contains("raid_host_uuid")) this.raidHost = UUID.fromString(compoundTag.getString("raid_host_uuid"));
        this.clears = compoundTag.getInt("raid_cleared");
        this.lastReset = compoundTag.getLong("last_reset");
        this.inactiveTicks = compoundTag.getInt("raid_inactive_for");

        if (compoundTag.contains("uuid")) this.uuid = UUID.fromString(compoundTag.getString("uuid"));
        else this.uuid = UUID.randomUUID();
        if (compoundTag.contains("raid_bucket")) this.raidBucket = ResourceLocation.parse(compoundTag.getString("raid_bucket"));
        if (compoundTag.contains("raid_boss")) this.raidBoss = ResourceLocation.parse(compoundTag.getString("raid_boss"));
        if (compoundTag.contains("is_open")) this.isOpen = true;
        if (compoundTag.contains("is_shiny")) this.isShiny = compoundTag.getBoolean("is_shiny");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
        if (this.raidHost != null) compoundTag.putString("raid_host_uuid", this.raidHost.toString());

        compoundTag.putInt("raid_cleared", this.clears);
        compoundTag.putLong("last_reset", this.lastReset);
        compoundTag.putInt("raid_inactive_for", this.inactiveTicks);

        if (this.uuid != null) compoundTag.putString("uuid", this.uuid.toString());
        if (this.raidBucket != null) compoundTag.putString("raid_bucket", this.raidBucket.toString());
        if (this.raidBoss != null) compoundTag.putString("raid_boss", this.raidBoss.toString());
        if (this.isOpen) compoundTag.putBoolean("is_open", true);
        if (this.isShiny != null) compoundTag.putBoolean("is_shiny", this.isShiny);
    }

    public void setRaidBoss(ResourceLocation raidBoss, long gameTime) {
        RaidHelper.resetClearedRaids(this.getUuid());
        this.resetClears();
        this.inactiveTicks = 0;
        this.lastReset = gameTime;
        this.raidBoss = raidBoss;
        this.isShiny = null;
        this.setChanged();
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}
