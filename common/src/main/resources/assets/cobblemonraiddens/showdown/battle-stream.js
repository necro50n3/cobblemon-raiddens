"use strict";
var __defProp = Object.defineProperty;
var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
var __getOwnPropNames = Object.getOwnPropertyNames;
var __hasOwnProp = Object.prototype.hasOwnProperty;
var __export = (target, all) => {
  for (var name in all)
    __defProp(target, name, { get: all[name], enumerable: true });
};
var __copyProps = (to, from, except, desc) => {
  if (from && typeof from === "object" || typeof from === "function") {
    for (let key of __getOwnPropNames(from))
      if (!__hasOwnProp.call(to, key) && key !== except)
        __defProp(to, key, { get: () => from[key], enumerable: !(desc = __getOwnPropDesc(from, key)) || desc.enumerable });
  }
  return to;
};
var __toCommonJS = (mod) => __copyProps(__defProp({}, "__esModule", { value: true }), mod);
var battle_stream_exports = {};
__export(battle_stream_exports, {
  BattlePlayer: () => BattlePlayer,
  BattleStream: () => BattleStream,
  BattleTextStream: () => BattleTextStream,
  getPlayerStreams: () => getPlayerStreams
});
module.exports = __toCommonJS(battle_stream_exports);
var import_lib = require("../lib");
var import_teams = require("./teams");
var import_battle = require("./battle");
/**
 * Battle Stream
 * Pokemon Showdown - http://pokemonshowdown.com/
 *
 * Supports interacting with a PS battle in Stream format.
 *
 * This format is VERY NOT FINALIZED, please do not use it directly yet.
 *
 * @license MIT
 */
