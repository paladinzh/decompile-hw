package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.EventBus.Event;

public class DraggingInRecentsEvent extends Event {
    public final float distanceFromTop;

    public DraggingInRecentsEvent(float distanceFromTop) {
        this.distanceFromTop = distanceFromTop;
    }
}
