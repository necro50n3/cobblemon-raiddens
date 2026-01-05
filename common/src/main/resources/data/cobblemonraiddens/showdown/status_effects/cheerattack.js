{
    name: 'cheerattack',
    onModifyAtkPriority: 1,
    onModifyAtk(atk, source, target, move) {
        this.debug('Attack Cheer Boost');
        return this.chainModify(1.5);
    },
    onModifySpAPriority: 1,
    onModifySpA(spa, source, target, move) {
        this.debug('Attack Cheer Boost');
        return this.chainModify(1.5);
    }
}