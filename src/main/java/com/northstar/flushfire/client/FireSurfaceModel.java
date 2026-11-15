package com.northstar.flushfire.client;

import com.northstar.flushfire.FireSurface;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Offsets existing fire vertices per block position, preserving other mods'
 * textures, quads, render layers and random variants.
 *
 * Uses NeoForge ModelData, not BlockRenderDispatcher or Sodium internals.
 */
public final class FireSurfaceModel extends BakedModelWrapper<BakedModel> {
    private static final ModelProperty<Float> Y_OFFSET = new ModelProperty<>();

    public FireSurfaceModel(BakedModel original) {
        super(original);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos,
                                  BlockState state, ModelData incoming) {
        ModelData original = super.getModelData(level, pos, state, incoming);
        float yOffset = (float) FireSurface.offset(level, pos);
        return original.derive().with(Y_OFFSET, yOffset).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face,
                                    RandomSource random, ModelData data,
                                    @Nullable RenderType renderType) {
        List<BakedQuad> quads = super.getQuads(state, face, random, data, renderType);
        Float offset = data.get(Y_OFFSET);
        if (offset == null || Math.abs(offset) <= 1.0e-6F || quads.isEmpty()) return quads;

        ArrayList<BakedQuad> shifted = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices().clone();
            // Each quad has 4 vertices; the vertex stride comes from its array.
            // Position fields are X,Y,Z floats at offsets 0,1,2.
            int stride = vertices.length / 4;
            if (stride < 3) {
                shifted.add(quad);
                continue;
            }
            for (int i = 0; i < 4; ++i) {
                int yIndex = i * stride + 1;
                float oldY = Float.intBitsToFloat(vertices[yIndex]);
                vertices[yIndex] = Float.floatToRawIntBits(oldY + offset);
            }
            shifted.add(new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(),
                    quad.getSprite(), quad.isShade(), quad.hasAmbientOcclusion()));
        }
        return shifted;
    }
}
