package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.EventBus.Event;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.AnimationProps;
import com.android.systemui.recents.views.TaskView;

public class TaskViewDismissedEvent extends Event {
    public final AnimationProps animation;
    public final Task task;
    public final TaskView taskView;

    public TaskViewDismissedEvent(Task task, TaskView taskView, AnimationProps animation) {
        this.task = task;
        this.taskView = taskView;
        this.animation = animation;
    }
}
