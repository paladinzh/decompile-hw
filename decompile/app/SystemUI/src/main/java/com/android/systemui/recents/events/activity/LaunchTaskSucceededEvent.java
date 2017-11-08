package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.EventBus.Event;

public class LaunchTaskSucceededEvent extends Event {
    public final int taskIndexFromStackFront;

    public LaunchTaskSucceededEvent(int taskIndexFromStackFront) {
        this.taskIndexFromStackFront = taskIndexFromStackFront;
    }
}
