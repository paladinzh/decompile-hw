package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.EventBus.AnimatedEvent;

public class ConfigurationChangedEvent extends AnimatedEvent {
    public final boolean fromDeviceOrientationChange;
    public final boolean fromDisplayDensityChange;
    public final boolean fromMultiWindow;
    public final boolean hasStackTasks;

    public ConfigurationChangedEvent(boolean fromMultiWindow, boolean fromDeviceOrientationChange, boolean fromDisplayDensityChange, boolean hasStackTasks) {
        this.fromMultiWindow = fromMultiWindow;
        this.fromDeviceOrientationChange = fromDeviceOrientationChange;
        this.fromDisplayDensityChange = fromDisplayDensityChange;
        this.hasStackTasks = hasStackTasks;
    }
}
