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
      case "raidstart":
        this.battle.sides[1].pokemon.forEach(p => p.addVolatile('raidboss'));
        const [isDynamax, isTera] = splitFirst(message, " ").map(value => value === "true");
        if (isDynamax) this.battle.sides[1].pokemon.forEach(p => p.addVolatile('dynamax'));
        if (isTera) this.battle.sides[1].pokemon.forEach(p => this.battle.actions.terastallize(p));
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
