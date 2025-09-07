package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.moves.MoveSet;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.megashowdown.RaidDensMSDCompat;
import com.necro.raid.dens.common.util.IHealthSetter;
import kotlin.Pair;
import kotlin.Unit;
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
    private final PokemonProperties baseProperties;
    private final PokemonProperties bossProperties;
    private Species displaySpecies;
    private Set<String> displayAspects;
    private final RaidTier raidTier;
    private final RaidFeature raidFeature;
    private final Pair<String, String> raidForm;
    private final RaidType raidType;
    private final List<ItemStack> bonusItems;
    private final int weight;

    public RaidBoss(PokemonProperties properties, RaidTier tier, RaidType raidType, RaidFeature raidFeature, Pair<String, String> raidForm, List<ItemStack> bonusItems, int weight) {
        this.bossProperties = properties;
        this.baseProperties = properties.copy();
        this.raidTier = tier;
        this.raidType = raidType;
        this.raidFeature = raidFeature;
        this.raidForm = raidForm;
        this.bonusItems = bonusItems;
        this.weight = weight;
    }

    public RaidBoss(PokemonProperties properties, RaidTier tier, RaidType raidType, RaidFeature raidFeature, Pair<String, String> raidForm, List<ItemStack> bonusItems) {
        this(properties, tier, raidType, raidFeature, raidForm, bonusItems, 10);
    }

    public RaidBoss(PokemonProperties properties, RaidTier tier, RaidType raidType, RaidFeature raidFeature, Pair<String, String> raidForm, int weight) {
        this(properties, tier, raidType, raidFeature, raidForm, new ArrayList<>(), weight);
    }

    public RaidBoss(PokemonProperties properties, RaidTier tier, RaidType raidType, RaidFeature raidFeature, Pair<String, String> raidForm) {
        this(properties, tier, raidType, raidFeature, raidForm, new ArrayList<>());
    }

    public RaidBoss(PokemonProperties baseProperties, PokemonProperties bossProperties, RaidTier tier, RaidType raidType, RaidFeature raidFeature, Pair<String, String> raidForm, List<ItemStack> bonusItems) {
        this.baseProperties = baseProperties;
        this.bossProperties = bossProperties;
        this.raidTier = tier;
        this.raidType = raidType;
        this.raidFeature = raidFeature;
        this.raidForm = raidForm;
        this.bonusItems = bonusItems;
        this.weight = 0;
    }

    public PokemonEntity getBossEntity(ServerLevel level) {
        PokemonProperties properties = PokemonProperties.Companion.parse(this.bossProperties.asString(" ") + " aspect=raid uncatchable");
        properties.setIvs(IVs.createRandomIVs(this.raidTier.getMaxIvs()));

        Pokemon pokemon = properties.create();
        ((IHealthSetter) pokemon).setMaxHealth(this.raidTier.getHealth() * pokemon.getMaxHealth());
        if (!this.raidForm.getFirst().isBlank() && !this.raidForm.getSecond().isBlank()) {
            new StringSpeciesFeature(this.raidForm.getFirst(), this.raidForm.getSecond()).apply(pokemon);
        }
        else if (this.raidFeature == RaidFeature.DYNAMAX) {
            new StringSpeciesFeature("dynamax_form", "none").apply(pokemon);
        }
        else if (this.raidFeature == RaidFeature.MEGA) {
            new StringSpeciesFeature("mega_evolution", "mega").apply(pokemon);
        }
        this.setMoveSet(properties, pokemon);

        PokemonEntity pokemonEntity = new PokemonEntity(level, pokemon, CobblemonEntities.POKEMON);
        pokemonEntity.setNoAi(true);
        pokemonEntity.setInvulnerable(true);
        pokemonEntity.setDrops(new DropTable());

        if (this.isTera() && ModCompat.MEGA_SHOWDOWN.isLoaded()) RaidDensMSDCompat.INSTANCE.setupTera(pokemonEntity, pokemon);
        else if (this.isDynamax() && ModCompat.MEGA_SHOWDOWN.isLoaded()) RaidDensMSDCompat.INSTANCE.setupDmax(pokemonEntity);

        return pokemonEntity;
    }

    public void createDisplayAspects() {
        Pokemon displayPokemon = this.baseProperties.create();
        this.displaySpecies = displayPokemon.getSpecies();
        this.displayAspects = displayPokemon.getAspects();
    }

    public Pokemon getRewardPokemon(ServerPlayer player) {
        PokemonProperties properties = this.baseProperties.copy();
        properties.setIvs(IVs.createRandomIVs(this.raidTier.getMaxIvs()));
        Pokemon pokemon = properties.create(player);
        if (this.isDynamax()) pokemon.setDmaxLevel(10);
        if (this.raidForm.getFirst().equals("dynamax_form") && this.raidForm.getSecond().equals("gmax")) pokemon.setGmaxFactor(true);
        this.setMoveSet(properties, pokemon);
        return pokemon;
    }

    private void setMoveSet(PokemonProperties properties, Pokemon pokemon) {
        List<String> moves = properties.getMoves();
        if (moves != null) {
            MoveSet moveSet = pokemon.getMoveSet();
            moveSet.clear();
            List<MoveTemplate> moveTemplates = moves.stream().map(Moves.INSTANCE::getByName).toList();
            moveSet.doWithoutEmitting(() -> {
                for (int i = 0; i < moves.size(); i++) {
                    MoveTemplate mt = moveTemplates.get(i);
                    moveSet.setMove(i, mt.create());
                    Objects.requireNonNull(moveSet.get(i)).update();
                }
                return Unit.INSTANCE;
            });
            moveSet.update();
        }
    }

    public PokemonProperties getProperties() {
        return this.bossProperties;
    }

    public Species getDisplaySpecies() {
        return this.displaySpecies;
    }

    public Set<String> getDisplayAspects() {
        return this.displayAspects;
    }

    public RaidTier getTier() {
        return this.raidTier;
    }

    public RaidType getType() {
        return this.raidType;
    }

    public RaidFeature getFeature() {
        return this.raidFeature;
    }

    public Pair<String, String> getRaidForm() {
        return this.raidForm;
    }

    public List<ItemStack> getBonusItems() {
        return this.bonusItems;
    }

    public int getWeight() {
        return this.weight;
    }

    public boolean isMega() {
        return this.raidFeature == RaidFeature.MEGA;
    }

    public boolean isTera() {
        return this.raidFeature == RaidFeature.TERA;
    }

    public boolean isDynamax() {
        return this.raidFeature == RaidFeature.DYNAMAX;
    }

    public CompoundTag saveNbt(CompoundTag tag) {
        tag.putString("base_properties", this.baseProperties.asString(" "));
        tag.putString("boss_properties", this.bossProperties.asString(" "));
        tag.putString("raid_tier", this.raidTier.getSerializedName());
        tag.putString("raid_type", this.raidType.getSerializedName());
        tag.putString("raid_feature", this.raidFeature.getSerializedName());

        CompoundTag raidFormTag = new CompoundTag();
        raidFormTag.putString("name", this.raidForm.getFirst());
        raidFormTag.putString("value", this.raidForm.getSecond());
        tag.put("raid_form", raidFormTag);

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
        Pair<String, String> raidForm;
        if (tag.contains("raid_form")) {
            CompoundTag raidFormTag = tag.getCompound("raid_form");
            raidForm = new Pair<>(raidFormTag.getString("name"), raidFormTag.getString("value"));
        }
        else raidForm = new Pair<>("", "");

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
            RaidTier.fromString(tag.getString("raid_tier").toUpperCase()),
            RaidType.fromString(tag.getString("raid_type").toUpperCase()),
            RaidFeature.fromString(tag.getString("raid_feature").toUpperCase()),
            raidForm, bonusItems
        );
    }

    public static Optional<Boolean> getShiny(PokemonProperties properties) {
        return Optional.ofNullable(properties.getShiny());
    }

    public static String getGender(PokemonProperties properties) {
        if (properties.getGender() == null) return "";
        else return properties.getGender().getSerializedName();
    }

    public static String getFormName(Pair<String, String> raidForm) {
        if (raidForm == null) return "";
        else return raidForm.getFirst();
    }

    public static String getFormValue(Pair<String, String> raidForm) {
        if (raidForm == null) return "";
        else return raidForm.getSecond();
    }

    public static Codec<PokemonProperties> propertiesCodec() {
        return RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("species").forGetter(PokemonProperties::getSpecies),
            Codec.STRING.optionalFieldOf("form", "").forGetter(PokemonProperties::getForm),
            Codec.STRING.optionalFieldOf("gender", "").forGetter(RaidBoss::getGender),
            Codec.STRING.optionalFieldOf("ability", "").forGetter(PokemonProperties::getAbility),
            Codec.STRING.optionalFieldOf("nature", "").forGetter(PokemonProperties::getNature),
            Codec.optionalField("shiny", Codec.BOOL, true).forGetter(RaidBoss::getShiny),
            Codec.STRING.listOf().optionalFieldOf("moves", new ArrayList<>()).forGetter(PokemonProperties::getMoves)
            ).apply(inst, (species, form, gender, ability, nature, shiny, moves) -> {
                PokemonProperties properties = PokemonProperties.Companion.parse("");
                properties.setSpecies(species);
                if (!form.isBlank()) properties.setForm(form);
                try { if (!gender.isBlank()) properties.setGender(Gender.valueOf(gender)); }
                catch (IllegalArgumentException ignored) {}
                if (!ability.isBlank()) properties.setAbility(ability);
                if (!nature.isBlank()) properties.setNature(nature);
                shiny.ifPresent(properties::setShiny);
                if (!moves.isEmpty()) properties.setMoves(moves);
                return properties;
            })
        );
    }

    public static Codec<Pair<String, String>> raidFormCodec() {
        return RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("name").forGetter(RaidBoss::getFormName),
            Codec.STRING.fieldOf("value").forGetter(RaidBoss::getFormValue)
        ).apply(inst,Pair::new));
    }

    public static Codec<RaidBoss> codec() {
        return RecordCodecBuilder.create(inst -> inst.group(
            propertiesCodec().fieldOf("pokemon").forGetter(RaidBoss::getProperties),
            RaidTier.codec().fieldOf("raid_tier").forGetter(RaidBoss::getTier),
            RaidType.codec().fieldOf("raid_type").forGetter(RaidBoss::getType),
            RaidFeature.codec().optionalFieldOf("raid_feature", RaidFeature.DEFAULT).forGetter(RaidBoss::getFeature),
            raidFormCodec().optionalFieldOf("raid_form", new Pair<>("", "")).forGetter(RaidBoss::getRaidForm),
            ItemStack.CODEC.listOf().optionalFieldOf("bonus_items", new ArrayList<>()).forGetter(RaidBoss::getBonusItems),
            Codec.INT.optionalFieldOf("weight", 19).forGetter(RaidBoss::getWeight)
            ).apply(inst, (properties, tier, type, feature, form, bonusItems, weight) -> {
                properties.setLevel(tier.getLevel());
                properties.setTeraType(type.getSerializedName());
                return new RaidBoss(properties, tier, type, feature, form, bonusItems, weight);
            })
        );
    }
}
