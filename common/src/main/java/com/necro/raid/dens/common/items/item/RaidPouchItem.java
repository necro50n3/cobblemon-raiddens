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
        RaidFeature feature = itemStack.get(ModComponents.FEATURE_COMPONENT.value());
        RaidType raidType = itemStack.get(ModComponents.TYPE_COMPONENT.value());
        RaidBoss boss = RaidRegistry.getRaidBoss(itemStack.get(ModComponents.BOSS_COMPONENT.value()));
        if (tier == null || feature == null || raidType == null) return InteractionResultHolder.fail(itemStack);

        if (!level.isClientSide) {
            List<ItemStack> rewards = this.getRewardItems(itemStack, boss, tier, (ServerLevel) level, player);
            if (!RaidEvents.OPEN_POUCH.postWithResult(new OpenPouchEvent((ServerPlayer) player, itemStack, rewards))) {
                return InteractionResultHolder.fail(itemStack);
            }

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

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        RaidTier tier = itemStack.get(ModComponents.TIER_COMPONENT.value());
        RaidFeature feature = itemStack.get(ModComponents.FEATURE_COMPONENT.value());
        if (tier == null || feature == null) return;
        tooltip.add(Component.translatable(feature.getTranslatable()).append(" | ").append(tier.getStars()));
    }

    private List<ItemStack> getRewardItems(ItemStack itemStack, @Nullable RaidBoss boss, RaidTier tier, ServerLevel level, Player player) {
        List<ItemStack> rewards;
        BossLootTable lootTable = boss == null ? null : boss.getLootTable();
        if (lootTable != null && lootTable.replace()) rewards = new ArrayList<>();
        else rewards = this.getDefault(itemStack, tier, level, player);

        if (lootTable != null) rewards.addAll(boss.getRandomRewards(level, itemStack, player));
        return rewards;
    }

    private List<ItemStack> getDefault( ItemStack itemStack, RaidTier tier, ServerLevel level, Player player) {
        return level.getServer().reloadableRegistries()
            .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, tier.getLootTableId())))
            .getRandomItems(
                new LootParams.Builder(level)
                    .withParameter(RaidLootContexts.RAID_POUCH, itemStack)
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                    .create(RaidLootContexts.RAID_POUCH_USE)
            );
    }
}
