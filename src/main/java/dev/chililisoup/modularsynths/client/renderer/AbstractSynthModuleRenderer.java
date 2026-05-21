package dev.chililisoup.modularsynths.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public abstract class AbstractSynthModuleRenderer<T extends AbstractSynth, S extends SynthModuleRenderState> {
    private final Class<T> synthClass;

    public AbstractSynthModuleRenderer(Class<T> synthClass) {
        this.synthClass = synthClass;
    }

    protected abstract void submit(
            S state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera,
            BlockEntityRendererProvider.Context context
    );

    protected void extractRenderState(T synth, S state, float partialTicks, FrontAndTop orientation) {
        state.orientation = orientation;
        state.frontLightCoords = synth.synthBlockEntity.getLevel() != null ?
                LevelRenderer.getLightCoords(
                        synth.synthBlockEntity.getLevel(),
                        synth.synthBlockEntity.getBlockPos().relative(orientation.front())
                ) : 0xf000f0;
    }

    protected abstract S createRenderState();

    protected final @Nullable T cast(AbstractSynth synth) {
        return this.synthClass.isInstance(synth) ? this.synthClass.cast(synth) : null;
    }

    public final @Nullable ModuleRendererSubmit prepareSubmit(
            AbstractSynth abstractSynth, float partialTicks, FrontAndTop orientation, BlockEntityRendererProvider.Context context
    ) {
        T synth = this.cast(abstractSynth);
        if (synth == null) return null;

        S state = this.createRenderState();
        this.extractRenderState(synth, state, partialTicks, orientation);
        return (poseStack, submitNodeCollector, camera) -> this.submit(state, poseStack, submitNodeCollector, camera, context);
    }

    public static void transformPoseStackToFront(PoseStack poseStack, FrontAndTop orientation, double scale) {
        poseStack.translate(orientation.front().getUnitVec3().scale(scale).add(0.5));
        poseStack.mulPose(orientation.front().getRotation());
        poseStack.mulPose(Direction.NORTH.getRotation());

        if (orientation.front().getAxis() == Direction.Axis.Y) {
            int dir = orientation.front().getAxisDirection().getStep();
            int rot = (dir + 1) * 90;
            poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(0, 0, dir, orientation.top().toYRot() + rot));
        }
    }

    public static void transformPoseStackToFront(PoseStack poseStack, FrontAndTop orientation) {
        transformPoseStackToFront(poseStack, orientation, 0.5);
    }

    @FunctionalInterface
    public interface ModuleRendererSubmit {
        void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera);
    }
}
