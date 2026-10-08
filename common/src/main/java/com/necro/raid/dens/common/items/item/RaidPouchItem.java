package com.necro.raid.dens.common.items.item;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.data.raid.*;
import com.necro.raid.dens.common.events.OpenPouchEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.loot.context.RaidLootContexts;
import com.necro.raid.dens.common.registry.RaidRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RaidPouchItem extends Item {
    public RaidPouchItem() {
        super(new Properties().stacksTo(1).fireResistant().rarity(Rarity.UNCOMMON));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        RaidTier tier = itemStack.get(ModComponents.TIER_COMPONENT.value());
        String feature = itemStack.get(ModComponents.FEATURE_COMPONENT.value());
        RaidType type = itemStack.get(ModComponents.TYPE_COMPONENT.value());
        ResourceLocation bossId = itemStack.get(ModComponents.BOSS_COMPONENT.value());
        if (tier == null || feature == null || type == null || bossId == null) return InteractionResultHolder.fail(itemStack);
        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(itemStack, true);

        ItemStack snapshot = itemStack.copyWithCount(1);
        boolean applyBonus = Boolean.TRUE.equals(itemStack.get(ModComponents.BONUS_LOOT_COMPONENT.value()));
        player.getCooldowns().addCooldown(this, 2);

        RaidRegistry.requestRaidBoss(
            bossId,
            boss -> this.openPouch((ServerLevel) level, (ServerPlayer) player, interactionHand, snapshot, boss, tier, applyBonus),
            (id, error) -> this.onFailed((ServerPlayer) player),
            player.getServer()
        );

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    private void openPouch(ServerLevel level, ServerPlayer player, InteractionHand hand, ItemStack snapshot, RaidBoss boss, RaidTier tier, boolean applyBonus) {
        player.getCooldowns().removeCooldown(this);
        if (player.isRemoved() || !player.isAlive()) return;

        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.isEmpty() || !ItemStack.isSameItemSameComponents(itemStack, snapshot)) return;
        List<ItemStack> rewards = this.getRewardItems(itemStack, boss, tier, level, player, applyBonus);
        if (!RaidEvents.OPEN_POUCH.postWithResult(new OpenPouchEvent(player, itemStack, rewards))) return;

        for (ItemStack item : rewards) {
            if (!player.getInventory().add(item)) {
                ItemEntity itemEntity = player.drop(item, false);
                if (itemEntity == null) continue;
                itemEntity.setNoPickUpDelay();
                itemEntity.setTarget(player.getUUID());
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        player.awardStat(Stats.ITEM_USED.get(this));
        itemStack.consume(1, player);
    }

    private void onFailed(ServerPlayer player) {
        player.getCooldowns().removeCooldown(this);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        RaidTier tier = itemStack.get(ModComponents.TIER_COMPONENT.value());
        String feature = itemStack.get(ModComponents.FEATURE_COMPONENT.value());
        if (tier == null || feature == null) return;
        tooltip.add(Component.translatable(RaidFeature.getTranslatable(feature)).append(" | ").append(tier.getStars()));
    }

    private List<ItemStack> getRewardItems(ItemStack itemStack, @Nullable RaidBoss boss, RaidTier tier, ServerLevel level, Player player, boolean applyBonus) {
        List<ItemStack> rewards;
        BossLootTable lootTable = boss == null ? null : boss.getLootTable();
        if (lootTable != null && lootTable.replace()) rewards = new ArrayList<>();
        else rewards = this.getDefault(itemStack, tier, level, player, applyBonus);

        if (lootTable != null) rewards.addAll(boss.getRandomRewards(level, itemStack, player, applyBonus));
        return rewards;
    }

    private List<ItemStack> getDefault( ItemStack itemStack, RaidTier tier, ServerLevel level, Player player, boolean applyBonus) {
        return level.getServer().reloadableRegistries()
            .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, tier.getLootTableId())))
            .getRandomItems(
                new LootParams.Builder(level)
                    .withParameter(RaidLootContexts.RAID_POUCH, itemStack)
                    .withOptionalParameter(RaidLootContexts.BONUS_LOOT, applyBonus)
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                    .create(RaidLootContexts.RAID_POUCH_USE)
            );
    }
}
