package com.necro.raid.dens.common.raids.moves;

import com.cobblemon.mod.common.api.moves.MoveSelector;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.MovesetBuilder;
import com.cobblemon.mod.common.api.moves.categories.DamageCategories;
import com.cobblemon.mod.common.api.moves.categories.DamageCategory;
import com.cobblemon.mod.common.api.pokemon.moves.Learnset;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.pokemon.FormData;
import com.necro.raid.dens.common.util.DoubleWeightedRandomMap;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FunctionalInterface
public interface RaidMoveSelector extends MoveSelector {
    MoveSelector BEST_LEVEL_UP_PRIMARY = (form, learnset, level, chosenMoves) ->
        getBest(getLevelUpMoves(learnset, level, getSuitableCategories(form), chosenMoves, form.getPrimaryType()), form);

    MoveSelector BEST_LEVEL_UP_SECONDARY = (form, learnset, level, chosenMoves) -> {
        if (form.getSecondaryType() == null) return null;
        return getBest(getLevelUpMoves(learnset, level, getSuitableCategories(form), chosenMoves, form.getSecondaryType()), form);
    };

    MoveSelector BEST_LEVEL_UP_ANY = (form, learnset, level, chosenMoves) ->
        getBest(getLevelUpMoves(learnset, level, getSuitableCategories(form), chosenMoves, null), form);

    MoveSelector BEST_TM_PRIMARY = (form, learnset, level, chosenMoves) ->
        getBest(getTMMoves(learnset, getSuitableCategories(form), chosenMoves, form.getPrimaryType()), form);

    MoveSelector BEST_TM_SECONDARY = (form, learnset, level, chosenMoves) -> {
        if (form.getSecondaryType() == null) return null;
        return getBest(getTMMoves(learnset, getSuitableCategories(form), chosenMoves, form.getSecondaryType()), form);
    };

    MoveSelector BEST_TM_ANY = (form, learnset, level, chosenMoves) ->
        getBest(getTMMoves(learnset, getSuitableCategories(form), chosenMoves, null), form);

    MoveSelector BEST_MOVE_PRIMARY = (form, learnset, level, chosenMoves) ->
        getBest(getTMOrLevelUpMoves(learnset, level, getSuitableCategories(form), chosenMoves, form.getPrimaryType()), form);

    MoveSelector BEST_MOVE_SECONDARY = (form, learnset, level, chosenMoves) -> {
        if (form.getSecondaryType() == null) return null;
        return getBest(getLevelUpMoves(learnset, level, getSuitableCategories(form), chosenMoves, form.getSecondaryType()), form);
    };

    MoveSelector BEST_MOVE_ANY = (form, learnset, level, chosenMoves) ->
        getBest(getTMOrLevelUpMoves(learnset, level, getSuitableCategories(form), chosenMoves, null), form);

    MoveSelector RANDOM_LEVEL_UP_COVERAGE = (form, learnset, level, chosenMoves) ->
        getWeightedRandom(getLevelUpCoverage(learnset, level, getSuitableCategories(form), chosenMoves), form);

    MoveSelector RANDOM_TM_COVERAGE = (form, learnset, level, chosenMoves) ->
        getWeightedRandom(getTMCoverage(learnset, getSuitableCategories(form), chosenMoves), form);

    MoveSelector RANDOM_MOVE_COVERAGE = (form, learnset, level, chosenMoves) ->
        getWeightedRandom(getTMOrLevelUpCoverage(learnset, level, getSuitableCategories(form), chosenMoves), form);

    MoveSelector RANDOM_OFFENSIVE_TM = (form, learnset, level, chosenMoves) ->
        getWeightedRandom(getTMMoves(learnset, getSuitableCategories(form), chosenMoves, null), form);

    private static Set<DamageCategory> getSuitableCategories(FormData form) {
        if (Math.abs(form.getBaseStats().get(Stats.ATTACK) - form.getBaseStats().get(Stats.SPECIAL_ATTACK)) <= 10)
            return Set.of(DamageCategories.INSTANCE.getPHYSICAL(), DamageCategories.INSTANCE.getSPECIAL());
        else if (form.getBaseStats().get(Stats.ATTACK) > form.getBaseStats().get(Stats.SPECIAL_ATTACK))
            return Set.of(DamageCategories.INSTANCE.getPHYSICAL());
        else
            return Set.of(DamageCategories.INSTANCE.getSPECIAL());
    }

    private static Set<MoveTemplate> getLevelUpMoves(Learnset learnset, int level, Set<DamageCategory> suitableCategories, Set<? extends MoveTemplate> chosenMoves, ElementalType type) {
        return filterMoves(learnset.getLevelUpMovesUpTo(level).stream(), suitableCategories, chosenMoves, type);
    }

    private static Set<MoveTemplate> getTMMoves(Learnset learnset, Set<DamageCategory> suitableCategories, Set<? extends MoveTemplate> chosenMoves, ElementalType type) {
        return filterMoves(learnset.getTmMoves().stream(), suitableCategories, chosenMoves, type);
    }

