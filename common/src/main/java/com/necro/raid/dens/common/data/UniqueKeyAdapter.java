package com.necro.raid.dens.common.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.components.ModComponents;
import net.minecraft.world.item.ItemStack;

public record UniqueKeyAdapter(String item, String id) {
    public UniqueKeyAdapter() {
        this("", "");
    }

    public UniqueKeyAdapter {
        if (!item.isEmpty() && !item.contains(":")) item = "minecraft:" + item;
    }

    public static final Codec<UniqueKeyAdapter> DIRECT_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("item").forGetter(UniqueKeyAdapter::item),
            Codec.STRING.fieldOf("id").forGetter(UniqueKeyAdapter::id)
        ).apply(instance, UniqueKeyAdapter::new)
    );

    public static final Codec<UniqueKeyAdapter> CODEC = Codec.either(Codec.STRING, DIRECT_CODEC)
        .xmap(either -> either.map(
                str -> new UniqueKeyAdapter(str, ""),
                data -> data
            ),
            Either::right
        );

    public boolean matches(ItemStack itemStack) {
        if (this.item().isEmpty()) return true;
        if (!itemStack.getItem().toString().equals(this.item())) return false;
        else if (this.id().isEmpty()) return true;
        String id = itemStack.getComponents().get(ModComponents.UNIQUE_KEY.value());
        return id != null && id.equals(this.id());
    }

    public boolean isEmpty() {
        return this.item().isEmpty();
    }
}