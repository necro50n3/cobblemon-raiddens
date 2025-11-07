package com.necro.raid.dens.common.reloaders;

import  com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.mixins.tags.TagEntryMixin;
import com.necro.raid.dens.common.util.RaidRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;

import java.util.*;

public class RaidTagReloadImpl extends AbstractReloadImpl {
    private final Map<ResourceLocation, TagFile> files;

    public RaidTagReloadImpl() {
        super("tags/raid/boss", DataType.JSON);
        this.files = new HashMap<>();
    }

    @Override
    protected void preLoad() {}

    @Override
    protected void onLoad(ResourceLocation key, JsonObject object) {
        Optional<TagFile> tagOpt = TagFile.CODEC.decode(JsonOps.INSTANCE, object).result().map(Pair::getFirst);
        tagOpt.ifPresent(tag -> this.files.put(key, tag));
    }

    @Override
    protected void onError(ResourceLocation id, Exception e) {
        CobblemonRaidDens.LOGGER.error("Failed to load boss tag {}", id, e);
    }

    @Override
    protected void postLoad() {
        RaidRegistry.setTags(this.resolve(this.files));
    }

    private Map<ResourceLocation, Set<ResourceLocation>> resolve(Map<ResourceLocation, TagFile> files) {
        Map<ResourceLocation, Set<ResourceLocation>> resolved = new HashMap<>();

        for (var entry : files.entrySet()) {
            resolveTag(entry.getKey(), files, resolved, new HashSet<>());
        }

        return resolved;
    }

    private Set<ResourceLocation> resolveTag(ResourceLocation id, Map<ResourceLocation, TagFile> all,
                                             Map<ResourceLocation, Set<ResourceLocation>> cache, Set<ResourceLocation> visited) {
        if (cache.containsKey(id)) return cache.get(id);
        if (!visited.add(id)) throw new IllegalStateException("Circular tag reference in " + id);

        TagFile file = all.get(id);
        if (file == null) return Set.of();

        Set<ResourceLocation> elements = new HashSet<>();
        for (TagEntry tag : file.entries()) {
            if (((TagEntryMixin) tag).isTag()) elements.addAll(this.resolveTag(((TagEntryMixin) tag).getId(), all, cache, visited));
            else elements.add(((TagEntryMixin) tag).getId());
        }

        cache.put(id, elements);
        return elements;
    }
}
