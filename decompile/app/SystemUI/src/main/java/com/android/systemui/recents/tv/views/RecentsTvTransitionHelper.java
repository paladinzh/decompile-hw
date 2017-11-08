package com.android.systemui.recents.tv.views;

import android.app.ActivityOptions;
import android.app.ActivityOptions.OnAnimationStartedListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.IRemoteCallback.Stub;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManagerGlobal;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.ExitRecentsWindowFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.LaunchTaskFailedEvent;
import com.android.systemui.recents.events.activity.LaunchTaskSucceededEvent;
import com.android.systemui.recents.events.activity.LaunchTvTaskStartedEvent;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;

public class RecentsTvTransitionHelper {
    private Context mContext;
    private Handler mHandler;

    public RecentsTvTransitionHelper(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
    }

    public void launchTaskFromRecents(TaskStack stack, final Task task, TaskStackHorizontalGridView stackView, TaskCardView taskView, Rect bounds, int destinationStack) {
        OnAnimationStartedListener animStartedListener;
        ActivityOptions opts = ActivityOptions.makeBasic();
        if (bounds != null) {
            if (bounds.isEmpty()) {
                bounds = null;
            }
            opts.setLaunchBounds(bounds);
        }
        if (task.thumbnail == null || task.thumbnail.getWidth() <= 0 || task.thumbnail.getHeight() <= 0) {
            animStartedListener = new OnAnimationStartedListener() {
                public void onAnimationStarted() {
                    EventBus.getDefault().send(new ExitRecentsWindowFirstAnimationFrameEvent());
                }
            };
        } else {
            animStartedListener = new OnAnimationStartedListener() {
                public void onAnimationStarted() {
                    EventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(task));
                    EventBus.getDefault().send(new ExitRecentsWindowFirstAnimationFrameEvent());
                }
            };
        }
        if (taskView == null) {
            startTaskActivity(stack, task, taskView, opts, animStartedListener);
            return;
        }
        EventBus.getDefault().send(new LaunchTvTaskStartedEvent(taskView));
        startTaskActivity(stack, task, taskView, opts, animStartedListener);
    }

    private void startTaskActivity(TaskStack stack, Task task, TaskCardView taskView, ActivityOptions opts, OnAnimationStartedListener animStartedListener) {
        if (Recents.getSystemServices().startActivityFromRecents(this.mContext, task.key, task.title, opts)) {
            int taskIndexFromFront = 0;
            int taskIndex = stack.indexOfStackTask(task);
            if (taskIndex > -1) {
                taskIndexFromFront = (stack.getTaskCount() - taskIndex) - 1;
            }
            EventBus.getDefault().send(new LaunchTaskSucceededEvent(taskIndexFromFront));
        } else {
            EventBus.getDefault().send(new LaunchTaskFailedEvent());
        }
        Rect taskRect = taskView.getFocusedThumbnailRect();
        if (taskRect != null && task.thumbnail != null) {
            IRemoteCallback iRemoteCallback = null;
            if (animStartedListener != null) {
                final OnAnimationStartedListener onAnimationStartedListener = animStartedListener;
                iRemoteCallback = new Stub() {
                    public void sendResult(Bundle data) throws RemoteException {
                        Handler -get0 = RecentsTvTransitionHelper.this.mHandler;
                        final OnAnimationStartedListener onAnimationStartedListener = onAnimationStartedListener;
                        -get0.post(new Runnable() {
                            public void run() {
                                if (onAnimationStartedListener != null) {
                                    onAnimationStartedListener.onAnimationStarted();
                                }
                            }
                        });
                    }
                };
            }
            try {
                WindowManagerGlobal.getWindowManagerService().overridePendingAppTransitionAspectScaledThumb(Bitmap.createScaledBitmap(task.thumbnail, taskRect.width(), taskRect.height(), false), taskRect.left, taskRect.top, taskRect.width(), taskRect.height(), iRemoteCallback, true);
            } catch (RemoteException e) {
                Log.w("RecentsTvTransitionHelper", "Failed to override transition: " + e);
            }
        }
    }
}
