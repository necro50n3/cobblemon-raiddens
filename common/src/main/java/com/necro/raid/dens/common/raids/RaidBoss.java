package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.moves.MoveSet;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.feature.*;
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.megashowdown.RaidDensMSDCompat;
import com.necro.raid.dens.common.util.IHealthSetter;
import com.necro.raid.dens.common.util.RaidUtils;
import kotlin.Unit;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.*;

public class RaidBoss {
    private final PokemonProperties baseProperties;
    private final PokemonProperties bossProperties;
    private Species displaySpecies;
    private Set<String> displayAspects;
    private final RaidTier raidTier;
    private final RaidFeature raidFeature;
    private final List<SpeciesFeature> raidForm;
    private final List<SpeciesFeature> baseForm;
    private final RaidType raidType;
    private final String lootTableId;
    private LootTable lootTable;
    private final double weight;
    private final boolean isCatchable;
    private final float shinyRate;

    public RaidBoss(PokemonProperties properties, RaidTier tier, RaidType raidType, RaidFeature raidFeature,
                    List<SpeciesFeature> raidForm, List<SpeciesFeature> baseForm, String lootTableId,
                    double weight, boolean isCatchable, float shinyRate) {
        this.bossProperties = properties;
        this.baseProperties = properties.copy();
        this.raidTier = tier;
        this.raidType = raidType;
        this.raidFeature = raidFeature;
        this.raidForm = raidForm;
        this.baseForm = baseForm;
        this.lootTableId = lootTableId;
        this.weight = weight;
        this.isCatchable = isCatchable;
        this.shinyRate = shinyRate;
    }

    public RaidBoss(PokemonProperties baseProperties, PokemonProperties bossProperties, RaidTier tier,
                    RaidType raidType, RaidFeature raidFeature, List<SpeciesFeature> raidForm,
                    List<SpeciesFeature> baseForm, String lootTableId, boolean isCatchable, float shinyRate) {
        this.baseProperties = baseProperties;
        this.bossProperties = bossProperties;
        this.raidTier = tier;
        this.raidType = raidType;
        this.raidFeature = raidFeature;
        this.raidForm = raidForm;
        this.baseForm = baseForm;
        this.lootTableId = lootTableId;
        this.weight = 0;
        this.isCatchable = isCatchable;
        this.shinyRate = shinyRate;
    }

