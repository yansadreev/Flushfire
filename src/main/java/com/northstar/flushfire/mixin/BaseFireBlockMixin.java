package com.northstar.flushfire.mixin;

import com.northstar.flushfire.FireSurface;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** BaseFireBlock handles soul fire's outline. */
@Mixin(BaseFireBlock.class)
public abstract class BaseFireBlockMixin {
    @Inject(method = "getShape", at = @At("RETURN"), cancellable = true, remap = false)
    private void flushfire$offsetBaseShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context,
                                           CallbackInfoReturnable<VoxelShape> callback) {
        double delta = FireSurface.offset(level, pos);
        if (Math.abs(delta) > 1.0e-6) {
            callback.setReturnValue(callback.getReturnValue().move(0, delta, 0));
        }
    }
}
