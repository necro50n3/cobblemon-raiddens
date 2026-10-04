package com.necro.raid.dens.common.data.adapters;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.IntSpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.cobblemon.mod.common.pokemon.EVs;
import com.cobblemon.mod.common.pokemon.Gender;
import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.util.IEVExtension;
import com.necro.raid.dens.common.util.IProperties;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

import static com.cobblemon.mod.common.util.MiscUtilsKt.cobblemonResource;

public class PropertiesAdapter implements JsonSerializer<PokemonProperties>, JsonDeserializer<PokemonProperties> {
    private static <T> Optional<T> optionalAdapter(PokemonProperties properties, Function<PokemonProperties, T> getter) {
        return Optional.ofNullable(getter.apply(properties));
    }

    private static String genderAdapter(PokemonProperties properties) {
        Gender gender = properties.getGender();
        return gender == null ? "" : gender.getSerializedName();
    }

    private static Optional<List<List<String>>> moveAdapter(PokemonProperties properties) {
        return Optional.ofNullable(((IProperties) properties).crd_getMovesetBuilder());
    }

    private static <T> T valueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static Stat statMap(String id) {
        return switch (id) {
            case "hp" -> Stats.HP;
            case "atk" -> Stats.ATTACK;
            case "def" -> Stats.DEFENCE;
            case "spa" -> Stats.SPECIAL_ATTACK;
            case "spd" -> Stats.SPECIAL_DEFENCE;
            case "spe" -> Stats.SPEED;
            default -> Cobblemon.INSTANCE.getStatProvider().fromIdentifier(id.contains(":") ? ResourceLocation.parse(id) : cobblemonResource(id));
        };
    }

    private static final Codec<Stat> STAT_CODEC =
        Codec.STRING.comapFlatMap(
            id -> {
                Stat stat = statMap(id);
                if (stat == null) return DataResult.error(() -> "Unknown stat: " + id);
                if (stat.getType() != Stat.Type.PERMANENT) {
                    return DataResult.error(() -> stat.getIdentifier() + " is not of type " + Stat.Type.PERMANENT);
                }
                return DataResult.success(stat);
            },
            stat -> stat.getIdentifier().getPath()
        );

    @SuppressWarnings("ConstantConditions")
    private static final Codec<EVs> EV_CODEC = Codec.unboundedMap(STAT_CODEC, Codec.intRange(0, EVs.MAX_STAT_VALUE))
        .comapFlatMap(
            map -> {
                EVs evs = Cobblemon.INSTANCE.getStatProvider().createEmptyEVs();
                map.forEach((stat, value) -> ((IEVExtension) (Object) evs).crd_forceSet(stat, value));
                return DataResult.success(evs);
            },
            evs -> {
                Map<Stat, Integer> map = new HashMap<>();
                evs.forEach(entry -> map.put(entry.getKey(), entry.getValue()));
                return map;
            }
        );

