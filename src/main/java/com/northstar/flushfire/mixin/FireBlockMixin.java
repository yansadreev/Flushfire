package com.northstar.flushfire.mixin;

import com.northstar.flushfire.FireRules;
import com.northstar.flushfire.FireSurface;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Ordinary fire remains VANILLA fire: its entityInside burns entities normally. */
@Mixin(FireBlock.class)
public abstract class FireBlockMixin {
    @Inject(method = "getShape", at = @At("RETURN"), cancellable = true, remap = false)
    private void flushfire$offsetShape(BlockState state, BlockGetter level, BlockPos pos,
                                       CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        double delta = FireSurface.offset(level, pos);
        if (Math.abs(delta) > 1.0e-6) {
            cir.setReturnValue(cir.getReturnValue().move(0, delta, 0));
        }
    }

    @Inject(method = "canSurvive", at = @At("RETURN"), cancellable = true, remap = false)
    private void flushfire$allowPartialSupports(BlockState state, LevelReader level, BlockPos pos,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (FireRules.shouldExtinguish(level, pos)) {
            cir.setReturnValue(false);
        } else if (!cir.getReturnValue() && FireRules.maySurvive(state, level, pos)) {
            cir.setReturnValue(true);
        }
    }

    // Vanilla's scheduled tick independently checks valid adjacent fuel.
    // A partial but non-flammable platform (e.g. stone slab) must use the same
    // transient lifetime as fire on a full stone block rather than be removed
    // immediately just because the top face is not a full support face.
    @Inject(method = "isValidFireLocation", at = @At("RETURN"), cancellable = true, remap = false)
    private void flushfire$partialFireLocation(BlockGetter level, BlockPos pos,
                                               CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && FireRules.hasPartialPlatform(level, pos)) {
            cir.setReturnValue(true);
        }
    }

    // Force a floor fire model rather than the variable wall-flame variants when
    // a partial platform is present. Preserve age, keep vanilla state elsewhere.
    @Inject(method = "getStateForPlacement(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"), cancellable = true, remap = false)
    private void flushfire$stableSurfaceFire(BlockGetter level, BlockPos pos,
                                              CallbackInfoReturnable<BlockState> cir) {
        if (FireRules.hasPartialPlatform(level, pos)) {
            BlockState previous = cir.getReturnValue();
            cir.setReturnValue(previous.setValue(FireBlock.NORTH, false)
                .setValue(FireBlock.SOUTH, false).setValue(FireBlock.EAST, false)
                .setValue(FireBlock.WEST, false).setValue(FireBlock.UP, false));
        }
    }

    @Inject(method = "updateShape", at = @At("RETURN"), cancellable = true, remap = false)
    private void flushfire$removeSpentFire(BlockState state, Direction side, BlockState neighbor,
                                           LevelAccessor level, BlockPos pos, BlockPos neighborPos,
                                           CallbackInfoReturnable<BlockState> cir) {
        if (FireRules.shouldExtinguish(level, pos)) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private void flushfire$scheduledSpentCleanup(BlockState state, ServerLevel level, BlockPos pos,
                                                  RandomSource random, CallbackInfo ci) {
        if (FireRules.shouldExtinguish(level, pos)) {
            level.removeBlock(pos, false);
            ci.cancel();
        }
    }
}
