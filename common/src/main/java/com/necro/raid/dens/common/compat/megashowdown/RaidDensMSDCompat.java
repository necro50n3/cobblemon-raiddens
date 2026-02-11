package com.necro.raid.dens.common.compat.megashowdown;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.AspectPropertyType;
import com.github.yajatkaul.mega_showdown.block.MegaShowdownBlocks;
import com.github.yajatkaul.mega_showdown.item.MegaShowdownItems;
import com.github.yajatkaul.mega_showdown.utils.GlowHandler;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.raid.RaidType;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public abstract class RaidDensMSDCompat {
    public static void setupTera(PokemonEntity pokemonEntity, Pokemon pokemon) {
        AspectPropertyType.INSTANCE.fromString("msd:tera_" + pokemon.getTeraType().showdownId()).apply(pokemon);
        applyEffects(pokemon, "mega_showdown:tera_init_" + pokemon.getTeraType().showdownId().toLowerCase());
        pokemon.getPersistentData().putBoolean("is_tera", true);
    }

    public static void setupDmax(PokemonEntity pokemonEntity, Pokemon pokemon) {
        AspectPropertyType.INSTANCE.fromString("msd:dmax").apply(pokemon);
        applyEffects(pokemon, "mega_showdown:dynamax");
        pokemon.getPersistentData().putBoolean("is_max", true);
        GlowHandler.applyDynamaxGlow(pokemonEntity);
    }

    public static ItemStack getTeraShard(RaidType raidType) {
        return switch (raidType) {
            case FIGHTING -> MegaShowdownItems.FIGHTING_TERA_SHARD.get().getDefaultInstance();
            case FLYING -> MegaShowdownItems.FLYING_TERA_SHARD.get().getDefaultInstance();
            case POISON -> MegaShowdownItems.POISON_TERA_SHARD.get().getDefaultInstance();
            case GROUND -> MegaShowdownItems.GROUND_TERA_SHARD.get().getDefaultInstance();
            case ROCK -> MegaShowdownItems.ROCK_TERA_SHARD.get().getDefaultInstance();
            case BUG -> MegaShowdownItems.BUG_TERA_SHARD.get().getDefaultInstance();
            case GHOST -> MegaShowdownItems.GHOST_TERA_SHARD.get().getDefaultInstance();
            case STEEL -> MegaShowdownItems.STEEL_TERA_SHARD.get().getDefaultInstance();
            case FIRE -> MegaShowdownItems.FIRE_TERA_SHARD.get().getDefaultInstance();
            case WATER -> MegaShowdownItems.WATER_TERA_SHARD.get().getDefaultInstance();
            case GRASS -> MegaShowdownItems.GRASS_TERA_SHARD.get().getDefaultInstance();
            case ELECTRIC -> MegaShowdownItems.ELECTRIC_TERA_SHARD.get().getDefaultInstance();
            case PSYCHIC -> MegaShowdownItems.PSYCHIC_TERA_SHARD.get().getDefaultInstance();
            case ICE -> MegaShowdownItems.ICE_TERA_SHARD.get().getDefaultInstance();
            case DRAGON -> MegaShowdownItems.DRAGON_TERA_SHARD.get().getDefaultInstance();
            case DARK -> MegaShowdownItems.DARK_TERA_SHARD.get().getDefaultInstance();
            case FAIRY -> MegaShowdownItems.FAIRY_TERA_SHARD.get().getDefaultInstance();
            case STELLAR -> MegaShowdownItems.STELLAR_TERA_SHARD.get().getDefaultInstance();
            default -> MegaShowdownItems.NORMAL_TERA_SHARD.get().getDefaultInstance();
        };
    }

    public static ItemStack getMaxMushroom() {
        return MegaShowdownBlocks.MAX_MUSHROOM.get().asItem().getDefaultInstance();
    }

    // Reflection to maintain compatibility with older versions
    // To be removed in Cobblemon 1.8 update
    private static void applyEffects(Pokemon pokemon, String effectId) {
        try {
            Class<?> effect = getEffectClass();
            Method getEffect = effect.getMethod("getEffect", String.class);
            Object effectInstance = getEffect.invoke( null, effectId);
            runApplyEffects(effect, effectInstance, pokemon);
        }
        catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
            CobblemonRaidDens.LOGGER.error("Error applying MSD Effect:", e);
        }
    }

    private static Class<?> getEffectClass() throws ClassNotFoundException {
        try {
            return Class.forName("com.github.yajatkaul.mega_showdown.api.codec.Effect");
        }
        catch (ClassNotFoundException e) {
            return Class.forName("com.github.yajatkaul.mega_showdown.codec.Effect");
        }
    }

    private static void runApplyEffects(Class<?> clazz, Object instance, Pokemon pokemon) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        try {
            Method method = clazz.getMethod("applyEffects", Pokemon.class, List.class, Optional.class, PokemonEntity.class);
            method.invoke(instance, pokemon, List.of(), Optional.empty(), null);
        }
        catch (NoSuchMethodException e) {
            Method method = clazz.getMethod("applyEffects", Pokemon.class, List.class, PokemonEntity.class);
            method.invoke(instance, pokemon, List.of(), null);
        }
    }
}
