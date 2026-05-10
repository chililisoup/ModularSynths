package dev.chililisoup.modularsynths.client.renderer.feature;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.chililisoup.modularsynths.client.inject.ModularSynthsSubmitNodeCollection;
import dev.chililisoup.modularsynths.client.reg.ModRenderTypes;
import dev.chililisoup.modularsynths.client.renderer.CableRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class CableFeatureRenderer {
    public void renderSolid(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource) {
        float time = getTime();
        for (ModularSynthsSubmitNodeCollection.CableSubmit cableSubmit : nodeCollection.modularSynths$getCableSubmits())
            renderCable(bufferSource.getBuffer(RenderTypes.leash()), cableSubmit, time);
    }

    public void renderTranslucent(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource) {
        float time = getTime();
        for (ModularSynthsSubmitNodeCollection.CableSubmit cableSubmit : nodeCollection.modularSynths$getTranslucentCableSubmits())
            renderCable(bufferSource.getBuffer(ModRenderTypes.TRANSLUCENT_CABLE), cableSubmit, time);
    }

    private static float getTime() {
        return (System.currentTimeMillis() % 3600_000) * 0.02F;
    }

    private static void renderCable(
            VertexConsumer buffer, ModularSynthsSubmitNodeCollection.CableSubmit cableSubmit, float time
    ) {
        for (int i = 0; i < cableSubmit.state().state().bezierPoints.length; i++)
            addCableVertexPair(buffer, -1, i, cableSubmit, time);
        for (int i = cableSubmit.state().state().bezierPoints.length - 1; i >= 0; i--)
            addCableVertexPair(buffer, 1, i, cableSubmit, time);
    }

    private static void addCableVertexPair(
            VertexConsumer buffer, int flip, int index, ModularSynthsSubmitNodeCollection.CableSubmit cableSubmit, float time
    ) {
        CableFeatureRenderState featureState = cableSubmit.state();
        CableRenderState state = featureState.state();

        float delta = (float) index / (state.bezierPoints.length - 1);
        float selectSin = featureState.selected() ?
                (Mth.sin(delta * state.bezierPoints.length * 0.25F + time * 0.15F) + 1) / 2 : 0;

        Vector3f pos = state.bezierPoints[index];
        Vector3f norm = state.bezierNormals[index];
        Vector3f up = state.bezierUps[index];
        Vector3f off = up.add(norm.mul(flip, new Vector3f()), new Vector3f());

        int checkerMultiplier = index % 2 == (flip == 1 ? 1 : 0) ? 0xb4b4b4 : 0xffffff;
        int alpha = featureState.selectionExists() && !featureState.selected() ? 85 : 255;
        int finalColor = ARGB.color(alpha, ARGB.multiply(
                state.color, ARGB.srgbLerp(selectSin, checkerMultiplier, 0xffffff)
        ));

        int blockLight = Mth.lerpInt(delta, featureState.startBlockLight(), featureState.endBlockLight());
        int skyLight = Mth.lerpInt(delta, featureState.startSkyLight(), featureState.endSkyLight());
        int light = featureState.selected() ? 0xffffff : LightCoordsUtil.pack(blockLight, skyLight);

        buffer.addVertex(cableSubmit.pose(), pos.x - off.x, pos.y - off.y, pos.z - off.z).setColor(finalColor).setLight(light);
        buffer.addVertex(cableSubmit.pose(), pos.x + off.x, pos.y + off.y, pos.z + off.z).setColor(finalColor).setLight(light);
    }
}
