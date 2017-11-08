package com.android.systemui.recents.views;

import android.app.ActivityManager.StackId;
import android.app.ActivityOptions;
import android.app.ActivityOptions.OnAnimationStartedListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.IRemoteCallback.Stub;
import android.os.RemoteException;
import android.util.Log;
import android.view.AppTransitionAnimationSpec;
import android.view.IAppTransitionAnimationSpecsFuture;
import com.android.internal.annotations.GuardedBy;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.EventBus.Event;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.ExitRecentsWindowFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.LaunchTaskFailedEvent;
import com.android.systemui.recents.events.activity.LaunchTaskStartedEvent;
import com.android.systemui.recents.events.activity.LaunchTaskSucceededEvent;
import com.android.systemui.recents.events.component.ScreenPinningRequestEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecentsTransitionHelper {
    private static final List<AppTransitionAnimationSpec> SPECS_WAITING = new ArrayList();
    @GuardedBy("this")
    private List<AppTransitionAnimationSpec> mAppTransitionAnimationSpecs = SPECS_WAITING;
    private Context mContext;
    private Handler mHandler;
    private StartScreenPinningRunnableRunnable mStartScreenPinningRunnable = new StartScreenPinningRunnableRunnable();
    private TaskViewTransform mTmpTransform = new TaskViewTransform();

    public interface AnimationSpecComposer {
        List<AppTransitionAnimationSpec> composeSpecs();
    }

    private class StartScreenPinningRunnableRunnable implements Runnable {
        private int taskId;

        private StartScreenPinningRunnableRunnable() {
            this.taskId = -1;
        }

        public void run() {
            EventBus.getDefault().send(new ScreenPinningRequestEvent(RecentsTransitionHelper.this.mContext, this.taskId));
        }
    }

    public RecentsTransitionHelper(Context context) {
        this.mContext = context;
        this.mHandler = new Handler();
    }

    public void launchTaskFromRecents(TaskStack stack, Task task, TaskStackView stackView, TaskView taskView, boolean screenPinningRequested, Rect bounds, int destinationStack) {
        IAppTransitionAnimationSpecsFuture transitionFuture;
        OnAnimationStartedListener animStartedListener;
        ActivityOptions opts = ActivityOptions.makeBasic();
        if (bounds != null) {
            if (bounds.isEmpty()) {
                bounds = null;
            }
            opts.setLaunchBounds(bounds);
        }
        final Task task2;
        final TaskStackView taskStackView;
        if (taskView != null) {
            task2 = task;
            taskStackView = stackView;
            final int i = destinationStack;
            transitionFuture = getAppTransitionFuture(new AnimationSpecComposer() {
                public List<AppTransitionAnimationSpec> composeSpecs() {
                    return RecentsTransitionHelper.this.composeAnimationSpecs(task2, taskStackView, i);
                }
            });
            task2 = task;
            taskStackView = stackView;
            final boolean z = screenPinningRequested;
            animStartedListener = new OnAnimationStartedListener() {
                public void onAnimationStarted() {
                    EventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(task2));
                    EventBus.getDefault().send(new ExitRecentsWindowFirstAnimationFrameEvent());
                    taskStackView.cancelAllTaskViewAnimations();
                    if (z) {
                        RecentsTransitionHelper.this.mStartScreenPinningRunnable.taskId = task2.key.id;
                        RecentsTransitionHelper.this.mHandler.postDelayed(RecentsTransitionHelper.this.mStartScreenPinningRunnable, 350);
                    }
                }
            };
        } else {
            transitionFuture = null;
            task2 = task;
            taskStackView = stackView;
            animStartedListener = new OnAnimationStartedListener() {
                public void onAnimationStarted() {
                    EventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(task2));
                    EventBus.getDefault().send(new ExitRecentsWindowFirstAnimationFrameEvent());
                    taskStackView.cancelAllTaskViewAnimations();
                }
            };
        }
        if (taskView == null) {
            startTaskActivity(stack, task, taskView, opts, transitionFuture, animStartedListener);
        } else {
            Event launchTaskStartedEvent = new LaunchTaskStartedEvent(taskView, screenPinningRequested);
            if (task.group == null || task.group.isFrontMostTask(task)) {
                EventBus.getDefault().send(launchTaskStartedEvent);
                startTaskActivity(stack, task, taskView, opts, transitionFuture, animStartedListener);
            } else {
                final TaskStack taskStack = stack;
                final Task task3 = task;
                final TaskView taskView2 = taskView;
                final ActivityOptions activityOptions = opts;
                final IAppTransitionAnimationSpecsFuture iAppTransitionAnimationSpecsFuture = transitionFuture;
                final OnAnimationStartedListener onAnimationStartedListener = animStartedListener;
                launchTaskStartedEvent.addPostAnimationCallback(new Runnable() {
                    public void run() {
                        RecentsTransitionHelper.this.startTaskActivity(taskStack, task3, taskView2, activityOptions, iAppTransitionAnimationSpecsFuture, onAnimationStartedListener);
                    }
                });
                EventBus.getDefault().send(launchTaskStartedEvent);
            }
        }
        Recents.getSystemServices().sendCloseSystemWindows("homekey");
    }

    public IRemoteCallback wrapStartedListener(final OnAnimationStartedListener listener) {
        if (listener == null) {
            return null;
        }
        return new Stub() {
            public void sendResult(Bundle data) throws RemoteException {
                Handler -get3 = RecentsTransitionHelper.this.mHandler;
                final OnAnimationStartedListener onAnimationStartedListener = listener;
                -get3.post(new Runnable() {
                    public void run() {
                        onAnimationStartedListener.onAnimationStarted();
                    }
                });
            }
        };
    }

    private void startTaskActivity(TaskStack stack, Task task, TaskView taskView, ActivityOptions opts, IAppTransitionAnimationSpecsFuture transitionFuture, OnAnimationStartedListener animStartedListener) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp.startActivityFromRecents(this.mContext, task.key, task.title, opts)) {
            int taskIndexFromFront = 0;
            int taskIndex = stack.indexOfStackTask(task);
            if (taskIndex > -1) {
                taskIndexFromFront = (stack.getTaskCount() - taskIndex) - 1;
            }
            EventBus.getDefault().send(new LaunchTaskSucceededEvent(taskIndexFromFront));
        } else {
            if (taskView != null) {
                taskView.dismissTask();
            }
            EventBus.getDefault().send(new LaunchTaskFailedEvent());
        }
        if (transitionFuture != null) {
            ssp.overridePendingAppTransitionMultiThumbFuture(transitionFuture, wrapStartedListener(animStartedListener), true);
        }
    }

    public IAppTransitionAnimationSpecsFuture getAppTransitionFuture(final AnimationSpecComposer composer) {
        synchronized (this) {
            this.mAppTransitionAnimationSpecs = SPECS_WAITING;
        }
        return new IAppTransitionAnimationSpecsFuture.Stub() {
            public AppTransitionAnimationSpec[] get() throws RemoteException {
                Handler -get3 = RecentsTransitionHelper.this.mHandler;
                final AnimationSpecComposer animationSpecComposer = composer;
                -get3.post(new Runnable() {
                    public void run() {
                        synchronized (RecentsTransitionHelper.this) {
                            RecentsTransitionHelper.this.mAppTransitionAnimationSpecs = animationSpecComposer.composeSpecs();
                            RecentsTransitionHelper.this.notifyAll();
                        }
                    }
                });
                synchronized (RecentsTransitionHelper.this) {
                    while (RecentsTransitionHelper.this.mAppTransitionAnimationSpecs == RecentsTransitionHelper.SPECS_WAITING) {
                        try {
                            RecentsTransitionHelper.this.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    if (RecentsTransitionHelper.this.mAppTransitionAnimationSpecs == null) {
                        return null;
                    }
                    AppTransitionAnimationSpec[] specs = new AppTransitionAnimationSpec[RecentsTransitionHelper.this.mAppTransitionAnimationSpecs.size()];
                    RecentsTransitionHelper.this.mAppTransitionAnimationSpecs.toArray(specs);
                    RecentsTransitionHelper.this.mAppTransitionAnimationSpecs = RecentsTransitionHelper.SPECS_WAITING;
                    return specs;
                }
            }
        };
    }

    public List<AppTransitionAnimationSpec> composeDockAnimationSpec(TaskView taskView, Rect bounds) {
        this.mTmpTransform.fillIn(taskView);
        Task task = taskView.getTask();
        return Collections.singletonList(new AppTransitionAnimationSpec(task.key.id, composeTaskBitmap(taskView, this.mTmpTransform), bounds));
    }

    private List<AppTransitionAnimationSpec> composeAnimationSpecs(Task task, TaskStackView stackView, int destinationStack) {
        int targetStackId;
        if (destinationStack != -1) {
            targetStackId = destinationStack;
        } else {
            targetStackId = task.key.stackId;
        }
        if (!StackId.useAnimationSpecForAppTransition(targetStackId)) {
            return null;
        }
        TaskView taskView = stackView.getChildViewForTask(task);
        TaskStackLayoutAlgorithm stackLayout = stackView.getStackAlgorithm();
        Rect offscreenTaskRect = new Rect();
        stackLayout.getFrontOfStackTransform().rect.round(offscreenTaskRect);
        List<AppTransitionAnimationSpec> specs = new ArrayList();
        AppTransitionAnimationSpec spec;
        if (targetStackId == 1 || targetStackId == 3 || targetStackId == -1) {
            if (taskView == null) {
                specs.add(composeOffscreenAnimationSpec(task, offscreenTaskRect));
            } else {
                this.mTmpTransform.fillIn(taskView);
                stackLayout.transformToScreenCoordinates(this.mTmpTransform, null);
                spec = composeAnimationSpec(stackView, taskView, this.mTmpTransform, true);
                if (spec != null) {
                    specs.add(spec);
                }
            }
            return specs;
        }
        ArrayList<Task> tasks = stackView.getStack().getStackTasks();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task t = (Task) tasks.get(i);
            if (t.isFreeformTask() || targetStackId == 2) {
                TaskView tv = stackView.getChildViewForTask(t);
                if (tv == null) {
                    specs.add(composeOffscreenAnimationSpec(t, offscreenTaskRect));
                } else {
                    this.mTmpTransform.fillIn(taskView);
                    stackLayout.transformToScreenCoordinates(this.mTmpTransform, null);
                    spec = composeAnimationSpec(stackView, tv, this.mTmpTransform, true);
                    if (spec != null) {
                        specs.add(spec);
                    }
                }
            }
        }
        return specs;
    }

    private static AppTransitionAnimationSpec composeOffscreenAnimationSpec(Task task, Rect taskRect) {
        return new AppTransitionAnimationSpec(task.key.id, null, taskRect);
    }

    public static Bitmap composeTaskBitmap(TaskView taskView, TaskViewTransform transform) {
        float scale = transform.scale;
        int fromWidth = (int) (transform.rect.width() * scale);
        int fromHeight = (int) (transform.rect.height() * scale);
        if (fromWidth == 0 || fromHeight == 0) {
            Log.e("RecentsTransitionHelper", "Could not compose thumbnail for task: " + taskView.getTask() + " at transform: " + transform);
            Bitmap b = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
            b.eraseColor(0);
            return b;
        }
        b = Bitmap.createBitmap(fromWidth, fromHeight, Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.scale(scale, scale);
        taskView.draw(c);
        c.setBitmap(null);
        return b.createAshmemBitmap();
    }

    private static Bitmap composeHeaderBitmap(TaskView taskView, TaskViewTransform transform) {
        float scale = transform.scale;
        int headerWidth = (int) transform.rect.width();
        int headerHeight = (int) (((float) taskView.mHeaderView.getMeasuredHeight()) * scale);
        if (headerWidth == 0 || headerHeight == 0) {
            return null;
        }
        Bitmap b = Bitmap.createBitmap(headerWidth, headerHeight, Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.scale(scale, scale);
        taskView.mHeaderView.draw(c);
        c.setBitmap(null);
        return b.createAshmemBitmap();
    }

    private static AppTransitionAnimationSpec composeAnimationSpec(TaskStackView stackView, TaskView taskView, TaskViewTransform transform, boolean addHeaderBitmap) {
        Bitmap bitmap = null;
        if (addHeaderBitmap) {
            bitmap = composeHeaderBitmap(taskView, transform);
            if (bitmap == null) {
                return null;
            }
        }
        Rect taskRect = new Rect();
        transform.rect.round(taskRect);
        if (stackView.getStack().getStackFrontMostTask(false) != taskView.getTask()) {
            taskRect.bottom = taskRect.top + stackView.getMeasuredHeight();
        }
        return new AppTransitionAnimationSpec(taskView.getTask().key.id, bitmap, taskRect);
    }
}
