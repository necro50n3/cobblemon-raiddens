package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.Cobblemon;
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
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.megashowdown.RaidDensMSDCompat;
import com.necro.raid.dens.common.util.IHealthSetter;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IShinyRate;
import com.necro.raid.dens.common.util.RaidDenRegistry;
import kotlin.Unit;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.*;

public class RaidBoss {
    private final PokemonProperties baseProperties;
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
    private final int healthMulti;
    private final float shinyRate;
    private final Map<String, String> script;
    private List<ResourceLocation> structures;

    private final List<String> structuresInner;

    private ResourceLocation id;

    public RaidBoss(PokemonProperties properties, RaidTier tier, RaidType raidType, RaidFeature raidFeature,
                    List<SpeciesFeature> raidForm, List<SpeciesFeature> baseForm, String lootTableId, double weight,
                    boolean isCatchable, int healthMulti, float shinyRate, Map<String, String> script, List<String> structures) {
        this.baseProperties = properties;
        this.raidTier = tier;
        this.raidType = raidType;
        this.raidFeature = raidFeature;
        this.raidForm = raidForm;
        this.baseForm = baseForm;
        this.lootTableId = lootTableId;
        this.weight = weight;
        this.isCatchable = isCatchable;
        this.healthMulti = healthMulti;
        this.shinyRate = shinyRate;
        this.script = script;
        this.structures = new ArrayList<>();

        this.structuresInner = structures;

        this.id = null;
    }

    public PokemonEntity getBossEntity(ServerLevel level) {
        PokemonProperties properties = PokemonProperties.Companion.parse(this.baseProperties.asString(" ") + " aspect=raid uncatchable");
        if (properties.getShiny() == null) properties.setShiny(false);
        if (properties.getLevel() == null) properties.setLevel(this.raidTier.getLevel());

        Pokemon pokemon = properties.create();
        int healthMulti = this.healthMulti > 0 ? this.healthMulti : this.raidTier.getHealth();
        ((IHealthSetter) pokemon).setMaxHealth(healthMulti * pokemon.getMaxHealth());

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

        ((IRaidAccessor) pokemonEntity).setRaidBoss(this.id);
        float scale = Mth.clamp(80f / pokemonEntity.getExposedSpecies().getHeight(), 1.0f, 5.0f);
        pokemonEntity.getPokemon().setScaleModifier(scale);
        pokemonEntity.refreshDimensions();

        return pokemonEntity;
    }

    public void createDisplayAspects() {
        Pokemon displayPokemon = this.baseProperties.create();
        displayPokemon.setShiny(this.shinyRate == 1.0f);

        for (SpeciesFeature form : this.raidForm) {
            ((CustomPokemonProperty) form).apply(displayPokemon);
        }

        this.displaySpecies = displayPokemon.getSpecies();
        this.displayAspects = displayPokemon.getAspects();
    }

    public Pokemon getRewardPokemon(ServerPlayer player) {
        PokemonProperties properties = this.baseProperties.copy();
        if (properties.getLevel() == null) properties.setLevel(this.raidTier.getRewardLevel());

        Pokemon pokemon = new Pokemon();
        properties.apply(pokemon);
        pokemon.initialize();
        ((IShinyRate) pokemon).setRaidShinyRate(this.shinyRate);
        properties.roll(pokemon, player);

        if (properties.getAbility() == null && player.getRandom().nextDouble() < CobblemonRaidDens.CONFIG.ha_rate) {
            pokemon.getForm().getAbilities().getMapping().values().forEach(
                abilities -> {
                    List<HiddenAbility> hidden = abilities.stream()
                        .filter(a -> a instanceof HiddenAbility)
                        .map(a -> (HiddenAbility) a)
                        .toList();
                    if (hidden.isEmpty()) return;
                    HiddenAbility chosen = hidden.get(player.getRandom().nextInt(hidden.size()));
                    pokemon.setAbility$common(chosen.getTemplate().create(false, chosen.getPriority()));
                }
            );
        }

        for (SpeciesFeature form : this.baseForm) {
            ((CustomPokemonProperty) form).apply(pokemon);
        }

        if (this.isDynamax()) pokemon.setDmaxLevel(Cobblemon.config.getMaxDynamaxLevel());
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
        return this.baseProperties;
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

    public int getHealthMulti() {
        return this.healthMulti;
    }

    public float getShinyRate() {
        return this.shinyRate;
    }

    public Map<String, String> getScript() {
        return this.script;
    }

    public List<String> getStructures() {
        return this.structuresInner;
    }

    public ResourceLocation getRandomStructure(RandomSource random) {
        if (this.structures.isEmpty()) this.resolveStructures();

        if (this.structures.size() == 1) return this.structures.getFirst();
        else return this.structures.get(random.nextInt(this.structures.size()));
    }

    private void resolveStructures() {
        List<ResourceLocation> validStructures = new ArrayList<>();
        for (String value : this.structuresInner) {
            if (value.startsWith("#")) validStructures.addAll(RaidDenRegistry.getStructures(ResourceLocation.parse(value.substring(1))));
            else validStructures.add(ResourceLocation.parse(value));
        }
        if (validStructures.isEmpty()) validStructures.addAll(RaidDenRegistry.getStructures(RaidDenRegistry.DEFAULT));
        this.structures = validStructures;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public void setId(ResourceLocation id) {
        this.id = id;
    }

    @SuppressWarnings("unused")
    public boolean isMega() {
        return this.raidFeature == RaidFeature.MEGA;
    }

    public boolean isTera() {
        return this.raidFeature == RaidFeature.TERA;
    }

    public boolean isDynamax() {
        return this.raidFeature == RaidFeature.DYNAMAX;
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
            Codec.STRING.optionalFieldOf("gender", "").forGetter(RaidBoss::getGender),
            Codec.STRING.optionalFieldOf("ability", "").forGetter(PokemonProperties::getAbility),
            Codec.STRING.optionalFieldOf("nature", "").forGetter(PokemonProperties::getNature),
            Codec.INT.optionalFieldOf("level", -1).forGetter(PokemonProperties::getLevel),
            Codec.STRING.listOf().optionalFieldOf("moves", new ArrayList<>()).forGetter(PokemonProperties::getMoves)
            ).apply(inst, (species, gender, ability, nature, level, moves) -> {
                PokemonProperties properties = PokemonProperties.Companion.parse("");
                properties.setSpecies(species);
                try { if (!gender.isBlank()) properties.setGender(Gender.valueOf(gender)); }
                catch (IllegalArgumentException ignored) {}
                if (!ability.isBlank()) properties.setAbility(ability);
                if (!nature.isBlank()) properties.setNature(nature);
                if (level > 0) properties.setLevel(level);
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
            Codec.INT.optionalFieldOf("health_multi", 0).forGetter(RaidBoss::getHealthMulti),
            Codec.FLOAT.optionalFieldOf("shiny_rate", CobblemonRaidDens.CONFIG.shiny_rate).forGetter(RaidBoss::getShinyRate),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("script", new HashMap<>()).forGetter(RaidBoss::getScript),
            Codec.STRING.listOf().optionalFieldOf("structures", List.of("cobblemonraiddens:default")).forGetter(RaidBoss::getStructures)
            ).apply(inst, (properties, tier, type, feature, raidForm, baseForm, bonusItems, weight, isCatchable, healthMulti, shinyRate, script, structures) -> {
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

                return new RaidBoss(properties, tier, type, feature, raidForm, baseForm, bonusItems, weight, isCatchable, healthMulti, shinyRate, script, structures);
            })
        );
    }
}
