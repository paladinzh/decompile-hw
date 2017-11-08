package com.android.systemui.recents.events.activity;

import android.graphics.Rect;
import com.android.systemui.recents.events.EventBus.Event;

public class DockedTopTaskEvent extends Event {
    public int dragMode;
    public Rect initialRect;

    public DockedTopTaskEvent(int dragMode, Rect initialRect) {
        this.dragMode = dragMode;
        this.initialRect = initialRect;
    }
}
