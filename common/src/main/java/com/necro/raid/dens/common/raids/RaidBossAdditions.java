package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.api.mark.Mark;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature;
import com.cobblemon.mod.common.pokemon.Gender;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.config.TierConfig;
import com.necro.raid.dens.common.data.ScriptAdapter;
import com.necro.raid.dens.common.data.UniqueKeyAdapter;
import com.necro.raid.dens.common.util.RaidRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class RaidBossAdditions {
    private static Set<ResourceLocation> BLACKLIST;
    private static boolean CACHED = false;

    private final int priority;
    private final List<String> include;
    private final List<String> exclude;
    private final RaidBoss additions;

    private final boolean replace;
    private final String suffix;

    public RaidBossAdditions(int priority, List<String> include, List<String> exclude, RaidBoss additions, boolean replace, String suffix) {
        this.priority = priority;
        this.include = include;
        this.exclude = exclude;
        this.additions = additions;
        this.replace = replace;
        if (!replace && !suffix.startsWith("_")) suffix = "_" + suffix;
        this.suffix = suffix;
    }

    public void apply(List<ResourceLocation> registry) {
        if (!this.replace() && this.suffix().equals("_")) return;
        List<ResourceLocation> targets = new ArrayList<>();
        if (this.include().isEmpty()) {
            targets = registry;
        }
        else {
            for (String target : this.include()) {
                ResourceLocation id = ResourceLocation.parse(target.startsWith("#") ? target.substring(1) : target);
                if (target.startsWith("#")) targets.addAll(RaidRegistry.getTagEntries(id));
                else if (RaidRegistry.getRaidBoss(id) != null) targets.add(id);
            }
        }

        Set<ResourceLocation> excluded = new HashSet<>();
        for (String exclude : this.exclude()) {
            ResourceLocation id = ResourceLocation.parse(exclude.startsWith("#") ? exclude.substring(1) : exclude);
            if (exclude.startsWith("#")) excluded.addAll(RaidRegistry.getTagEntries(id));
            else if (RaidRegistry.getRaidBoss(id) != null) excluded.add(id);
        }

        if (!CACHED) {
            BLACKLIST = RaidRegistry.getTagEntries(ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "additions_blacklist"));
            CACHED = true;
        }

        for (ResourceLocation loc : targets) {
            if (excluded.contains(loc)) continue;
            RaidBoss temp = RaidRegistry.getRaidBoss(loc);
            if (temp == null) continue;
            ResourceLocation id = temp.getId();
            if (!this.replace() && BLACKLIST.contains(id)) continue;
            final RaidBoss boss = this.replace() ? temp : temp.copy();

            PokemonProperties properties = this.additions().getProperties();
            if (properties != null) {
                if (properties.getSpecies() != null) boss.getProperties().setSpecies(properties.getSpecies());
                if (properties.getGender() != null) boss.getProperties().setGender(properties.getGender());
                if (properties.getAbility() != null) boss.getProperties().setAbility(properties.getAbility());
                if (properties.getNature() != null) boss.getProperties().setNature(properties.getNature());
                if (properties.getLevel() != null) boss.getProperties().setLevel(properties.getLevel());
                if (properties.getMoves() != null) boss.getProperties().setMoves(properties.getMoves());
                if (properties.getHeldItem() != null) boss.getProperties().setHeldItem(properties.getHeldItem());
                if (properties.getTeraType() != null) boss.getProperties().setTeraType(properties.getTeraType());
            }

            getTier(this.additions()).ifPresent(boss::setTier);
            getFeature(this.additions()).ifPresent(boss::setFeature);
            boss.setForm(getRaidForm(this.additions()).orElse(null), getBaseForm(this.additions()).orElse(null));
            getType(this.additions()).ifPresent(boss::setType);
            getLootTable(this.additions()).ifPresent(boss::setLootTable);
            getWeight(this.additions()).ifPresent(weight -> boss.setWeight(boss.getWeight() * weight));
            getMaxCatches(this.additions()).ifPresent(boss::setMaxCatches);
            getHealthMulti(this.additions()).ifPresent(boss::setHealthMulti);
            getShinyRate(this.additions()).ifPresent(boss::setShinyRate);
            getScript(this.additions()).ifPresent(boss::setScript);
            getDens(this.additions()).ifPresent(boss::setDens);
            getKey(this.additions()).ifPresent(boss::setKey);
            getCurrency(this.additions()).ifPresent(boss::setCurrency);
            getRaidAI(this.additions()).ifPresent(boss::setRaidAI);
            getMarks(this.additions()).ifPresent(boss::setMarks);

            if (!this.replace()) {
                boss.setId(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + this.suffix()));
                RaidRegistry.register(boss);
            }
        }
    }

    public int priority() {
        return this.priority;
    }

    private List<String> include() {
        return this.include;
    }

    private List<String> exclude() {
        return this.exclude;
    }

    private RaidBoss additions() {
        return this.additions;
    }

    private boolean replace() {
        return this.replace;
    }

    private String suffix() {
        return this.suffix;
    }

    private static Optional<PokemonProperties> getProperties(RaidBoss boss) {
        return Optional.ofNullable(boss.getProperties());
    }

    private static Optional<RaidTier> getTier(RaidBoss boss) {
        return Optional.ofNullable(boss.getTier());
    }

    private static Optional<RaidFeature> getFeature(RaidBoss boss) {
        return Optional.ofNullable(boss.getFeature());
    }

    private static Optional<List<SpeciesFeature>> getRaidForm(RaidBoss boss) {
        return Optional.ofNullable(boss.getRaidForm());
    }

    private static Optional<List<SpeciesFeature>> getBaseForm(RaidBoss boss) {
        return Optional.ofNullable(boss.getBaseForm());
    }

    private static Optional<RaidType> getType(RaidBoss boss) {
        return Optional.ofNullable(boss.getType());
    }

    private static Optional<String> getLootTable(RaidBoss boss) {
        return Optional.ofNullable(boss.getLootTableId());
    }

    private static Optional<Double> getWeight(RaidBoss boss) {
        return Optional.ofNullable(boss.getWeight());
    }

    private static Optional<Integer> getMaxCatches(RaidBoss boss) {
        return Optional.ofNullable(boss.getMaxCatches());
    }

    private static Optional<Integer> getHealthMulti(RaidBoss boss) {
        return Optional.ofNullable(boss.getHealthMulti());
    }

    private static Optional<Float> getShinyRate(RaidBoss boss) {
        return Optional.ofNullable(boss.getShinyRate());
    }

    private static Optional<Map<String, ScriptAdapter>> getScript(RaidBoss boss) {
        return Optional.ofNullable(boss.getScript());
    }

    private static Optional<List<String>> getDens(RaidBoss boss) {
        return Optional.ofNullable(boss.getDens());
    }

    private static Optional<UniqueKeyAdapter> getKey(RaidBoss boss) {
        return Optional.ofNullable(boss.getKey());
    }

    private static Optional<Integer> getCurrency(RaidBoss boss) {
        return Optional.ofNullable(boss.getCurrency());
    }

    private static Optional<RaidAI> getRaidAI(RaidBoss boss) {
        return Optional.ofNullable(boss.getRaidAI());
    }

    private static Optional<List<Mark>> getMarks(RaidBoss boss) {
        return Optional.ofNullable(boss.getMarks());
    }

    private static Optional<String> species(PokemonProperties properties) {
        return Optional.ofNullable(properties.getSpecies());
    }

    private static Optional<String> gender(PokemonProperties properties) {
        Gender gender = properties.getGender();
        if (gender == null) return Optional.empty();
        else return Optional.of(gender.getSerializedName());
    }

    private static Optional<String> ability(PokemonProperties properties) {
        return Optional.ofNullable(properties.getAbility());
    }

    private static Optional<String> nature(PokemonProperties properties) {
        return Optional.ofNullable(properties.getNature());
    }

    private static Optional<Integer> level(PokemonProperties properties) {
        return Optional.ofNullable(properties.getLevel());
    }

    private static Optional<List<String>> moves(PokemonProperties properties) {
        return Optional.ofNullable(properties.getMoves());
    }

    private static Optional<String> heldItem(PokemonProperties properties) {
        return Optional.ofNullable(properties.getHeldItem());
    }

    private static Codec<PokemonProperties> propertiesCodec() {
        return RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.optionalFieldOf("species").forGetter(RaidBossAdditions::species),
                Codec.STRING.optionalFieldOf("gender").forGetter(RaidBossAdditions::gender),
                Codec.STRING.optionalFieldOf("ability").forGetter(RaidBossAdditions::ability),
                Codec.STRING.optionalFieldOf("nature").forGetter(RaidBossAdditions::nature),
                Codec.INT.optionalFieldOf("level").forGetter(RaidBossAdditions::level),
                Codec.STRING.listOf().optionalFieldOf("moves").forGetter(RaidBossAdditions::moves),
                Codec.STRING.optionalFieldOf("heldItem").forGetter(RaidBossAdditions::heldItem)
                ).apply(inst, (species, gender, ability, nature, level, moves, heldItem) -> {
                PokemonProperties properties = new PokemonProperties();
                species.ifPresent(properties::setSpecies);
                try {
                    gender.ifPresent(s -> properties.setGender(Gender.valueOf(s)));
                } catch (IllegalArgumentException ignored) {
                }
                ability.ifPresent(properties::setAbility);
                nature.ifPresent(properties::setNature);
                level.ifPresent(properties::setLevel);
                moves.ifPresent(properties::setMoves);
                heldItem.ifPresent(properties::setHeldItem);
                return properties;
            })
        );
    }

    private static Codec<RaidBoss> bossCodec() {
        return RecordCodecBuilder.create(inst -> inst.group(
                propertiesCodec().optionalFieldOf("pokemon").forGetter(RaidBossAdditions::getProperties),
                RaidTier.codec().optionalFieldOf("raid_tier").forGetter(RaidBossAdditions::getTier),
                RaidType.codec().optionalFieldOf("raid_type").forGetter(RaidBossAdditions::getType),
                RaidFeature.codec().optionalFieldOf("raid_feature").forGetter(RaidBossAdditions::getFeature),
                RaidBoss.raidFormCodec().listOf().optionalFieldOf("raid_form").forGetter(RaidBossAdditions::getRaidForm),
                RaidBoss.raidFormCodec().listOf().optionalFieldOf("base_form").forGetter(RaidBossAdditions::getBaseForm),
                Codec.STRING.optionalFieldOf("loot_table").forGetter(RaidBossAdditions::getLootTable),
                Codec.DOUBLE.optionalFieldOf("weight").forGetter(RaidBossAdditions::getWeight),
                Codec.INT.optionalFieldOf("health_multi").forGetter(RaidBossAdditions::getHealthMulti),
                Codec.FLOAT.optionalFieldOf("shiny_rate").forGetter(RaidBossAdditions::getShinyRate),
                Codec.INT.optionalFieldOf("currency").forGetter(RaidBossAdditions::getCurrency),
                Codec.INT.optionalFieldOf("max_catches").forGetter(RaidBossAdditions::getMaxCatches),
                Codec.unboundedMap(Codec.STRING, ScriptAdapter.CODEC).optionalFieldOf("script").forGetter(RaidBossAdditions::getScript),
                Codec.STRING.listOf().optionalFieldOf("den").forGetter(RaidBossAdditions::getDens),
                UniqueKeyAdapter.CODEC.optionalFieldOf("key").forGetter(RaidBossAdditions::getKey),
                RaidAI.codec().optionalFieldOf("raid_ai").forGetter(RaidBossAdditions::getRaidAI)
            ).apply(inst, (properties, tier, type, feature, raidForm, baseForm, bonusItems, weight, healthMulti, shinyRate, currency, maxCatches, script, dens, key, raidAI) -> {
                Integer hm = healthMulti.orElse(null);
                Float sr = shinyRate.orElse(null);
                Integer c = currency.orElse(null);
                Integer mc = maxCatches.orElse(null);
                Map<String, ScriptAdapter> s = script.orElse(null);

                RaidTier t = tier.orElse(null);
                if (t != null) {
                    TierConfig tierConfig = CobblemonRaidDens.TIER_CONFIG.get(t);
                    if (hm == null) hm = tierConfig.healthMultiplier();
                    if (sr == null) sr = tierConfig.shinyRate();
                    if (c == null) c = tierConfig.currency();
                    if (mc == null) mc = tierConfig.maxCatches();
                    if (s == null) s = tierConfig.defaultScripts();
                }

                PokemonProperties p = properties.orElse(new PokemonProperties());
                type.ifPresent(t1 -> p.setTeraType(t1.getSerializedName()));
                if (sr == null) {
                } else if (sr == 1.0f) p.setShiny(true);
                else if (sr == 0.0f) p.setShiny(false);

                RaidFeature f = feature.orElse(null);
                List<SpeciesFeature> rf = raidForm.orElse(null);
                if ((f == RaidFeature.DYNAMAX || f == RaidFeature.MEGA) && rf == null) rf = new ArrayList<>();
                if (f == RaidFeature.DYNAMAX && rf.stream().noneMatch(form -> form.getName().equals("dynamax_form"))) {
                    rf.add(new StringSpeciesFeature("dynamax_form", "none"));
                } else if (f == RaidFeature.MEGA && rf.stream().noneMatch(form -> form.getName().equals("mega_evolution"))) {
                    rf.add(new StringSpeciesFeature("mega_evolution", "mega"));
                }

                return new RaidBoss(
                    p, t, type.orElse(null), feature.orElse(null), rf, baseForm.orElse(null),
                    bonusItems.orElse(null), weight.orElse(null), mc, hm, sr, s, dens.orElse(null),
                    key.orElse(null), c, raidAI.orElse(null), null
                );
            })
        );
    }

    public static Codec<RaidBossAdditions> codec() {
        return RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("priority", 0).forGetter(RaidBossAdditions::priority),
            Codec.STRING.listOf().optionalFieldOf("include", new ArrayList<>()).forGetter(RaidBossAdditions::include),
            Codec.STRING.listOf().optionalFieldOf("exclude", new ArrayList<>()).forGetter(RaidBossAdditions::exclude),
            bossCodec().fieldOf("additions").forGetter(RaidBossAdditions::additions),
            Codec.BOOL.optionalFieldOf("replace", true).forGetter(RaidBossAdditions::replace),
            Codec.STRING.optionalFieldOf("suffix", "").forGetter(RaidBossAdditions::suffix)
        ).apply(inst, RaidBossAdditions::new));
    }
}
