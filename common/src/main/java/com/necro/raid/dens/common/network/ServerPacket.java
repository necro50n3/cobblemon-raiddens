package com.necro.raid.dens.common.network;

import net.minecraft.server.level.ServerPlayer;

public interface ServerPacket {
    void handleServer(ServerPlayer player);
}
