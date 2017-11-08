package com.android.systemui.recents.events.ui.focus;

import com.android.systemui.recents.events.EventBus.Event;

public class FocusNextTaskViewEvent extends Event {
    public final int timerIndicatorDuration;

    public FocusNextTaskViewEvent(int timerIndicatorDuration) {
        this.timerIndicatorDuration = timerIndicatorDuration;
    }
}
