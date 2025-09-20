package com.necro.raid.dens.common.compat.jade;

import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class RaidDensJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockIcon(RaidCrystalComponents.INSTANCE, RaidCrystalBlock.class);
        registration.registerBlockComponent(RaidCrystalComponents.INSTANCE, RaidCrystalBlock.class);
    }
}