    private static final  Codec<SpeciesFeature> FEATURE_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("name").forGetter(SpeciesFeature::getName),
            Codec.either(Codec.STRING, Codec.either(Codec.BOOL, Codec.INT)).fieldOf("value").forGetter(form -> {
                if (form instanceof StringSpeciesFeature string) return Either.left(string.getValue());
                else if (form instanceof FlagSpeciesFeature flag) return Either.right(Either.left(flag.getEnabled()));
                else return Either.right(Either.right(((IntSpeciesFeature) form).getValue()));
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

    private static final Codec<PokemonProperties> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.STRING.fieldOf("species").orElse("").forGetter(properties -> valueOrDefault(properties.getSpecies(), "")),
        Codec.STRING.fieldOf("gender").orElse("").forGetter(PropertiesAdapter::genderAdapter),
        Codec.STRING.fieldOf("ability").orElse("").forGetter(properties -> valueOrDefault(properties.getAbility(), "")),
        Codec.STRING.fieldOf("nature").orElse("").forGetter(properties -> valueOrDefault(properties.getNature(), "")),
        Codec.INT.fieldOf("level").orElse(-1).forGetter(properties -> valueOrDefault(properties.getLevel(), -1)),
        Codec.either(Codec.STRING, Codec.STRING.listOf()).xmap(either -> either.map(List::of, s -> s), Either::right)
            .listOf().optionalFieldOf("moves").forGetter(PropertiesAdapter::moveAdapter),
        Codec.INT.fieldOf("min_perfect_ivs").orElse(-1).forGetter(properties -> valueOrDefault(properties.getMinPerfectIVs(), -1)),
        EV_CODEC.optionalFieldOf("evs").forGetter(properties -> optionalAdapter(properties, PokemonProperties::getEvs)),
        Codec.STRING.fieldOf("held_item").orElse("").forGetter(properties -> valueOrDefault(properties.getHeldItem(), "")),
        Codec.STRING.listOf()
            .xmap(list -> (Set<String>) new HashSet<>(list), ArrayList::new)
            .fieldOf("aspects").orElse(new HashSet<>())
            .forGetter(properties -> valueOrDefault(properties.getAspects(), new HashSet<>())),
        Codec.STRING.fieldOf("form").orElse("").forGetter(properties -> valueOrDefault(properties.getForm(), "")),
        Codec.INT.fieldOf("dmax_level").orElse(-1).forGetter(properties -> valueOrDefault(properties.getDmaxLevel(), -1)),
        Codec.BOOL.fieldOf("gmax").orElse(false).forGetter(properties -> valueOrDefault(properties.getGmaxFactor(), false)),
        Codec.STRING.optionalFieldOf("tera_type").forGetter(properties -> optionalAdapter(properties, PokemonProperties::getTeraType)),
        FEATURE_CODEC.listOf()
            .xmap(
                list -> list.stream().map(feature -> (CustomPokemonProperty) feature).toList(),
                list -> list.stream().map(feature -> (SpeciesFeature) feature).toList()
            )
            .fieldOf("custom_properties")
            .orElse(new ArrayList<>())
            .forGetter(PokemonProperties::getCustomProperties)
    ).apply(inst, (species, gender, ability, nature, level, moves, minIvs, evs, heldItem, aspects, form, dmaxLevel, gmax, tera, customProperties) -> {
        PokemonProperties properties = PokemonProperties.Companion.parse("");
        if (!ability.isBlank()) properties.setAbility(ability);
        if (dmaxLevel >= 0) properties.setDmaxLevel(dmaxLevel);
        evs.ifPresent(properties::setEvs);
        if (!form.isBlank()) properties.setForm(form);
        try { if (!gender.isBlank()) properties.setGender(Gender.valueOf(gender)); }
        catch (IllegalArgumentException ignored) {}
        if (gmax) properties.setGmaxFactor(true);
        if (!heldItem.isBlank()) properties.setHeldItem(heldItem);
        if (level > 0) properties.setLevel(level);
        moves.ifPresent(builder -> ((IProperties) properties).crd_setMovesetBuilder(builder));
        if (minIvs >= 0) properties.setMinPerfectIVs(Math.clamp(minIvs, 0, 6));
        if (!nature.isBlank()) properties.setNature(nature);
        if (!species.isBlank()) properties.setSpecies(species);
        tera.ifPresent(properties::setTeraType);

        if (!aspects.isEmpty()) properties.setAspects(aspects);
        if (!customProperties.isEmpty()) properties.setCustomProperties(new ArrayList<>(customProperties));

        return properties;
    }));

    public static PokemonProperties apply(PokemonProperties base, PokemonProperties extra) {
        PokemonProperties properties = new PokemonProperties();
        properties.setAbility(extra.getAbility() == null ? base.getAbility() : extra.getAbility());
        properties.setDmaxLevel(extra.getDmaxLevel() == null ? base.getDmaxLevel() : extra.getDmaxLevel());
        properties.setEvs(extra.getEvs() == null ? base.getEvs() : extra.getEvs());
        properties.setForm(extra.getForm() == null ? base.getForm() : extra.getForm());
        properties.setGender(extra.getGender() == null ? base.getGender() : extra.getGender());
        properties.setGmaxFactor(extra.getGmaxFactor() == null ? base.getGmaxFactor() : extra.getGmaxFactor());
        properties.setHeldItem(extra.getHeldItem() == null ? base.getHeldItem() : extra.getHeldItem());
        properties.setLevel(extra.getLevel() == null ? base.getLevel() : extra.getLevel());
        ((IProperties) properties).crd_setMovesetBuilder(((IProperties) extra).crd_getMovesetBuilder() == null ? ((IProperties) base).crd_getMovesetBuilder() : ((IProperties) extra).crd_getMovesetBuilder());
        properties.setMinPerfectIVs(extra.getMinPerfectIVs() == null ? base.getMinPerfectIVs() : extra.getMinPerfectIVs());
        properties.setNature(extra.getNature() == null ? base.getNature() : extra.getNature());
        properties.setSpecies(extra.getSpecies() == null ? base.getSpecies() : extra.getSpecies());
        properties.setTeraType(extra.getTeraType() == null ? base.getTeraType() : extra.getTeraType());

        Set<String> aspects = new HashSet<>(base.getAspects());
        aspects.addAll(extra.getAspects());
        properties.setAspects(aspects);

        List<CustomPokemonProperty> customProperties = new ArrayList<>(base.getCustomProperties());
        customProperties.addAll(extra.getCustomProperties());
        properties.setCustomProperties(customProperties);

        return properties;
    }

    @Override
    public JsonElement serialize(PokemonProperties src, Type typeOfSrc, JsonSerializationContext context) {
        return CODEC.encodeStart(JsonOps.INSTANCE, src).getOrThrow();
    }

    @Override
    public PokemonProperties deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
    }
}
