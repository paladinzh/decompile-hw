package com.android.systemui.recents.events.activity;

import android.graphics.Rect;
import com.android.systemui.recents.events.EventBus.Event;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.TaskView;

public class LaunchTaskEvent extends Event {
    public final boolean screenPinningRequested;
    public final Rect targetTaskBounds;
    public final int targetTaskStack;
    public final Task task;
    public final TaskView taskView;

    public LaunchTaskEvent(TaskView taskView, Task task, Rect targetTaskBounds, int targetTaskStack, boolean screenPinningRequested) {
        this.taskView = taskView;
        this.task = task;
        this.targetTaskBounds = targetTaskBounds;
        this.targetTaskStack = targetTaskStack;
        this.screenPinningRequested = screenPinningRequested;
    }
}
