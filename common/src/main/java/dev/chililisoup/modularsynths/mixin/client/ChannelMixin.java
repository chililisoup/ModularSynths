package dev.chililisoup.modularsynths.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.audio.Channel;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.client.synthesis.SynthesizedAudioFormat;
import dev.chililisoup.modularsynths.client.synthesis.SynthesizedAudioStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.sound.sampled.AudioFormat;

@Mixin(Channel.class)
public abstract class ChannelMixin {
    @WrapOperation(
            method = "attachBufferStream",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/audio/Channel;calculateBufferSize(Ljavax/sound/sampled/AudioFormat;I)I"))
    private int assignBufferSize(AudioFormat format, int sampleAmount, Operation<Integer> original) {
        if (format instanceof SynthesizedAudioFormat) {
            return ModularSynths.SAMPLE_BUFFER_SIZE * 2;
        } else return original.call(format, sampleAmount);
    }

    // Restarts synth audio stream if it gets cut off prematurely
    // this happens due to a lag spike, causing the audio playback to catch up to the buffered audio.
    // when it catches up, it sees there's no more audio, and stops itself
    // This could be fixed with better threading? maybe? I'm not good at that
    // or by having larger/more buffers, but that increases input delay
    @Inject(method = "getState", at = @At("RETURN"), cancellable = true)
    private void checkState(CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() != 4116) return;

        Channel channel = (Channel) (Object) this;
        if (!channel.initialized.get()) return;

        if (channel.stream instanceof SynthesizedAudioStream audioStream) {
            if (audioStream.isStreaming()) {
                channel.play();
                cir.setReturnValue(4114);
            }
        }
    }
}
