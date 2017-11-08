package com.android.systemui.recents.events.component;

import android.content.Context;
import com.android.systemui.recents.events.EventBus.Event;

public class RecentsVisibilityChangedEvent extends Event {
    public final Context applicationContext;
    public final boolean visible;

    public RecentsVisibilityChangedEvent(Context context, boolean visible) {
        this.applicationContext = context.getApplicationContext();
        this.visible = visible;
    }
}
