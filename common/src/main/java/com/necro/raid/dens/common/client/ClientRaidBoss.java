package com.necro.raid.dens.common.client;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public record ClientRaidBoss(ResourceLocation id, String species, Set<String> aspects) {}
