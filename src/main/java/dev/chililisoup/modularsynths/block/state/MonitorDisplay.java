package dev.chililisoup.modularsynths.block.state;

import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum MonitorDisplay implements StringRepresentable {
    STRAIGHT ("straight"),
    CIRCULAR ("circular");

    public final String serializedName;

    MonitorDisplay(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public @NonNull String getSerializedName() {
        return this.serializedName;
    }
}
