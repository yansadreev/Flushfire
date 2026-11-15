package com.northstar.flushfire;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Calculates a Y offset from the actual geometric support below a fire block.
 * No block or mod IDs are hardcoded; this works with blocks publishing vanilla
 * voxel shapes (including most NeoForge slabs, stairs, fences, walls, etc.).
 *
 * Returns 0 for full-height surfaces, negative for lower surfaces and positive
 * for structures whose center protrudes into the fire cell (e.g. fences).
 */
public final class FireSurface {
    private static final double EPS = 1.0e-7;

    private FireSurface() {}

    public static double offset(BlockGetter level, BlockPos firePos) {
        BlockPos below = firePos.below();
        BlockState support = level.getBlockState(below);

        // Collision geometry generally captures real surface heights (fence posts
        // extend beyond y=1). For blocks with no collision, use the outline shape.
        VoxelShape shape = support.getCollisionShape(level, below);
        if (shape.isEmpty()) {
            shape = support.getShape(level, below);
        }
        if (shape.isEmpty()) return 0.0;

        double centeredHeight = Double.NEGATIVE_INFINITY;
        double highestHeight = Double.NEGATIVE_INFINITY;
        for (AABB box : shape.toAabbs()) {
            highestHeight = Math.max(highestHeight, box.maxY);
            // The cross-shaped fire model is centered in its block.
            if (box.minX <= 0.5 + EPS && box.maxX >= 0.5 - EPS
                    && box.minZ <= 0.5 + EPS && box.maxZ >= 0.5 - EPS) {
                centeredHeight = Math.max(centeredHeight, box.maxY);
            }
        }
        double top = centeredHeight != Double.NEGATIVE_INFINITY
                ? centeredHeight : highestHeight;
        if (!Double.isFinite(top) || top <= EPS) return 0.0;

        // Prevent pathological shapes from moving fire more than one block.
        return Math.max(-1.0, Math.min(1.0, top - 1.0));
    }
}
