package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.EventBus.Event;
import com.android.systemui.recents.model.RecentsPackageMonitor;

public class PackagesChangedEvent extends Event {
    public final RecentsPackageMonitor monitor;
    public final String packageName;
    public final int userId;

    public PackagesChangedEvent(RecentsPackageMonitor monitor, String packageName, int userId) {
        this.monitor = monitor;
        this.packageName = packageName;
        this.userId = userId;
    }
}
