package com.necro.raid.dens.common.mixins.raid;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.necro.raid.dens.common.util.IProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PokemonProperties.class)
public class PokemonPropertiesMixin implements IProperties {
    @Unique
    private List<List<String>> crd_movesetBuilder = null;

    @Override
    public List<List<String>> crd_getMovesetBuilder() {
        return this.crd_movesetBuilder;
    }

    @Override
    public void crd_setMovesetBuilder(List<List<String>> builder) {
        this.crd_movesetBuilder = builder;
    }

    @Inject(method = "loadFromJSON", at = @At("RETURN"), remap = false)
    private void loadFromJSONInject(JsonObject json, CallbackInfoReturnable<PokemonProperties> cir) {
        JsonArray builder = json.getAsJsonArray("CustomRaidMovesetBuilder");
        if (builder == null) return;
        this.crd_movesetBuilder = builder
            .asList()
            .stream()
            .map(element -> ((JsonArray) element).asList().stream().map(JsonElement::getAsString).toList())
            .toList();
    }

    @Inject(method = "saveToJSON", at = @At("RETURN"), remap = false)
    private void saveToJSONInject(CallbackInfoReturnable<JsonObject> cir) {
        if (this.crd_movesetBuilder == null) return;
        JsonArray builder = new JsonArray();
        for (List<String> slot : this.crd_movesetBuilder) {
            JsonArray builderSlot = new JsonArray();
            slot.forEach(builderSlot::add);
            builder.add(builderSlot);
        }
        cir.getReturnValue().add("CustomRaidMovesetBuilder", builder);
    }
}
