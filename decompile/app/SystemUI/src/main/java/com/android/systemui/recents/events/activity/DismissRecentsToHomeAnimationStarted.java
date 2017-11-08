package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.EventBus.AnimatedEvent;

public class DismissRecentsToHomeAnimationStarted extends AnimatedEvent {
    public final boolean animated;

    public DismissRecentsToHomeAnimationStarted(boolean animated) {
        this.animated = animated;
    }
}
