package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.EventBus.Event;

public class HideRecentsEvent extends Event {
    public final boolean triggeredFromAltTab;
    public final boolean triggeredFromHomeKey;

    public HideRecentsEvent(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) {
        this.triggeredFromAltTab = triggeredFromAltTab;
        this.triggeredFromHomeKey = triggeredFromHomeKey;
    }
}
