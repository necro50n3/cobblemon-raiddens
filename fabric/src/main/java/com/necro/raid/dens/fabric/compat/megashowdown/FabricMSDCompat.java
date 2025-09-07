package com.necro.raid.dens.fabric.compat.megashowdown;

import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.yajatkaul.mega_showdown.event.cobblemon.utils.DynamaxUtils;
import com.cobblemon.yajatkaul.mega_showdown.item.DynamaxItems;
import com.cobblemon.yajatkaul.mega_showdown.item.TeraMoves;
import com.cobblemon.yajatkaul.mega_showdown.sound.ModSounds;
import com.cobblemon.yajatkaul.mega_showdown.utility.GlowHandler;
import com.necro.raid.dens.common.compat.megashowdown.RaidDensMSDCompat;
import com.necro.raid.dens.common.raids.RaidType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class FabricMSDCompat extends RaidDensMSDCompat {
    @Override
    public void setupTera(PokemonEntity pokemonEntity, Pokemon pokemon) {
        Vec3 entityPos = pokemonEntity.position();
        pokemonEntity.level().playSound(
            null, entityPos.x(), entityPos.y(), entityPos.z(),
            ModSounds.TERASTALLIZATION,
            SoundSource.PLAYERS, 0.2f, 1f
        );

        if (pokemon.getSpecies().getName().equals("Ogerpon")) {
            new FlagSpeciesFeature("embody-aspect", true).apply(pokemon);
        }

        pokemon.getPersistentData().putBoolean("is_tera", true);
        GlowHandler.applyTeraGlow(pokemonEntity);
    }

    @Override
    public void setupDmax(PokemonEntity pokemonEntity) {
        Vec3 entityPos = pokemonEntity.position();
        pokemonEntity.level().playSound(
            null, entityPos.x(), entityPos.y(), entityPos.z(),
            ModSounds.DYNAMAX,
            SoundSource.PLAYERS, 0.4f, 0.5f + (float) Math.random() * 0.5f
        );

        if (DynamaxUtils.server == null && pokemonEntity.level() instanceof ServerLevel serverWorld) {
            DynamaxUtils.server = serverWorld.getServer();
        }

        GlowHandler.applyDynamaxGlow(pokemonEntity);
    }

    @Override
    public ItemStack getTeraShard(RaidType raidType) {
        return switch (raidType) {
            case FIGHTING -> TeraMoves.FIGHTING_TERA_SHARD.getDefaultInstance();
            case FLYING -> TeraMoves.FLYING_TERA_SHARD.getDefaultInstance();
            case POISON -> TeraMoves.POISON_TERA_SHARD.getDefaultInstance();
            case GROUND -> TeraMoves.GROUND_TERA_SHARD.getDefaultInstance();
            case ROCK -> TeraMoves.ROCK_TERA_SHARD.getDefaultInstance();
            case BUG -> TeraMoves.BUG_TERA_SHARD.getDefaultInstance();
            case GHOST -> TeraMoves.GHOST_TERA_SHARD.getDefaultInstance();
            case STEEL -> TeraMoves.STEEL_TERA_SHARD.getDefaultInstance();
            case FIRE -> TeraMoves.FIRE_TERA_SHARD.getDefaultInstance();
            case WATER -> TeraMoves.WATER_TERA_SHARD.getDefaultInstance();
            case GRASS -> TeraMoves.GRASS_TERA_SHARD.getDefaultInstance();
            case ELECTRIC -> TeraMoves.ELECTRIC_TERA_SHARD.getDefaultInstance();
            case PSYCHIC -> TeraMoves.PSYCHIC_TERA_SHARD.getDefaultInstance();
            case ICE -> TeraMoves.ICE_TERA_SHARD.getDefaultInstance();
            case DRAGON -> TeraMoves.DRAGON_TERA_SHARD.getDefaultInstance();
            case DARK -> TeraMoves.DARK_TERA_SHARD.getDefaultInstance();
            case FAIRY -> TeraMoves.FAIRY_TERA_SHARD.getDefaultInstance();
            case STELLAR -> TeraMoves.STELLAR_TERA_SHARD.getDefaultInstance();
            default -> TeraMoves.NORMAL_TERA_SHARD.getDefaultInstance();
        };
    }

    @Override
    public ItemStack getMaxMushroom() {
        return DynamaxItems.MAX_MUSHROOM.getDefaultInstance();
    }

    public static void init() {
        INSTANCE = new FabricMSDCompat();
    }
}
