package com.necro.raid.dens.common.compat.jade;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.util.ResourceLocationExtensionsKt;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.raids.RaidFeature;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.raids.RaidType;
import com.necro.raid.dens.common.util.RaidRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum RaidCrystalComponents implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @SuppressWarnings("ConstantConditions")
    private IElement getIconClient(BlockAccessor accessor, IElement currentIcon) {
        RaidCrystalBlockEntity blockEntity = (RaidCrystalBlockEntity) accessor.getBlockEntity();
        if (RaidRegistry.REGISTRY == null) RaidRegistry.REGISTRY = blockEntity.getLevel().registryAccess().registryOrThrow(RaidRegistry.RAID_BOSS_KEY);
        RaidBoss raidBoss = RaidRegistry.REGISTRY.get(blockEntity.getRaidBossLocation());
        if (raidBoss == null) return currentIcon;

        if (raidBoss.getDisplayAspects() == null) raidBoss.createDisplayAspects();
        ItemStack stack = PokemonItem.from(raidBoss.getDisplaySpecies(), raidBoss.getDisplayAspects(), 1, null);
        return IElementHelper.get().item(stack, 1.5f);
    }

    @Override
    public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        if (!accessor.isServerConnected()) return this.getIconClient(accessor, currentIcon);

        CompoundTag serverData = accessor.getServerData();
        if (!serverData.contains("boss_aspects") || !serverData.contains("boss_species")) return currentIcon;

        IElementHelper elements = IElementHelper.get();
        Species species = PokemonSpecies.INSTANCE.getByIdentifier(ResourceLocationExtensionsKt.asIdentifierDefaultingNamespace(serverData.getString("boss_species"), Cobblemon.MODID));
        if (species == null) return currentIcon;

        Set<String> aspects = new HashSet<>();
        ListTag listTag = serverData.getList("boss_aspects", StringTag.TAG_STRING);
        for (Tag t : listTag) {
            StringTag tag = (StringTag) t;
            aspects.add(tag.getAsString());
        }
        ItemStack stack = PokemonItem.from(species, aspects, 1, null);
        return elements.item(stack, 1.5f);
    }

    private IElement getTeraTypeIcon(RaidType type) {
        String string = String.format("mega_showdown:textures/gui/summary/tera_types/%s.png", type.getSerializedName());
        return new TeraTypeElement(ResourceLocation.parse(string), 32, 32);
    }

    private IElement getElementalTypeIcon(RaidType type) {
        String string = "cobblemon:textures/gui/types_small.png";
        return new ElementalTypeElement(ResourceLocation.parse(string), 324, 18, type);
    }

    @SuppressWarnings("ConstantConditions")
    private void appendTooltipClient(ITooltip tooltip, BlockAccessor accessor) {
        RaidCrystalBlockEntity blockEntity = (RaidCrystalBlockEntity) accessor.getBlockEntity();
        BlockState blockState = accessor.getBlockState();

        if (RaidRegistry.REGISTRY == null) RaidRegistry.REGISTRY = blockEntity.getLevel().registryAccess().registryOrThrow(RaidRegistry.RAID_BOSS_KEY);
        RaidBoss raidBoss = RaidRegistry.REGISTRY.get(blockEntity.getRaidBossLocation());
        if (raidBoss == null) return;
        RaidTier tier = blockState.getValue(RaidCrystalBlock.RAID_TIER);
        RaidType type = blockState.getValue(RaidCrystalBlock.RAID_TYPE);
        RaidFeature feature = raidBoss.getFeature();

        if (raidBoss.getDisplayAspects() == null) raidBoss.createDisplayAspects();

        MutableComponent component = raidBoss.getDisplaySpecies().getTranslatedName();
        component.append(" | ").append(Component.translatable(feature.getTranslatable()));
        component.append(" | ").append(tier.getStars());
        tooltip.add(component, this.getUid());
        List<IElement> elements = new ArrayList<>();
        elements.add(IElementHelper.get().text(Component.literal(" ")));
        if (type != RaidType.STELLAR){
            elements.add(this.getElementalTypeIcon(type));
            tooltip.append(0, elements);
        }
        else if (ModCompat.MEGA_SHOWDOWN.isLoaded()) {
            elements.add(this.getTeraTypeIcon(type));
            tooltip.append(0, elements);
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!accessor.isServerConnected()) {
            this.appendTooltipClient(tooltip, accessor);
            return;
        }

        CompoundTag serverData = accessor.getServerData();
        if (!serverData.contains("raid_boss")) return;
        MutableComponent component = Component.translatable(serverData.getString("raid_boss"));

        if (serverData.contains("raid_feature")) {
            component.append(" | ").append(Component.translatable(serverData.getString("raid_feature")));
        }

        BlockState blockState = accessor.getBlockState();
        RaidTier tier = blockState.getValue(RaidCrystalBlock.RAID_TIER);
        component.append(" | ").append(tier.getStars());

        RaidType type = blockState.getValue(RaidCrystalBlock.RAID_TYPE);
        tooltip.add(component, this.getUid());
        List<IElement> elements = new ArrayList<>();
        elements.add(IElementHelper.get().text(Component.literal(" ")));
        if (type != RaidType.STELLAR){
            elements.add(this.getElementalTypeIcon(type));
            tooltip.append(0, elements);
        }
        else if (ModCompat.MEGA_SHOWDOWN.isLoaded()) {
            elements.add(this.getTeraTypeIcon(type));
            tooltip.append(0, elements);
        }

        MutableComponent component1 = Component.empty();
        if (serverData.contains("player_count")) {
            component1.append(Component.translatable("jade.cobblemonraiddens.player_count", serverData.getInt("player_count")));
            component1.append(" | ");
        }
        if (serverData.contains("next_reset")) component1.append(serverData.getString("next_reset"));
        if (!component1.equals(Component.empty())) tooltip.add(component1);
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        RaidCrystalBlockEntity blockEntity = (RaidCrystalBlockEntity) blockAccessor.getBlockEntity();
        if (blockEntity == null) return;
        RaidBoss raidBoss = blockEntity.getRaidBoss();
        if (raidBoss == null) return;
        if (raidBoss.getDisplaySpecies() == null) raidBoss.createDisplayAspects();

        Species species = raidBoss.getDisplaySpecies();
        String translatable = String.format("%s.species.%s.name", species.getResourceIdentifier().getNamespace(), species.showdownId());
        compoundTag.putString("raid_boss", translatable);
        compoundTag.putString("raid_feature", raidBoss.getFeature().getTranslatable());

        compoundTag.putString("boss_species", species.getResourceIdentifier().toString());
        ListTag bossAspects = new ListTag();
        for (String aspect : raidBoss.getDisplayAspects()) {
            if (aspect == null) continue;
            bossAspects.add(StringTag.valueOf(aspect));
        }
        for (SpeciesFeature form : raidBoss.getRaidForm()) {
            String aspect;
            if (form instanceof StringSpeciesFeature) aspect = ((StringSpeciesFeature) form).getValue();
            else if (form instanceof FlagSpeciesFeature) aspect = form.getName();
            else continue;
            bossAspects.add(StringTag.valueOf(aspect));
        }
        compoundTag.put("boss_aspects", bossAspects);

        if (blockEntity.getPlayerCount() > 0) compoundTag.putInt("player_count", blockEntity.getPlayerCount());
        if (blockEntity.getTicksUntilNextReset() > 0) compoundTag.putString("next_reset", this.formatTicks(blockEntity.getTicksUntilNextReset()));
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