    public PokemonEntity getBossEntity(ServerLevel level) {
        PokemonProperties properties = PokemonProperties.Companion.parse(this.bossProperties.asString(" ") + " aspect=raid uncatchable");

        Pokemon pokemon = properties.create();
        ((IHealthSetter) pokemon).setMaxHealth(this.raidTier.getHealth() * pokemon.getMaxHealth());

        for (SpeciesFeature form : this.raidForm) {
            ((CustomPokemonProperty) form).apply(pokemon);
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
        Pokemon pokemon = properties.create(player);

        for (SpeciesFeature form : this.baseForm) {
            ((CustomPokemonProperty) form).apply(pokemon);
        }

        if (this.isDynamax()) pokemon.setDmaxLevel(10);
        if (this.raidForm.stream().anyMatch(form -> form instanceof StringSpeciesFeature ssf && ssf.getValue().equals("gmax")))
            pokemon.setGmaxFactor(true);

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

    public List<SpeciesFeature> getRaidForm() {
        return this.raidForm;
    }

    public List<SpeciesFeature> getBaseForm() {
        return this.baseForm;
    }

    public String getLootTableId() {
        return this.lootTableId;
    }

    public List<ItemStack> getRandomRewards(ServerLevel level) {
        if (this.lootTable == null) {
            this.lootTable = level.getServer().reloadableRegistries().getLootTable(
                ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(this.lootTableId))
            );
        }
        return this.lootTable.getRandomItems(new LootParams.Builder(level).create(LootContextParamSet.builder().build()));
    }

    public double getWeight() {
        return this.weight;
    }

    public boolean isCatchable() {
        return this.isCatchable;
    }

    public float getShinyRate() {
        return this.shinyRate;
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

        ListTag raidFormTag = new ListTag();
        for (SpeciesFeature form : this.raidForm) {
            raidFormTag.add(RaidBoss.encodeFormTag(form));
        }
        tag.put("raid_form", raidFormTag);

        ListTag baseFormTag = new ListTag();
        for (SpeciesFeature form : this.baseForm) {
            baseFormTag.add(RaidBoss.encodeFormTag(form));
        }
        tag.put("base_form", baseFormTag);

        tag.putString("loot_table", this.lootTableId);
        tag.putBoolean("is_catchable", this.isCatchable);
        tag.putFloat("shiny_rate", this.shinyRate);
        return tag;
    }

    public static RaidBoss loadNbt(CompoundTag tag) {
        List<SpeciesFeature> raidForm;
        if (tag.contains("raid_form")) raidForm = RaidBoss.decodeFormTag(tag.getList("raid_form", Tag.TAG_COMPOUND));
        else raidForm = new ArrayList<>();

        List<SpeciesFeature> baseForm;
        if (tag.contains("base_form")) baseForm = RaidBoss.decodeFormTag(tag.getList("base_form", Tag.TAG_COMPOUND));
        else baseForm = new ArrayList<>();

        String lootTableId = tag.contains("loot_table") ? tag.getString("loot_table") : "";
        boolean isCatchable = !tag.contains("is_catchable") || tag.getBoolean("is_catchable");
        float shinyRate = tag.contains("shiny_rate") ? tag.getFloat("shiny_rate") : RaidUtils.getDefaultShinyRate();

        return new RaidBoss(
            PokemonProperties.Companion.parse(tag.getString("base_properties")),
            PokemonProperties.Companion.parse(tag.getString("boss_properties")),
            RaidTier.fromString(tag.getString("raid_tier").toUpperCase()),
            RaidType.fromString(tag.getString("raid_type").toUpperCase()),
            RaidFeature.fromString(tag.getString("raid_feature").toUpperCase()),
            raidForm, baseForm, lootTableId, isCatchable, shinyRate
        );
    }

    public static CompoundTag encodeFormTag(SpeciesFeature form) {
        CompoundTag formTag = new CompoundTag();
        formTag.putString("name", form.getName());
        if (form instanceof StringSpeciesFeature) {
            formTag.putString("value", ((StringSpeciesFeature) form).getValue());
            formTag.putString("type", "string");
        }
        else if (form instanceof FlagSpeciesFeature) {
            formTag.putBoolean("value", ((FlagSpeciesFeature) form).getEnabled());
            formTag.putString("type", "flag");
        }
        else {
            formTag.putInt("value", ((IntSpeciesFeature) form).getValue());
            formTag.putString("type", "int");
        }
        return formTag;
    }

    public static List<SpeciesFeature> decodeFormTag(ListTag tag) {
        List<SpeciesFeature> forms = new ArrayList<>();
        for (Tag t : tag) {
            CompoundTag compoundTag = (CompoundTag) t;

            String type = compoundTag.getString("type");
            if (type.equals("string")) forms.add(new StringSpeciesFeature(compoundTag.getString("name"), compoundTag.getString("value")));
            else if (type.equals("flag")) forms.add(new FlagSpeciesFeature(compoundTag.getString("name"), compoundTag.getBoolean("value")));
            else forms.add(new IntSpeciesFeature(compoundTag.getString("name"), compoundTag.getInt("value")));
        }
        return forms;
    }

    public static String getGender(PokemonProperties properties) {
        if (properties.getGender() == null) return "";
        else return properties.getGender().getSerializedName();
    }

    public static String getFormName(SpeciesFeature raidForm) {
        if (raidForm == null) return "";
        else return raidForm.getName();
    }

    public static String getFormValue(StringSpeciesFeature raidForm) {
        if (raidForm == null) return "";
        else return raidForm.getValue();
    }

    public static Boolean getFormValue(FlagSpeciesFeature raidForm) {
        if (raidForm == null) return false;
        else return raidForm.getEnabled();
    }

    public static Integer getFormValue(IntSpeciesFeature raidForm) {
        if (raidForm == null) return 0;
        else return raidForm.getValue();
    }

    public static Codec<PokemonProperties> propertiesCodec() {
        return RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("species").forGetter(PokemonProperties::getSpecies),
            Codec.STRING.optionalFieldOf("form", "").forGetter(PokemonProperties::getForm),
            Codec.STRING.optionalFieldOf("gender", "").forGetter(RaidBoss::getGender),
            Codec.STRING.optionalFieldOf("ability", "").forGetter(PokemonProperties::getAbility),
            Codec.STRING.optionalFieldOf("nature", "").forGetter(PokemonProperties::getNature),
            Codec.STRING.listOf().optionalFieldOf("moves", new ArrayList<>()).forGetter(PokemonProperties::getMoves)
            ).apply(inst, (species, form, gender, ability, nature, moves) -> {
                PokemonProperties properties = PokemonProperties.Companion.parse("");
                properties.setSpecies(species);
                if (!form.isBlank()) properties.setForm(form);
                try { if (!gender.isBlank()) properties.setGender(Gender.valueOf(gender)); }
                catch (IllegalArgumentException ignored) {}
                if (!ability.isBlank()) properties.setAbility(ability);
                if (!nature.isBlank()) properties.setNature(nature);
                if (!moves.isEmpty()) properties.setMoves(moves);
                return properties;
            })
        );
    }

    public static Codec<SpeciesFeature> raidFormCodec() {
        return RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("name").forGetter(RaidBoss::getFormName),
            Codec.either(Codec.STRING, Codec.either(Codec.BOOL, Codec.INT)).fieldOf("value").forGetter(form -> {
                if (form instanceof StringSpeciesFeature) return Either.left(RaidBoss.getFormValue((StringSpeciesFeature) form));
                else if (form instanceof FlagSpeciesFeature) return Either.right(Either.left(RaidBoss.getFormValue((FlagSpeciesFeature) form)));
                else return Either.right(Either.right(RaidBoss.getFormValue((IntSpeciesFeature) form)));
            })
        ).apply(inst, (name, either) -> {
            if (either.left().isPresent()) return new StringSpeciesFeature(name, either.left().get());
            else {
                assert either.right().isPresent();
                Either<Boolean, Integer> inner = either.right().get();
                if (inner.left().isPresent()) return new FlagSpeciesFeature(name, inner.left().get());
                else assert inner.right().isPresent();
                return new IntSpeciesFeature(name, inner.right().get());
            }
        }));
    }

    public static Codec<RaidBoss> codec() {
        return RecordCodecBuilder.create(inst -> inst.group(
            propertiesCodec().fieldOf("pokemon").forGetter(RaidBoss::getProperties),
            RaidTier.codec().fieldOf("raid_tier").forGetter(RaidBoss::getTier),
            RaidType.codec().fieldOf("raid_type").forGetter(RaidBoss::getType),
            RaidFeature.codec().optionalFieldOf("raid_feature", RaidFeature.DEFAULT).forGetter(RaidBoss::getFeature),
            raidFormCodec().listOf().optionalFieldOf("raid_form", new ArrayList<>()).forGetter(RaidBoss::getRaidForm),
            raidFormCodec().listOf().optionalFieldOf("base_form", new ArrayList<>()).forGetter(RaidBoss::getBaseForm),
            Codec.STRING.optionalFieldOf("loot_table", "").forGetter(RaidBoss::getLootTableId),
            Codec.DOUBLE.optionalFieldOf("weight", 20.0).forGetter(RaidBoss::getWeight),
            Codec.BOOL.optionalFieldOf("is_catchable", true).forGetter(RaidBoss::isCatchable),
            Codec.FLOAT.optionalFieldOf("shiny_rate", CobblemonRaidDens.CONFIG.shiny_rate).forGetter(RaidBoss::getShinyRate)
            ).apply(inst, (properties, tier, type, feature, raidForm, baseForm, bonusItems, weight, isCatchable, shinyRate) -> {
                properties.setLevel(tier.getLevel());
                properties.setTeraType(type.getSerializedName());
                properties.setIvs(IVs.createRandomIVs(tier.getMaxIvs()));
                if (shinyRate == 1.0f) properties.setShiny(true);
                else if (shinyRate == 0.0f) properties.setShiny(false);

                raidForm = new ArrayList<>(raidForm);
                raidForm.addAll(baseForm);

                if (feature == RaidFeature.DYNAMAX && raidForm.stream().noneMatch(form -> form.getName().equals("dynamax_form"))) {
                    raidForm.add(new StringSpeciesFeature("dynamax_form", "none"));
                }
                else if (feature == RaidFeature.MEGA && raidForm.stream().noneMatch(form -> form.getName().equals("mega_evolution"))) {
                    raidForm.add(new StringSpeciesFeature("mega_evolution", "mega"));
                }

                return new RaidBoss(properties, tier, type, feature, raidForm, baseForm, bonusItems, weight, isCatchable, shinyRate);
            })
        );
    }
}
