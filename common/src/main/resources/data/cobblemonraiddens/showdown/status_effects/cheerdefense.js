{
    name: 'cheerdefense',
    onModifyDefPriority: 1,
    onModifyDef(def, pokemon) {
        this.debug('Defense Cheer Boost');
        return this.chainModify(1.5);
    },
    onModifySpDPriority: 1,
    onModifySpD(spd, pokemon) {
        this.debug('Defense Cheer Boost');
        return this.chainModify(1.5);
    }
}