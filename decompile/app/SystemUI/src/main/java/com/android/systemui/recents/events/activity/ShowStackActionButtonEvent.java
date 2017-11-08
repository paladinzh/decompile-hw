package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.EventBus.Event;

public class ShowStackActionButtonEvent extends Event {
    public final boolean translate;

    @FindBugsSuppressWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public ShowStackActionButtonEvent(boolean translate) {
        this.translate = translate;
    }
}
