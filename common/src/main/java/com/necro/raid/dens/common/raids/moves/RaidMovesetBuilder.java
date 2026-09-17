package com.necro.raid.dens.common.raids.moves;

import com.cobblemon.mod.common.api.moves.*;
import com.cobblemon.mod.common.pokemon.FormData;
import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RaidMovesetBuilder implements MovesetBuilder {
    private static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_custom");
    private ResourceLocation id = TYPE;

    private final List<String> slot1;
    private final List<String> slot2;
    private final List<String> slot3;
    private final List<String> slot4;

    public RaidMovesetBuilder(List<String> slot1, List<String> slot2, List<String> slot3, List<String> slot4) {
        this.slot1 = slot1;
        this.slot2 = slot2;
        this.slot3 = slot3;
        this.slot4 = slot4;
    }

    public static RaidMovesetBuilder fromMap(Map<String, List<String>> builder) {
        return new RaidMovesetBuilder(builder.get("slot1"), builder.get("slot2"), builder.get("slot3"), builder.get("slot4"));
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return this.id;
    }

    @Override
    public void setId(@NotNull ResourceLocation id) {
        this.id = id;
    }

    @Override
    public @NotNull ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public @NotNull MoveSet build(@NotNull FormData form, int level) {
        Set<MoveTemplate> chosenMoves = new HashSet<>();
        parseMoveOrMoveSelector(form, level, this.slot1, chosenMoves);
        parseMoveOrMoveSelector(form, level, this.slot2, chosenMoves);
        parseMoveOrMoveSelector(form, level, this.slot3, chosenMoves);
        parseMoveOrMoveSelector(form, level, this.slot4, chosenMoves);

        MoveSet moveset = new MoveSet();
        chosenMoves.stream().map(MoveTemplate::create).forEach(moveset::add);
        return moveset;
    }

    private void parseMoveOrMoveSelector(FormData form, int level, List<String> selectors, Set<MoveTemplate> chosenMoves) {
        for (String moveOrSelector : selectors) {
            MoveSelector selector = MoveSelector.Companion.getSelectors().get(moveOrSelector);
            MoveTemplate move;
            if (selector == null) {
                move = Moves.getByName(moveOrSelector);
            }
            else {
                move = selector.invoke(form, form.getMoves(), level, chosenMoves);
            }

            if (move != null) {
                chosenMoves.add(move);
                return;
            }
        }
    }

    static final Set<String> RECHARGE_MOVES = Set.of(
        "blastburn",
        "eternabeam",
        "frenzyplant",
        "gigaimpact",
        "hydrocannon",
        "hyperbeam",
        "meteorassault",
        "prismaticlaser",
        "roaroftime",
        "rockwrecker"
    );

    static final Set<String> DEBUFF_MOVES = Set.of(
        "superpower",
        "dracometeor",
        "fleurcannon",
        "leafstorm",
        "makeitrain",
        "overheat",
        "psychoboost"
    );
}
