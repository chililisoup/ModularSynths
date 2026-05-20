package dev.chililisoup.modularsynths.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.FrontAndTop;

@Environment(EnvType.CLIENT)
public class SynthModuleRenderState {
    public FrontAndTop orientation;
    public int frontLightCoords;
}
