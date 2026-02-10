package com.necro.raid.dens.common.compat.wthit;

import com.cobblemon.mod.common.item.PokemonItem;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.client.ClientRaidRegistry;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidFeature;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import mcp.mobius.waila.api.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RaidCrystalComponents implements IBlockComponentProvider {
    @Override
    public ITooltipComponent getIcon(IBlockAccessor accessor, IPluginConfig config) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (!(blockEntity instanceof RaidCrystalBlockEntity raidCrystal)) return null;
        RaidBoss raidBoss = ClientRaidRegistry.getRaidBoss(raidCrystal.getRaidBossLocation());
        if (raidBoss == null || raidBoss.getDisplaySpecies() == null) return null;

        ItemStack stack = PokemonItem.from(raidBoss.getDisplaySpecies(), raidBoss.getDisplayAspects(), 1, null);
        return new ScalableComponent(stack, 1.5f);
    }

    private ITooltipComponent getTeraTypeIcon(RaidType type) {
        String string = String.format("mega_showdown:textures/gui/summary/tera_types/%s.png", type.getSerializedName());
        return new TeraTypeComponent(ResourceLocation.parse(string), 32, 32);
    }

    private ITooltipComponent getElementalTypeIcon(RaidType type) {
        String string = "cobblemon:textures/gui/types_small.png";
        return new ElementalTypeComponent(ResourceLocation.parse(string), 324, 18, type);
    }

    @Override
    public void appendHead(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (!(blockEntity instanceof RaidCrystalBlockEntity)) return;
        BlockState blockState = accessor.getBlockState();
        RaidType type = blockState.getValue(RaidCrystalBlock.RAID_TYPE);

        ITooltipLine line = tooltip.getLine(WailaConstants.OBJECT_NAME_TAG);
        if (line == null) return;
        line.with(Component.literal(" "));

        if (type != RaidType.STELLAR){
            line.with(this.getElementalTypeIcon(type));
        }
        else if (ModCompat.MEGA_SHOWDOWN.isLoaded()) {
            line.with(this.getTeraTypeIcon(type));
        }
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (!(blockEntity instanceof RaidCrystalBlockEntity raidCrystal)) return;
        BlockState blockState = accessor.getBlockState();

        RaidBoss raidBoss = ClientRaidRegistry.getRaidBoss(raidCrystal.getRaidBossLocation());
        if (raidBoss == null || raidBoss.getDisplaySpecies() == null) return;

        RaidTier tier = blockState.getValue(RaidCrystalBlock.RAID_TIER);
        RaidFeature feature = raidBoss.getFeature();


        MutableComponent component = raidBoss.getDisplaySpecies().getTranslatedName();
        component.append(" | ").append(Component.translatable(feature.getTranslatable()));
        component.append(" | ").append(tier.getStars());
        tooltip.addLine(component);

        CompoundTag serverData = accessor.getData().raw();
        if (serverData.contains("player_count") || serverData.contains("next_reset")) {
            MutableComponent component1 = Component.empty();
            if (serverData.contains("player_count")) {
                component1.append(Component.translatable("jade.cobblemonraiddens.player_count", serverData.getInt("player_count")));
                if (serverData.contains("next_reset")) component1.append(" | ");
            }
            if (serverData.contains("next_reset")) component1.append(serverData.getString("next_reset"));
            if (!component1.equals(Component.empty())) tooltip.addLine(component1);
        }

        int catches = raidBoss.getMaxCatches();
        if (catches == 0) tooltip.addLine(new ScalableComponent(Component.translatable("jade.cobblemonraiddens.not_catchable").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY), 0.5f));
        else if (catches > 0) tooltip.addLine(new ScalableComponent(Component.translatable("jade.cobblemonraiddens.max_catches", catches).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY), 0.5f));
    }
}
