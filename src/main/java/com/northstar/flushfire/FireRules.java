package com.northstar.flushfire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Server-safe vanilla/NeoForge fire rules. Never assumes that a custom block
 * extends SlabBlock/StairBlock/FenceBlock: it uses collision shapes and
 * NeoForge's per-face flammability hooks instead.
 *
 * A fire block occupies an AIR cell adjacent to its fuel. It cannot share the
 * same world position as a door/trapdoor/gate; those are supported by lighting
 * an adjacent cell (see FireIgnitionHandler).
 */
public final class FireRules {
    private static final double EPS = 1.0e-5;

    private FireRules() {}

    public static boolean hasPartialPlatform(BlockGetter world, BlockPos firePos) {
        BlockPos below = firePos.below();
        BlockState state = world.getBlockState(below);
        if (isSpent(state)) return false;
        VoxelShape shape = state.getCollisionShape(world, below);
        if (shape.isEmpty()) shape = state.getShape(world, below);
        if (shape.isEmpty()) return false;
        double top = shape.max(Direction.Axis.Y);
        // No phantom flames over air, but include narrow/tall shapes such as fences.
        return top > EPS && top < 2.01D && !Block.isShapeFullBlock(shape);
    }

    public static boolean hasLiveFuel(BlockGetter world, BlockPos firePos) {
        for (Direction side : Direction.values()) {
            BlockPos neighbor = firePos.relative(side);
            BlockState state = world.getBlockState(neighbor);
            if (!isSpent(state) && state.isFlammable(world, neighbor, side.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBurnable(BlockGetter world, BlockPos pos, Direction face) {
        BlockState state = world.getBlockState(pos);
        return !isSpent(state) && state.isFlammable(world, pos, face);
    }

    /**
     * A conservative Burnt compatibility check. This deliberately doesn't treat
     * smouldering/ember stage blocks as finished, nor all blocks from Burnt as
     * finished. The whole set of other mods' burnt variants is not knowable.
     */
    public static boolean isSpent(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (!"burnt".equals(id.getNamespace()) && !"burnt_additions".equals(id.getNamespace())) {
            return false;
        }
        String path = id.getPath();
        return path.startsWith("burnt_") || path.startsWith("broken_burnt_")
                || path.startsWith("charred_") || path.startsWith("spent_")
                || path.equals("ash") || path.equals("ash_block")
                || path.startsWith("blackened_");
    }

    /**
     * Do not retain flames solely on an already spent Burnt material. Other
     * non-spent flammable neighbours may still support the same fire normally.
     * Run during neighbour shape updates and again on scheduled vanilla ticks.
     */
    public static boolean shouldExtinguish(BlockGetter world, BlockPos firePos) {
        boolean adjacentSpent = false;
        for (Direction direction : Direction.values()) {
            if (isSpent(world.getBlockState(firePos.relative(direction)))) {
                adjacentSpent = true;
                break;
            }
        }
        return adjacentSpent && !hasLiveFuel(world, firePos);
    }

    public static boolean maySurvive(BlockState state, LevelReader world, BlockPos firePos) {
        return hasPartialPlatform(world, firePos) || hasLiveFuel(world, firePos);
    }
}
