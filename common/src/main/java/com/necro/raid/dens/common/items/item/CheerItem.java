package com.necro.raid.dens.common.items.item;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.item.battle.SimpleBagItemLike;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.showdown.CheerBagItem;
import com.necro.raid.dens.common.util.IRaidAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class CheerItem extends Item implements SimpleBagItemLike {
    private final CheerBagItem.CheerType cheerType;
    private final BagItem bagItem;

    private CheerItem(CheerBagItem.CheerType cheerType, BagItem bagItem) {
        super(new Properties().rarity(Rarity.RARE));
        this.cheerType = cheerType;
        this.bagItem = bagItem;
    }

    public CheerItem(CheerBagItem.CheerType cheerType, int data) {
        this(cheerType, new CheerBagItem(cheerType, data));
    }

    public CheerItem(CheerBagItem.CheerType cheerType, double data) {
        this(cheerType, new CheerBagItem(cheerType, data));
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

        String data = player.getName().getString();
        RaidHelper.ACTIVE_RAIDS.get(raidId).runCheer(battle, this.getBagItem(), data);
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
            itemStack.consume(1, player);
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) player, itemStack);
            return InteractionResultHolder.success(itemStack);
        }
        else return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.cobblemonraiddens.cheer.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(String.format("item.cobblemonraiddens.%s.tooltip", this.cheerType.getId())).withStyle(ChatFormatting.GRAY));
    }
}
