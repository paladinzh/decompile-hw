package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.EventBus.AnimatedEvent;
import com.android.systemui.recents.model.Task;

public class HwLockTaskViewEvent extends AnimatedEvent {
    public final Task mTask;

    public HwLockTaskViewEvent(Task task) {
        this.mTask = task;
    }
}
