package com.necro.raid.dens.common.registry.generator;

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
        if (species == null) return;
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
        int count = 0;
        for (Species species : PokemonSpecies.getImplemented()) {
            // TODO: Add conditional checks/whitelists/blacklists

            String name = species.getResourceIdentifier().getPath().toLowerCase(Locale.ROOT);

            // TODO: Loop forms instead of species

            if (this.registered.contains(name)) continue;
            int bst = species.getBaseStats().values().stream().reduce(Integer::sum).orElse(0);

            // TODO: Disable raids for Pokemon with no valid moves

            RaidType type = RaidType.fromType(species.getPrimaryType());
            RaidTier tier;
            // TODO: Configurable tier thresholds
            if (bst <= 255) tier = RaidTier.TIER_ONE;
            else if (bst <= 320) tier = RaidTier.TIER_TWO;
            else if (bst <= 420) tier = RaidTier.TIER_THREE;
            else if (bst <= 510) tier = RaidTier.TIER_FOUR;
            else tier = RaidTier.TIER_FIVE;

            RaidBoss boss = new RaidBoss();
            boss.setReward(PokemonProperties.Companion.parse(name));
            boss.setType(type);
            boss.setTier(tier);
            boss.setId(ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name));
            boss.applyDefaults();
            CustomRaidRegistries.RAID_REGISTRY.register(boss);

            CobblemonRaidDens.LOGGER.info("Generated {}", boss.getId());
            count++;
        }

        this.registered.clear();

        CobblemonRaidDens.LOGGER.info("Generated {} raid bosses", count);
        this.registerRaids();
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
