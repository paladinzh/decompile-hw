package com.android.systemui.recents.events.activity;

import android.graphics.Rect;
import com.android.systemui.recents.events.EventBus.Event;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.tv.views.TaskCardView;

public class LaunchTvTaskEvent extends Event {
    public final Rect targetTaskBounds;
    public final int targetTaskStack;
    public final Task task;
    public final TaskCardView taskView;

    public LaunchTvTaskEvent(TaskCardView taskView, Task task, Rect targetTaskBounds, int targetTaskStack) {
        this.taskView = taskView;
        this.task = task;
        this.targetTaskBounds = targetTaskBounds;
        this.targetTaskStack = targetTaskStack;
    }
}
