package com.northstar.flushfire.client;

import com.northstar.flushfire.Flushfire;
import java.util.List;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

/** Wrap each fire model after baking so no vanilla/Sodium renderer Mixin is needed. */
@EventBusSubscriber(modid = Flushfire.MOD_ID, value = Dist.CLIENT)
public final class ClientModels {
    private ClientModels() {}

    @SubscribeEvent
    public static void onModelsBaked(ModelEvent.ModifyBakingResult event) {
        wrapBlock(event, Blocks.FIRE);
        wrapBlock(event, Blocks.SOUL_FIRE);
    }

    private static void wrapBlock(ModelEvent.ModifyBakingResult event, Block fire) {
        // Minecraft 1.21.1 declares getPossibleStates() as ImmutableList<BlockState>.
        // Keep iteration via the List interface without altering that JVM descriptor.
        List<BlockState> states = fire.getStateDefinition().getPossibleStates();
        for (BlockState state : states) {
            event.getModels().computeIfPresent(
                BlockModelShaper.stateToModelLocation(state),
                (key, model) -> model instanceof FireSurfaceModel ? model : new FireSurfaceModel(model)
            );
        }
    }
}
