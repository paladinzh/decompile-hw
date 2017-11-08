package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.events.EventBus.AnimatedEvent;

public class EnterRecentsWindowAnimationCompletedEvent extends AnimatedEvent {
    public RecentsActivityLaunchState launchState;

    public EnterRecentsWindowAnimationCompletedEvent(RecentsActivityLaunchState launchState) {
        this.launchState = launchState;
    }
}
