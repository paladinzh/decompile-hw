package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.EventBus.Event;
import com.android.systemui.recents.model.Task;

public class CancelEnterRecentsWindowAnimationEvent extends Event {
    public final Task launchTask;

    public CancelEnterRecentsWindowAnimationEvent(Task launchTask) {
        this.launchTask = launchTask;
    }
}
