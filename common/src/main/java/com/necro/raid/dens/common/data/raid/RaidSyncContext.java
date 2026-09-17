package com.necro.raid.dens.common.data.raid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record RaidSyncContext(Set<String> aspects, List<String> moves) {
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag aspects = new ListTag();
        this.aspects.forEach(aspect -> aspects.add(StringTag.valueOf(aspect)));
        compoundTag.put("aspects", aspects);

        ListTag moves = new ListTag();
        this.moves.forEach(move -> moves.add(StringTag.valueOf(move)));
        compoundTag.put("moves", moves);

        return compoundTag;
    }

    public static RaidSyncContext load(CompoundTag compoundTag) {
        Set<String> aspects = compoundTag.getList("aspects", Tag.TAG_STRING).stream().map(Tag::getAsString).collect(Collectors.toSet());
        List<String> moves = compoundTag.getList("moves", Tag.TAG_STRING).stream().map(Tag::getAsString).toList();
        return new RaidSyncContext(aspects, moves);
    }
}
