package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.EventBus.AnimatedEvent;
import com.android.systemui.recents.model.TaskStack;

public class MultiWindowStateChangedEvent extends AnimatedEvent {
    public final boolean inMultiWindow;
    public final boolean showDeferredAnimation;
    public final TaskStack stack;

    public MultiWindowStateChangedEvent(boolean inMultiWindow, boolean showDeferredAnimation, TaskStack stack) {
        this.inMultiWindow = inMultiWindow;
        this.showDeferredAnimation = showDeferredAnimation;
        this.stack = stack;
    }
}
