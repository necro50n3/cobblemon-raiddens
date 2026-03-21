package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record FormeChangeShowdownEvent(String form) implements BroadcastingShowdownEvent {
    @Override
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
            "for (let p of battle.sides[1].pokemon) { " +
                "if (!p) continue; " +
                "p.formeChange('%1s', null, true); " +
            "} ",
            capitalize(this.form)
        );
    }

    private static String capitalize(String input) {
        String[] parts = input.split("(?<=[-_ ])|(?=[-_ ])");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (part.matches("[-_ ]")) {
                result.append(part);
            } else if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }
}
