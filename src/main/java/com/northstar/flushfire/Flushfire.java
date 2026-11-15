package com.northstar.flushfire;

import net.neoforged.fml.common.Mod;

/** Independent implementation of surface-aligned fire. */
@Mod(Flushfire.MOD_ID)
public final class Flushfire {
    public static final String MOD_ID = "flushfire";

    public Flushfire() {
        // All behavior is provided by the model event and the two shape mixins.
    }
}
