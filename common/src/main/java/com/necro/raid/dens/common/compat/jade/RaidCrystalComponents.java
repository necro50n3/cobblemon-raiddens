package com.necro.raid.dens.common.compat.jade;

import com.cobblemon.mod.common.item.PokemonItem;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.client.ClientRaidRegistry;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidFeature;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import java.util.ArrayList;
import java.util.List;

public enum RaidCrystalComponents implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (!(blockEntity instanceof RaidCrystalBlockEntity raidCrystal)) return currentIcon;
        RaidBoss raidBoss = ClientRaidRegistry.getRaidBoss(raidCrystal.getRaidBossLocation());
        if (raidBoss == null) return currentIcon;

        if (raidBoss.getDisplaySpecies() == null && raidBoss.getReward() != null) raidBoss.createDisplayAspects();
        else if (raidBoss.getDisplaySpecies() == null) return currentIcon;

        ItemStack stack = PokemonItem.from(raidBoss.getDisplaySpecies(), raidBoss.getDisplayAspects(), 1, null);
        return IElementHelper.get().item(stack, 1.5f);
    }

    private IElement getTeraTypeIcon(RaidType type) {
        String string = String.format("mega_showdown:textures/gui/summary/tera_types/%s.png", type.getSerializedName());
        return new TeraTypeElement(ResourceLocation.parse(string), 32, 32);
    }

    private IElement getElementalTypeIcon(RaidType type) {
        String string = "cobblemon:textures/gui/types_small.png";
        return new ElementalTypeElement(ResourceLocation.parse(string), 324, 18, type);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (!(blockEntity instanceof RaidCrystalBlockEntity raidCrystal)) return;
        BlockState blockState = accessor.getBlockState();

        RaidBoss raidBoss = ClientRaidRegistry.getRaidBoss(raidCrystal.getRaidBossLocation());
        if (raidBoss == null) return;
        if (raidBoss.getDisplaySpecies() == null && raidBoss.getReward() != null) raidBoss.createDisplayAspects();
        if (raidBoss.getDisplaySpecies() == null) return;

        RaidTier tier = blockState.getValue(RaidCrystalBlock.RAID_TIER);
        RaidType type = blockState.getValue(RaidCrystalBlock.RAID_TYPE);
        RaidFeature feature = raidBoss.getFeature();


        MutableComponent component = raidBoss.getDisplaySpecies().getTranslatedName();
        component.append(" | ").append(Component.translatable(feature.getTranslatable()));
        component.append(" | ").append(tier.getStars());
        tooltip.add(component, this.getUid());

        IElementHelper helper = IElementHelper.get();
        List<IElement> elements = new ArrayList<>();
        elements.add(helper.text(Component.literal(" ")));
        if (type != RaidType.STELLAR){
            elements.add(this.getElementalTypeIcon(type));
            tooltip.append(0, elements);
        }
        else if (ModCompat.MEGA_SHOWDOWN.isLoaded()) {
            elements.add(this.getTeraTypeIcon(type));
            tooltip.append(0, elements);
        }

        if (accessor.isServerConnected()) {
            CompoundTag serverData = accessor.getServerData();
            MutableComponent component1 = Component.empty();
            if (serverData.contains("player_count")) {
                component1.append(Component.translatable("jade.cobblemonraiddens.player_count", serverData.getInt("player_count")));
                if (serverData.contains("next_reset")) component1.append(" | ");
            }
            if (serverData.contains("next_reset")) component1.append(serverData.getString("next_reset"));
            if (!component1.equals(Component.empty())) tooltip.add(component1);
        }

        int catches = raidBoss.getMaxCatches();
        if (catches == 0) tooltip.add(helper.text(Component.translatable("jade.cobblemonraiddens.not_catchable").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY)).scale(0.5f));
        else if (catches > 0) tooltip.add(helper.text(Component.translatable("jade.cobblemonraiddens.max_catches", catches).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY)).scale(0.5f));
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor accessor) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (!(blockEntity instanceof RaidCrystalBlockEntity raidCrystal)) return;

        if (raidCrystal.getPlayerCount() > 0) compoundTag.putInt("player_count", raidCrystal.getPlayerCount());
        if (raidCrystal.getTicksUntilNextReset() > 0) compoundTag.putString("next_reset", this.formatTicks(raidCrystal.getTicksUntilNextReset()));
    }

    private String formatTicks(long ticks) {
        int totalSeconds = (int) ticks / 20;

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_crystal");
    }
}