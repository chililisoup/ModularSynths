package dev.chililisoup.modularsynths.block.state;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class ModBlockStateProperties {
    public static final IntegerProperty SYNTH_NOTE = IntegerProperty.create("note", 0, 36);
    public static final EnumProperty<MonitorDisplay> MONITOR_DISPLAY = EnumProperty.create("display", MonitorDisplay.class);
    public static final BooleanProperty POLYPHONIC = BooleanProperty.create("polyphonic");
    public static final BooleanProperty SUM = BooleanProperty.create("sum");
}
