package com.necro.raid.dens.common.data.raid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.adapters.PropertiesAdapter;
import com.necro.raid.dens.common.data.adapters.RaidBossAdapter;
import com.necro.raid.dens.common.registry.RaidRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Supplier;

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

            getOptional(this.additions()::getReward).ifPresent(properties -> boss.setReward(PropertiesAdapter.apply(boss.getReward(), properties)));
            getOptional(this.additions()::getBoss).ifPresent(properties -> boss.setBoss(PropertiesAdapter.apply(boss.getBoss(), properties)));
            getOptional(this.additions()::getTier).ifPresent(boss::setTier);
            getOptional(this.additions()::getType).ifPresent(boss::setType);
            getOptional(this.additions()::getFeature).ifPresent(boss::setFeature);
            getOptional(this.additions()::getLootTable).ifPresent(boss::setLootTable);
            getOptional(this.additions()::getWeight).ifPresent(weight -> boss.setWeight(boss.getWeight() * weight));
            getOptional(this.additions()::getDens).ifPresent(boss::setDens);
            getOptional(this.additions()::getKey).ifPresent(boss::setKey);
            getOptional(this.additions()::getBossBarText).ifPresent(boss::setBossBarText);
            getOptional(this.additions()::getScale).ifPresent(boss::setScale);
            getOptional(this.additions()::getForceDynamax).ifPresent(boss::setForceDynamax);
            getOptional(this.additions()::getMusic).ifPresent(boss::setMusic);
            getOptional(this.additions()::getNoAi).ifPresent(boss::setNoAi);

            getOptional(this.additions()::getMaxPlayers).ifPresent(boss::setMaxPlayers);
            getOptional(this.additions()::getMaxClears).ifPresent(boss::setMaxClears);
            getOptional(this.additions()::getHaRate).ifPresent(boss::setHaRate);
            getOptional(this.additions()::getMaxCheers).ifPresent(boss::setMaxCheers);
            getOptional(this.additions()::getRaidPartySize).ifPresent(boss::setRaidPartySize);
            getOptional(this.additions()::getHealthMulti).ifPresent(boss::setHealthMulti);
            getOptional(this.additions()::getMultiplayerHealthMulti).ifPresent(boss::setMultiplayerHealthMulti);
            getOptional(this.additions()::getShinyRate).ifPresent(boss::setShinyRate);
            getOptional(this.additions()::getCurrency).ifPresent(boss::setCurrency);
            getOptional(this.additions()::getMaxCatches).ifPresent(boss::setMaxCatches);
            getOptional(this.additions()::getScript).ifPresent(boss::setScript);
            getOptional(this.additions()::getRaidAI).ifPresent(boss::setRaidAI);
            getOptional(this.additions()::getMarks).ifPresent(boss::setMarks);
            getOptional(this.additions()::getLives).ifPresent(boss::setLives);
            getOptional(this.additions()::getPlayersShareLives).ifPresent(boss::setPlayersShareLives);
            getOptional(this.additions()::getEnergy).ifPresent(boss::setEnergy);
            getOptional(this.additions()::getRequiredDamage).ifPresent(boss::setRequiredDamage);
            getOptional(this.additions()::getCatchRate).ifPresent(boss::setCatchRate);

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

    private static <T> Optional<T> getOptional(Supplier<T> supplier) {
        return Optional.ofNullable(supplier.get());
    }

    static {
        GSON = new GsonBuilder()
            .registerTypeAdapter(RaidBoss.class, new RaidBossAdapter())
            .create();
    }
}
