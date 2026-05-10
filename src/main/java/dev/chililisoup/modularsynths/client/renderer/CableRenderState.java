package dev.chililisoup.modularsynths.client.renderer;

import dev.chililisoup.modularsynths.block.SynthBlock;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import dev.chililisoup.modularsynths.synthesis.SynthInputConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CableRenderState {
    private static final double BEZIER_SCALE = 0.25;
    public static final float CABLE_SIZE = 0.05F;

    public int inPort;
    public int outPort;
    public int color;
    public BlockPos startBlock;
    public BlockPos endBlock;
    public Vec3 cableStart;
    public Vec3 cableEnd;
    public Vec3 cableStartNorm;
    public Vec3 cableEndNorm;
    public Vector3f[] bezierPoints;
    public Vector3f[] bezierNormals;
    public Vector3f[] bezierUps;

    public static List<CableRenderState> getCableRenderStates(AbstractSynth synth) {
        List<AbstractSynth.InPort> inputList = synth.getInputList();
        ArrayList<CableRenderState> renderStates = new ArrayList<>();

        for (int i = 0; i < inputList.size(); i++) {
            for (SynthInputConnection connection : inputList.get(i).connections()) {
                if (connection.cableRenderState != null) {
                    renderStates.add(connection.cableRenderState);
                    continue;
                }

                CableRenderState state = new CableRenderState();
                state.extractRenderState(synth, connection, i);
                connection.cableRenderState = state;
                renderStates.add(state);
            }
        }

        return renderStates;
    }

    public void extractRenderState(AbstractSynth synth, SynthInputConnection connection, int inPort) {
        this.inPort = inPort;
        this.outPort = connection.outPort();
        this.color = connection.color();
        this.extractCablePositions(synth, connection);
        this.extractBezier();
    }

    private void extractCablePositions(AbstractSynth synth, SynthInputConnection connection) {
        FrontAndTop orientation = synth.synthBlockEntity
                .getBlockState()
                .getValueOrElse(SynthBlock.ORIENTATION, FrontAndTop.NORTH_UP);

        AbstractSynth otherSynth = connection.synth();
        FrontAndTop otherOrientation = Optional.ofNullable(otherSynth)
                .map(i -> i.synthBlockEntity
                        .getBlockState()
                        .getValueOrElse(SynthBlock.ORIENTATION, FrontAndTop.NORTH_UP)
                ).orElse(FrontAndTop.NORTH_UP);

        Vec2[] inputPositions = synth.synthBlockEntity.getBlockState().getBlock() instanceof SynthBlock<?> synthBlock ?
                synthBlock.inputPositions() : new Vec2[0];
        Vec2[] outputPositions = otherSynth != null
                && otherSynth.synthBlockEntity.getBlockState().getBlock() instanceof SynthBlock<?> synthBlock ?
                synthBlock.outputPositions() : new Vec2[0];

        Vec2 inputPosition = inputPositions.length > this.inPort ?
                inputPositions[this.inPort] : new Vec2(0.5F, 0.5F);
        Vec2 outputPosition = outputPositions.length > connection.outPort() ?
                outputPositions[connection.outPort()] : new Vec2(0.5F, 0.5F);

        this.startBlock = synth.synthBlockEntity.getBlockPos();
        this.cableStart = SynthBlock.face3DPos(inputPosition, orientation);

        this.endBlock = connection.pos();
        this.cableEnd = SynthBlock.face3DPos(outputPosition, otherOrientation)
                .add(Vec3.atLowerCornerOf(this.endBlock.subtract(this.startBlock)));

        this.cableStartNorm = Vec3.atLowerCornerOf(orientation.front().getUnitVec3i());
        this.cableEndNorm = Vec3.atLowerCornerOf(otherOrientation.front().getUnitVec3i());
    }

    public void extractBezier() {
        double length = this.cableStart.subtract(this.cableEnd).length();
        int pointCount = (int) (length * 16) + 2;

        this.bezierPoints = new Vector3f[pointCount];
        this.bezierNormals = new Vector3f[pointCount];
        this.bezierUps = new Vector3f[pointCount];

        double usedScale = BEZIER_SCALE * (
                1 - this.cableStartNorm.dot(this.cableEndNorm) +
                        4 * (1 - Math.exp(-length / 4))
        );

        Vec3[] controlPoints = new Vec3[]{
                this.cableStart,
                this.cableStart.add(this.cableStartNorm.scale(usedScale)),
                this.cableEnd.add(this.cableEndNorm.scale(usedScale)),
                this.cableEnd
        };

        // init values
        for (int i = 0; i < pointCount; i++) {
            this.bezierPoints[i] = cubicBezier((float) i / (pointCount - 1), controlPoints);
            this.bezierUps[i] = cubicBezierDerivative((float) i / (pointCount - 1), controlPoints);
            this.bezierNormals[i] = new Vector3f(this.bezierUps[i].z, 0, this.bezierUps[i].x).orthogonalize(this.bezierUps[i]);

            if (this.bezierNormals[i].y < 0) this.bezierNormals[i].rotateAxis(Mth.PI, this.bezierUps[i].x, this.bezierUps[i].y, this.bezierUps[i].z);
        }

        // blur normals
        for (int b = 0; b < 4; b++) {
            for (int i = 0; i < pointCount - 1; i++) {
                this.bezierNormals[i].lerp(this.bezierNormals[i + 1], 0.5F);
            }

            for (int i = pointCount - 1; i > 0; i--) {
                this.bezierNormals[i].lerp(this.bezierNormals[i - 1], 0.5F);
            }
        }

        // finalize values
        for (int i = 0; i < pointCount; i++) {
            this.bezierNormals[i].normalize();
            this.bezierUps[i].cross(this.bezierNormals[i]);
            this.bezierNormals[i].mul(CABLE_SIZE / 2);
            this.bezierUps[i].mul(CABLE_SIZE / 2);
        }
    }

    private static Vector3f cubicBezier(float delta, Vec3[] points) {
        Vec3 first = points[1].subtract(points[0]).scale(delta * 3);
        Vec3 second = points[2].subtract(points[1].scale(2)).add(points[0]).scale(delta * delta * 3);
        Vec3 third = points[3].subtract(points[0]).add(points[1].subtract(points[2]).scale(3)).scale(delta * delta * delta);
        return points[0].add(first).add(second).add(third).toVector3f();
    }

    private static Vector3f cubicBezierDerivative(float delta, Vec3[] points) {
        Vec3 first = points[1].subtract(points[0]).scale(3);
        Vec3 second = points[2].subtract(points[1].scale(2)).add(points[0]).scale(delta * 6);
        Vec3 third = points[3].subtract(points[0]).add(points[1].subtract(points[2]).scale(3)).scale(delta * delta * 3);
        return first.add(second).add(third).normalize().toVector3f();
    }
}
