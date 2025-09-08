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
import com.necro.raid.dens.common.raids.RaidBoss;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import java.util.HashSet;
import java.util.Set;

public enum RaidCrystalComponents implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
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

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!accessor.getServerData().contains("raid_boss")) return;
        MutableComponent component = Component.translatable(accessor.getServerData().getString("raid_boss"));

        if (accessor.getServerData().contains("raid_feature")) {
            component.append(" | ").append(Component.translatable(accessor.getServerData().getString("raid_feature")));
        }
        if (accessor.getServerData().contains("raid_tier")) {
            component.append(" | ").append(accessor.getServerData().getString("raid_tier"));
        }
        tooltip.add(component, this.getUid());
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
        compoundTag.putString("raid_tier", blockEntity.getBlockState().getValue(RaidCrystalBlock.RAID_TIER).getStars());

        compoundTag.putString("boss_species", species.getResourceIdentifier().toString());
        ListTag bossAspects = new ListTag();
        for (String aspect : raidBoss.getDisplayAspects()) {
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
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_crystal");
    }
}