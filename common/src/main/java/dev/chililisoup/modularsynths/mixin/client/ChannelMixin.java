package dev.chililisoup.modularsynths.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.audio.Channel;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.client.synthesis.SynthesizedAudioFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import javax.sound.sampled.AudioFormat;

@Mixin(Channel.class)
public abstract class ChannelMixin {
    @WrapOperation(
            method = "attachBufferStream",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/audio/Channel;calculateBufferSize(Ljavax/sound/sampled/AudioFormat;I)I")
    )
    private int assignBufferSize(AudioFormat format, int sampleAmount, Operation<Integer> original) {
        if (format instanceof SynthesizedAudioFormat) {
            return ModularSynths.SAMPLE_BUFFER_SIZE * 2;
        } else return original.call(format, sampleAmount);
    }
}
