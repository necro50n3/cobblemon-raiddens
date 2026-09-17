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
import java.util.Map;

@Mixin(PokemonProperties.class)
public class PokemonPropertiesMixin implements IProperties {
    @Unique
    private Map<String, List<String>> crd_movesetBuilder = null;

    @Override
    public Map<String, List<String>> crd_getMovesetBuilder() {
        return this.crd_movesetBuilder;
    }

    @Override
    public void crd_setMovesetBuilder(Map<String, List<String>> builder) {
        this.crd_movesetBuilder = builder;
    }

    @Inject(method = "loadFromJSON", at = @At("RETURN"), remap = false)
    private void loadFromJSONInject(JsonObject json, CallbackInfoReturnable<PokemonProperties> cir) {
        JsonObject obj = json.getAsJsonObject("CustomRaidMovesetBuilder");
        if (obj == null) return;
        JsonArray slot1 = obj.getAsJsonArray("slot1");
        JsonArray slot2 = obj.getAsJsonArray("slot2");
        JsonArray slot3 = obj.getAsJsonArray("slot3");
        JsonArray slot4 = obj.getAsJsonArray("slot4");
        this.crd_movesetBuilder = Map.of(
            "slot1", slot1.asList().stream().map(JsonElement::getAsString).toList(),
            "slot2", slot2.asList().stream().map(JsonElement::getAsString).toList(),
            "slot3", slot3.asList().stream().map(JsonElement::getAsString).toList(),
            "slot4", slot4.asList().stream().map(JsonElement::getAsString).toList()
        );
    }

    @Inject(method = "saveToJSON", at = @At("RETURN"), remap = false)
    private void saveToJSONInject(CallbackInfoReturnable<JsonObject> cir) {
        if (this.crd_movesetBuilder == null) return;
        JsonObject obj = new JsonObject();
        JsonArray slot1 = new JsonArray();
        for (String selector : this.crd_movesetBuilder.get("slot1")) slot1.add(selector);
        obj.add("slot1", slot1);
        JsonArray slot2 = new JsonArray();
        for (String selector : this.crd_movesetBuilder.get("slot2")) slot2.add(selector);
        obj.add("slot2", slot2);
        JsonArray slot3 = new JsonArray();
        for (String selector : this.crd_movesetBuilder.get("slot3")) slot3.add(selector);
        obj.add("slot3", slot3);
        JsonArray slot4 = new JsonArray();
        for (String selector : this.crd_movesetBuilder.get("slot4")) slot4.add(selector);
        obj.add("slot4", slot4);
        cir.getReturnValue().add("CustomRaidMovesetBuilder", obj);
    }
}
