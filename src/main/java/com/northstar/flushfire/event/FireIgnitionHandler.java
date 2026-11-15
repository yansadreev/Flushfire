package com.northstar.flushfire.event;

import com.northstar.flushfire.FireRules;
import com.northstar.flushfire.Flushfire;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Optional explicit attachment to doors, gates, fences, etc. Shift + ignite the
 * target face. Without sneak, Burnt/vanilla keep normal right-click precedence.
 * Only ordinary air-cell fire is placed, not fake cosmetic flames.
 */
@EventBusSubscriber(modid = Flushfire.MOD_ID)
public final class FireIgnitionHandler {
    private FireIgnitionHandler() {}

    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!player.isShiftKeyDown()) return;
        ItemStack held = event.getItemStack();
        if (!held.is(Items.FLINT_AND_STEEL) && !held.is(Items.FIRE_CHARGE)) return;
        Level level = event.getLevel();
        BlockPos sourcePos = event.getPos();
        Direction side = event.getFace();
        if (side == null || !FireRules.isBurnable(level, sourcePos, side)) return;

        BlockPos firePos = sourcePos.relative(side);
        if (!level.isEmptyBlock(firePos)) return; // Never overwrite the door/gate etc.
        BlockState fire = BaseFireBlock.getState(level, firePos);
        if (!fire.canSurvive(level, firePos)) return;

        if (!level.isClientSide()) {
            level.setBlock(firePos, fire, 3);
            if (!player.getAbilities().instabuild) {
                if (held.is(Items.FLINT_AND_STEEL)) {
                    EquipmentSlot slot = event.getHand() == InteractionHand.MAIN_HAND
                            ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                    held.hurtAndBreak(1, player, slot);
                } else {
                    held.shrink(1);
                }
            }
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
        event.setCanceled(true);
    }
}
