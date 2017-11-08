package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.EventBus.Event;

public class AllTaskViewsDismissedEvent extends Event {
    public final int msgResId;

    public AllTaskViewsDismissedEvent(int msgResId) {
        this.msgResId = msgResId;
    }
}
