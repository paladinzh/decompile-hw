package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.EventBus.Event;

public class UpdateFreeformTaskViewVisibilityEvent extends Event {
    public final boolean visible;

    public UpdateFreeformTaskViewVisibilityEvent(boolean visible) {
        this.visible = visible;
    }
}
