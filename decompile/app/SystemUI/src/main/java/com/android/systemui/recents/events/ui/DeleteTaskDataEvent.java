package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.EventBus.Event;
import com.android.systemui.recents.model.Task;

public class DeleteTaskDataEvent extends Event {
    public boolean isRemoveAll = false;
    public final Task task;

    public DeleteTaskDataEvent(Task task) {
        this.task = task;
    }

    public DeleteTaskDataEvent(Task task, boolean isRemoveAll) {
        this.task = task;
        this.isRemoveAll = isRemoveAll;
    }
}
