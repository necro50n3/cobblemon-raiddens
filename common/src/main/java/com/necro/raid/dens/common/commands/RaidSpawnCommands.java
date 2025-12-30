package com.necro.raid.dens.common.commands;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.util.RaidRegistry;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class RaidSpawnCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("crd")
            .then(Commands.literal("spawnboss")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("pos", Vec3Argument.vec3())
                    .then(Commands.argument("dimension", DimensionArgument.dimension())
                        .then(Commands.literal("boss")
                            .then(Commands.argument("boss", ResourceLocationArgument.id())
                                .suggests(RaidDenCommands.RAID_BOSSES)
                                .executes(context -> spawnBoss(
                                    Vec3Argument.getVec3(context, "pos"),
                                    DimensionArgument.getDimension(context, "dimension"),
                                    ResourceLocationArgument.getId(context, "boss"),
                                    true, true
                                ))
                                .then(Commands.argument("noAI", BoolArgumentType.bool())
                                    .executes(context -> spawnBoss(
                                        Vec3Argument.getVec3(context, "pos"),
                                        DimensionArgument.getDimension(context, "dimension"),
                                        ResourceLocationArgument.getId(context, "boss"),
                                        BoolArgumentType.getBool(context, "noAI"), true
                                    ))
                                    .then(Commands.argument("isInvulnerable", BoolArgumentType.bool())
                                        .executes(context -> spawnBoss(
                                            Vec3Argument.getVec3(context, "pos"),
                                            DimensionArgument.getDimension(context, "dimension"),
                                            ResourceLocationArgument.getId(context, "boss"),
                                            BoolArgumentType.getBool(context, "noAI"),
                                            BoolArgumentType.getBool(context, "isInvulnerable")
                                        ))
                                    )
                                )
                            )
                        )
                    )
                    .then(Commands.literal("boss")
                        .requires(CommandSourceStack::isPlayer)
                        .then(Commands.argument("boss", ResourceLocationArgument.id())
                            .suggests(RaidDenCommands.RAID_BOSSES)
                            .executes(context -> spawnBoss(
                                context,
                                Vec3Argument.getVec3(context, "pos"),
                                ResourceLocationArgument.getId(context, "boss"),
                                true, true
                            ))
                            .then(Commands.argument("noAI", BoolArgumentType.bool())
                                .executes(context -> spawnBoss(
                                    context,
                                    Vec3Argument.getVec3(context, "pos"),
                                    ResourceLocationArgument.getId(context, "boss"),
                                    BoolArgumentType.getBool(context, "noAI"), true
                                ))
                                .then(Commands.argument("isInvulnerable", BoolArgumentType.bool())
                                    .executes(context -> spawnBoss(
                                        context,
                                        Vec3Argument.getVec3(context, "pos"),
                                        ResourceLocationArgument.getId(context, "boss"),
                                        BoolArgumentType.getBool(context, "noAI"),
                                        BoolArgumentType.getBool(context, "isInvulnerable")
                                    ))
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        register(dispatcher);
    }

    private static int spawnBoss(CommandContext<CommandSourceStack> context, Vec3 vec3, ResourceLocation boss, boolean noAI, boolean isInvulnerable) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        return spawnBoss(vec3, player.serverLevel(), boss, noAI, isInvulnerable);
    }

    private static int spawnBoss(Vec3 vec3, ServerLevel dimension, ResourceLocation boss, boolean noAI, boolean isInvulnerable) {
        if (RaidUtils.isCustomDimension(dimension)) return 0;
        RaidBoss raidBoss = RaidRegistry.getRaidBoss(boss);
        if (raidBoss == null) return 0;

        PokemonEntity pokemonEntity = raidBoss.getBossEntity(dimension);
        if (noAI) pokemonEntity.setNoAi(true);
        if (isInvulnerable) pokemonEntity.setInvulnerable(true);
        pokemonEntity.moveTo(vec3);
        dimension.addFreshEntity(pokemonEntity);
        return 1;
    }
}
