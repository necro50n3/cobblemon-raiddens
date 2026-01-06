package com.necro.raid.dens.common.compat.megashowdown;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.github.yajatkaul.mega_showdown.block.MegaShowdownBlocks;
import com.github.yajatkaul.mega_showdown.item.MegaShowdownItems;
import com.github.yajatkaul.mega_showdown.sound.MegaShowdownSounds;
import com.github.yajatkaul.mega_showdown.utils.GlowHandler;
import com.necro.raid.dens.common.data.raid.RaidType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public abstract class RaidDensMSDCompat {
    public static void setupTera(PokemonEntity pokemonEntity, Pokemon pokemon) {
        Vec3 entityPos = pokemonEntity.position();
        pokemonEntity.level().playSound(
            null, entityPos.x(), entityPos.y(), entityPos.z(),
            MegaShowdownSounds.TERASTALLIZATION.get(),
            SoundSource.PLAYERS, 0.2f, 1f
        );

        pokemon.getPersistentData().putBoolean("is_tera", true);
        GlowHandler.applyTeraGlow(pokemonEntity);
    }

    public static void setupDmax(PokemonEntity pokemonEntity) {
        Vec3 entityPos = pokemonEntity.position();
        pokemonEntity.level().playSound(
            null, entityPos.x(), entityPos.y(), entityPos.z(),
            MegaShowdownSounds.DYNAMAX.get(),
            SoundSource.PLAYERS, 0.4f, 0.5f + (float) Math.random() * 0.5f
        );

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
}
