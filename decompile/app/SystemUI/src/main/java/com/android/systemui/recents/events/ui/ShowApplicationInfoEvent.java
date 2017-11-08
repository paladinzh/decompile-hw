package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.EventBus.Event;
import com.android.systemui.recents.model.Task;

public class ShowApplicationInfoEvent extends Event {
    public final Task task;

    public ShowApplicationInfoEvent(Task task) {
        this.task = task;
    }
}
