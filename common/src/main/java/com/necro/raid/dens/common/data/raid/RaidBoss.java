package com.necro.raid.dens.common.data.raid;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.mark.Mark;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveSet;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.feature.*;
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility;
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.megashowdown.RaidDensMSDCompat;
import com.necro.raid.dens.common.compat.sizevariations.RaidDensSizeVariationsCompat;
import com.necro.raid.dens.common.config.TierConfig;
import com.necro.raid.dens.common.data.adapters.*;
import com.necro.raid.dens.common.registry.RaidDenRegistry;
import com.necro.raid.dens.common.util.*;
import kotlin.Unit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class RaidBoss {
    public static final Gson GSON;

    @SerializedName("pokemon")
    private PokemonProperties reward;
    private PokemonProperties boss;
    @SerializedName("raid_tier")
    private RaidTier raidTier;
    @SerializedName("raid_type")
    private RaidType raidType;
    @SerializedName("raid_feature")
    private RaidFeature raidFeature;
    @SerializedName("loot_table")
    private BossLootTable lootTable;
    private Double weight;
    private List<String> den;
    private UniqueKey key;

    @SerializedName("max_players")
    private Integer maxPlayers;
    @SerializedName("max_clears")
    private Integer maxClears;
    @SerializedName("ha_rate")
    private Double haRate;
    @SerializedName("max_cheers")
    private Integer maxCheers;
    @SerializedName("raid_party_size")
    private Integer raidPartySize;
    @SerializedName("health_multi")
    private Integer healthMulti;
    @SerializedName("multiplayer_health_multi")
    private Float multiplayerHealthMulti;
    @SerializedName("shiny_rate")
    private Float shinyRate;
    private Integer currency;
    @SerializedName("max_catches")
    private Integer maxCatches;
    private Map<String, Script> script;
    @SerializedName("raid_ai")
    private RaidAI raidAI;
    private List<Mark> marks;
    private Integer lives;
    @SerializedName("players_share_lives")
    private Boolean playersShareLives;
    private Integer energy;
    @SerializedName("required_damage")
    private Float requiredDamage;
    @SerializedName("boss_bar_name")
    private String bossBarName;

    private transient PokemonProperties cachedBossProperties;
    private transient List<ResourceLocation> densActual;

    private transient ResourceLocation displaySpecies;
    private transient Set<String> displayAspects;

    private transient ResourceLocation id;

    public RaidBoss(PokemonProperties reward,PokemonProperties boss, RaidTier raidTier, RaidType raidType,
                    RaidFeature raidFeature, BossLootTable lootTable, Double weight, List<String> den, UniqueKey key,
                    Integer maxPlayers, Integer maxClears, Double haRate, Integer maxCheers, Integer raidPartySize,
                    Integer healthMulti, Float multiplayerHealthMulti, Float shinyRate, Integer currency, Integer maxCatches,
                    Map<String, Script> script, RaidAI raidAI, List<Mark> marks, Integer lives, Boolean playersShareLives,
                    Integer energy, Float requiredDamage, String bossBarName) {
        this.reward = reward;
        this.boss = boss;
        this.raidTier = raidTier;
        this.raidType = raidType;
        this.raidFeature = raidFeature;
        this.lootTable = lootTable;
        this.weight = weight;
        this.den = den;
        this.key = key;

        this.maxPlayers = maxPlayers;
        this.maxClears = maxClears;
        this.haRate = haRate;
        this.maxCheers = maxCheers;
        this.raidPartySize = raidPartySize;
        this.healthMulti = healthMulti;
        this.multiplayerHealthMulti = multiplayerHealthMulti;
        this.shinyRate = shinyRate;
        this.currency = currency;
        this.maxCatches = maxCatches;
        this.script = script;
        this.raidAI = raidAI;
        this.marks = marks;
        this.lives = lives;
        this.playersShareLives = playersShareLives;
        this.energy = energy;
        this.requiredDamage = requiredDamage;
        this.bossBarName = bossBarName;

        this.cachedBossProperties = null;
        this.densActual = new ArrayList<>();
        this.displaySpecies = null;
        this.displayAspects = null;
        this.id = null;
    }

    public RaidBoss() {
        this.reward = null;
        this.boss = null;
        this.raidTier = null;
        this.raidType = null;
        this.raidFeature = null;
        this.lootTable = null;
        this.weight = null;
        this.den = null;
        this.key = null;

        this.maxPlayers = null;
        this.maxClears = null;
        this.haRate = null;
        this.maxCheers = null;
        this.raidPartySize = null;
        this.healthMulti = null;
        this.multiplayerHealthMulti = null;
        this.shinyRate = null;
        this.currency = null;
        this.maxCatches = null;
        this.script = null;
        this.raidAI = null;
        this.marks = null;
        this.lives = null;
        this.playersShareLives = null;
        this.energy = null;
        this.requiredDamage = null;
        this.bossBarName = null;

        this.cachedBossProperties = null;
        this.densActual = new ArrayList<>();
        this.displaySpecies = null;
        this.displayAspects = null;
        this.id = null;
    }

    public void createDisplayAspects() {
        Pokemon displayPokemon = this.getBossProperties().create();
        displayPokemon.setShiny(this.getShinyRate() == 1.0f);

        this.displaySpecies = displayPokemon.getSpecies().getResourceIdentifier();
        this.displayAspects = displayPokemon.getAspects();
    }

    public void setDisplaySpecies(ResourceLocation species) {
        this.displaySpecies = species;
    }

    public void setDisplayAspects(Set<String> aspects) {
        this.displayAspects = aspects;
    }

    public void applyDefaults() {
        if (this.reward == null) throw new JsonSyntaxException("Missing required field: \"pokemon\"");
        if (this.reward.getSpecies() == null || this.reward.getSpecies().isBlank()) throw new JsonSyntaxException("Missing required field: \"pokemon.species\"");
        if (this.raidTier == null) throw new JsonSyntaxException("Missing required field: \"raid_tier\"");
        if (this.raidType == null) throw new JsonSyntaxException("Missing required field: \"raid_type\"");

        TierConfig tierConfig = CobblemonRaidDens.TIER_CONFIG.get(this.raidTier);

        if (this.raidFeature == null) this.raidFeature = RaidFeature.DEFAULT;
        if (this.weight == null) this.weight = 20.0;
        if (this.den == null) this.den = List.of("#cobblemonraiddens:default");
        if (this.key == null) this.key = new UniqueKey();

        if (this.maxPlayers == null) this.maxPlayers = tierConfig.maxPlayers();
        if (this.maxClears == null) this.maxClears = tierConfig.maxClears();
        if (this.haRate == null) this.haRate = tierConfig.haRate();
        if (this.maxCheers == null) this.maxCheers = tierConfig.maxCheers();
        if (this.raidPartySize == null) this.raidPartySize = tierConfig.raidPartySize();
        if (this.healthMulti == null) this.healthMulti = tierConfig.healthMultiplier();
        if (this.multiplayerHealthMulti == null) this.multiplayerHealthMulti = tierConfig.multiplayerHealthMultiplier();
        if (this.shinyRate == null) this.shinyRate = tierConfig.shinyRate();
        if (this.currency == null) this.currency = tierConfig.currency();
        if (this.maxCatches == null) this.maxCatches = tierConfig.maxCatches();
        if (this.script == null) this.script = tierConfig.defaultScripts();
        if (this.raidAI == null) this.raidAI = tierConfig.raidAI();
        if (this.marks == null) this.marks = tierConfig.marks();
        if (this.lives == null) this.lives = tierConfig.lives();
        if (this.playersShareLives == null) this.playersShareLives = tierConfig.playersShareLives();
        if (this.energy == null) this.energy = tierConfig.energy();
        if (this.requiredDamage == null) this.requiredDamage = tierConfig.requiredDamage();

        if (this.boss == null) this.boss = new PokemonProperties();
        this.applyAspects();
    }

    public void applyAspects() {
        Set<String> aspects = new HashSet<>(this.boss.getAspects());
        aspects.add("raid");
        this.boss.setAspects(aspects);

        List<CustomPokemonProperty> customProperties = new ArrayList<>(this.boss.getCustomProperties());

        if (this.raidFeature == RaidFeature.MEGA && customProperties.stream().filter(SpeciesFeature.class::isInstance).noneMatch(prop -> ((SpeciesFeature) prop).getName().equals("mega_evolution")))
            customProperties.add(new StringSpeciesFeature("mega_evolution", "mega"));
        if (this.raidFeature == RaidFeature.DYNAMAX && customProperties.stream().filter(SpeciesFeature.class::isInstance).noneMatch(prop -> ((SpeciesFeature) prop).getName().equals("dynamax_form")))
            customProperties.add(new StringSpeciesFeature("dynamax_form", "none"));
        customProperties.add(new StringSpeciesFeature("aspect", "raid"));
        customProperties.add(new FlagSpeciesFeature("uncatchable", true));

        this.boss.setCustomProperties(customProperties);
        this.reward.setTeraType(this.raidType.getSerializedName());
    }

    public PokemonEntity getBossEntity(ServerLevel level, Set<String> aspects) {
        PokemonProperties properties = this.getBossProperties().copy();
        TierConfig tierConfig = CobblemonRaidDens.TIER_CONFIG.get(this.getTier());
        if (properties.getLevel() == null) properties.setLevel(tierConfig.bossLevel());
        properties.setMinPerfectIVs(6);

        Pokemon pokemon;
        if (aspects != null) {
            properties.setAspects(aspects);
            pokemon = properties.create();
            pokemon.setShiny(aspects.contains("shiny"));

            if (aspects.contains("male")) pokemon.setGender(Gender.MALE);
            else if (aspects.contains("female")) pokemon.setGender(Gender.FEMALE);

            if (aspects.contains("radiant-radiant")) new StringSpeciesFeature("radiant", "radiant").apply(pokemon);
            else if (aspects.contains("radiant-regular")) new StringSpeciesFeature("radiant", "regular").apply(pokemon);
        }
        else if (CobblemonRaidDens.CONFIG.sync_rewards && properties.getShiny() == null) {
            pokemon = new Pokemon();
            properties.apply(pokemon);
            pokemon.initialize();
            ((IShinyRate) pokemon).crd_setRaidShinyRate(this.getShinyRate());
            properties.roll(pokemon, null);
        }
        else {
            if (properties.getShiny() == null) properties.setShiny(false);
            pokemon = properties.create();
        }
        if (aspects == null && !CobblemonRaidDens.CONFIG.sync_rewards) new StringSpeciesFeature("radiant", "regular").apply(pokemon);

        if (properties.getAbility() == null && level.getRandom().nextDouble() < this.getHaRate()) {
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

        this.setMoveSet(properties, pokemon, true);
        pokemon.getPersistentData().putBoolean("raid", true);

        PokemonEntity pokemonEntity = new PokemonEntity(level, pokemon, CobblemonEntities.POKEMON);
        pokemonEntity.setDrops(new DropTable());
        pokemonEntity.addTag("alphas.non_wild");

        if (this.isTera() && ModCompat.MEGA_SHOWDOWN.isLoaded()) RaidDensMSDCompat.setupTera(pokemonEntity, pokemon);
        else if (this.isDynamax() && ModCompat.MEGA_SHOWDOWN.isLoaded()) RaidDensMSDCompat.setupDmax(pokemonEntity, pokemon);

        ((IRaidAccessor) pokemonEntity).crd_setRaidBoss(this.id);
        float scale = Mth.clamp(80f / pokemonEntity.getExposedSpecies().getForm(pokemonEntity.getAspects()).getHeight(), 1.0f, 5.0f);
        pokemonEntity.getPokemon().setScaleModifier(scale);
        pokemonEntity.refreshDimensions();
        pokemon.onChange(null);

        return pokemonEntity;
    }

    public Pokemon getRewardPokemon(ServerPlayer player) {
        PokemonProperties properties = this.reward.copy();
        TierConfig tierConfig = CobblemonRaidDens.TIER_CONFIG.get(this.getTier());
        if (properties.getMinPerfectIVs() == null) properties.setMinPerfectIVs(tierConfig.ivs());
        if (properties.getLevel() == null) properties.setLevel(tierConfig.rewardLevel());

        Pokemon pokemon = new Pokemon();
        properties.apply(pokemon);
        pokemon.initialize();
        if (!CobblemonRaidDens.CONFIG.sync_rewards) {
            ((IShinyRate) pokemon).crd_setRaidShinyRate(this.shinyRate);
            properties.roll(pokemon, player);

            if (properties.getAbility() == null && player.getRandom().nextDouble() < this.getHaRate()) {
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

        if (this.isDynamax()) pokemon.setDmaxLevel(Cobblemon.config.getMaxDynamaxLevel());
        if (this.raidFeature == RaidFeature.DYNAMAX && new StringSpeciesFeature("dynamax_form", "gmax").matches(pokemon)) pokemon.setGmaxFactor(true);
        if (ModCompat.SIZE_VARIATIONS.isLoaded()) RaidDensSizeVariationsCompat.setRandomSize(pokemon, player);

        this.setMoveSet(properties, pokemon, false);
        for (Mark mark : this.getMarks()) pokemon.exchangeMark(mark, true);

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

    public ResourceLocation getId() {
        return this.id;
    }

    public PokemonProperties getReward() {
        return this.reward;
    }

    public PokemonProperties getBoss() {
        return this.boss;
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

    public BossLootTable getLootTable() {
        return this.lootTable;
    }

    public Double getWeight() {
        return this.weight;
    }

    public List<String> getDens() {
        return this.den;
    }

    public UniqueKey getKey() {
        return this.key;
    }

    public Integer getMaxPlayers() {
        return this.maxPlayers;
    }

    public Integer getMaxClears() {
        return this.maxClears;
    }

    public Double getHaRate() {
        return this.haRate;
    }

    public Integer getMaxCheers() {
        return this.maxCheers;
    }

    public Integer getRaidPartySize() {
        return this.raidPartySize;
    }

    public Integer getHealthMulti() {
        return this.healthMulti;
    }

    public Float getMultiplayerHealthMulti() {
        return this.multiplayerHealthMulti;
    }

    public Float getShinyRate() {
        return this.shinyRate;
    }

    public Integer getCurrency() {
        return this.currency;
    }

    public Integer getMaxCatches() {
        return this.maxCatches;
    }

    public Map<String, Script> getScript() {
        return this.script;
    }

    public RaidAI getRaidAI() {
        return this.raidAI;
    }

    public List<Mark> getMarks() {
        return this.marks;
    }

    public Integer getLives() {
        return this.lives;
    }

    public Boolean getPlayersShareLives() {
        return this.playersShareLives;
    }

    public Integer getEnergy() {
        return this.energy;
    }

    public Float getRequiredDamage() {
        return this.requiredDamage;
    }

    public Species getDisplaySpecies() {
        if (this.displaySpecies == null) return null;
        return PokemonSpecies.getByIdentifier(this.displaySpecies);
    }

    public ResourceLocation getDisplaySpeciesIdentifier() {
        return this.displaySpecies;
    }

    public Set<String> getDisplayAspects() {
        return this.displayAspects;
    }

    public PokemonProperties getBossProperties() {
        if (this.cachedBossProperties == null) this.cachedBossProperties = PropertiesAdapter.apply(this.reward, this.boss);
        return this.cachedBossProperties;
    }

    public List<ItemStack> getRandomRewards(ServerLevel level, ItemStack itemStack, Player player) {
        if (this.lootTable == null) return new ArrayList<>();
        return this.lootTable.getRandomRewards(level, itemStack, player);
    }

    public ResourceLocation getRandomDen(RandomSource random) {
        if (this.densActual.isEmpty()) this.resolveDens();

        if (this.densActual.size() == 1) return this.densActual.getFirst();
        else return this.densActual.get(random.nextInt(this.densActual.size()));
    }

    public String getBossBarName() {
        return this.bossBarName;
    }

    private void resolveDens() {
        List<ResourceLocation> validDens = new ArrayList<>();
        for (String value : this.den) {
            if (value.startsWith("#")) validDens.addAll(RaidDenRegistry.getStructures(ResourceLocation.parse(value.substring(1))));
            else validDens.add(ResourceLocation.parse(value));
        }
        validDens.removeIf(RaidDenRegistry::isNotValidStructure);
        if (validDens.isEmpty()) validDens.add(RaidDenRegistry.DEFAULT);
        this.densActual = validDens;
    }

    public void setId(ResourceLocation id) {
        this.id = id;
    }

    public void setReward(PokemonProperties properties) {
        this.reward = properties;
    }

    public void setBoss(PokemonProperties properties) {
        this.boss = properties;
    }

    public void setTier(RaidTier tier) {
        this.raidTier = tier;
    }

    public void setFeature(RaidFeature feature) {
        this.raidFeature = feature;
    }

    public void setType(RaidType type) {
        this.raidType = type;
    }

    public void setLootTable(BossLootTable lootTable) {
        this.lootTable = lootTable;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public void setDens(List<String> den) {
        this.den = new ArrayList<>(den);
    }

    public void setKey(UniqueKey key) {
        this.key = key;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setMaxClears(Integer maxClears) {
        this.maxClears = maxClears;
    }

    public void setHaRate(Double haRate) {
        this.haRate = haRate;
    }

    public void setMaxCheers(Integer maxCheers) {
        this.maxCheers = maxCheers;
    }

    public void setRaidPartySize(Integer raidPartySize) {
        this.raidPartySize = raidPartySize;
    }

    public void setHealthMulti(Integer healthMulti) {
        this.healthMulti = healthMulti;
    }

    public void setMultiplayerHealthMulti(Float multiplayerHealthMulti) {
        this.multiplayerHealthMulti = multiplayerHealthMulti;
    }

    public void setShinyRate(Float shinyRate) {
        this.shinyRate = shinyRate;
    }

    public void setCurrency(Integer currency) {
        this.currency= currency;
    }

    public void setMaxCatches(Integer maxCatches) {
        this.maxCatches = maxCatches;
    }

    public void setScript(Map<String, Script> script) {
        this.script = new HashMap<>(script);
    }

    public void setRaidAI(RaidAI raidAI) {
        this.raidAI = raidAI;
    }

    public void setMarks(List<Mark> marks) {
        this.marks = new ArrayList<>(marks);
    }

    public void setLives(Integer lives) {
        this.lives = lives;
    }

    public void setPlayersShareLives(Boolean playersShareLives) {
        this.playersShareLives = playersShareLives;
    }

    public void setEnergy(Integer energy) {
        this.energy = energy;
    }

    public void setRequiredDamage(Float requiredDamage) {
        this.requiredDamage = requiredDamage;
    }

    public void clearCaches() {
        this.cachedBossProperties = null;
        if (this.lootTable != null) this.lootTable.clearCache();
        this.densActual = new ArrayList<>();
        this.displaySpecies = null;
        this.displayAspects = null;
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
            this.reward.copy(),
            this.boss.copy(),
            this.raidTier,
            this.raidType,
            this.raidFeature,
            this.lootTable,
            this.weight,
            new ArrayList<>(this.den),
            this.key,
            this.maxPlayers,
            this.maxClears,
            this.haRate,
            this.maxCheers,
            this.raidPartySize,
            this.healthMulti,
            this.multiplayerHealthMulti,
            this.shinyRate,
            this.currency,
            this.maxCatches,
            new HashMap<>(this.script),
            this.raidAI,
            new ArrayList<>(this.marks),
            this.lives,
            this.playersShareLives,
            this.energy,
            this.requiredDamage,
            this.bossBarName
        );
    }

    static {
        GSON = new GsonBuilder()
            .registerTypeAdapter(PokemonProperties.class, new PropertiesAdapter())
            .registerTypeAdapter(ResourceLocation.class, IdentifierAdapter.INSTANCE)
            .registerTypeAdapter(Mark.class, new MarkAdapter())
            .registerTypeAdapter(Script.class, new ScriptAdapter())
            .registerTypeAdapter(UniqueKey.class, new UniqueKeyAdapter())
            .registerTypeAdapter(BossLootTable.class, new BossLootTableAdapter())
            .create();
    }
}
