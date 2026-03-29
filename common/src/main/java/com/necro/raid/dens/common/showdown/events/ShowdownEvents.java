package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.status.Status;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.status.VolatileStatus;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.megashowdown.RaidDensMSDCompat;

import java.util.Map;

public class ShowdownEvents {
    public record CheerAttackShowdownEvent(String origin) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "battle.add('cheer', 'cheer_attack', '%1$s'); " +
                    "for (let p of battle.sides[0].pokemon) { " +
                        "if (!p) continue; " +
                        "p.addVolatile('cheerattack'); " +
                    "} ",
                this.origin
            );
        }
    }

    public record CheerDefenseShowdownEvent(String origin) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "battle.add('cheer', 'cheer_defense', '%1$s'); " +
                    "for (let p of battle.sides[0].pokemon) { " +
                        "if (!p) continue; " +
                        "p.addVolatile('cheerdefense'); " +
                    "} ",
                this.origin
            );
        }
    }

    public record CheerHealShowdownEvent(String origin) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "battle.add('cheer', 'cheer_heal', '%1$s'); " +
                    "for (let p of battle.sides[0].pokemon) { " +
                        "if (!p) continue; " +
                        "p.heal(Math.floor(p.maxhp * 0.5)); " +
                        "p.cureStatus(); " +
                        "battle.add('-heal', p, p.getHealth, '[from] bagitem: ' + 'cheer_heal'); " +
                    "} ",
                this.origin
            );
        }
    }

    public record ClearBoostShowdownEvent(int targetSide) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "for (let p of battle.sides[%1$d].pokemon) { " +
                        "if (!p) continue; " +
                        "for (let boost in p.boosts) { " +
                            "p.boosts[boost] = 0; " +
                        "} " +
                    "} ",
                this.targetSide - 1
            );
        }
    }

    public record ClearFieldShowdownEvent(String field) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "const pseudoWeather = battle.dex.conditions.get('%1$s'); " +
                    "if (battle.field.pseudoWeather[pseudoWeather.id]) delete battle.field.pseudoWeather[pseudoWeather.id]; ",
                this.field
            );
        }
    }

    public record ClearNegativeBoostShowdownEvent(int targetSide) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "for (let p of battle.sides[%1$d].pokemon) { " +
                        "if (!p) continue; " +
                        "for (let i in p.boosts) { " +
                            "if (p.boosts[i] >= 0) continue; " +
                            "p.boosts[i] = 0; " +
                        "} " +
                    "}",
                this.targetSide - 1
            );
        }
    }

    public record ClearSideConditionShowdownEvent(String sideCondition, int side) implements ShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "const condition = battle.dex.conditions.get('%1$s'); " +
                    "const side = battle.sides[%2$d]; " +
                    "if (condition && side.sideConditions[condition.id]) delete side.sideConditions[condition.id]; ",
                this.sideCondition, this.side - 1
            );
        }
    }

    public static class ClearTerrainShowdownEvent implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">eval battle.field.terrain = ''; battle.field.terrainState = { id: '' };";
        }
    }

    public static class ClearWeatherShowdownEvent implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">eval battle.field.weather = ''; battle.field.weatherState = { id: '' };";
        }
    }

    public record CureStatusShowdownEvent(Status status, int targetSide) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            if (this.status instanceof VolatileStatus) {
                return String.format(
                    ">eval " +
                        "const status = battle.dex.conditions.get('%1$s'); " +
                        "for (let p of battle.sides[%2$d].pokemon) { " +
                            "if (!p || !status || !p.volatiles[status.id]) continue; " +
                            "delete p.volatiles[status.id]; " +
                        "} ",
                    this.status.getShowdownName(), this.targetSide - 1);
            }
            else {
                return String.format(
                    ">eval " +
                        "for (let p of battle.sides[%1$d].pokemon) { " +
                            "if (!p) continue; " +
                            "if (p.status !== 'shield') p.clearStatus(); " +
                        "}",
                    this.targetSide - 1);
            }
        }
    }

    public static class DoNothingShowdownEvent implements ShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            return ">eval battle.add('donothing');";
        }
    }

    public static class DragonCheerShowdownEvent implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">eval " +
                "const status = battle.dex.conditions.get('dragoncheer'); " +
                "for (let p of battle.sides[0].pokemon) { " +
                    "if (!p) continue; " +
                    "else if (p.volatiles['focusenergy']) continue; " +
                    "else if (status && !p.volatiles[status.id]) { " +
                        "p.volatiles[status.id] = { id: status.id, name: status.name, target: p }; " +
                    "} " +
                    "battle.effectState.hasDragonType = p.hasType('Dragon'); " +
                    "battle.add('-start', p, 'move: Dragon Cheer'); " +
                "} ";
        }
    }

    public record InvertBoostShowdownEvent(int targetSide) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "for (let p of battle.sides[%1$d].pokemon) { " +
                    "if (!p) continue; " +
                    "for (let i in p.boosts) { " +
                    "if (p.boosts[i] === 0) continue; " +
                    "p.boosts[i] = -p.boosts[i]; " +
                    "} " +
                    "}",
                this.targetSide - 1
            );
        }
    }

    public record PlayerJoinShowdownEvent(String player) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(">eval battle.add('playerjoin', battle.sides[1].pokemon[0], '%1$s');", this.player);
        }
    }

    public record RaidBoostShowdownEvent(Stat stat, int stages) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "var boosts = {};" +
                    "boosts['%1$s'] = %2$d; " +
                    "for (let p of battle.sides[1].pokemon) { " +
                        "if (!p) continue; " +
                        "var boost = battle.runEvent('ChangeBoost', p, p, {name: 'Raid Energy'}, {...boosts}); " +
                        "boost = p.getCappedBoost(boost); " +
                        "boost = battle.runEvent('TryBoost', p, p, {name: 'Raid Energy'}, {...boost}); " +
                        "for (let boostName in boost) { " +
                            "const currentBoost = { [boostName]: boost[boostName], }; " +
                            "let boostBy = p.boostBy(currentBoost); " +
                            "let msg = '-raidboost'; " +
                            "if (boost[boostName] < 0 || p.boosts[boostName] === -6) { " +
                                "msg = '-raidunboost'; " +
                                "boostBy = -boostBy; " +
                            "} " +
                            "if (boostBy) { " +
                                "battle.add(msg, p, boostName, boostBy); " +
                                "battle.runEvent('AfterEachBoost', p, p, {name: 'Raid Energy'}, currentBoost); " +
                            "} " +
                        "} " +
                    "} ",
                this.stat.getShowdownId(), this.stages
            );
        }
    }

    public static class RaidCureShowdownEvent implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">eval " +
                "for (let p of battle.sides[0].pokemon) { " +
                    "if (!p) continue; " +
                    "p.cureStatus(); " +
                "}";
        }
    }

    public record RaidHealShowdownEvent(float heal) implements ShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "for (let p of battle.sides[0].pokemon) { " +
                        "if (!p) continue; " +
                        "battle.heal(Math.floor(p.maxhp * %1$f), p); " +
                    "} ",
                this.heal
            );
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
            return String.format(
                ">eval " +
                    "battle.add('raidenergy', '%1$s', true); " +
                    "for (let p of battle.sides[1].pokemon) { " +
                        "if (!p) continue; " +
                        "for (let i in p.boosts) { " +
                            "if (p.boosts[i] >= 0) continue; " +
                            "p.boosts[i] = 0; " +
                        "} " +
                        "if (p.status !== 'shield') p.cureStatus(); " +
                        "battle.add('clearboss', p, '%1$s'); " +
                    "}",
                battle.getSide2().getActors()[0].getUuid()
            );
        }
    }

    public record SetBoostShowdownEvent(Stat stat, int stages, int targetSide) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "for (let p of battle.sides[%3$d].pokemon) { " +
                        "if (!p) continue; " +
                        "p.boosts['%1$s'] = %2$d; " +
                    "} ",
                this.stat.getShowdownId(), this.stages, this.targetSide - 1
            );
        }
    }

    public record SetFieldShowdownEvent(String field) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "const pokemon = battle.sides[1].pokemon[0]; " +
                    "const pseudoWeather = battle.dex.conditions.get('%1$s'); " +
                    "battle.field.pseudoWeather[pseudoWeather.id] = { " +
                        "id: pseudoWeather.id, " +
                        "source: pokemon, " +
                        "sourceSlot: pokemon.getSlot(), " +
                        "duration: pseudoWeather.duration " +
                    "};",
                this.field
            );
        }
    }

    public record SetSideConditionShowdownEvent(String sideCondition, int side) implements ShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "const condition = battle.dex.conditions.get('%1$s'); " +
                    "const side = battle.sides[%2$d]; " +
                    "const pokemon = side.pokemon[0]; " +
                    "if (condition && !side.sideConditions[condition.id]) { " +
                        "side.sideConditions[condition.id] = { " +
                            "id: condition.id, " +
                            "target: side, " +
                            "source: pokemon, " +
                            "sourceSlot: pokemon.getSlot(), " +
                            "duration: condition.duration, " +
                        "}; " +
                        "battle.effectState.layers = 1; " +
                    "} " +
                    "else if (condition && side.sideConditions[condition.id]) { " +
                        "if (condition.id === 'spikes' && battle.effectState.layers < 3) battle.effectState.layers++; " +
                        "if (condition.id === 'toxicspikes' && battle.effectState.layers < 2) battle.effectState.layers++; " +
                    "} ",
                this.sideCondition, this.side - 1
            );
        }
    }

    public record SetStatusShowdownEvent(Status status, int targetSide) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            if (this.status instanceof VolatileStatus) {
                return String.format(
                    ">eval " +
                        "const status = battle.dex.conditions.get('%1$s'); " +
                        "for (let p of battle.sides[%2$d].pokemon) { " +
                            "if (!status || status.id === 'typechange') continue; " +
                            "if (p.volatiles[status.id]) { " +
                                "if (status.onRestart) battle.singleEvent('Restart', status, p.volatiles[status.id], p, null, null); " +
                                "continue;" +
                            "} " +
                            "p.volatiles[status.id] = { id: status.id, name: status.name, target: p }; " +
                            "if (status.id === 'confusion') p.volatiles[status.id].time = battle.random(2, 6); " +
                        "} ",
                    this.status.getShowdownName(), this.targetSide - 1);
            }
            else {
                return String.format(
                    ">eval " +
                        "const status = battle.dex.conditions.get('%1$s'); " +
                        "for (let p of battle.sides[%2$d].pokemon) { " +
                            "if (status && p.status !== 'shield') {" +
                                "if (p.status === status.id) continue; " +
                                "battle.runEvent('SetStatus', p, null, null, status); " +
                                "p.status = status.id; " +
                                "p.statusState = { id: status.id, target: p };" +
                                "if (status.duration) p.statusState.duration = status.duration;" +
                                "if (status.durationCallback) p.statusState.duration = status.durationCallback.call(battle, p, null, null);" +
                                "if (status.id === 'slp') {" +
                                    "p.statusState.startTime = battle.random(2, 5); " +
                                    "p.statusState.time = p.statusState.startTime; " +
                                    "battle.effectState = p.statusState; " +
                                "} " +
                                "battle.runEvent('AfterSetStatus', p, null, null, status);" +
                            "} " +
                        "}",
                    this.status.getShowdownName(), this.targetSide - 1);
            }
        }
    }

    public record SetTerrainShowdownEvent(String terrain, boolean isSilent) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            if (this.isSilent) {
                return String.format(
                    ">eval " +
                        "const pokemon = battle.sides[1].pokemon[0]; " +
                        "const terrain = battle.dex.conditions.get('%1$s'); " +
                        "battle.field.terrain = terrain.id; " +
                        "battle.field.terrainState = { " +
                            "id: terrain.id, " +
                            "source: pokemon, " +
                            "sourceSlot: pokemon.getSlot(), " +
                            "duration: terrain.duration " +
                        "};",
                    this.terrain
                );
            }
            else {
                return String.format(
                    ">eval " +
                        "const pokemon = battle.sides[1].pokemon[0]; " +
                        "battle.field.setTerrain('%s', pokemon); ",
                    this.terrain
                );
            }
        }
    }

    public record SetWeatherShowdownEvent(String weather, boolean isSilent) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            if (this.isSilent) {
                return String.format(
                    ">eval " +
                        "const weather = battle.dex.conditions.get('%1$s'); " +
                        "battle.field.weather = weather.id; " +
                        "battle.field.weatherState = { id: weather.id };" +
                        "if (weather.duration) battle.field.weatherState.duration = weather.duration;",
                    this.weather
                );
            }
            else {
                return String.format(
                    ">eval " +
                        "const pokemon = battle.sides[1].pokemon[0]; " +
                        "battle.field.setWeather('%s', pokemon);",
                    this.weather
                );
            }
        }
    }

    public static class ShieldAddShowdownEvent implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">eval " +
                "for (let p of battle.sides[1].pokemon) { " +
                    "if (!p) continue; " +
                    "p.clearStatus(); " +
                    "p.trySetStatus('shield', p); " +
                    "battle.add('shieldadd', p); " +
                "} ";
        }
    }

    public static class ShieldRemoveShowdownEvent implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">eval " +
                "for (let p of battle.sides[1].pokemon) { " +
                    "if (!p) continue;" +
                    "p.cureStatus(); " +
                    "battle.add('shieldremove', p);" +
                "}";
        }
    }

    public record StatBoostShowdownEvent(Stat stat, int stages, int targetSide, boolean isSilent) implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            if (this.isSilent) {
                return String.format(
                    ">eval " +
                        "var boosts = {};" +
                        "boosts['%1$s'] = %2$d; " +
                        "for (let p of battle.sides[%3$d].pokemon) { " +
                            "if (!p) continue; " +
                            "var boost = battle.runEvent('ChangeBoost', p, p, {name: 'Raid Energy'}, {...boosts}); " +
                            "boost = p.getCappedBoost(boost); " +
                            "boost = battle.runEvent('TryBoost', p, p, {name: 'Raid Energy'}, {...boost}); " +
                            "for (let boostName in boost) { " +
                                "const currentBoost = { [boostName]: boost[boostName], }; " +
                                "let boostBy = p.boostBy(currentBoost); " +
                                "if (boostBy) battle.runEvent('AfterEachBoost', p, p, {name: 'Raid Energy'}, currentBoost); " +
                            "} " +
                        "} ",
                    this.stat.getShowdownId(), this.stages, this.targetSide - 1
                );
            }
            else {
                return String.format(
                    ">eval " +
                        "var boosts = {};" +
                        "boosts['%1$s'] = %2$d; " +
                        "for (let p of battle.sides[%3$d].pokemon) { " +
                            "if (!p) continue; " +
                            "battle.boost(boosts, p, null, '[from] Raid'); " +
                        "} ",
                    this.stat.getShowdownId(), this.stages, this.targetSide - 1
                );
            }
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

    public static class SwapSideConditionsShowdownEvent implements ShowdownEvent {
        public String build(PokemonBattle battle) {
            return ">eval " +
                "const sourceSideConditions = battle.sides[0].sideConditions; " +
                "const targetSideConditions = battle.sides[1].sideConditions; " +
                "const sourceTemp: typeof sourceSideConditions = {}; " +
                "const targetTemp: typeof targetSideConditions = {}; " +
                "for (const id in sourceSideConditions) { " +
                    "if (!sideConditions.includes(id)) continue; " +
                    "sourceTemp[id] = sourceSideConditions[id]; " +
                    "delete sourceSideConditions[id]; " +
                "} " +
                "for (const id in targetSideConditions) { " +
                    "if (!sideConditions.includes(id)) continue; " +
                    "targetTemp[id] = targetSideConditions[id]; " +
                    "delete targetSideConditions[id]; " +
                "} " +
                "for (const id in sourceTemp) { " +
                    "targetSideConditions[id] = sourceTemp[id]; " +
                "} " +
                "for (const id in targetTemp) { " +
                    "sourceSideConditions[id] = targetTemp[id]; " +
                "}";
        }
    }

    public static class DynamaxShowdownEvent implements BroadcastingShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            if (ModCompat.MEGA_SHOWDOWN.isLoaded()) {
                battle.dispatch(() -> {
                    battle.getSide2().getActivePokemon().forEach(active -> {
                        BattlePokemon battlePokemon = active.getBattlePokemon();
                        if (battlePokemon == null || battlePokemon.getEntity() == null) return;
                        RaidDensMSDCompat.setupDmax(battlePokemon.getEntity(), battlePokemon.getEffectedPokemon());
                    });
                    return DispatchResultKt.getGO();
                });
            }

            return ">eval " +
                "for (let p of battle.sides[1].pokemon) { " +
                    "if (!p) continue; " +
                    "p.addVolatile('dynamax'); " +
                "} ";
        }
    }

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

    public static class MegaEvolveShowdownEvent implements BroadcastingShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            return ">eval " +
                "for (let p of battle.sides[1].pokemon) { " +
                    "if (!p) continue; " +
                    "if (p.species.baseSpecies === 'Zygarde') p.canMegaEvo = 'Zygarde-Mega'; " +
                    "battle.actions.runMegaEvo(p); " +
                "} ";
        }
    }

    public record PlayerBoostShowdownEvent(Stat stat, int stages) implements BroadcastingShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "var boosts = {};" +
                    "boosts['%1$s'] = %2$d; " +
                    "for (let p of battle.sides[0].pokemon) { " +
                        "if (!p) continue; " +
                        "battle.boost(boosts, p, null, '[from] Raid'); " +
                    "} ",
                this.stat.getShowdownId(), this.stages
            );
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

    public static class TerastallizeShowdownEvent implements BroadcastingShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            if (ModCompat.MEGA_SHOWDOWN.isLoaded()) {
                battle.dispatch(() -> {
                    battle.getSide2().getActivePokemon().forEach(active -> {
                        BattlePokemon battlePokemon = active.getBattlePokemon();
                        if (battlePokemon == null || battlePokemon.getEntity() == null) return;
                        RaidDensMSDCompat.setupTera(battlePokemon.getEffectedPokemon());
                    });
                    return DispatchResultKt.getGO();
                });
            }

            return ">eval " +
                "for (let p of battle.sides[1].pokemon) { " +
                    "if (!p) continue; " +
                    "battle.actions.terastallize(p); " +
                "} ";
        }
    }

    public record RaidSupportShowdownEvent(String move, Stat stat, int stages) implements BroadcastingShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "var boosts = {};" +
                    "boosts['%2$s'] = %3$d; " +
                    "for (let p of battle.sides[0].pokemon) { " +
                        "if (!p) continue; " +
                        "else if (['gearup', 'magneticflux'].includes('%1$s')) { " +
                            "if (!p.hasAbility('plus') && !p.hasAbility('minus')) continue; " +
                        "} " +
                        "battle.boost(boosts, p, null, '[from] Raid'); " +
                    "} ",
                this.move, this.stat.getShowdownId(), this.stages
            );
        }
    }

    public static class ResetPlayerShowdownEvent implements BroadcastingShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "battle.add('raidenergy', '%1$s'); " +
                    "for (let p of battle.sides[0].pokemon) { " +
                        "if (!p) continue; " +
                        "for (let i in p.boosts) { " +
                            "if (p.boosts[i] <= 0) continue; " +
                            "p.boosts[i] = 0; " +
                        "} " +
                        "if (p.volatiles['cheerattack']) delete p.volatiles['cheerattack']; " +
                        "if (p.volatiles['cheerdefense']) delete p.volatiles['cheerdefense']; " +
                        "battle.add('clearplayer', p, '%1$s'); " +
                    "}",
                battle.getSide2().getActors()[0].getUuid()
            );
        }
    }

    public record UseMoveShowdownEvent(String move, int target) implements BroadcastingShowdownEvent {
        public String build(PokemonBattle battle) {
            return String.format(
                ">eval " +
                    "let p = battle.sides[1].pokemon[0]; " +
                    "p.side.lastSelectedMove = battle.toID('%1$s'); " +
                    "battle.actions.runMove('%1$s', p, %2$d, null, null, true);",
                this.move, this.target
            );
        }
    }

    public static class RaidEnergyShowdownEvent implements ShowdownEvent {
        @Override
        public String build(PokemonBattle battle) {
            return String.format(">eval battle.add('raidenergy', '%1$s');", battle.getSide2().getActors()[0].getUuid());
        }
    }
}
