package com.android.systemui.recents.events.ui.dragndrop;

import android.graphics.Point;
import com.android.systemui.recents.events.EventBus.Event;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.TaskView;

public class DragStartEvent extends Event {
    public final Task task;
    public final TaskView taskView;
    public final Point tlOffset;

    public DragStartEvent(Task task, TaskView taskView, Point tlOffset) {
        this.task = task;
        this.taskView = taskView;
        this.tlOffset = tlOffset;
    }
}
