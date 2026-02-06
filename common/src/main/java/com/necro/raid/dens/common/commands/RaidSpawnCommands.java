package com.necro.raid.dens.common.commands;

import com.cobblemon.mod.common.api.permission.Permission;
import com.cobblemon.mod.common.api.permission.PermissionLevel;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.util.PermissionUtilsKt;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.necro.raid.dens.common.commands.permission.RaidDenPermission;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidBucket;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.registry.RaidBucketRegistry;
import com.necro.raid.dens.common.registry.RaidRegistry;
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
    private static final Permission SPAWN_BOSS = new RaidDenPermission("command.spawnboss", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("crd")
            .then(PermissionUtilsKt.permission(
                Commands.literal("spawnboss")
                    .then(Commands.argument("pos", Vec3Argument.vec3())
                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                            .requires(context -> !context.isPlayer())
                            .then(Commands.literal("boss")
                                .then(Commands.argument("boss", ResourceLocationArgument.id())
                                    .suggests(RaidDenCommands.RAID_BOSSES)
                                    .executes(context -> spawnBoss(
                                        Vec3Argument.getVec3(context, "pos"),
                                        DimensionArgument.getDimension(context, "dimension"),
                                        ResourceLocationArgument.getId(context, "boss"),
                                        true, true, false
                                    ))
                                    .then(Commands.argument("noAI", BoolArgumentType.bool())
                                        .executes(context -> spawnBoss(
                                            Vec3Argument.getVec3(context, "pos"),
                                            DimensionArgument.getDimension(context, "dimension"),
                                            ResourceLocationArgument.getId(context, "boss"),
                                            BoolArgumentType.getBool(context, "noAI"), true, false
                                        ))
                                        .then(Commands.argument("isInvulnerable", BoolArgumentType.bool())
                                            .executes(context -> spawnBoss(
                                                Vec3Argument.getVec3(context, "pos"),
                                                DimensionArgument.getDimension(context, "dimension"),
                                                ResourceLocationArgument.getId(context, "boss"),
                                                BoolArgumentType.getBool(context, "noAI"),
                                                BoolArgumentType.getBool(context, "isInvulnerable"), false
                                            ))
                                            .then(Commands.argument("isPersistent", BoolArgumentType.bool())
                                                .executes(context -> spawnBoss(
                                                    Vec3Argument.getVec3(context, "pos"),
                                                    DimensionArgument.getDimension(context, "dimension"),
                                                    ResourceLocationArgument.getId(context, "boss"),
                                                    BoolArgumentType.getBool(context, "noAI"),
                                                    BoolArgumentType.getBool(context, "isInvulnerable"),
                                                    BoolArgumentType.getBool(context, "isPersistent")
                                                ))
                                            )
                                        )
                                    )
                                )
                            )
                            .then(Commands.literal("tier")
                                .then(Commands.argument("tier", StringArgumentType.word())
                                    .suggests(RaidDenCommands.RAID_TIERS)
                                    .executes(context -> spawnBossFromTier(
                                        Vec3Argument.getVec3(context, "pos"),
                                        DimensionArgument.getDimension(context, "dimension"),
                                        RaidTier.fromString(StringArgumentType.getString(context, "tier").toUpperCase()),
                                        true, true, false
                                    ))
                                    .then(Commands.argument("noAI", BoolArgumentType.bool())
                                        .executes(context -> spawnBossFromTier(
                                            Vec3Argument.getVec3(context, "pos"),
                                            DimensionArgument.getDimension(context, "dimension"),
                                            RaidTier.fromString(StringArgumentType.getString(context, "tier").toUpperCase()),
                                            BoolArgumentType.getBool(context, "noAI"), true, false
                                        ))
                                        .then(Commands.argument("isInvulnerable", BoolArgumentType.bool())
                                            .executes(context -> spawnBossFromTier(
                                                Vec3Argument.getVec3(context, "pos"),
                                                DimensionArgument.getDimension(context, "dimension"),
                                                RaidTier.fromString(StringArgumentType.getString(context, "tier").toUpperCase()),
                                                BoolArgumentType.getBool(context, "noAI"),
                                                BoolArgumentType.getBool(context, "isInvulnerable"), false
                                            ))
                                            .then(Commands.argument("isPersistent", BoolArgumentType.bool())
                                                .executes(context -> spawnBossFromTier(
                                                    Vec3Argument.getVec3(context, "pos"),
                                                    DimensionArgument.getDimension(context, "dimension"),
                                                    RaidTier.fromString(StringArgumentType.getString(context, "tier").toUpperCase()),
                                                    BoolArgumentType.getBool(context, "noAI"),
                                                    BoolArgumentType.getBool(context, "isInvulnerable"),
                                                    BoolArgumentType.getBool(context, "isPersistent")
                                                ))
                                            )
                                        )
                                    )
                                )
                            )
                            .then(Commands.literal("bucket")
                                .then(Commands.argument("bucket", ResourceLocationArgument.id())
                                    .suggests(RaidDenCommands.RAID_BUCKETS)
                                    .executes(context -> spawnBossFromBucket(
                                        Vec3Argument.getVec3(context, "pos"),
                                        DimensionArgument.getDimension(context, "dimension"),
                                        ResourceLocationArgument.getId(context, "bucket"),
                                        true, true, false
                                    ))
                                    .then(Commands.argument("noAI", BoolArgumentType.bool())
                                        .executes(context -> spawnBossFromBucket(
                                            Vec3Argument.getVec3(context, "pos"),
                                            DimensionArgument.getDimension(context, "dimension"),
                                            ResourceLocationArgument.getId(context, "bucket"),
                                            BoolArgumentType.getBool(context, "noAI"), true, false
                                        ))
                                        .then(Commands.argument("isInvulnerable", BoolArgumentType.bool())
                                            .executes(context -> spawnBossFromBucket(
                                                Vec3Argument.getVec3(context, "pos"),
                                                DimensionArgument.getDimension(context, "dimension"),
                                                ResourceLocationArgument.getId(context, "bucket"),
                                                BoolArgumentType.getBool(context, "noAI"),
                                                BoolArgumentType.getBool(context, "isInvulnerable"), false
                                            ))
                                            .then(Commands.argument("isPersistent", BoolArgumentType.bool())
                                                .executes(context -> spawnBossFromBucket(
                                                    Vec3Argument.getVec3(context, "pos"),
                                                    DimensionArgument.getDimension(context, "dimension"),
                                                    ResourceLocationArgument.getId(context, "bucket"),
                                                    BoolArgumentType.getBool(context, "noAI"),
                                                    BoolArgumentType.getBool(context, "isInvulnerable"),
                                                    BoolArgumentType.getBool(context, "isPersistent")
                                                ))
                                            )
                                        )
                                    )
                                )
                            )
                            .then(Commands.literal("random")
                                .executes(context -> spawnBoss(
                                    Vec3Argument.getVec3(context, "pos"),
                                    DimensionArgument.getDimension(context, "dimension"),
                                    null, true, true, false
                                ))
                                .then(Commands.argument("noAI", BoolArgumentType.bool())
                                    .executes(context -> spawnBoss(
                                        Vec3Argument.getVec3(context, "pos"),
                                        DimensionArgument.getDimension(context, "dimension"), null,
                                        BoolArgumentType.getBool(context, "noAI"), true, false
                                    ))
                                    .then(Commands.argument("isInvulnerable", BoolArgumentType.bool())
                                        .executes(context -> spawnBoss(
                                            Vec3Argument.getVec3(context, "pos"),
                                            DimensionArgument.getDimension(context, "dimension"), null,
                                            BoolArgumentType.getBool(context, "noAI"),
                                            BoolArgumentType.getBool(context, "isInvulnerable"), false
                                        ))
                                        .then(Commands.argument("isPersistent", BoolArgumentType.bool())
                                            .executes(context -> spawnBoss(
                                                Vec3Argument.getVec3(context, "pos"),
                                                DimensionArgument.getDimension(context, "dimension"), null,
                                                BoolArgumentType.getBool(context, "noAI"),
                                                BoolArgumentType.getBool(context, "isInvulnerable"),
                                                BoolArgumentType.getBool(context, "isPersistent")
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
                                    true, true, false
                                ))
                                .then(Commands.argument("noAI", BoolArgumentType.bool())
                                    .executes(context -> spawnBoss(
                                        context,
                                        Vec3Argument.getVec3(context, "pos"),
                                        ResourceLocationArgument.getId(context, "boss"),
                                        BoolArgumentType.getBool(context, "noAI"), true, false
                                    ))
                                    .then(Commands.argument("isInvulnerable", BoolArgumentType.bool())
                                        .executes(context -> spawnBoss(
                                            context,
                                            Vec3Argument.getVec3(context, "pos"),
                                            ResourceLocationArgument.getId(context, "boss"),
                                            BoolArgumentType.getBool(context, "noAI"),
                                            BoolArgumentType.getBool(context, "isInvulnerable"), false
                                        ))
                                        .then(Commands.argument("isPersistent", BoolArgumentType.bool())
                                            .executes(context -> spawnBoss(
                                                context,
                                                Vec3Argument.getVec3(context, "pos"),
                                                ResourceLocationArgument.getId(context, "boss"),
                                                BoolArgumentType.getBool(context, "noAI"),
                                                BoolArgumentType.getBool(context, "isInvulnerable"),
                                                BoolArgumentType.getBool(context, "isPersistent")
                                            ))
                                        )
                                    )
                                )
                            )
                        )
                        .then(Commands.literal("tier")
                            .requires(CommandSourceStack::isPlayer)
                            .then(Commands.argument("tier", StringArgumentType.word())
                                .suggests(RaidDenCommands.RAID_TIERS)
                                .executes(context -> spawnBossFromTier(
                                    context,
                                    Vec3Argument.getVec3(context, "pos"),
                                    RaidTier.fromString(StringArgumentType.getString(context, "tier").toUpperCase()),
                                    true, true, false
                                ))
                                .then(Commands.argument("noAI", BoolArgumentType.bool())
                                    .executes(context -> spawnBossFromTier(
                                        context,
                                        Vec3Argument.getVec3(context, "pos"),
                                        RaidTier.fromString(StringArgumentType.getString(context, "tier").toUpperCase()),
                                        BoolArgumentType.getBool(context, "noAI"), true, false
                                    ))
                                    .then(Commands.argument("isInvulnerable", BoolArgumentType.bool())
                                        .executes(context -> spawnBossFromTier(
                                            context,
                                            Vec3Argument.getVec3(context, "pos"),
                                            RaidTier.fromString(StringArgumentType.getString(context, "tier").toUpperCase()),
                                            BoolArgumentType.getBool(context, "noAI"),
                                            BoolArgumentType.getBool(context, "isInvulnerable"), false
                                        ))
                                        .then(Commands.argument("isPersistent", BoolArgumentType.bool())
                                            .executes(context -> spawnBossFromTier(
                                                context,
                                                Vec3Argument.getVec3(context, "pos"),
                                                RaidTier.fromString(StringArgumentType.getString(context, "tier").toUpperCase()),
                                                BoolArgumentType.getBool(context, "noAI"),
                                                BoolArgumentType.getBool(context, "isInvulnerable"),
                                                BoolArgumentType.getBool(context, "isPersistent")
                                            ))
                                        )
                                    )
                                )
                            )
                        )
                        .then(Commands.literal("bucket")
                            .requires(CommandSourceStack::isPlayer)
                            .then(Commands.argument("bucket", ResourceLocationArgument.id())
                                .suggests(RaidDenCommands.RAID_BUCKETS)
                                .executes(context -> spawnBossFromBucket(
                                    context,
                                    Vec3Argument.getVec3(context, "pos"),
                                    ResourceLocationArgument.getId(context, "bucket"),
                                    true, true, false
                                ))
                                .then(Commands.argument("noAI", BoolArgumentType.bool())
                                    .executes(context -> spawnBossFromBucket(
                                        context,
                                        Vec3Argument.getVec3(context, "pos"),
                                        ResourceLocationArgument.getId(context, "bucket"),
                                        BoolArgumentType.getBool(context, "noAI"), true, false
                                    ))
                                    .then(Commands.argument("isInvulnerable", BoolArgumentType.bool())
                                        .executes(context -> spawnBossFromBucket(
                                            context,
                                            Vec3Argument.getVec3(context, "pos"),
                                            ResourceLocationArgument.getId(context, "bucket"),
                                            BoolArgumentType.getBool(context, "noAI"),
                                            BoolArgumentType.getBool(context, "isInvulnerable"), false
                                        ))
                                        .then(Commands.argument("isPersistent", BoolArgumentType.bool())
                                            .executes(context -> spawnBossFromBucket(
                                                context,
                                                Vec3Argument.getVec3(context, "pos"),
                                                ResourceLocationArgument.getId(context, "bucket"),
                                                BoolArgumentType.getBool(context, "noAI"),
                                                BoolArgumentType.getBool(context, "isInvulnerable"),
                                                BoolArgumentType.getBool(context, "isPersistent")
                                            ))
                                        )
                                    )
                                )
                            )
                        )
                        .then(Commands.literal("random")
                            .requires(CommandSourceStack::isPlayer)
                            .executes(context -> spawnBoss(
                                context,
                                Vec3Argument.getVec3(context, "pos"),
                                null, true, true, false
                            ))
                            .then(Commands.argument("noAI", BoolArgumentType.bool())
                                .executes(context -> spawnBoss(
                                    context,
                                    Vec3Argument.getVec3(context, "pos"), null,
                                    BoolArgumentType.getBool(context, "noAI"), true, false
                                ))
                                .then(Commands.argument("isInvulnerable", BoolArgumentType.bool())
                                    .executes(context -> spawnBoss(
                                        context,
                                        Vec3Argument.getVec3(context, "pos"), null,
                                        BoolArgumentType.getBool(context, "noAI"),
                                        BoolArgumentType.getBool(context, "isInvulnerable"), false
                                    ))
                                    .then(Commands.argument("isPersistent", BoolArgumentType.bool())
                                        .executes(context -> spawnBoss(
                                            context,
                                            Vec3Argument.getVec3(context, "pos"), null,
                                            BoolArgumentType.getBool(context, "noAI"),
                                            BoolArgumentType.getBool(context, "isInvulnerable"),
                                            BoolArgumentType.getBool(context, "isPersistent")
                                        ))
                                    )
                                )
                            )
                        )
                    ),
                SPAWN_BOSS, true
            ))
        );
    }

    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        register(dispatcher);
    }

    private static int spawnBoss(CommandContext<CommandSourceStack> context, Vec3 vec3, ResourceLocation boss, boolean noAI, boolean isInvulnerable, boolean isPersistent) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        return spawnBoss(vec3, player.serverLevel(), boss, noAI, isInvulnerable, isPersistent);
    }

    private static int spawnBoss(Vec3 vec3, ServerLevel dimension, ResourceLocation boss, boolean noAI, boolean isInvulnerable, boolean isPersistent) {
        if (RaidUtils.isRaidDimension(dimension)) return 0;
        if (boss == null) boss = RaidRegistry.getRandomRaidBoss(dimension.getRandom(), dimension);
        RaidBoss raidBoss = RaidRegistry.getRaidBoss(boss);
        if (raidBoss == null) return 0;

        PokemonEntity pokemonEntity = raidBoss.getBossEntity(dimension, null);
        if (noAI) pokemonEntity.setNoAi(true);
        if (isInvulnerable) pokemonEntity.setInvulnerable(true);
        if (isPersistent) pokemonEntity.setPersistenceRequired();
        pokemonEntity.moveTo(vec3);
        dimension.addFreshEntity(pokemonEntity);
        return 1;
    }

    private static int spawnBossFromTier(CommandContext<CommandSourceStack> context, Vec3 vec3, RaidTier raidTier, boolean noAI, boolean isInvulnerable, boolean isPersistent) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        return spawnBossFromTier(vec3, player.serverLevel(), raidTier, noAI, isInvulnerable, isPersistent);
    }

    private static int spawnBossFromTier(Vec3 vec3, ServerLevel dimension, RaidTier raidTier, boolean noAI, boolean isInvulnerable, boolean isPersistent) {
        ResourceLocation boss = RaidRegistry.getRandomRaidBoss(dimension.getRandom(), dimension, raidTier, null, null);
        return spawnBoss(vec3, dimension, boss, noAI, isInvulnerable, isPersistent);
    }

    private static int spawnBossFromBucket(CommandContext<CommandSourceStack> context, Vec3 vec3, ResourceLocation bucket, boolean noAI, boolean isInvulnerable, boolean isPersistent) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        return spawnBossFromBucket(vec3, player.serverLevel(), bucket, noAI, isInvulnerable, isPersistent);
    }

    private static int spawnBossFromBucket(Vec3 vec3, ServerLevel dimension, ResourceLocation bucket, boolean noAI, boolean isInvulnerable, boolean isPersistent) {
        RaidBucket raidBucket = RaidBucketRegistry.getBucket(bucket);
        if (raidBucket == null) return 0;
        ResourceLocation boss = raidBucket.getRandomRaidBoss(dimension.getRandom(), dimension);
        return spawnBoss(vec3, dimension, boss, noAI, isInvulnerable, isPersistent);
    }
}
