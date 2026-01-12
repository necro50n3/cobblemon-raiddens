package com.necro.raid.dens.common.data.support;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;
import java.util.stream.Collectors;

public record RaidSupport(String move, Map<Stat, Integer> boosts, float heal, boolean cure) {
    public boolean sendUpdate() {
        return !this.boosts.isEmpty() || this.heal > 0 || this.cure;
    }

    public static Codec<RaidSupport> codec() {
        return RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("move").forGetter(RaidSupport::move),
            Codec.unboundedMap(Codec.STRING, Codec.INT)
                .xmap(RaidSupport::convertToStat, RaidSupport::convertFromStat)
                .fieldOf("boosts").orElse(Map.of()).forGetter(RaidSupport::boosts),
            Codec.FLOAT.fieldOf("heal").orElse(0f).forGetter(RaidSupport::heal),
            Codec.BOOL.fieldOf("cure").orElse(false).forGetter(RaidSupport::cure)
        ).apply(inst, RaidSupport::new));
    }

    private static Map<Stat, Integer> convertToStat(Map<String, Integer> map) {
        return map.entrySet().stream()
            .map(e -> Map.entry(Stats.Companion.getStat(e.getKey()), e.getValue()))
            .filter(e -> e.getKey() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<String, Integer> convertFromStat(Map<Stat, Integer> map) {
        return map.entrySet().stream()
            .map(e -> Map.entry(e.getKey().getShowdownId(), e.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
