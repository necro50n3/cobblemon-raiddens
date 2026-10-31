package com.necro.raid.dens.common.commands;

import com.cobblemon.mod.common.api.permission.Permission;
import com.cobblemon.mod.common.api.permission.PermissionLevel;
import com.cobblemon.mod.common.util.PermissionUtilsKt;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.necro.raid.dens.common.commands.permission.RaidDenPermission;
import com.necro.raid.dens.common.data.raid.RaidBucket;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.raids.RaidSession;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.UUID;

public class RaidSpawnCommands {
    private static final Permission SPAWN_BOSS = new RaidDenPermission("command.spawnboss", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("crd")
            .then(PermissionUtilsKt.permission(
                Commands.literal("spawnboss")
                    .then(
                        addSpawnOptions(
                            Commands.argument("pos", Vec3Argument.vec3()),
                            false
                        )
                        .then(addSpawnOptions(
                            Commands.argument("dimension", DimensionArgument.dimension()).requires(context -> !context.isPlayer()),
                            true
                        ))
                    ),
                SPAWN_BOSS, true
            ))
        );
    }

    private static <T extends ArgumentBuilder<CommandSourceStack, T>> T addBossOptions(T builder, SpawnAction action) {
        builder
            .executes(context -> action.execute(context, true, true, false))
            .then(Commands.argument("noAI", BoolArgumentType.bool())
                .executes(context -> action.execute(
                    context,
                    BoolArgumentType.getBool(context, "noAI"),
                    true,
                    false
                ))
                .then(Commands.argument("isInvulnerable", BoolArgumentType.bool())
                    .executes(context -> action.execute(
                        context,
                        BoolArgumentType.getBool(context, "noAI"),
                        BoolArgumentType.getBool(context, "isInvulnerable"),
                        false
                    ))
                    .then(Commands.argument("isPersistent", BoolArgumentType.bool())
                        .executes(context -> action.execute(
                            context,
                            BoolArgumentType.getBool(context, "noAI"),
                            BoolArgumentType.getBool(context, "isInvulnerable"),
                            BoolArgumentType.getBool(context, "isPersistent")
                        ))
                    )
                )
            );

        return builder;
    }

    private static <T extends ArgumentBuilder<CommandSourceStack, T>> T addSpawnOptions(T builder, boolean hasDimensionArg) {
        builder
            .executes(context -> spawnBoss(
                context,
                Vec3Argument.getVec3(context, "pos"),
                hasDimensionArg ? DimensionArgument.getDimension(context, "dimension") : context.getSource().getLevel(),
                ResourceLocationArgument.getId(context, "boss"),
                true,
                true,
                false
            ))
            .then(Commands.literal("boss")
                .then(addBossOptions(Commands.argument("boss", ResourceLocationArgument.id()).suggests(RaidDenCommands.RAID_BOSSES),
                    (context, noAI, isInvulnerable, isPersistent) -> spawnBoss(
                        context,
                        Vec3Argument.getVec3(context, "pos"),
                        hasDimensionArg ? DimensionArgument.getDimension(context, "dimension") : context.getSource().getLevel(),
                        ResourceLocationArgument.getId(context, "boss"),
                        noAI,
                        isInvulnerable,
                        isPersistent
                    )
                ))
            )
            .then(Commands.literal("tier")
                .then(addBossOptions(Commands.argument("tier", StringArgumentType.word()).suggests(RaidDenCommands.RAID_TIERS),
                    (context, noAI, isInvulnerable, isPersistent) -> spawnBossFromTier(
                        context,
                        Vec3Argument.getVec3(context, "pos"),
                        hasDimensionArg ? DimensionArgument.getDimension(context, "dimension") : context.getSource().getLevel(),
                        RaidTier.fromString(StringArgumentType.getString(context, "tier").toUpperCase(Locale.ROOT)),
                        noAI,
                        isInvulnerable,
                        isPersistent
                    )
                ))
            )
            .then(Commands.literal("bucket")
                .then(addBossOptions(Commands.argument("bucket", ResourceLocationArgument.id()).suggests(RaidDenCommands.RAID_BUCKETS),
                    (context, noAI, isInvulnerable, isPersistent) -> spawnBossFromBucket(
                        context,
                        Vec3Argument.getVec3(context, "pos"),
                        hasDimensionArg ? DimensionArgument.getDimension(context, "dimension") : context.getSource().getLevel(),
                        ResourceLocationArgument.getId(context, "bucket"),
                        noAI,
                        isInvulnerable,
                        isPersistent
                    )
                ))
            )
            .then(addBossOptions(Commands.literal("random"),
                (context, noAI, isInvulnerable, isPersistent) -> spawnBoss(
                    context,
                    Vec3Argument.getVec3(context, "pos"),
                    hasDimensionArg ? DimensionArgument.getDimension(context, "dimension") : context.getSource().getLevel(),
                    null,
                    noAI,
                    isInvulnerable,
                    isPersistent
                ))
            );

        return builder;
    }

    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        register(dispatcher);
    }

    private static int spawnBoss(CommandContext<CommandSourceStack> context, Vec3 vec3, ServerLevel dimension, ResourceLocation location, boolean noAI, boolean isInvulnerable, boolean isPersistent) {
        if (RaidUtils.isRaidDimension(dimension)) return 0;
        if (location == null) location = CustomRaidRegistries.RAID_REGISTRY.getRandomRaidBoss(dimension.getRandom(), dimension);
        if (!CustomRaidRegistries.RAID_REGISTRY.exists(location)) return 0;

        CustomRaidRegistries.RAID_REGISTRY.requestRaidBoss(location, boss -> {
            RaidSession raid = new RaidSession(null, UUID.randomUUID(), boss, vec3, dimension);
            RaidHelper.ACTIVE_RAIDS.put(raid.getRaid(), raid);
            raid.setFlags(noAI, isInvulnerable, isPersistent);
            raid.start(context.getSource().getServer());
            if (raid.isFinished()) context.getSource().sendFailure(Component.translatable("error.cobblemonraiddens.raid_boss_spawn_fail"));
        }, dimension.getServer());
        return 1;
    }

    private static int spawnBossFromTier(CommandContext<CommandSourceStack> context, Vec3 vec3, ServerLevel dimension, RaidTier raidTier, boolean noAI, boolean isInvulnerable, boolean isPersistent) {
        ResourceLocation boss = CustomRaidRegistries.RAID_REGISTRY.getRandomRaidBoss(dimension.getRandom(), dimension, raidTier, null, null, null);
        return spawnBoss(context, vec3, dimension, boss, noAI, isInvulnerable, isPersistent);
    }

    private static int spawnBossFromBucket(CommandContext<CommandSourceStack> context, Vec3 vec3, ServerLevel dimension, ResourceLocation bucket, boolean noAI, boolean isInvulnerable, boolean isPersistent) {
        RaidBucket raidBucket = CustomRaidRegistries.RAID_BUCKET_REGISTRY.get(bucket);
        if (raidBucket == null) return 0;
        ResourceLocation boss = raidBucket.getRandomRaidBoss(dimension.getRandom(), dimension);
        return spawnBoss(context, vec3, dimension, boss, noAI, isInvulnerable, isPersistent);
    }

    @FunctionalInterface
    private interface SpawnAction {
        int execute(CommandContext<CommandSourceStack> context, boolean noAI, boolean isInvulnerable, boolean isPersistent) throws CommandSyntaxException;
    }
}