function splitFirst(str, delimiter, limit = 1) {
  const splitStr = [];
  while (splitStr.length < limit) {
    const delimiterIndex = str.indexOf(delimiter);
    if (delimiterIndex >= 0) {
      splitStr.push(str.slice(0, delimiterIndex));
      str = str.slice(delimiterIndex + delimiter.length);
    } else {
      splitStr.push(str);
      str = "";
    }
  }
  splitStr.push(str);
  return splitStr;
}
class BattleStream extends import_lib.Streams.ObjectReadWriteStream {
  constructor(options = {}) {
    super();
    this.debug = !!options.debug;
    this.noCatch = !!options.noCatch;
    this.replay = options.replay || false;
    this.keepAlive = !!options.keepAlive;
    this.battle = null;
  }
  _write(chunk) {
    if (this.noCatch) {
      this._writeLines(chunk);
    } else {
      try {
        this._writeLines(chunk);
      } catch (err) {
        this.pushError(err, true);
        return;
      }
    }
    if (this.battle)
      this.battle.sendUpdates();
  }
  _writeLines(chunk) {
    for (const line of chunk.split("\n")) {
      if (line.startsWith(">")) {
        const [type, message] = splitFirst(line.slice(1), " ");
        this._writeLine(type, message);
      }
    }
  }
  pushMessage(type, data) {
    if (this.replay) {
      if (type === "update") {
        if (this.replay === "spectator") {
          const channelMessages = (0, import_battle.extractChannelMessages)(data, [0]);
          this.push(channelMessages[0].join("\n"));
        } else {
          const channelMessages = (0, import_battle.extractChannelMessages)(data, [-1]);
          this.push(channelMessages[-1].join("\n"));
        }
      }
      return;
    }
    this.push(`${type}
${data}`);
  }
  _writeLine(type, message) {
    switch (type) {
      case "start":
        const options = JSON.parse(message);
        options.send = (t, data) => {
          if (Array.isArray(data))
            data = data.join("\n");
          this.pushMessage(t, data);
          if (t === "end" && !this.keepAlive)
            this.pushEnd();
        };
        if (this.debug)
          options.debug = true;
        try {
          this.battle = new import_battle.Battle(options);
        } catch (e) {
          console.log(e);
        }
        break;
      case "player":
        const [slot, optionsText] = splitFirst(message, " ");
        this.battle.setPlayer(slot, JSON.parse(optionsText));
        break;
      case "capture":
        this.battle.inputLog.push(`>capture ${message}`);
        const pokemon = this.battle.getPokemonByPNX(message);
        if (pokemon) {
          pokemon.capture();
        } else {
          throw new Error(`Capture targeted ${message} but that Pok\xE9mon does not exist.`);
        }
        break;
      case "p1":
      case "p2":
      case "p3":
      case "p4":
        if (message === "undo") {
          this.battle.undoChoice(type);
        } else {
          this.battle.choose(type, message);
        }
        break;
      case "forcewin":
      case "forcetie":
        this.battle.win(type === "forcewin" ? message : null);
        if (message) {
          this.battle.inputLog.push(`>forcewin ${message}`);
        } else {
          this.battle.inputLog.push(`>forcetie`);
        }
        break;
      case "forcelose":
        this.battle.lose(message);
        this.battle.inputLog.push(`>forcelose ${message}`);
        break;
      case "reseed":
        const seed = message ? message.split(",").map(Number) : null;
        this.battle.resetRNG(seed);
        this.battle.inputLog.push(`>reseed ${this.battle.prng.seed.join(",")}`);
        break;
      case "tiebreak":
        this.battle.tiebreak();
        break;
      case "chat-inputlogonly":
        this.battle.inputLog.push(`>chat ${message}`);
        break;
      case "chat":
        this.battle.inputLog.push(`>chat ${message}`);
        this.battle.add("chat", `${message}`);
        break;
      case "requestlog":
        this.push(`requesteddata
${this.battle.inputLog.join("\n")}`);
        break;
      case "requestexport":
        this.push(`requesteddata
${this.battle.prngSeed}
${this.battle.inputLog.join("\n")}`);
        break;
      case "requestteam":
        message = message.trim();
        const slotNum = parseInt(message.slice(1)) - 1;
        if (isNaN(slotNum) || slotNum < 0) {
          throw new Error(`Team requested for slot ${message}, but that slot does not exist.`);
        }
        const side = this.battle.sides[slotNum];
        const team = import_teams.Teams.pack(side.team);
        this.push(`requesteddata
${team}`);
        break;
      case "show-openteamsheets":
        this.battle.showOpenTeamSheets(this.battle.rated === true);
        break;
      case "version":
      case "version-origin":
        break;
      case "insertactor":
        const [side, partySize, actor, team] = splitFirst(message, " ");
        const side = battle.sides[parseInt(side)];
        side.name = actor;
        side.initTeam(battle.getTeam({ team: team }));
        side.totalFainted = 0;

        for (let i = 0; i < side.pokemon.length; i++) {
          battle.initPokemon(side.pokemon[i]);
        }
        for (let i = 0; i < Math.min(side.active.length, side.pokemon.length); i++) {
          side.active[i] = side.pokemon[i];
        }

        const requests = battle.getRequests(move);
        side.emitRequest(requests[parseInt(side)]);
        side.chooseTeam(parseInt(partySize));
        for (let i = 0; i < Math.min(side.active.length, side.pokemon.length); i++) {
          side.active[i] = null;
          battle.actions.switchIn(side.pokemon[i], i);
        }

        battle.sendUpdates();
        if (battle.awaitTurnEnd) battle.delayedNextTurn();
        break;
      case "raidstart":
        this.battle.sides[1].pokemon.forEach(p => p.addVolatile('raidboss'));
        const [isDynamax, isTera] = splitFirst(message, " ").map(value => value === "true");
        if (isDynamax) this.battle.sides[1].pokemon.forEach(p => p.addVolatile('dynamax'));
        if (isTera) this.battle.sides[1].pokemon.forEach(p => this.battle.actions.terastallize(p));
        break;
      case "cheerattack":
        const [origin] = splitFirst(message, " ");
        this.battle.add('cheer', 'cheer_attack', origin);
        for (let p of battle.sides[0].active) {
          if (!p) continue;
          p.addVolatile('cheerattack');
        }
        break;
      case "cheerdefense":
        const [origin] = splitFirst(message, " ");
        this.battle.add('cheer', 'cheer_defense', origin);
        for (let p of battle.sides[0].active) {
          if (!p) continue;
          p.addVolatile('cheerdefense');
        }
        break;
      case "cheerheal":
        const [origin] = splitFirst(message, " ");
        this.battle.add('cheer', 'cheer_heal', origin);
        for (let p of battle.sides[0].active) {
          if (!p) continue;
          p.heal(Math.floor(p.maxhp * 0.5));
          p.cureStatus();
          battle.add('-heal', p, p.getHealth, '[from] bagitem: ' + 'cheer_heal');
        }
        break;
      case "clearboost":
        const [side, isSilent] = splitFirst(message, " ");
        for (let p of this.battle.sides[parseInt(side)].active) {
          if (!p) continue;
          for (let boost in p.boosts) {
            p.boosts[boost] = 0;
          }
          if (!isSilent) this.battle.add('-clearboost', p);
        }
        break;
      case "clearfield":
        const [id] = splitFirst(message, " ");
        const pseudoWeather = this.battle.dex.conditions.get(id);
        if (this.battle.field.pseudoWeather[pseudoWeather.id]) delete this.battle.field.pseudoWeather[pseudoWeather.id];
        break;
      case "clearnegativeboost":
        const [side] = splitFirst(message, " ");
        for (let p of this.battle.sides[parseInt(side)].active) {
          if (!p) continue;
          for (let i in p.boosts) {
            if (p.boosts[i] >= 0) continue;
            p.boosts[i] = 0;
          }
        }
        break;
      case "clearsidecondition":
        const [id, side] = splitFirst(message, " ");
        const condition = this.battle.dex.conditions.get(id);
        const side = this.battle.sides[parseInt(side)];
        if (condition && side.sideConditions[condition.id]) delete side.sideConditions[condition.id];
        break;
      case "clearterrain":
        this.battle.field.terrain = "";
        this.battle.field.terrainState = { id: "" };
        break;
      case "clearweather":
        this.battle.field.weather = "";
        this.battle.field.weatherState = { id: "" };
        break;
      case "curestatus":
        const [side] = splitFirst(message, " ");
        for (let p of this.battle.sides[parseInt(side)].active) {
          if (!p) continue;
          if (p.status !== 'shield') p.clearStatus();
        }
        break;
      case "curevolatile":
        const [id, side] = splitFirst(message, " ");
        const status = this.battle.dex.conditions.get(id);
        for (let p of this.battle.sides[parseInt(side)].active) {
          if (!p || !status || !p.volatiles[status.id]) continue;
          delete p.volatiles[status.id];
        }
        break;
      case "dragoncheer":
        const status = this.battle.dex.conditions.get('dragoncheer');
        for (let p of this.battle.sides[0].active) {
          if (!p) continue;
          else if (p.volatiles['focusenergy']) continue;
          else if (status && !p.volatiles[status.id]) {
            p.volatiles[status.id] = { id: status.id, name: status.name, target: p };
          }
          this.battle.effectState.hasDragonType = p.hasType('Dragon');
          this.battle.add('-start', p, 'move: Dragon Cheer');
        }
        break;
      case "invertboost":
        const [side] = splitFirst(message, " ");
        for (let p of this.battle.sides[parseInt(side)].active) {
          if (!p) continue;
          for (let i in p.boosts) {
            if (p.boosts[i] === 0) continue;
            p.boosts[i] = -p.boosts[i];
          }
        }
        break;
      case "playerjoin":
        const [player] = splitFirst(message, " ");
        this.battle.add('playerjoin', battle.sides[1].active[0], player);
        break;
      case "raidboost":
        const [stat, stages] = splitFirst(message, " ");
        var boosts = {};
        boosts[stat] = parseInt(stages);
        for (let p of this.battle.sides[1].active) {
          if (!p) continue;
          var boost = this.battle.runEvent('ChangeBoost', p, p, {name: 'Raid Energy'}, {...boosts});
          boost = p.getCappedBoost(boost);
          boost = this.battle.runEvent('TryBoost', p, p, {name: 'Raid Energy'}, {...boost});
          for (let boostName in boost) {
            const currentBoost = { [boostName]: boost[boostName], };
            let boostBy = p.boostBy(currentBoost);
            let msg = '-raidboost';
            if (boost[boostName] < 0 || p.boosts[boostName] === -6) {
              msg = '-raidunboost';
              boostBy = -boostBy;
            }
            if (boostBy) {
              this.battle.add(msg, p, boostName, boostBy);
              this.battle.runEvent('AfterEachBoost', p, p, {name: 'Raid Energy'}, currentBoost);
            }
          }
        }
        break;
      case "resetboss":
        const [uuid] = splitFirst(message, " ");
        this.battle.add('raidenergy', uuid, true);
        for (let p of this.battle.sides[1].active) {
          if (!p) continue;
          for (let i in p.boosts) {
            if (p.boosts[i] >= 0) continue;
            p.boosts[i] = 0;
          }
          if (p.status !== 'shield') p.cureStatus();
          this.battle.add('clearboss', p, uuid);
        }
        break;
      case "setboost":
        const [stat, stages, side] = splitFirst(message, " ");
        for (let p of this.battle.sides[parseInt(side)].active) {
          if (!p) continue;
          p.boosts[stat] = parseInt(stages);
        }
        break;
      case "setfield":
        const [id] = splitFirst(message, " ");
        const pokemon = this.battle.sides[1].active[0];
        const pseudoWeather = this.battle.dex.conditions.get(id);
        this.battle.field.pseudoWeather[pseudoWeather.id] = {
          id: pseudoWeather.id,
          source: pokemon,
          sourceSlot: pokemon.getSlot(),
          duration: pseudoWeather.duration
        };
        break;
      case "setsidecondition":
        const [id, side] = splitFirst(message, " ");
        const condition = this.battle.dex.conditions.get(id);
        const side = this.battle.sides[parseInt(side)];
        const pokemon = side.active[0];
        if (condition && !side.sideConditions[condition.id]) {
          side.sideConditions[condition.id] = {
            id: condition.id,
            target: side,
            source: pokemon,
            sourceSlot: pokemon.getSlot(),
            duration: condition.duration,
          };
          this.battle.effectState.layers = 1;
        }
        else if (condition && side.sideConditions[condition.id]) {
          if (condition.id === 'spikes' && this.battle.effectState.layers < 3) this.battle.effectState.layers++;
          if (condition.id === 'toxicspikes' && this.battle.effectState.layers < 2) this.battle.effectState.layers++;
        }
        break;
      case "setstatus":
        const [id, side] = splitFirst(message, " ");
        const status = this.battle.dex.conditions.get(id);
        for (let p of this.battle.sides[parseInt(side)].active) {
          if (status && p.status !== 'shield') {
            if (p.status === status.id) continue;
            this.battle.runEvent('SetStatus', p, null, null, status);
            p.status = status.id;
            p.statusState = { id: status.id, target: p };
            if (status.duration) p.statusState.duration = status.duration;
            if (status.durationCallback) p.statusState.duration = status.durationCallback.call(this.battle, p, null, null);
            if (status.id === 'slp') {
              p.statusState.startTime = this.battle.random(2, 5);
              p.statusState.time = p.statusState.startTime;
              this.battle.effectState = p.statusState;
            }
            this.battle.runEvent('AfterSetStatus', p, null, null, status);
          }
        }
        break;
      case "setvolatile":
        const [id, side] = splitFirst(message, " ");
        const status = this.battle.dex.conditions.get(id);
        for (let p of this.battle.sides[parseInt(side)].active) {
          if (!status || status.id === 'typechange') continue;
          if (p.volatiles[status.id]) {
            if (status.onRestart) this.battle.singleEvent('Restart', status, p.volatiles[status.id], p, null, null);
            continue;
          }
          p.volatiles[status.id] = { id: status.id, name: status.name, target: p };
          if (status.id === 'confusion') p.volatiles[status.id].time = this.battle.random(2, 6);
        }
        break;
      case "setterrain":
        const [id, isSilent] = splitFirst(message, " ");
        if (isSilent === "true") {
          const pokemon = this.battle.sides[1].active[0];
          const terrain = this.battle.dex.conditions.get(id);
          this.battle.field.terrain = terrain.id;
          this.battle.field.terrainState = {
            id: terrain.id,
            source: pokemon,
            sourceSlot: pokemon.getSlot(),
            duration: terrain.duration
          };
        }
        else {
          const pokemon = this.battle.sides[1].active[0];
          this.battle.field.setTerrain(id, pokemon);
        }
        break;
      case "setweather":
        const [id, isSilent] = splitFirst(message, " ");
        if (isSilent === "true") {
          const weather = this.battle.dex.conditions.get(id);
          this.battle.field.weather = weather.id;
          this.battle.field.weatherState = { id: weather.id };
          if (weather.duration) this.battle.field.weatherState.duration = weather.duration;
        }
        else {
          const pokemon = this.battle.sides[1].active[0];
          this.battle.field.setWeather(id, pokemon);
        }
        break;
      case "shieldadd":
        for (let p of this.battle.sides[1].active) {
          if (!p) continue;
          p.clearStatus();
          p.trySetStatus('shield', p);
          this.battle.add('shieldadd', p);
        }
        break;
      case "shieldremove":
        for (let p of this.battle.sides[1].active) {
          if (!p) continue;
          p.cureStatus();
          this.battle.add('shieldremove', p);
        }
        break;
      case "statboost":
        const [stat, stages, side, isSilent] = splitFirst(message, " ");
        var boosts = {};
        boosts[stat] = parseInt(stages);
        if (isSilent) {
          for (let p of this.battle.sides[parseInt(side)].active) {
            if (!p) continue;
            var boost = this.battle.runEvent('ChangeBoost', p, p, {name: 'Raid Energy'}, {...boosts});
            boost = p.getCappedBoost(boost);
            boost = this.battle.runEvent('TryBoost', p, p, {name: 'Raid Energy'}, {...boost});
            for (let boostName in boost) {
              const currentBoost = { [boostName]: boost[boostName], };
              let boostBy = p.boostBy(currentBoost);
              if (boostBy) this.battle.runEvent('AfterEachBoost', p, p, {name: 'Raid Energy'}, currentBoost);
            }
          }
        }
        else {
          for (let p of this.battle.sides[parseInt(side)].active) {
            if (!p) continue;
            this.battle.boost(boosts, p, null, '[from] Raid');
          }
        }
        break;
      case "supportboost":
        const [move, stat, stages] = splitFirst(message, " ");
        var boosts = {};
        boosts[stat] = parseInt(stages);
        for (let p of this.battle.sides[0].active) {
          if (!p) continue;
          else if (['gearup', 'magneticflux'].includes(move)) {
            if (!p.hasAbility('plus') && !p.hasAbility('minus')) continue;
          }
          battle.boost(boosts, p, null, '[from] Raid');
        }
        break;
      case "supportcure":
        for (let p of this.battle.sides[0].active) {
          if (!p) continue;
          p.cureStatus();
        }
        break;
      case "supportheal":
        const [heal] = splitFirst(message, " ");
        for (let p of this.battle.sides[0].active) {
          if (!p) continue;
          this.battle.heal(Math.floor(p.maxhp * parseFloat(heal)), p);
        }
        break;
      case "swapsideconditions":
        const sourceSideConditions = this.battle.sides[0].sideConditions;
        const targetSideConditions = this.battle.sides[1].sideConditions;
        const sourceTemp: typeof sourceSideConditions = {};
        const targetTemp: typeof targetSideConditions = {};
        for (const id in sourceSideConditions) {
          if (!sideConditions.includes(id)) continue;
          sourceTemp[id] = sourceSideConditions[id];
          delete sourceSideConditions[id];
        }
        for (const id in targetSideConditions) {
          if (!sideConditions.includes(id)) continue;
          targetTemp[id] = targetSideConditions[id];
          delete targetSideConditions[id];
        }
        for (const id in sourceTemp) {
          targetSideConditions[id] = sourceTemp[id];
        }
        for (const id in targetTemp) {
          sourceSideConditions[id] = targetTemp[id];
        }
        break;
      case "raiddynamax":
        for (let p of this.battle.sides[1].active) {
          if (!p) continue;
          p.addVolatile('dynamax');
        }
        break;
      case "raidformechange":
        const [forme] = splitFirst(message, " ");
        for (let p of this.battle.sides[1].active) {
          if (!p) continue;
          p.formeChange(forme, null, true);
        }
        break;
      case "raidmega":
        for (let p of this.battle.sides[1].active) {
          if (!p) continue;
          if (p.species.baseSpecies === 'Zygarde') p.canMegaEvo = 'Zygarde-Mega';
          this.battle.actions.runMegaEvo(p);
        }
        break;
      case "raidtera":
        for (let p of this.battle.sides[1].active) {
          if (!p) continue;
          this.battle.actions.terastallize(p);
        }
        break;
      case "playerboost":
        const [stat, stages] = splitFirst(message, " ");
        var boosts = {};
        boosts[stat] = parseInt(stages);
        for (let p of this.battle.sides[0].active) {
          if (!p) continue;
          this.battle.boost(boosts, p, null, '[from] Raid');
        }
        break;
      case "resetplayer":
        const [origin] = splitFirst(message, " ");
        battle.add('raidenergy', origin);
        for (let p of this.battle.sides[0].active) {
          if (!p) continue;
          for (let i in p.boosts) {
            if (p.boosts[i] <= 0) continue;
            p.boosts[i] = 0;
          }
          if (p.volatiles['cheerattack']) delete p.volatiles['cheerattack'];
          if (p.volatiles['cheerdefense']) delete p.volatiles['cheerdefense'];
          this.battle.add('clearplayer', p, origin);
        }
        break;
      case "usemove":
        const [move, side] = splitFirst(message, " ");
        let p = this.battle.sides[1].active[0];
        p.side.lastSelectedMove = this.battle.toID(move);
        this.battle.actions.runMove(move, p, parseInt(side), null, null, true, false);
        break;
      case "raidenergy":
        const [origin] = splitFirst(message, " ");
        this.battle.add('raidenergy', origin);
        break;
      case "donothing":
        this.battle.add('donothing');
        break;
      default:
        throw new Error(`Unrecognized command ">${type} ${message}"`);
    }
  }
  _writeEnd() {
    if (!this.atEOF)
      this.pushEnd();
    this._destroy();
  }
  _destroy() {
    if (this.battle)
      this.battle.destroy();
  }
}
function getPlayerStreams(stream) {
  const streams = {
    omniscient: new import_lib.Streams.ObjectReadWriteStream({
      write(data) {
        void stream.write(data);
      },
      writeEnd() {
        return stream.writeEnd();
      }
    }),
    spectator: new import_lib.Streams.ObjectReadStream({
      read() {
      }
    }),
    p1: new import_lib.Streams.ObjectReadWriteStream({
      write(data) {
        void stream.write(data.replace(/(^|\n)/g, `$1>p1 `));
      }
    }),
    p2: new import_lib.Streams.ObjectReadWriteStream({
      write(data) {
        void stream.write(data.replace(/(^|\n)/g, `$1>p2 `));
      }
    }),
    p3: new import_lib.Streams.ObjectReadWriteStream({
      write(data) {
        void stream.write(data.replace(/(^|\n)/g, `$1>p3 `));
      }
    }),
    p4: new import_lib.Streams.ObjectReadWriteStream({
      write(data) {
        void stream.write(data.replace(/(^|\n)/g, `$1>p4 `));
      }
    })
  };
  (async () => {
    for await (const chunk of stream) {
      const [type, data] = splitFirst(chunk, `
`);
      switch (type) {
        case "update":
          const channelMessages = (0, import_battle.extractChannelMessages)(data, [-1, 0, 1, 2, 3, 4]);
          streams.omniscient.push(channelMessages[-1].join("\n"));
          streams.spectator.push(channelMessages[0].join("\n"));
          streams.p1.push(channelMessages[1].join("\n"));
          streams.p2.push(channelMessages[2].join("\n"));
          streams.p3.push(channelMessages[3].join("\n"));
          streams.p4.push(channelMessages[4].join("\n"));
          break;
        case "sideupdate":
          const [side, sideData] = splitFirst(data, `
`);
          streams[side].push(sideData);
          break;
        case "end":
          break;
      }
    }
    for (const s of Object.values(streams)) {
      s.pushEnd();
    }
  })().catch((err) => {
    for (const s of Object.values(streams)) {
      s.pushError(err, true);
    }
  });
  return streams;
}
class BattlePlayer {
  constructor(playerStream, debug = false) {
    this.stream = playerStream;
    this.log = [];
    this.debug = debug;
  }
  async start() {
    for await (const chunk of this.stream) {
      this.receive(chunk);
    }
  }
  receive(chunk) {
    for (const line of chunk.split("\n")) {
      this.receiveLine(line);
    }
  }
  receiveLine(line) {
    if (this.debug)
      console.log(line);
    if (!line.startsWith("|"))
      return;
    const [cmd, rest] = splitFirst(line.slice(1), "|");
    if (cmd === "request")
      return this.receiveRequest(JSON.parse(rest));
    if (cmd === "error")
      return this.receiveError(new Error(rest));
    this.log.push(line);
  }
  receiveError(error) {
    throw error;
  }
  choose(choice) {
    void this.stream.write(choice);
  }
}
class BattleTextStream extends import_lib.Streams.ReadWriteStream {
  constructor(options) {
    super();
    this.battleStream = new BattleStream(options);
    this.currentMessage = "";
    void this._listen();
  }
  async _listen() {
    for await (let message of this.battleStream) {
      if (!message.endsWith("\n"))
        message += "\n";
      this.push(message + "\n");
    }
    this.pushEnd();
  }
  _write(message) {
    this.currentMessage += "" + message;
    const index = this.currentMessage.lastIndexOf("\n");
    if (index >= 0) {
      void this.battleStream.write(this.currentMessage.slice(0, index));
      this.currentMessage = this.currentMessage.slice(index + 1);
    }
  }
  _writeEnd() {
    return this.battleStream.writeEnd();
  }
}
//# sourceMappingURL=battle-stream.js.map
