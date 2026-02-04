package com.necro.raid.dens.common.compat.wthit;


import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.IWailaCommonPlugin;

@SuppressWarnings("unused")
public class RaidDensWTHITPlugin implements IWailaCommonPlugin, IWailaClientPlugin {
    @Override
    public void register(ICommonRegistrar registrar) {
        registrar.blockData(new RaidCrystalDataProvider(), RaidCrystalBlockEntity.class);
    }

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.head(new RaidCrystalComponents(), RaidCrystalBlockEntity.class);
        registrar.body(new RaidCrystalComponents(), RaidCrystalBlockEntity.class);
        registrar.icon(new RaidCrystalComponents(), RaidCrystalBlockEntity.class);
    }
}
