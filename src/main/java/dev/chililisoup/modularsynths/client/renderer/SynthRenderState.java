package dev.chililisoup.modularsynths.client.renderer;

import dev.chililisoup.modularsynths.client.renderer.feature.CableFeatureRenderState;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.FrontAndTop;

import java.util.List;

@Environment(EnvType.CLIENT)
public class SynthRenderState extends BlockEntityRenderState {
    public AbstractSynth synth;
    public FrontAndTop orientation;
    public List<CableFeatureRenderState> cables;
}
