package com.android.systemui.recents.events.ui.dragndrop;

import com.android.systemui.recents.events.EventBus.Event;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.RecentsViewTouchHandler;
import com.android.systemui.recents.views.TaskView;

public class DragStartInitializeDropTargetsEvent extends Event {
    public final RecentsViewTouchHandler handler;
    public final Task task;
    public final TaskView taskView;

    public DragStartInitializeDropTargetsEvent(Task task, TaskView taskView, RecentsViewTouchHandler handler) {
        this.task = task;
        this.taskView = taskView;
        this.handler = handler;
    }
}
