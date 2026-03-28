package com.wildfire.render;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum BreastSide {
    LEFT(true),
    RIGHT(false);

    public final boolean isLeft;

    private BreastSide(boolean isLeft) {
        this.isLeft = isLeft;
    }
}