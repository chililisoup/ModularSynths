package dev.chililisoup.modularsynths.block.state;

import net.minecraft.world.level.block.state.properties.EnumProperty;

public final class ModBlockStateProperties {
    public static final EnumProperty<MonitorDisplay> MONITOR_DISPLAY = EnumProperty.create("display", MonitorDisplay.class);
}
