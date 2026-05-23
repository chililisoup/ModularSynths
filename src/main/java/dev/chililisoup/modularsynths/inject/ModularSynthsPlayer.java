package dev.chililisoup.modularsynths.inject;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;

public interface ModularSynthsPlayer {
    default void modularSynths$openSynthScreen(SynthBlockEntity synthBlockEntity) {}
}
