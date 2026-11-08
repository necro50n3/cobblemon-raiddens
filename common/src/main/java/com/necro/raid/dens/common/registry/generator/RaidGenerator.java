package com.necro.raid.dens.common.registry.generator;

import com.cobblemon.mod.common.api.moves.categories.DamageCategories;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Species;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidBossAdditions;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class RaidGenerator {
    public static final RaidGenerator INSTANCE = new RaidGenerator();

    private final Set<String> registered;
    private final List<RaidBossAdditions> additions;

    public RaidGenerator() {
        this.registered = new HashSet<>();
        this.additions = new ArrayList<>();
    }

    public void add(String species) {
        if (species == null || !CobblemonRaidDens.GENERATOR_CONFIG.enabled) return;
        species = (species.contains(":") ? species.split(":")[1] : species).toLowerCase(Locale.ROOT);
        this.registered.add(species);
    }

    public void add(RaidBossAdditions additions) {
        this.additions.add(additions);
    }

    public int additionsSize() {
        return this.additions.size();
    }

    public void generate() {
        if (!CobblemonRaidDens.GENERATOR_CONFIG.enabled) return;
        GeneratorHelper.init();

        boolean isWhitelistEmpty = GeneratorHelper.WHITELIST.isEmpty();

        int count = 0;
        for (Species species : PokemonSpecies.getImplemented()) {
            String name = species.getResourceIdentifier().getPath().toLowerCase(Locale.ROOT);

            if (this.registered.contains(name)) continue;
            else if (isWhitelistEmpty) {
                if (GeneratorHelper.BLACKLIST.contains(name.toLowerCase(Locale.ROOT))) continue;
                else if (GeneratorHelper.LABELS.stream().anyMatch(label -> species.getLabels().contains(label))) continue;
            }
            else if (!GeneratorHelper.WHITELIST.contains(name.toLowerCase(Locale.ROOT))) continue;

            int bst = species.getBaseStats().values().stream().reduce(Integer::sum).orElse(0);

            RaidType type = RaidType.fromType(species.getPrimaryType());
            RaidTier tier;
            if (species.getLabels().contains("legendary") || species.getLabels().contains("mythical")) tier = RaidTier.TIER_SEVEN;
            else if (species.getLabels().contains("ultra_beast") || species.getLabels().contains("paradox")) tier = RaidTier.TIER_SIX;
            else if (bst >= CobblemonRaidDens.GENERATOR_CONFIG.tier_five_bst_threshold) tier = RaidTier.TIER_FIVE;
            else if (bst >= CobblemonRaidDens.GENERATOR_CONFIG.tier_four_bst_threshold) tier = RaidTier.TIER_FOUR;
            else if (bst >= CobblemonRaidDens.GENERATOR_CONFIG.tier_three_bst_threshold) tier = RaidTier.TIER_THREE;
            else if (bst >= CobblemonRaidDens.GENERATOR_CONFIG.tier_two_bst_threshold) tier = RaidTier.TIER_TWO;
            else if (bst >= CobblemonRaidDens.GENERATOR_CONFIG.tier_one_bst_threshold) tier = RaidTier.TIER_ONE;
            else continue;

            int level = CobblemonRaidDens.TIER_CONFIG.get(tier).bossLevel();
            int moveCount = (int) species.getMoves().getLevelUpMovesUpTo(level).stream()
                .filter(move -> move.getDamageCategory() != DamageCategories.INSTANCE.getSTATUS())
                .count();
            if (moveCount == 0) continue;

            RaidBoss boss = new RaidBoss();
            boss.setReward(PokemonProperties.Companion.parse(name));
            boss.setType(type);
            boss.setTier(tier);
            boss.setId(ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name));
            boss.applyDefaults();
            CustomRaidRegistries.RAID_REGISTRY.addTags(boss.getId(), Set.of(boss.getId()));
            CustomRaidRegistries.RAID_REGISTRY.register(boss);

            CobblemonRaidDens.LOGGER.info("Generated {}", boss.getId());
            count++;
        }

        this.registered.clear();
        CobblemonRaidDens.LOGGER.info("Generated {} raid bosses", count);
    }

    public void registerRaids() {
        List<ResourceLocation> registry = new ArrayList<>(CustomRaidRegistries.RAID_REGISTRY.getAll());
        this.additions.sort(Comparator.comparingInt(RaidBossAdditions::priority).reversed());
        this.additions.forEach(additions -> {
            if (additions.replace() || additions.forceApply()) additions.apply(CustomRaidRegistries.RAID_REGISTRY.getAll());
            else additions.apply(registry);
        });

        CustomRaidRegistries.RAID_REGISTRY.registerAll();
        RaidTier.updateRandom();
        CustomRaidRegistries.RAID_REGISTRY.clearReverseLookup();

        this.additions.clear();
    }
}
