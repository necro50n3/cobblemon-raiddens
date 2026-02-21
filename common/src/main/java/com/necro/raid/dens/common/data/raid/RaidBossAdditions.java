package com.necro.raid.dens.common.data.raid;

import com.cobblemon.mod.common.api.mark.Mark;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.adapters.PropertiesAdapter;
import com.necro.raid.dens.common.data.adapters.RaidBossAdapter;
import com.necro.raid.dens.common.registry.RaidRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class RaidBossAdditions {
    public static final Gson GSON;
    private static final ResourceLocation BLACKLIST = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "additions_blacklist");

    private Integer priority;
    private List<String> include;
    private List<String> exclude;
    @SuppressWarnings("all")
    private RaidBoss additions;
    private Boolean replace;
    private String suffix;
    @SerializedName("force_apply")
    private Boolean forceApply;

    public void applyDefaults() {
        if (this.additions == null) throw new JsonSyntaxException("Missing required field: \"additions\"");

        if (this.priority == null) this.priority = 0;
        if (this.include == null) this.include = new ArrayList<>();
        if (this.exclude == null) this.exclude = new ArrayList<>();
        if (this.replace == null) this.replace = true;
        if (this.suffix == null) this.suffix = "";
        if (!this.replace && !this.suffix.startsWith("_")) this.suffix = "_" + this.suffix;
        if (this.forceApply == null) this.forceApply = false;
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

        for (ResourceLocation loc : targets) {
            if (excluded.contains(loc)) continue;
            RaidBoss temp = RaidRegistry.getRaidBoss(loc);
            if (temp == null) continue;
            ResourceLocation id = temp.getId();
            if (!this.replace() && !this.forceApply() && RaidRegistry.isTag(BLACKLIST, id)) continue;
            final RaidBoss boss = this.replace() ? temp : temp.copy();

            getReward(this.additions()).ifPresent(properties -> boss.setReward(PropertiesAdapter.apply(boss.getReward(), properties)));
            getBoss(this.additions()).ifPresent(properties -> boss.setBoss(PropertiesAdapter.apply(boss.getBoss(), properties)));
            getTier(this.additions()).ifPresent(boss::setTier);
            getType(this.additions()).ifPresent(boss::setType);
            getFeature(this.additions()).ifPresent(boss::setFeature);
            getLootTable(this.additions()).ifPresent(boss::setLootTable);
            getWeight(this.additions()).ifPresent(weight -> boss.setWeight(boss.getWeight() * weight));
            getDens(this.additions()).ifPresent(boss::setDens);
            getKey(this.additions()).ifPresent(boss::setKey);
            getBossBarText(this.additions()).ifPresent(boss::setBossBarText);
            getScale(this.additions()).ifPresent(boss::setScale);

            getMaxPlayers(this.additions()).ifPresent(boss::setMaxPlayers);
            getMaxClears(this.additions()).ifPresent(boss::setMaxClears);
            getHaRate(this.additions()).ifPresent(boss::setHaRate);
            getMaxCheers(this.additions()).ifPresent(boss::setMaxCheers);
            getRaidPartySize(this.additions()).ifPresent(boss::setRaidPartySize);
            getHealthMulti(this.additions()).ifPresent(boss::setHealthMulti);
            getMultiplayerHealthMulti(this.additions()).ifPresent(boss::setMultiplayerHealthMulti);
            getShinyRate(this.additions()).ifPresent(boss::setShinyRate);
            getCurrency(this.additions()).ifPresent(boss::setCurrency);
            getMaxCatches(this.additions()).ifPresent(boss::setMaxCatches);
            getScript(this.additions()).ifPresent(boss::setScript);
            getRaidAI(this.additions()).ifPresent(boss::setRaidAI);
            getMarks(this.additions()).ifPresent(boss::setMarks);
            getLives(this.additions()).ifPresent(boss::setLives);
            getPlayersShareLives(this.additions()).ifPresent(boss::setPlayersShareLives);
            getEnergy(this.additions()).ifPresent(boss::setEnergy);
            getRequiredDamage(this.additions()).ifPresent(boss::setRequiredDamage);

            boss.clearCaches();
            boss.applyAspects();

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

    public boolean replace() {
        return this.replace;
    }

    private String suffix() {
        return this.suffix;
    }

    public boolean forceApply() {
        return this.forceApply;
    }

    private static Optional<PokemonProperties> getReward(RaidBoss boss) {
        return Optional.ofNullable(boss.getReward());
    }

    private static Optional<PokemonProperties> getBoss(RaidBoss boss) {
        return Optional.ofNullable(boss.getBoss());
    }

    private static Optional<RaidTier> getTier(RaidBoss boss) {
        return Optional.ofNullable(boss.getTier());
    }

    private static Optional<RaidFeature> getFeature(RaidBoss boss) {
        return Optional.ofNullable(boss.getFeature());
    }

    private static Optional<RaidType> getType(RaidBoss boss) {
        return Optional.ofNullable(boss.getType());
    }

    private static Optional<BossLootTable> getLootTable(RaidBoss boss) {
        return Optional.ofNullable(boss.getLootTable());
    }

    private static Optional<Double> getWeight(RaidBoss boss) {
        return Optional.ofNullable(boss.getWeight());
    }

    private static Optional<List<String>> getDens(RaidBoss boss) {
        return Optional.ofNullable(boss.getDens());
    }

    private static Optional<UniqueKey> getKey(RaidBoss boss) {
        return Optional.ofNullable(boss.getKey());
    }

    private static Optional<Component> getBossBarText(RaidBoss boss) {
        return Optional.ofNullable(boss.getBossBarText());
    }

    private static Optional<Float> getScale(RaidBoss boss) {
        return Optional.ofNullable(boss.getScale());
    }

    private static Optional<Integer> getMaxPlayers(RaidBoss boss) {
        return Optional.ofNullable(boss.getMaxPlayers());
    }

    private static Optional<Integer> getMaxClears(RaidBoss boss) {
        return Optional.ofNullable(boss.getMaxClears());
    }

    private static Optional<Double> getHaRate(RaidBoss boss) {
        return Optional.ofNullable(boss.getHaRate());
    }

    private static Optional<Integer> getMaxCheers(RaidBoss boss) {
        return Optional.ofNullable(boss.getMaxCheers());
    }

    private static Optional<Integer> getRaidPartySize(RaidBoss boss) {
        return Optional.ofNullable(boss.getRaidPartySize());
    }

    private static Optional<Integer> getHealthMulti(RaidBoss boss) {
        return Optional.ofNullable(boss.getHealthMulti());
    }

    private static Optional<Float> getMultiplayerHealthMulti(RaidBoss boss) {
        return Optional.ofNullable(boss.getMultiplayerHealthMulti());
    }

    private static Optional<Float> getShinyRate(RaidBoss boss) {
        return Optional.ofNullable(boss.getShinyRate());
    }

    private static Optional<Integer> getCurrency(RaidBoss boss) {
        return Optional.ofNullable(boss.getCurrency());
    }

    private static Optional<Integer> getMaxCatches(RaidBoss boss) {
        return Optional.ofNullable(boss.getMaxCatches());
    }

    private static Optional<Map<String, Script>> getScript(RaidBoss boss) {
        return Optional.ofNullable(boss.getScript());
    }

    private static Optional<RaidAI> getRaidAI(RaidBoss boss) {
        return Optional.ofNullable(boss.getRaidAI());
    }

    private static Optional<List<Mark>> getMarks(RaidBoss boss) {
        return Optional.ofNullable(boss.getMarks());
    }

    private static Optional<Integer> getLives(RaidBoss boss) {
        return Optional.ofNullable(boss.getLives());
    }

    private static Optional<Boolean> getPlayersShareLives(RaidBoss boss) {
        return Optional.ofNullable(boss.getPlayersShareLives());
    }

    private static Optional<Integer> getEnergy(RaidBoss boss) {
        return Optional.ofNullable(boss.getEnergy());
    }

    private static Optional<Float> getRequiredDamage(RaidBoss boss) {
        return Optional.ofNullable(boss.getRequiredDamage());
    }

    static {
        GSON = new GsonBuilder()
            .registerTypeAdapter(RaidBoss.class, new RaidBossAdapter())
            .create();
    }
}
