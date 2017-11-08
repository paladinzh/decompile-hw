package com.android.systemui.recents.events.component;

import android.content.Context;
import com.android.systemui.recents.events.EventBus.Event;

public class ScreenPinningRequestEvent extends Event {
    public final Context applicationContext;
    public final int taskId;

    public ScreenPinningRequestEvent(Context context, int taskId) {
        this.applicationContext = context.getApplicationContext();
        this.taskId = taskId;
    }
}