    private static Set<MoveTemplate> getTMOrLevelUpMoves(Learnset learnset, int level, Set<DamageCategory> suitableCategories, Set<? extends MoveTemplate> chosenMoves, ElementalType type) {
        Set<MoveTemplate> moves = learnset.getLevelUpMovesUpTo(level);
        moves.addAll(learnset.getTmMoves());
        return filterMoves(moves.stream(), suitableCategories, chosenMoves, type);
    }

    private static Set<MoveTemplate> getLevelUpCoverage(Learnset learnset, int level, Set<DamageCategory> suitableCategories, Set<? extends MoveTemplate> chosenMoves) {
        return filterCoverage(learnset.getLevelUpMovesUpTo(level).stream(), suitableCategories, chosenMoves);
    }

    private static Set<MoveTemplate> getTMCoverage(Learnset learnset, Set<DamageCategory> suitableCategories, Set<? extends MoveTemplate> chosenMoves) {
        return filterCoverage(learnset.getTmMoves().stream(), suitableCategories, chosenMoves);
    }

    private static Set<MoveTemplate> getTMOrLevelUpCoverage(Learnset learnset, int level, Set<DamageCategory> suitableCategories, Set<? extends MoveTemplate> chosenMoves) {
        Set<MoveTemplate> moves = learnset.getLevelUpMovesUpTo(level);
        moves.addAll(learnset.getTmMoves());
        return filterCoverage(moves.stream(), suitableCategories, chosenMoves);
    }

    private static Set<MoveTemplate> filterMoves(Stream<MoveTemplate> learnset, Set<DamageCategory> suitableCategories, Set<? extends MoveTemplate> chosenMoves, ElementalType type) {
        return learnset
            .filter(move -> {
                if (suitableCategories != null && !suitableCategories.contains(move.getDamageCategory())) return false;
                else if (!chosenMoves.isEmpty() && chosenMoves.contains(move)) return false;
                return type == null || move.getElementalType() == type;
            })
            .collect(Collectors.toSet());
    }

    private static Set<MoveTemplate> filterCoverage(Stream<MoveTemplate> learnset, Set<DamageCategory> suitableCategories, Set<? extends MoveTemplate> chosenMoves) {
        return learnset
            .filter(move -> {
                if (suitableCategories != null && !suitableCategories.contains(move.getDamageCategory())) return false;
                else if (!chosenMoves.isEmpty() && chosenMoves.contains(move)) return false;
                return chosenMoves.stream().noneMatch(mv -> move.getElementalType() == mv.getElementalType());
            })
            .collect(Collectors.toSet());
    }

    private static MoveTemplate getBest(Set<MoveTemplate> moves, FormData form) {
        return moves.stream().max(Comparator.comparingDouble(move -> getMoveWeight(move, form))).orElse(null);
    }

    private static MoveTemplate getWeightedRandom(Set<MoveTemplate> moves, FormData form) {
        DoubleWeightedRandomMap<MoveTemplate> weightedMoves = new DoubleWeightedRandomMap<>();
        for (MoveTemplate move : moves) weightedMoves.add(move, getMoveWeight(move, form));
        return weightedMoves.getRandom(Math.random()).orElse(null);
    }

    private static double getMoveWeight(MoveTemplate move, FormData form) {
        if (RaidMovesetBuilder.RECHARGE_MOVES.contains(move.getName())) return 0d;

        double base = move.getPower() * move.getAccuracy();
        double multiplier = form.getSignatureMoves().contains(move) ? MovesetBuilder.Companion.getSignatureMoveWeightMultiplier() : 1d;
        if (RaidMovesetBuilder.DEBUFF_MOVES.contains(move.getName())) multiplier *= 0.5d;
        return base * multiplier;
    }

    static void init() {
        MoveSelector.Companion.getSelectors().put("level_up_primary", BEST_LEVEL_UP_PRIMARY);
        MoveSelector.Companion.getSelectors().put("level_up_secondary", BEST_LEVEL_UP_SECONDARY);
        MoveSelector.Companion.getSelectors().put("level_up_any", BEST_LEVEL_UP_ANY);
        MoveSelector.Companion.getSelectors().put("tm_primary", BEST_TM_PRIMARY);
        MoveSelector.Companion.getSelectors().put("tm_secondary", BEST_TM_SECONDARY);
        MoveSelector.Companion.getSelectors().put("tm_any", BEST_TM_ANY);
        MoveSelector.Companion.getSelectors().put("move_primary", BEST_MOVE_PRIMARY);
        MoveSelector.Companion.getSelectors().put("move_secondary", BEST_MOVE_SECONDARY);
        MoveSelector.Companion.getSelectors().put("move_any", BEST_MOVE_ANY);
        MoveSelector.Companion.getSelectors().put("level_up_coverage", RANDOM_LEVEL_UP_COVERAGE);
        MoveSelector.Companion.getSelectors().put("tm_coverage", RANDOM_TM_COVERAGE);
        MoveSelector.Companion.getSelectors().put("move_coverage", RANDOM_MOVE_COVERAGE);
        MoveSelector.Companion.getSelectors().put("offensive_tm", RANDOM_OFFENSIVE_TM);
    }
}
