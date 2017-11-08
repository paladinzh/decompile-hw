package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.EventBus.Event;
import com.android.systemui.recents.model.Task;

public class HwTaskViewsDismissedEvent extends Event {
    public final Task mTask;

    public HwTaskViewsDismissedEvent(Task task) {
        this.mTask = task;
    }
}
