package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveSet;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.feature.*;
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.megashowdown.RaidDensMSDCompat;
import com.necro.raid.dens.common.compat.sizevariations.RaidDensSizeVariationsCompat;
import com.necro.raid.dens.common.config.TierConfig;
import com.necro.raid.dens.common.data.UniqueKeyAdapter;
import com.necro.raid.dens.common.util.*;
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
    private PokemonProperties baseProperties;
    private Species displaySpecies;
    private Set<String> displayAspects;
    private RaidTier raidTier;
    private RaidFeature raidFeature;
    private List<SpeciesFeature> raidForm;
    private List<SpeciesFeature> baseForm;
    private RaidType raidType;
    private String lootTableId;
    private LootTable lootTable;
    private Double weight;
    private Integer maxCatches;
    private Integer healthMulti;
    private Float shinyRate;
    private Map<String, String> script;
    private List<ResourceLocation> dens;
    private UniqueKeyAdapter key;
    private Integer currency;
    private RaidAI raidAI;

    private List<String> densInner;

    private ResourceLocation id;

    public RaidBoss(PokemonProperties properties, RaidTier tier, RaidType raidType, RaidFeature raidFeature,
                    List<SpeciesFeature> raidForm, List<SpeciesFeature> baseForm, String lootTableId, Double weight,
                    Integer maxCatches, Integer healthMulti, Float shinyRate, Map<String, String> script, List<String> dens,
                    UniqueKeyAdapter key, Integer currency, RaidAI raidAI) {
        this.baseProperties = properties;
        this.raidTier = tier;
        this.raidType = raidType;
        this.raidFeature = raidFeature;
        this.raidForm = raidForm;
        this.baseForm = baseForm;
        this.lootTableId = lootTableId;
        this.weight = weight;
        this.maxCatches = maxCatches;
        this.healthMulti = healthMulti;
        this.shinyRate = shinyRate;
        this.script = script;
        this.dens = new ArrayList<>();
        this.key = key;
        this.currency = currency;
        this.raidAI = raidAI;

        this.densInner = dens;

        this.id = null;
    }

    public RaidBoss(PokemonProperties properties, RaidTier tier, RaidType raidType, RaidFeature raidFeature,
                    List<SpeciesFeature> raidForm, int maxCatches, float shinyRate) {
        this(
            properties, tier, raidType, raidFeature, raidForm, new ArrayList<>(), null, 0.0,
            maxCatches, 0, shinyRate, new HashMap<>(), new ArrayList<>(), new UniqueKeyAdapter(), 0, RaidAI.RANDOM
        );
    }

    public PokemonEntity getBossEntity(ServerLevel level) {
        PokemonProperties properties = PokemonProperties.Companion.parse(this.baseProperties.asString(" ") + " aspect=raid uncatchable");
        TierConfig tierConfig = CobblemonRaidDens.TIER_CONFIG.get(this.getTier());
        if (properties.getLevel() == null) properties.setLevel(tierConfig.bossLevel());
        properties.setMinPerfectIVs(6);

        Pokemon pokemon;
        if (CobblemonRaidDens.CONFIG.sync_rewards && properties.getShiny() == null) {
            pokemon = new Pokemon();
            properties.apply(pokemon);
            pokemon.initialize();
            ((IShinyRate) pokemon).setRaidShinyRate(this.shinyRate);
            properties.roll(pokemon, null);
        }
        else {
            if (properties.getShiny() == null) properties.setShiny(false);
            pokemon = properties.create();
        }

        if (properties.getAbility() == null && level.getRandom().nextDouble() < tierConfig.haRate()) {
            pokemon.getForm().getAbilities().getMapping().values().forEach(
                abilities -> {
                    List<HiddenAbility> hidden = abilities.stream()
                        .filter(a -> a instanceof HiddenAbility)
                        .map(a -> (HiddenAbility) a)
                        .toList();
                    if (hidden.isEmpty()) return;
                    HiddenAbility chosen = hidden.get(level.getRandom().nextInt(hidden.size()));
                    pokemon.setAbility$common(chosen.getTemplate().create(false, chosen.getPriority()));
                }
            );
        }

        int healthMulti = this.healthMulti;
        ((IHealthSetter) pokemon).setMaxHealth(healthMulti * pokemon.getMaxHealth());

        for (SpeciesFeature form : this.raidForm) {
            ((CustomPokemonProperty) form).apply(pokemon);
        }

        this.setMoveSet(properties, pokemon, true);
        pokemon.getPersistentData().putBoolean("raid", true);
        pokemon.onChange(null);

        PokemonEntity pokemonEntity = new PokemonEntity(level, pokemon, CobblemonEntities.POKEMON);
        pokemonEntity.setDrops(new DropTable());
        pokemonEntity.addTag("alphas.non_wild");

        if (this.isTera() && ModCompat.MEGA_SHOWDOWN.isLoaded()) RaidDensMSDCompat.setupTera(pokemonEntity, pokemon);
        else if (this.isDynamax() && ModCompat.MEGA_SHOWDOWN.isLoaded()) RaidDensMSDCompat.setupDmax(pokemonEntity);

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
        TierConfig tierConfig = CobblemonRaidDens.TIER_CONFIG.get(this.getTier());
        properties.setMinPerfectIVs(tierConfig.ivs());
        properties.setLevel(tierConfig.rewardLevel());

        Pokemon pokemon = new Pokemon();
        properties.apply(pokemon);
        pokemon.initialize();
        if (!CobblemonRaidDens.CONFIG.sync_rewards) {
            ((IShinyRate) pokemon).setRaidShinyRate(this.shinyRate);
            properties.roll(pokemon, player);

            if (properties.getAbility() == null && player.getRandom().nextDouble() < tierConfig.haRate()) {
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
        }

        for (SpeciesFeature form : this.baseForm) {
            ((CustomPokemonProperty) form).apply(pokemon);
        }

        if (this.isDynamax()) pokemon.setDmaxLevel(Cobblemon.config.getMaxDynamaxLevel());
        if (this.raidForm.stream().anyMatch(form -> form instanceof StringSpeciesFeature ssf && ssf.getValue().equals("gmax")))
            pokemon.setGmaxFactor(true);
        if (ModCompat.SIZE_VARIATIONS.isLoaded()) RaidDensSizeVariationsCompat.setRandomSize(pokemon, player);

        this.setMoveSet(properties, pokemon, false);
        return pokemon;
    }

    private void setMoveSet(PokemonProperties properties, Pokemon pokemon, boolean isRaidBoss) {
        List<String> moves = properties.getMoves();
        if (moves != null) {
            MoveSet moveSet = pokemon.getMoveSet();
            moveSet.clear();
            List<MoveTemplate> moveTemplates = moves.stream().map(Moves::getByName).toList();
            moveSet.doWithoutEmitting(() -> {
                for (int i = 0; i < moves.size(); i++) {
                    MoveTemplate mt = moveTemplates.get(i);
                    moveSet.setMove(i, mt.create());
                    Move move = moveSet.get(i);
                    assert move != null;
                    if (isRaidBoss) move.setCurrentPp(99);
                    else move.update();
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

    public Double getWeight() {
        return this.weight;
    }

    public Integer getMaxCatches() {
        return this.maxCatches;
    }

    public Integer getHealthMulti() {
        return this.healthMulti;
    }

    public Float getShinyRate() {
        return this.shinyRate;
    }

    public Map<String, String> getScript() {
        return this.script;
    }

    public List<String> getDens() {
        return this.densInner;
    }

    public UniqueKeyAdapter getKey() {
        return this.key;
    }

    public Integer getCurrency() {
        return this.currency;
    }

    public RaidAI getRaidAI() {
        return this.raidAI;
    }

    public String getRaidAIString() {
        return this.raidAI.getSerializedName();
    }

    public ResourceLocation getRandomDen(RandomSource random) {
        if (this.dens.isEmpty()) this.resolveDens();

        if (this.dens.size() == 1) return this.dens.getFirst();
        else return this.dens.get(random.nextInt(this.dens.size()));
    }

    private void resolveDens() {
        List<ResourceLocation> validDens = new ArrayList<>();
        for (String value : this.densInner) {
            if (value.startsWith("#")) validDens.addAll(RaidDenRegistry.getStructures(ResourceLocation.parse(value.substring(1))));
            else validDens.add(ResourceLocation.parse(value));
        }
        validDens.removeIf(RaidDenRegistry::isNotValidStructure);
        if (validDens.isEmpty()) validDens.add(RaidDenRegistry.DEFAULT);
        this.dens = validDens;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public void setId(ResourceLocation id) {
        this.id = id;
    }

    public void setProperties(PokemonProperties properties) {
        this.baseProperties = properties;
    }

    public void setTier(RaidTier tier) {
        this.raidTier = tier;
    }

    public void setFeature(RaidFeature feature) {
        this.raidFeature = feature;
    }

    public void setForm(List<SpeciesFeature> raidForm, List<SpeciesFeature> baseForm) {
        boolean appendForm = false;

        if (raidForm != null) {
            this.raidForm = new ArrayList<>(raidForm);
            appendForm = true;
        }
        if (baseForm != null) {
            this.baseForm = baseForm;
            appendForm = true;
        }

        if (appendForm) this.raidForm.addAll(this.baseForm);
    }

    public void setType(RaidType type) {
        this.raidType = type;
    }

    public void setLootTable(String lootTable) {
        this.lootTableId = lootTable;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public void setMaxCatches(Integer maxCatches) {
        this.maxCatches = maxCatches;
    }

    public void setHealthMulti(Integer healthMulti) {
        this.healthMulti = healthMulti;
    }

    public void setShinyRate(Float shinyRate) {
        this.shinyRate = shinyRate;
    }

    public void setScript(Map<String, String> script) {
        this.script = script;
    }

    public void setDens(List<String> dens) {
        this.densInner = dens;
    }

    public void setKey(UniqueKeyAdapter key) {
        this.key = key;
    }

    public void setCurrency(Integer currency) {
        this.currency= currency;
    }

    public void setRaidAI(RaidAI raidAI) {
        this.raidAI = raidAI;
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

    public RaidBoss copy() {
        return new RaidBoss(
            this.baseProperties.copy(),
            this.raidTier,
            this.raidType,
            this.raidFeature,
            new ArrayList<>(this.raidForm),
            new ArrayList<>(this.baseForm),
            this.lootTableId,
            this.weight,
            this.maxCatches,
            this.healthMulti,
            this.shinyRate,
            new HashMap<>(this.script),
            new ArrayList<>(this.densInner),
            this.key,
            this.currency,
            this.raidAI
        );
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
            Codec.INT.optionalFieldOf("health_multi", 0).forGetter(RaidBoss::getHealthMulti),
            Codec.FLOAT.optionalFieldOf("shiny_rate", -1.0f).forGetter(RaidBoss::getShinyRate),
            Codec.INT.optionalFieldOf("currency", -1).forGetter(RaidBoss::getCurrency),
            Codec.INT.optionalFieldOf("max_catches", -1).forGetter(RaidBoss::getMaxCatches),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("script", new HashMap<>()).forGetter(RaidBoss::getScript),
            Codec.STRING.listOf().optionalFieldOf("den", List.of("#cobblemonraiddens:default")).forGetter(RaidBoss::getDens),
            UniqueKeyAdapter.CODEC.optionalFieldOf("key", new UniqueKeyAdapter()).forGetter(RaidBoss::getKey),
            Codec.STRING.optionalFieldOf("raid_ai", "").forGetter(RaidBoss::getRaidAIString)
            ).apply(inst, (properties, tier, type, feature, raidForm, baseForm, bonusItems, weight, healthMulti, shinyRate, currency, maxCatches, script, dens, key, raidAIString) -> {
                properties.setTeraType(type.getSerializedName());

                TierConfig tierConfig = CobblemonRaidDens.TIER_CONFIG.get(tier);
                if (healthMulti <= 0) healthMulti = tierConfig.healthMultiplier();
                if (shinyRate == -1.0f) shinyRate = tierConfig.shinyRate();
                if (currency == -1) currency = tierConfig.currency();
                if (maxCatches == -1) maxCatches = tierConfig.maxCatches();
                if (script.isEmpty()) script = tierConfig.defaultScripts();

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

                RaidAI raidAI;
                if (raidAIString.isEmpty()) raidAI = tierConfig.raidAI();
                else raidAI = RaidAI.fromString(raidAIString);

                return new RaidBoss(properties, tier, type, feature, raidForm, baseForm, bonusItems, weight, maxCatches, healthMulti, shinyRate, script, dens, key, currency, raidAI);
            })
        );
    }
}
