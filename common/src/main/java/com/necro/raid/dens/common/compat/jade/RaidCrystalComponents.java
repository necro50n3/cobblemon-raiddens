package com.necro.raid.dens.common.compat.jade;

import com.cobblemon.mod.common.item.PokemonItem;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.raids.RaidFeature;
import com.necro.raid.dens.common.raids.RaidTier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum RaidCrystalComponents implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        RaidCrystalBlockEntity blockEntity = (RaidCrystalBlockEntity) accessor.getBlockEntity();
        RaidBoss raidBoss = blockEntity.getRaidBoss();
        if (raidBoss == null) return currentIcon;

        if (raidBoss.getDisplayAspects() == null) raidBoss.createDisplayAspects();
        ItemStack stack = PokemonItem.from(raidBoss.getDisplaySpecies(), raidBoss.getDisplayAspects(), 1, null);
        return IElementHelper.get().item(stack, 1.5f);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        RaidCrystalBlockEntity blockEntity = (RaidCrystalBlockEntity) accessor.getBlockEntity();
        BlockState blockState = accessor.getBlockState();

        RaidBoss raidBoss = blockEntity.getRaidBoss();
        if (raidBoss == null) return;
        RaidTier tier = blockState.getValue(RaidCrystalBlock.RAID_TIER);
        RaidFeature feature = raidBoss.getFeature();

        if (raidBoss.getDisplayAspects() == null) raidBoss.createDisplayAspects();

        MutableComponent component = raidBoss.getDisplaySpecies().getTranslatedName();
        component.append(" | ").append(Component.translatable(feature.getTranslatable()));
        component.append(" | ").append(tier.getStars());

        tooltip.add(component, this.getUid());
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_crystal");
    }
}