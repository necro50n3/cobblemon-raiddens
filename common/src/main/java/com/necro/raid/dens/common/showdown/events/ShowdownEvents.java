package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.status.Status;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.status.VolatileStatus;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.megashowdown.RaidDensMSDCompat;

import java.util.Locale;
import java.util.Map;

public class ShowdownEvents {
    public record CheerAttackShowdownEvent(String origin) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">cheerattack " + this.origin;
        }
    }

    public record CheerDefenseShowdownEvent(String origin) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">cheerdefense " + this.origin;
        }
    }

    public record CheerHealShowdownEvent(String origin) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">cheerheal " + this.origin;
        }
    }

    public record ClearBoostShowdownEvent(int targetSide, boolean isSilent) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(">clearboost %1$d %2$s", this.targetSide - 1, this.isSilent);
        }
    }

    public record ClearFieldShowdownEvent(String field) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">clearfield " + this.field;
        }
    }

    public record ClearNegativeBoostShowdownEvent(int targetSide) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">clearnegativeboost " + (this.targetSide - 1);
        }
    }

    public record ClearSideConditionShowdownEvent(String sideCondition, int side) implements ShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            return String.format(">clearsidecondition %1$s %2$d", this.sideCondition, this.side - 1);
        }
    }

    public static class ClearTerrainShowdownEvent implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">clearterrain";
        }
    }

    public static class ClearWeatherShowdownEvent implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">clearweather";
        }
    }

    public record CureStatusShowdownEvent(Status status, int targetSide) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            if (this.status instanceof VolatileStatus) return String.format(">curevolatile %1$s %2$d", this.status.getShowdownName(), this.targetSide - 1);
            else return ">curestatus " + (this.targetSide - 1);
        }
    }

    public static class DoNothingShowdownEvent implements ShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            return ">donothing";
        }
    }

    public static class DragonCheerShowdownEvent implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">dragoncheer";
        }
    }

    public record InvertBoostShowdownEvent(int targetSide) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">invertboost " + (this.targetSide - 1);
        }
    }

    public record PlayerJoinShowdownEvent(String player) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">playerjoin " + this.player;
        }
    }

    public record RaidBoostShowdownEvent(Stat stat, int stages) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(">raidboost %1$s %2$d", this.stat.getShowdownId(), this.stages);
        }
    }

    public record RaidMapBoostShowdownEvent(Map<Stat, Integer> stats) implements ShowdownEvent {
        @Override
        public void send(PokemonBattle battle) {
            new RaidEnergyShowdownEvent().send(battle);
            this.stats.forEach((stat, stages) -> new RaidBoostShowdownEvent(stat, stages).send(battle));
        }

        public String build(PokemonBattle battle) {
            return "";
        }
    }

    public static class ResetBossShowdownEvent implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">resetboss " + battle.getSide2().getActors()[0].getUuid();
        }
    }

    public record SetBoostShowdownEvent(Stat stat, int stages, int targetSide) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(">setboost %1$s %2$d %3$d", this.stat.getShowdownId(), this.stages, this.targetSide - 1);
        }
    }

    public record SetFieldShowdownEvent(String field) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">setfield " + this.field;
        }
    }

    public record SetSideConditionShowdownEvent(String sideCondition, int side) implements ShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            return String.format(">setsidecondition %1$s %2$d", this.sideCondition, this.side - 1);
        }
    }

    public record SetStatusShowdownEvent(Status status, int targetSide) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            if (this.status instanceof VolatileStatus) return String.format(">setvolatile %1$s %2$d", this.status.getShowdownName(), this.targetSide - 1);
            else return String.format(">setstatus %1$s %2$d", this.status.getShowdownName(), this.targetSide - 1);
        }
    }

    public record SetTerrainShowdownEvent(String terrain, boolean isSilent) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(">setterrain %1$s %2$s", this.terrain, this.isSilent);
        }
    }

    public record SetWeatherShowdownEvent(String weather, boolean isSilent) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(">setweather %1$s %2$s", this.weather, this.isSilent);
        }
    }

    public static class ShieldAddShowdownEvent implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">shieldadd";
        }
    }

    public static class ShieldRemoveShowdownEvent implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">shieldremove";
        }
    }

    public record StatBoostShowdownEvent(Stat stat, int stages, int targetSide, boolean isSilent) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(">statboost %1$s %2$d %3$d %4$s", this.stat.getShowdownId(), this.stages, this.targetSide - 1, this.isSilent);
        }
    }

    public record StatMapBoostShowdownEvent(Map<Stat, Integer> stats, int targetSide, boolean isSilent) implements ShowdownEvent {
        @Override
        public void send(PokemonBattle battle) {
            new RaidEnergyShowdownEvent().send(battle);
            this.stats.forEach((stat, stages) -> new StatBoostShowdownEvent(stat, stages, this.targetSide, this.isSilent).send(battle));
        }

        public String build(PokemonBattle battle) {
            return "";
        }
    }

    public record SupportBoostShowdownEvent(String move, Stat stat, int stages) implements BroadcastingShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(
                ">supportboost %1$s %2$s %3$d",
                this.move, this.stat.getShowdownId(), this.stages
            );
        }
    }

    public static class SupportCureShowdownEvent implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">supportcure";
        }
    }

    public record SupportHealShowdownEvent(float heal) implements ShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            return ">supportheal " + this.heal;
        }
    }

    public static class SwapSideConditionsShowdownEvent implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">swapsideconditions";
        }
    }

    public record DynamaxShowdownEvent(boolean playAnimation) implements BroadcastingShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            if (ModCompat.MEGA_SHOWDOWN.isLoaded() && this.playAnimation) {
                battle.dispatch(() -> {
                    battle.getSide2().getActivePokemon().forEach(active -> {
                        BattlePokemon battlePokemon = active.getBattlePokemon();
                        if (battlePokemon == null || battlePokemon.getEntity() == null) return;
                        RaidDensMSDCompat.setupDmax(battlePokemon.getEntity(), battlePokemon.getEffectedPokemon());
                    });
                    return DispatchResultKt.getGO();
                });
            }

            return ">raiddynamax";
        }
    }

    public record TerastallizeShowdownEvent(boolean playAnimation) implements BroadcastingShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            if (ModCompat.MEGA_SHOWDOWN.isLoaded() && this.playAnimation) {
                battle.dispatch(() -> {
                    battle.getSide2().getActivePokemon().forEach(active -> {
                        BattlePokemon battlePokemon = active.getBattlePokemon();
                        if (battlePokemon == null || battlePokemon.getEntity() == null) return;
                        RaidDensMSDCompat.setupTera(battlePokemon.getEffectedPokemon());
                    });
                    return DispatchResultKt.getGO();
                });
            }

            return ">raidtera";
        }
    }

    public record FormeChangeShowdownEvent(String form) implements BroadcastingShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            return ">raidformechange " + capitalize(this.form);
        }

        private static String capitalize(String input) {
            String[] parts = input.split("(?<=[-_ ])|(?=[-_ ])");
            StringBuilder result = new StringBuilder();

            for (String part : parts) {
                if (part.matches("[-_ ]")) {
                    result.append(part);
                } else if (!part.isEmpty()) {
                    result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase(Locale.ROOT));
                }
            }
            return result.toString();
        }
    }

    public static class MegaEvolveShowdownEvent implements BroadcastingShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            return ">raidmega";
        }
    }

    public record PlayerBoostShowdownEvent(Stat stat, int stages) implements BroadcastingShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(">playerboost %1$s %2$d", this.stat.getShowdownId(), this.stages);
        }
    }

    public record PlayerMapBoostShowdownEvent(Map<Stat, Integer> stats) implements BroadcastingShowdownEvent {
        @Override
        public void send(PokemonBattle battle) {
            new RaidEnergyShowdownEvent().send(battle);
            this.stats.forEach((stat, stages) -> new PlayerBoostShowdownEvent(stat, stages).send(battle));
        }

        public String build(PokemonBattle battle) {
            return "";
        }
    }

    public static class ResetPlayerShowdownEvent implements BroadcastingShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">resetplayer " + battle.getSide2().getActors()[0].getUuid();
        }
    }

    public record UseMoveShowdownEvent(String move, int target) implements BroadcastingShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(">usemove %1$s %2$d", this.move, this.target);
        }
    }

    public static class RaidEnergyShowdownEvent implements ShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            return String.format(">eval battle.add('raidenergy', '%1$s');", battle.getSide2().getActors()[0].getUuid());
        }
    }

    public static class RaidStartShowdownEvent implements ShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            return ">raidstart";
        }
    }
}
