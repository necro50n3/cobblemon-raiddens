{
    name: 'raidboss',
    onDamage(damage, target, source, effect) {
        try {
            const name = effect.fullname === 'tox' ? 'psn' : effect.fullname;
            this.add('split', 'p2');
            switch (effect.id) {
            case 'strugglerecoil':
                this.add('-raiddamage', target, damage, '[from] Recoil');
                this.add('-raiddamage', target, Math.floor(damage / target.maxhp * 100), '[from] Recoil');
                break;
            case 'partiallytrapped':
                this.add('-raiddamage', target, damage, '[from] ' + this.effectState.sourceEffect.fullname, '[partiallytrapped]');
                this.add('-raiddamage', target, Math.floor(damage / target.maxhp * 100), '[from] Recoil');
                break;
            case 'powder':
                this.add('-raiddamage', target, damage, '[silent]');
                this.add('-raiddamage', target, Math.floor(damage / target.maxhp * 100), '[silent]');
                break;
            case 'confused':
            case 'confusion':
                this.add('-raiddamage', target, damage, '[from] confusion');
                this.add('-raiddamage', target, Math.floor(damage / target.maxhp * 100), '[from] confusion');
                break;
            default:
                if (effect.effectType === 'Move' || !name) {
                    this.add('-raiddamage', target, damage);
                    this.add('-raiddamage', target, Math.floor(damage / target.maxhp * 100));
                } else if (source && (source !== target || effect.effectType === 'Ability')) {
                    this.add('-raiddamage', target, damage, '[from] ' + name, '[of] ' + source);
                    this.add('-raiddamage', target, Math.floor(damage / target.maxhp * 100), '[from] ' + name, '[of] ' + source);
                } else {
                    this.add('-raiddamage', target, damage, '[from] ' + name);
                    this.add('-raiddamage', target, Math.floor(damage / target.maxhp * 100), '[from] ' + name);
                }
                break;
            }

            if (effect.recoil) {
                let recoil = Math.round(damage * effect.recoil[0] / effect.recoil[1]);
                this.damage(recoil, source, target, 'recoil');
            }

            if (effect.drain) {
                let heal = Math.round(damage * effect.drain[0] / effect.drain[1]);
                this.heal(heal, source, target, 'drain');
            }

            if (effect.id === 'leechseed') {
                this.heal(target.baseMaxhp / 8, source, target, effect);
            }

            if (source && source.item === 'shellbell' && !source.forceSwitchFlag) {
                this.heal(damage / 8, source, source, source.getItem());
            }
        }
        catch (e) {
            console.log(e);
        }

        return 0;
    },
    onTryHeal(damage, target, source, effect) {
        if (target.hp === target.maxhp) return false;

        try {
            this.add('split', 'p2');
            switch (effect?.id) {
                case 'leechseed':
                case 'rest':
                    this.add('-raidheal', target, damage, '[silent]');
                    this.add('-raidheal', target, Math.floor(damage / target.maxhp * 100), '[silent]');
                    break;
                case 'drain':
                    this.add('-raidheal', target, damage, '[from] drain', '[of] ' + source);
                    this.add('-raidheal', target, Math.floor(damage / target.maxhp * 100), '[from] drain', '[of] ' + source);
                    break;
                case 'wish':
                    break;
                case 'zpower':
                    this.add('-raidheal', target, damage, '[zeffect]');
                    this.add('-raidheal', target, Math.floor(damage / target.maxhp * 100), '[zeffect]');
                    break;
                default:
                    if (!effect) break;
                    if (effect.effectType === 'Move') {
                        this.add('-raidheal', target, damage);
                    } else if (source && source !== target) {
                        this.add('-raidheal', target, damage, '[from] ' + effect.fullname, '[of] ' + source);
                        this.add('-raidheal', target, Math.floor(damage / target.maxhp * 100), '[from] ' + effect.fullname, '[of] ' + source);
                    } else {
                        this.add('-raidheal', target, damage, '[from] ' + effect.fullname);
                        this.add('-raidheal', target, Math.floor(damage / target.maxhp * 100), '[from] ' + effect.fullname);
                    }
                    break;
            }
        }
        catch (e) {
            console.log(e);
        }

        return false;
    },
    onResidual(pokemon) {
        let wasDamaged = false;
        for (let i = 0; i < this.log.length - 2; i++) {
            if (this.log[i] === '|split|p2' && this.log[i + 1].startsWith('|-damage|p2a:') && this.log[i + 2].startsWith('|-damage|p2a:')) {
                this.log.splice(i, 3);
                wasDamaged = true;
                i--;
            }
            else if (this.log[i] === '|split|p2' && this.log[i + 1].startsWith('|-heal|p2a:') && this.log[i + 2].startsWith('|-heal|p2a:')) {
                this.log.splice(i, 3);
                i--;
            }
        }

        this.effectState.stage = 0;
        if (wasDamaged && pokemon.hp === pokemon.maxhp) pokemon.hp = Math.max(1, pokemon.hp - 1);
    }
}