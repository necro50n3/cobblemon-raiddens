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
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.*;

import static com.cobblemon.mod.common.util.MiscUtilsKt.cobblemonResource;

public class PropertiesAdapter implements JsonSerializer<PokemonProperties>, JsonDeserializer<PokemonProperties> {
    private static String genderAdapter(PokemonProperties properties) {
        Gender gender = properties.getGender();
        return gender == null ? "" : gender.getSerializedName();
    }

    private static Optional<EVs> evsAdapter(PokemonProperties properties) {
        return Optional.ofNullable(properties.getEvs());
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

    private static final Codec<EVs> EV_CODEC = Codec.unboundedMap(STAT_CODEC, Codec.intRange(0, EVs.MAX_STAT_VALUE))
        .comapFlatMap(
            map -> {
                if (map.values().stream().mapToInt(Integer::intValue).sum() > EVs.MAX_TOTAL_VALUE) return DataResult.error(() -> "EVs cannot exceed a total of " + EVs.MAX_TOTAL_VALUE);
                EVs evs = Cobblemon.INSTANCE.getStatProvider().createEmptyEVs();
                map.forEach(evs::set);
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
        Codec.STRING.fieldOf("species").orElse("").forGetter(PokemonProperties::getSpecies),
        Codec.STRING.fieldOf("gender").orElse("").forGetter(PropertiesAdapter::genderAdapter),
        Codec.STRING.fieldOf("ability").orElse("").forGetter(PokemonProperties::getAbility),
        Codec.STRING.fieldOf("nature").orElse("").forGetter(PokemonProperties::getNature),
        Codec.INT.fieldOf("level").orElse(-1).forGetter(PokemonProperties::getLevel),
        Codec.STRING.listOf().fieldOf("moves").orElse(new ArrayList<>()).forGetter(PokemonProperties::getMoves),
        Codec.INT.fieldOf("min_perfect_ivs").orElse(-1).forGetter(PokemonProperties::getMinPerfectIVs),
        EV_CODEC.optionalFieldOf("evs").forGetter(PropertiesAdapter::evsAdapter),
        Codec.STRING.fieldOf("held_item").orElse("").forGetter(PokemonProperties::getHeldItem),
        Codec.STRING.listOf()
            .xmap(list -> (Set<String>) new HashSet<>(list), ArrayList::new)
            .fieldOf("aspects").orElse(new HashSet<>())
            .forGetter(PokemonProperties::getAspects),
        Codec.STRING.fieldOf("form").orElse("").forGetter(PokemonProperties::getForm),
        FEATURE_CODEC.listOf()
            .xmap(
                list -> list.stream().filter(CustomPokemonProperty.class::isInstance).map(CustomPokemonProperty.class::cast).toList(),
                list -> list.stream().filter(SpeciesFeature.class::isInstance).map(SpeciesFeature.class::cast).toList()
            )
            .fieldOf("custom_properties")
            .orElse(new ArrayList<>())
            .forGetter(PokemonProperties::getCustomProperties)
    ).apply(inst, (species, gender, ability, nature, level, moves, minIvs, evs, heldItem, aspects, form, customProperties) -> {
        PokemonProperties properties = PokemonProperties.Companion.parse("");
        if (!species.isBlank()) properties.setSpecies(species);
        try { if (!gender.isBlank()) properties.setGender(Gender.valueOf(gender)); }
        catch (IllegalArgumentException ignored) {}
        if (!ability.isBlank()) properties.setAbility(ability);
        if (!nature.isBlank()) properties.setNature(nature);
        if (level > 0) properties.setLevel(level);
        if (!moves.isEmpty()) properties.setMoves(moves);
        if (minIvs >= 0) properties.setMinPerfectIVs(minIvs);
        evs.ifPresent(properties::setEvs);
        if (!heldItem.isBlank()) properties.setHeldItem(heldItem);
        if (!aspects.isEmpty()) properties.setAspects(aspects);
        if (!form.isBlank()) properties.setForm(form);
        if (!customProperties.isEmpty()) properties.setCustomProperties(customProperties);
        return properties;
    }));

    public static PokemonProperties apply(PokemonProperties base, PokemonProperties extra) {
        PokemonProperties properties = base.copy();
        if (extra.getAbility() != null) properties.setAbility(extra.getAbility());
        if (extra.getEvs() != null) properties.setEvs(extra.getEvs());
        if (extra.getForm() != null) properties.setForm(extra.getForm());
        if (extra.getGender() != null) properties.setFullness(extra.getFullness());
        if (extra.getHeldItem() != null) properties.setHeldItem(extra.getHeldItem());
        if (extra.getLevel() != null) properties.setLevel(extra.getLevel());
        if (extra.getMoves() != null) properties.setMoves(extra.getMoves());
        if (extra.getMinPerfectIVs() != null) properties.setMinPerfectIVs(extra.getMinPerfectIVs());
        if (extra.getNature() != null) properties.setNature(extra.getNature());
        if (extra.getSpecies() != null) properties.setSpecies(extra.getSpecies());

        Set<String> aspects = new HashSet<>(properties.getAspects());
        aspects.addAll(extra.getAspects());
        properties.setAspects(aspects);

        List<CustomPokemonProperty> customProperties = new ArrayList<>(properties.getCustomProperties());
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
