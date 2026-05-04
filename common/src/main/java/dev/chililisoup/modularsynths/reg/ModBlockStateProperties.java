package dev.chililisoup.modularsynths.reg;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class ModBlockStateProperties {
    public static final IntegerProperty OFFSET_X;
    public static final IntegerProperty OFFSET_Y;
    public static final IntegerProperty OFFSET_Z;

    static {
        OFFSET_X = IntegerProperty.create("synth_offset_x", -8, 8);
        OFFSET_Y = IntegerProperty.create("synth_offset_y", -8, 8);
        OFFSET_Z = IntegerProperty.create("synth_offset_z", -8, 8);
    }
}
