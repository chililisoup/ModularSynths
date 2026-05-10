package dev.chililisoup.modularsynths.synthesis;

import dev.chililisoup.modularsynths.block.SpeakerBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.client.audio.SynthSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class ClientSynthSpeaker extends SynthSpeaker {
    private @Nullable SynthSoundInstance soundInstance = null;

    public ClientSynthSpeaker(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    @Override
    protected void startClient() {
        BlockPos pos = this.synthBlockEntity.getBlockPos();
        if (this.soundInstance != null
                && this.soundInstance.position.equals(pos)
                && soundManager().isActive(this.soundInstance)
        ) return;

        this.killSoundInstance();
        this.soundInstance = new SynthSoundInstance(pos, this);
        soundManager().play(this.soundInstance);
    }

    @Override
    public void stop() {
        super.stop();
        this.killSoundInstance();
    }

    private void killSoundInstance() {
        if (this.soundInstance == null) return;
        this.soundInstance.kill();
        this.soundInstance = null;
    }

    @Override
    public void onLoad(Level level) {
        super.onLoad(level);
        // A client synth output is created on the singleplayer server
        if (level == null || !level.isClientSide()) return;
        if (this.synthBlockEntity.getBlockState().getOptionalValue(SpeakerBlock.POWERED).orElse(false))
            this.start();
        else this.stop();
    }

    private static SoundManager soundManager() {
        return Minecraft.getInstance().getSoundManager();
    }
}
