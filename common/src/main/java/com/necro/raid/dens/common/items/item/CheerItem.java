package com.necro.raid.dens.common.items.item;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.item.battle.SimpleBagItemLike;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.util.IRaidAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CheerItem extends Item implements SimpleBagItemLike {
    private final CheerType cheerType;
    private final BagItem bagItem;

    private CheerItem(CheerType cheerType, String data) {
        super(new Properties().rarity(Rarity.RARE));
        this.cheerType = cheerType;
        this.bagItem = new BagItem() {
            @Override
            public @NotNull String getItemName() {
                return cheerType.getItemId();
            }

            @Override
            public @NotNull Item getReturnItem() {
                return Items.AIR;
            }

            @Override
            public boolean canUse(@NotNull PokemonBattle battle, @NotNull BattlePokemon target) {
                return target.getHealth() > 0;
            }

            @Override
            public @NotNull String getShowdownInput(@NotNull BattleActor actor, @NotNull BattlePokemon pokemon, @Nullable String s) {
                return cheerType.getShowdownString(data);
            }

            @Override
            public boolean canStillUse(@NotNull ServerPlayer player, @NotNull PokemonBattle battle, @NotNull BattleActor actor, @NotNull BattlePokemon target, @NotNull ItemStack stack) {
                return false;
            }
        };
    }

    public CheerItem(CheerType cheerType, int data) {
        this(cheerType, String.valueOf(data));
    }

    public CheerItem(CheerType cheerType, double data) {
        this(cheerType, String.valueOf(data));
    }

    @Override
    public @NotNull BagItem getBagItem() {
        return this.bagItem;
    }

    @Override
    public @Nullable BagItem getBagItem(@NotNull ItemStack stack) {
        return SimpleBagItemLike.DefaultImpls.getBagItem(this, stack);
    }

    @Override
    public boolean handleInteraction(@NotNull ServerPlayer player, @NotNull BattlePokemon battlePokemon, @NotNull ItemStack stack) {
        PokemonBattle battle = battlePokemon.getActor().getBattle();
        BattlePokemon raidPokemon = battle.getSide2().getActivePokemon().getFirst().getBattlePokemon();
        if (raidPokemon == null || raidPokemon.getEntity() == null || !((IRaidAccessor) raidPokemon.getEntity()).isRaidBoss()) return false;

        BagItem bagItem = this.getBagItem(stack);
        if (bagItem == null) return false;
        if (!battlePokemon.getActor().canFitForcedAction()) {
            player.sendSystemMessage(LocalizationUtilsKt.battleLang("bagitem.cannot").withStyle(ChatFormatting.RED));
            return false;
        }
        else if (!bagItem.canUse(battle, battlePokemon)) {
            player.sendSystemMessage(LocalizationUtilsKt.battleLang("bagitem.invalid").withStyle(ChatFormatting.RED));
            return false;
        }

        battlePokemon.getActor().sendUpdate(new BattleMakeChoicePacket());
        UUID raidId = ((IRaidAccessor) raidPokemon.getEntity()).getRaidId();
        RaidHelper.ACTIVE_RAIDS.get(raidId).cheer(battle, this.cheerType, this.getBagItem());
        return true;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!level.isClientSide()) {
            PokemonBattle battle = BattleRegistry.INSTANCE.getBattleByParticipatingPlayer((ServerPlayer) player);
            if (battle == null) return InteractionResultHolder.fail(itemStack);
            BattlePokemon battlePokemon = battle.getSide1().getActivePokemon().getFirst().getBattlePokemon();
            BattlePokemon raidPokemon = battle.getSide2().getActivePokemon().getFirst().getBattlePokemon();
            if (battlePokemon == null || raidPokemon == null) return InteractionResultHolder.fail(itemStack);
            else if (raidPokemon.getEntity() == null || !((IRaidAccessor) raidPokemon.getEntity()).isRaidBoss()) {
                return InteractionResultHolder.fail(itemStack);
            }

            boolean success = this.handleInteraction((ServerPlayer) player, battlePokemon, itemStack);
            if (!success) return InteractionResultHolder.fail(itemStack);
        }
        itemStack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    public enum CheerType {
        ATTACK("cheer_attack", "cheer_stat atk spa"),
        DEFENSE("cheer_defense", "cheer_stat def spd"),
        HEAL("cheer_heal", "cheer_heal");

        private final String id;
        private final String showdownString;

        CheerType(String id, String showdownString) {
            this.id = id;
            this.showdownString = showdownString;
        }

        public String getItemId() {
            return "item.cobblemonraiddens." + this.id;
        }

        public String getShowdownString(String data) {
            return this.showdownString + " " + data;
        }
    }
}
