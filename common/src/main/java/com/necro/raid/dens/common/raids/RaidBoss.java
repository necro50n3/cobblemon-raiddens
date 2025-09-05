package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.util.IHealthSetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class RaidBoss {
    private static final String MEGA = "mega_evolution=mega";
    private static final String MEGA_X = "mega_evolution=mega_x";
    private static final String MEGA_Y = "mega_evolution=mega_y";
    private static final String TERA = "tera_form=terastal-form";
    private static final String STELLAR = "tera_form=stellar-form";
    private static final String GMAX = "dynamax_form=gmax";
    private static final String ETERNAMAX = "dynamax_form=eternamax";

    private final PokemonProperties baseProperties;
    private final PokemonProperties bossProperties;
    private String displaySpecies;
    private Set<String> displayAspects;
    private final RaidTier raidTier;
    private final RaidFeature raidFeature;
    private RaidType raidType;
    private final List<ItemStack> bonusItems;
    private final int weight;

    public RaidBoss(PokemonProperties properties, RaidTier tier, RaidType raidType, List<ItemStack> bonusItems, int weight) {
        this.raidTier = tier;
        this.raidType = raidType;
        this.bonusItems = bonusItems;
        this.weight = weight;

        properties.setLevel(tier.getLevel());
        this.bossProperties = properties;
        this.baseProperties = properties.copy();
        if (this.isMega()) this.raidFeature = RaidFeature.MEGA;
        else if (this.isTera()) this.raidFeature = RaidFeature.TERA;
        else if (this.isDynamax()) this.raidFeature = RaidFeature.DYNAMAX;
        else this.raidFeature = RaidFeature.DEFAULT;

        Set<String> baseAspects = new HashSet<>(this.baseProperties.getAspects());
        baseAspects.remove(MEGA.split("=")[1]);
        baseAspects.remove(MEGA_X.split("=")[1]);
        baseAspects.remove(MEGA_Y.split("=")[1]);
        baseAspects.remove(TERA.split("=")[1]);
        baseAspects.remove(STELLAR.split("=")[1]);
        baseAspects.remove(GMAX.split("=")[1]);
        baseAspects.remove(ETERNAMAX.split("=")[1]);
        this.baseProperties.setAspects(baseAspects);
        this.baseProperties.setCustomProperties(new ArrayList<>());
    }

    public RaidBoss(PokemonProperties properties, RaidTier tier, RaidType raidType, List<ItemStack> bonusItems) {
        this(properties, tier, raidType, bonusItems, 10);
    }

    public RaidBoss(PokemonProperties properties, RaidTier tier, RaidType raidType, int weight) {
        this(properties, tier, raidType, new ArrayList<>(), weight);
    }

    public RaidBoss(PokemonProperties properties, RaidTier tier, RaidType raidType) {
        this(properties, tier, raidType, new ArrayList<>());
    }

    public RaidBoss(PokemonProperties baseProperties, PokemonProperties bossProperties, RaidTier tier, RaidType raidType, List<ItemStack> bonusItems) {
        this.baseProperties = baseProperties;
        this.bossProperties = bossProperties;
        this.raidTier = tier;
        if (this.isMega()) this.raidFeature = RaidFeature.MEGA;
        else if (this.isTera()) this.raidFeature = RaidFeature.TERA;
        else if (this.isDynamax()) this.raidFeature = RaidFeature.DYNAMAX;
        else this.raidFeature = RaidFeature.DEFAULT;
        this.raidType = raidType;
        this.bonusItems = bonusItems;
        this.weight = 0;
    }

    public PokemonEntity getBossEntity(ServerLevel level) {
        PokemonProperties properties = PokemonProperties.Companion.parse(this.bossProperties.asString(" ") + " aspect=raid uncatchable");
        properties.setIvs(IVs.createRandomIVs(this.raidTier.getMaxIvs()));
        Pokemon pokemon = properties.create();
        ((IHealthSetter) pokemon).setMaxHealth(this.raidTier.getHealth() * pokemon.getMaxHealth());
        PokemonEntity pokemonEntity = new PokemonEntity(level, pokemon, CobblemonEntities.POKEMON);
        pokemonEntity.setNoAi(true);
        pokemonEntity.setInvulnerable(true);
        pokemonEntity.setDrops(new DropTable());
        if (this.raidType == null) {
            this.raidType = RaidType.fromString(pokemonEntity.getPokemon().getTeraType().showdownId());
        }
        return pokemonEntity;
    }

    public void createDisplayAspects() {
        Pokemon displayPokemon = this.baseProperties.create();
        this.displaySpecies = displayPokemon.getSpecies().toString();
        this.displayAspects = displayPokemon.getAspects();
    }

    public Pokemon getRewardPokemon(ServerPlayer player) {
        PokemonProperties properties = this.baseProperties.copy();
        properties.setIvs(IVs.createRandomIVs(this.raidTier.getMaxIvs()));
        return properties.create(player);
    }

    public PokemonProperties getProperties() {
        return this.bossProperties;
    }

    public String getDisplaySpecies() {
        return this.displaySpecies;
    }

    public Set<String> getDisplayAspects() {
        return this.displayAspects;
    }

    public RaidTier getTier() {
        return this.raidTier;
    }

    public RaidFeature getFeature() {
        return this.raidFeature;
    }

    public RaidType getType() {
        return this.raidType;
    }

    public List<ItemStack> getBonusItems() {
        return this.bonusItems;
    }

    public int getWeight() {
        return this.weight;
    }

    public boolean isMega() {
        Set<String> aspects = this.bossProperties.getAspects();
        return aspects.contains(MEGA.split("=")[1]) || aspects.contains(MEGA_X.split("=")[1]) || aspects.contains(MEGA_Y.split("=")[1]);
    }

    public boolean isTera() {
        Set<String> aspects = this.bossProperties.getAspects();
        return aspects.contains(TERA.split("=")[1]) || aspects.contains(STELLAR.split("=")[1]);
    }

    public boolean isDynamax() {
        Set<String> aspects = this.bossProperties.getAspects();
        return aspects.contains(GMAX.split("=")[1]) || aspects.contains(ETERNAMAX.split("=")[1]);
    }

    public CompoundTag saveNbt(CompoundTag tag) {
        tag.putString("base_properties", this.baseProperties.asString(" "));
        tag.putString("boss_properties", this.bossProperties.asString(" "));
        tag.putString("raid_tier", this.raidTier.getSerializedName());
        tag.putString("raid_type", this.raidType.getSerializedName());
        ListTag bonusItemsTag = new ListTag();
        this.bonusItems.forEach(item -> {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putString("item_name", item.getItem().toString());
            itemTag.putInt("item_count", item.getCount());
            bonusItemsTag.add(itemTag);
        });
        tag.put("bonus_items", bonusItemsTag);
        return tag;
    }

    public static RaidBoss loadNbt(CompoundTag tag) {
        List<ItemStack> bonusItems = new ArrayList<>();
        if (tag.contains("bonus_items")) {
            ListTag listTag = tag.getList("bonus_items", Tag.TAG_COMPOUND);
            for (Tag t : listTag) {
                CompoundTag itemTag = (CompoundTag) t;
                bonusItems.add(new ItemStack(
                    BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemTag.getString("item_name"))),
                    itemTag.getInt("item_count")
                ));
            }
        }
        return new RaidBoss(
            PokemonProperties.Companion.parse(tag.getString("base_properties")),
            PokemonProperties.Companion.parse(tag.getString("boss_properties")),
            RaidTier.valueOf(tag.getString("raid_tier").toUpperCase()),
            RaidType.valueOf(tag.getString("raid_type").toUpperCase()),
            bonusItems
        );
    }

    public static String getGender(PokemonProperties properties) {
        if (properties.getGender() == null) return "";
        else return properties.getGender().getSerializedName();
    }

    public static Codec<PokemonProperties> propertiesCodec() {
        return RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("species").forGetter(PokemonProperties::getSpecies),
            Codec.STRING.optionalFieldOf("form", "").forGetter(PokemonProperties::getForm),
            Codec.STRING.optionalFieldOf("gender", "").forGetter(RaidBoss::getGender),
            Codec.STRING.optionalFieldOf("ability", "").forGetter(PokemonProperties::getAbility),
            Codec.STRING.optionalFieldOf("nature", "").forGetter(PokemonProperties::getNature),
            Codec.STRING.optionalFieldOf("tera_type", "").forGetter(PokemonProperties::getTeraType),
            Codec.STRING.listOf().optionalFieldOf("moves", new ArrayList<>()).forGetter(PokemonProperties::getMoves),
            PokemonProperties.getCUSTOM_PROPERTIES_CODEC().optionalFieldOf("custom_properties", new ArrayList<>()).forGetter(PokemonProperties::getCustomProperties)
            ).apply(inst, (species, form, gender, ability, nature, tera_type, moves, customProperties) -> {
                PokemonProperties properties = PokemonProperties.Companion.parse("");
                properties.setSpecies(species);
                if (!form.isBlank()) properties.setForm(form);
                try { if (!gender.isBlank()) properties.setGender(Gender.valueOf(gender)); }
                catch (IllegalArgumentException ignored) {}
                if (!ability.isBlank()) properties.setAbility(ability);
                if (!nature.isBlank()) properties.setNature(nature);
                if (!tera_type.isBlank()) properties.setTeraType(tera_type);
                if (!moves.isEmpty()) properties.setMoves(moves);
                if (!customProperties.isEmpty()) properties.setCustomProperties(customProperties);
                return properties;
            })
        );
    }

    public static Codec<RaidBoss> codec() {
        return RecordCodecBuilder.create(inst -> inst.group(
            propertiesCodec().fieldOf("pokemon").forGetter(RaidBoss::getProperties),
            RaidTier.codec().fieldOf("raid_tier").forGetter(RaidBoss::getTier),
            RaidType.codec().fieldOf("raid_type").forGetter(RaidBoss::getType),
            ItemStack.CODEC.listOf().optionalFieldOf("bonus_items", new ArrayList<>()).forGetter(RaidBoss::getBonusItems),
            Codec.INT.optionalFieldOf("weight", 10).forGetter(RaidBoss::getWeight)
            ).apply(inst, RaidBoss::new)
        );
    }
}
