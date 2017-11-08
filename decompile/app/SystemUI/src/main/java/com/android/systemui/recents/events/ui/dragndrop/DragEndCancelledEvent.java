package com.android.systemui.recents.events.ui.dragndrop;

import com.android.systemui.recents.events.EventBus.AnimatedEvent;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.TaskView;

public class DragEndCancelledEvent extends AnimatedEvent {
    public final TaskStack stack;
    public final Task task;
    public final TaskView taskView;

    public DragEndCancelledEvent(TaskStack stack, Task task, TaskView taskView) {
        this.stack = stack;
        this.task = task;
        this.taskView = taskView;
    }
}
