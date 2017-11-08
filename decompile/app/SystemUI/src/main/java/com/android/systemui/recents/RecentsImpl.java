package com.android.systemui.recents;

import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityOptions;
import android.app.ActivityOptions.OnAnimationFinishedListener;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.util.MutableBoolean;
import android.view.AppTransitionAnimationSpec;
import android.view.LayoutInflater;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.policy.DockedDividerUtils;
import com.android.systemui.R;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowLastAnimationFrameEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.IterateRecentsEvent;
import com.android.systemui.recents.events.activity.LaunchNextTaskRequestEvent;
import com.android.systemui.recents.events.activity.RecentsActivityStartingEvent;
import com.android.systemui.recents.events.activity.ToggleRecentsEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEndedEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEvent;
import com.android.systemui.recents.misc.DozeTrigger;
import com.android.systemui.recents.misc.ForegroundThread;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.SystemServicesProxy.TaskStackListener;
import com.android.systemui.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.recents.model.RecentsTaskLoadPlan.Options;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.Task.TaskKey;
import com.android.systemui.recents.model.TaskGrouping;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.TaskStackLayoutAlgorithm;
import com.android.systemui.recents.views.TaskStackLayoutAlgorithm.StackState;
import com.android.systemui.recents.views.TaskStackLayoutAlgorithm.VisibilityReport;
import com.android.systemui.recents.views.TaskStackView;
import com.android.systemui.recents.views.TaskStackViewScroller;
import com.android.systemui.recents.views.TaskViewHeader;
import com.android.systemui.recents.views.TaskViewTransform;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.analyze.BDReporter;
import com.android.systemui.utils.analyze.MonitorReporter;
import com.android.systemui.utils.analyze.PerfDebugUtils;
import java.util.ArrayList;

public class RecentsImpl implements OnAnimationFinishedListener {
    protected static RecentsTaskLoadPlan sInstanceLoadPlan;
    protected Context mContext;
    boolean mDraggingInRecents;
    protected TaskStackView mDummyStackView;
    DozeTrigger mFastAltTabTrigger = new DozeTrigger(225, new Runnable() {
        public void run() {
            RecentsImpl.this.showRecents(RecentsImpl.this.mTriggeredFromAltTab, false, true, false, false, -1);
        }
    });
    protected Handler mHandler;
    TaskViewHeader mHeaderBar;
    final Object mHeaderBarLock = new Object();
    protected long mLastToggleTime;
    boolean mLaunchedWhileDocking;
    int mNavBarHeight;
    int mNavBarWidth;
    int mStatusBarHeight;
    int mTaskBarHeight;
    Rect mTaskStackBounds = new Rect();
    TaskStackListenerImpl mTaskStackListener;
    protected Bitmap mThumbTransitionBitmapCache;
    TaskViewTransform mTmpTransform = new TaskViewTransform();
    protected boolean mTriggeredFromAltTab;

    class TaskStackListenerImpl extends TaskStackListener {
        TaskStackListenerImpl() {
        }

        public void onTaskStackChanged() {
            if (Recents.getConfiguration().svelteLevel == 0) {
                final RecentsTaskLoader loader = Recents.getTaskLoader();
                final SystemServicesProxy ssp = Recents.getSystemServices();
                SystemUIThread.runAsync(new SimpleAsyncTask() {
                    RunningTaskInfo runningTaskInfo;

                    public boolean runInThread() {
                        this.runningTaskInfo = ssp.getRunningTask();
                        return true;
                    }

                    public void runInUI() {
                        RecentsTaskLoadPlan plan = loader.createLoadPlan(RecentsImpl.this.mContext);
                        loader.preloadTasks(plan, -1, false);
                        Options launchOpts = new Options();
                        if (this.runningTaskInfo != null) {
                            launchOpts.runningTaskId = this.runningTaskInfo.id;
                        }
                        launchOpts.numVisibleTasks = 2;
                        launchOpts.numVisibleTaskThumbnails = 2;
                        launchOpts.onlyLoadForCache = true;
                        launchOpts.onlyLoadPausedActivities = true;
                        loader.loadTasks(RecentsImpl.this.mContext, plan, launchOpts);
                    }
                });
            }
        }
    }

    public RecentsImpl(Context context) {
        this.mContext = context;
        this.mHandler = new Handler();
        ForegroundThread.get();
        this.mTaskStackListener = new TaskStackListenerImpl();
        Recents.getSystemServices().registerTaskStackListener(this.mTaskStackListener);
        LayoutInflater inflater = LayoutInflater.from(this.mContext);
        this.mDummyStackView = new TaskStackView(this.mContext);
        this.mHeaderBar = (TaskViewHeader) inflater.inflate(R.layout.recents_task_view_header, null, false);
        reloadResources();
    }

    public void onBootCompleted() {
        RecentsTaskLoader loader = Recents.getTaskLoader();
        RecentsTaskLoadPlan plan = loader.createLoadPlan(this.mContext);
        loader.preloadTasks(plan, -1, false);
        Options launchOpts = new Options();
        launchOpts.numVisibleTasks = loader.getIconCacheSize();
        launchOpts.numVisibleTaskThumbnails = loader.getThumbnailCacheSize();
        launchOpts.onlyLoadForCache = true;
        loader.loadTasks(this.mContext, plan, launchOpts);
    }

    public void onConfigurationChanged() {
        reloadResources();
        this.mDummyStackView.reloadOnConfigurationChange();
        this.mHeaderBar.onConfigurationChanged();
    }

    public void onVisibilityChanged(Context context, boolean visible) {
        PhoneStatusBar statusBar = (PhoneStatusBar) ((SystemUIApplication) context).getComponent(PhoneStatusBar.class);
        if (statusBar != null) {
            statusBar.updateRecentsVisibility(visible);
        }
    }

    public void onStartScreenPinning(Context context, int taskId) {
        PhoneStatusBar statusBar = (PhoneStatusBar) ((SystemUIApplication) context).getComponent(PhoneStatusBar.class);
        if (statusBar != null) {
            statusBar.showScreenPinningRequest(taskId, false);
        }
    }

    public void showRecents(boolean triggeredFromAltTab, boolean draggingInRecents, boolean animate, boolean launchedWhileDockingTask, boolean fromHome, int growTarget) {
        this.mTriggeredFromAltTab = triggeredFromAltTab;
        this.mDraggingInRecents = draggingInRecents;
        this.mLaunchedWhileDocking = launchedWhileDockingTask;
        if (this.mFastAltTabTrigger.isAsleep()) {
            this.mFastAltTabTrigger.stopDozing();
        } else if (this.mFastAltTabTrigger.isDozing()) {
            if (triggeredFromAltTab) {
                this.mFastAltTabTrigger.stopDozing();
            } else {
                return;
            }
        } else if (triggeredFromAltTab) {
            this.mFastAltTabTrigger.startDozing();
            return;
        }
        try {
            SystemServicesProxy ssp = Recents.getSystemServices();
            boolean z = !launchedWhileDockingTask ? draggingInRecents : true;
            MutableBoolean isHomeStackVisible = new MutableBoolean(z);
            if (z || !ssp.isRecentsActivityVisible(isHomeStackVisible)) {
                RunningTaskInfo runningTask = ssp.getRunningTask();
                if (isHomeStackVisible.value) {
                    fromHome = true;
                }
                startRecentsActivity(runningTask, fromHome, animate, growTarget);
            }
        } catch (ActivityNotFoundException e) {
            Log.e("RecentsImpl", "Failed to launch RecentsActivity", e);
        }
    }

    public void hideRecents(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) {
        if (triggeredFromAltTab && this.mFastAltTabTrigger.isDozing()) {
            showNextTask();
            this.mFastAltTabTrigger.stopDozing();
            return;
        }
        EventBus.getDefault().post(new HideRecentsEvent(triggeredFromAltTab, triggeredFromHomeKey));
    }

    public void toggleRecents(int growTarget) {
        if (!this.mFastAltTabTrigger.isDozing()) {
            this.mDraggingInRecents = false;
            this.mLaunchedWhileDocking = false;
            this.mTriggeredFromAltTab = false;
            SystemServicesProxy ssp = Recents.getSystemServices();
            MutableBoolean isHomeStackVisible = new MutableBoolean(true);
            RunningTaskInfo runningTaskInfo = null;
            try {
                long elapsedTime = SystemClock.elapsedRealtime() - this.mLastToggleTime;
                if (ssp.isRecentsActivityVisible(isHomeStackVisible)) {
                    RecentsDebugFlags debugFlags = Recents.getDebugFlags();
                    if (Recents.getConfiguration().getLaunchState().launchedWithAltTab) {
                        if (elapsedTime >= 350) {
                            EventBus.getDefault().post(new ToggleRecentsEvent());
                            this.mLastToggleTime = SystemClock.elapsedRealtime();
                        }
                    } else if (!debugFlags.isPagingEnabled() || (((long) ViewConfiguration.getDoubleTapMinTime()) < elapsedTime && elapsedTime < ((long) ViewConfiguration.getDoubleTapTimeout()))) {
                        BDReporter.c(this.mContext, 352);
                        EventBus.getDefault().post(new LaunchNextTaskRequestEvent());
                    } else {
                        EventBus.getDefault().post(new IterateRecentsEvent());
                    }
                } else if (elapsedTime >= 350) {
                    runningTaskInfo = ssp.getRunningTask();
                    startRecentsActivity(runningTaskInfo, isHomeStackVisible.value, true, growTarget);
                    ssp.sendCloseSystemWindows("recentapps");
                    this.mLastToggleTime = SystemClock.elapsedRealtime();
                }
            } catch (ActivityNotFoundException e) {
                Log.e("RecentsImpl", "Failed to launch RecentsActivity", e);
                reportRecentTasksLoadException(new StringBuilder("Failed to start activity with ActivityNotFoundException: "), isHomeStackVisible.value, runningTaskInfo);
            } catch (RuntimeException e2) {
                reportRecentTasksLoadException(new StringBuilder("Failed to start activity with RuntimeException: "), isHomeStackVisible.value, runningTaskInfo);
            }
        }
    }

    private void reportRecentTasksLoadException(StringBuilder sbTag, boolean isTaskHome, RunningTaskInfo runningTask) {
        sbTag.append("isTopTaskHome=").append(isTaskHome);
        sbTag.append(", running task=").append(runningTask != null ? runningTask.toString() : "null");
        MonitorReporter.doMonitor(MonitorReporter.createInfoIntent(907033002, MonitorReporter.createMapInfo((short) 0, sbTag.toString())));
    }

    public void preloadRecents() {
        final SystemServicesProxy ssp = Recents.getSystemServices();
        final MutableBoolean isHomeStackVisible = new MutableBoolean(true);
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            RunningTaskInfo runningTask = null;

            public boolean runInThread() {
                HwLog.i("RecentsImpl", "preloadRecents runInThread start ");
                boolean flag = !ssp.isRecentsActivityVisible(isHomeStackVisible);
                if (flag) {
                    this.runningTask = ssp.getRunningTask();
                }
                HwLog.i("RecentsImpl", "preloadRecents runInThread flag=" + flag + ",runningTask=" + this.runningTask);
                return flag;
            }

            public void runInUI() {
                boolean z = false;
                if (this.runningTask == null) {
                    HwLog.e("RecentsImpl", "preloadRecents runningTask = null");
                    return;
                }
                boolean z2;
                HwLog.i("RecentsImpl", "preloadRecents runInUI isHomeStackVisible=" + isHomeStackVisible.value);
                RecentsTaskLoader loader = Recents.getTaskLoader();
                RecentsImpl.sInstanceLoadPlan = loader.createLoadPlan(RecentsImpl.this.mContext);
                RecentsTaskLoadPlan recentsTaskLoadPlan = RecentsImpl.sInstanceLoadPlan;
                if (isHomeStackVisible.value) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                recentsTaskLoadPlan.preloadRawTasks(z2);
                RecentsTaskLoadPlan recentsTaskLoadPlan2 = RecentsImpl.sInstanceLoadPlan;
                int i = this.runningTask.id;
                if (!isHomeStackVisible.value) {
                    z = true;
                }
                loader.preloadTasks(recentsTaskLoadPlan2, i, z);
                TaskStack stack = RecentsImpl.sInstanceLoadPlan.getTaskStack();
                if (stack.getTaskCount() > 0) {
                    RecentsImpl.this.preloadIcon(this.runningTask.id);
                    RecentsImpl.this.updateHeaderBarLayout(stack, null);
                }
            }
        });
    }

    public void cancelPreloadingRecents() {
    }

    public void onDraggingInRecents(float distanceFromTop) {
        EventBus.getDefault().sendOntoMainThread(new DraggingInRecentsEvent(distanceFromTop));
    }

    public void onDraggingInRecentsEnded(float velocity) {
        EventBus.getDefault().sendOntoMainThread(new DraggingInRecentsEndedEvent(velocity));
    }

    public void showNextTask() {
        SystemServicesProxy ssp = Recents.getSystemServices();
        RecentsTaskLoader loader = Recents.getTaskLoader();
        RecentsTaskLoadPlan plan = loader.createLoadPlan(this.mContext);
        loader.preloadTasks(plan, -1, false);
        TaskStack focusedStack = plan.getTaskStack();
        if (focusedStack != null && focusedStack.getTaskCount() != 0) {
            RunningTaskInfo runningTask = ssp.getRunningTask();
            if (runningTask != null) {
                boolean isRunningTaskInHomeStack = SystemServicesProxy.isHomeStack(runningTask.stackId);
                ArrayList<Task> tasks = focusedStack.getStackTasks();
                Task task = null;
                ActivityOptions launchOpts = null;
                int i = tasks.size() - 1;
                while (i >= 1) {
                    Task task2 = (Task) tasks.get(i);
                    if (isRunningTaskInHomeStack) {
                        task = (Task) tasks.get(i - 1);
                        launchOpts = ActivityOptions.makeCustomAnimation(this.mContext, R.anim.recents_launch_next_affiliated_task_target, R.anim.recents_fast_toggle_app_home_exit);
                        break;
                    } else if (task2.key.id == runningTask.id) {
                        task = (Task) tasks.get(i - 1);
                        launchOpts = ActivityOptions.makeCustomAnimation(this.mContext, R.anim.recents_launch_prev_affiliated_task_target, R.anim.recents_launch_prev_affiliated_task_source);
                        break;
                    } else {
                        i--;
                    }
                }
                if (task == null) {
                    ssp.startInPlaceAnimationOnFrontMostApplication(ActivityOptions.makeCustomInPlaceAnimation(this.mContext, R.anim.recents_launch_prev_affiliated_task_bounce));
                } else {
                    ssp.startActivityFromRecents(this.mContext, task.key, task.title, launchOpts);
                }
            }
        }
    }

    public void showRelativeAffiliatedTask(boolean showNextTask) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        RecentsTaskLoader loader = Recents.getTaskLoader();
        RecentsTaskLoadPlan plan = loader.createLoadPlan(this.mContext);
        loader.preloadTasks(plan, -1, false);
        TaskStack focusedStack = plan.getTaskStack();
        if (focusedStack != null && focusedStack.getTaskCount() != 0) {
            RunningTaskInfo runningTask = ssp.getRunningTask();
            if (runningTask != null && !SystemServicesProxy.isHomeStack(runningTask.stackId)) {
                ArrayList<Task> tasks = focusedStack.getStackTasks();
                Task task = null;
                ActivityOptions activityOptions = null;
                int taskCount = tasks.size();
                int numAffiliatedTasks = 0;
                for (int i = 0; i < taskCount; i++) {
                    Task task2 = (Task) tasks.get(i);
                    if (task2.key.id == runningTask.id) {
                        TaskKey toTaskKey;
                        TaskGrouping group = task2.group;
                        if (showNextTask) {
                            toTaskKey = group.getNextTaskInGroup(task2);
                            activityOptions = ActivityOptions.makeCustomAnimation(this.mContext, R.anim.recents_launch_next_affiliated_task_target, R.anim.recents_launch_next_affiliated_task_source);
                        } else {
                            toTaskKey = group.getPrevTaskInGroup(task2);
                            activityOptions = ActivityOptions.makeCustomAnimation(this.mContext, R.anim.recents_launch_prev_affiliated_task_target, R.anim.recents_launch_prev_affiliated_task_source);
                        }
                        if (toTaskKey != null) {
                            task = focusedStack.findTaskWithId(toTaskKey.id);
                        }
                        numAffiliatedTasks = group.getTaskCount();
                        if (task != null) {
                            if (numAffiliatedTasks > 1) {
                                if (showNextTask) {
                                    ssp.startInPlaceAnimationOnFrontMostApplication(ActivityOptions.makeCustomInPlaceAnimation(this.mContext, R.anim.recents_launch_prev_affiliated_task_bounce));
                                } else {
                                    ssp.startInPlaceAnimationOnFrontMostApplication(ActivityOptions.makeCustomInPlaceAnimation(this.mContext, R.anim.recents_launch_next_affiliated_task_bounce));
                                }
                            }
                        }
                        MetricsLogger.count(this.mContext, "overview_affiliated_task_launch", 1);
                        ssp.startActivityFromRecents(this.mContext, task.key, task.title, activityOptions);
                        return;
                    }
                }
                if (task != null) {
                    MetricsLogger.count(this.mContext, "overview_affiliated_task_launch", 1);
                    ssp.startActivityFromRecents(this.mContext, task.key, task.title, activityOptions);
                    return;
                }
                if (numAffiliatedTasks > 1) {
                    if (showNextTask) {
                        ssp.startInPlaceAnimationOnFrontMostApplication(ActivityOptions.makeCustomInPlaceAnimation(this.mContext, R.anim.recents_launch_prev_affiliated_task_bounce));
                    } else {
                        ssp.startInPlaceAnimationOnFrontMostApplication(ActivityOptions.makeCustomInPlaceAnimation(this.mContext, R.anim.recents_launch_next_affiliated_task_bounce));
                    }
                }
            }
        }
    }

    public void showNextAffiliatedTask() {
        MetricsLogger.count(this.mContext, "overview_affiliated_task_next", 1);
        showRelativeAffiliatedTask(true);
    }

    public void showPrevAffiliatedTask() {
        MetricsLogger.count(this.mContext, "overview_affiliated_task_prev", 1);
        showRelativeAffiliatedTask(false);
    }

    public void dockTopTask(int topTaskId, int dragMode, int stackCreateMode, Rect initialBounds) {
        if (Recents.getSystemServices().moveTaskToDockedStack(topTaskId, stackCreateMode, initialBounds)) {
            boolean z;
            EventBus.getDefault().send(new DockedTopTaskEvent(dragMode, initialBounds));
            if (dragMode == 0) {
                z = true;
            } else {
                z = false;
            }
            showRecents(false, z, false, true, false, -1);
        }
    }

    public static RecentsTaskLoadPlan consumeInstanceLoadPlan() {
        RecentsTaskLoadPlan plan = sInstanceLoadPlan;
        sInstanceLoadPlan = null;
        return plan;
    }

    private void reloadResources() {
        Resources res = this.mContext.getResources();
        this.mStatusBarHeight = res.getDimensionPixelSize(17104919);
        this.mNavBarHeight = res.getDimensionPixelSize(17104920);
        this.mNavBarWidth = res.getDimensionPixelSize(17104922);
        this.mTaskBarHeight = TaskStackLayoutAlgorithm.getDimensionForDevice(this.mContext, R.dimen.recents_task_view_header_height, R.dimen.recents_task_view_header_height, R.dimen.recents_task_view_header_height, R.dimen.recents_task_view_header_height_tablet_land, R.dimen.recents_task_view_header_height, R.dimen.recents_task_view_header_height_tablet_land);
    }

    private void updateHeaderBarLayout(TaskStack stack, Rect windowRectOverride) {
        Rect windowRect;
        SystemServicesProxy ssp = Recents.getSystemServices();
        Rect displayRect = ssp.getDisplayRect();
        Rect systemInsets = new Rect();
        ssp.getStableInsets(systemInsets);
        if (windowRectOverride != null) {
            windowRect = new Rect(windowRectOverride);
        } else {
            windowRect = ssp.getWindowRect();
        }
        if (ssp.hasDockedTask()) {
            windowRect.bottom -= systemInsets.bottom;
            systemInsets.bottom = 0;
        }
        calculateWindowStableInsets(systemInsets, windowRect);
        windowRect.offsetTo(0, 0);
        TaskStackLayoutAlgorithm stackLayout = this.mDummyStackView.getStackAlgorithm();
        stackLayout.setSystemInsets(systemInsets);
        if (stack != null) {
            stackLayout.getTaskStackBounds(displayRect, windowRect, systemInsets.top, systemInsets.right, this.mTaskStackBounds);
            stackLayout.reset();
            stackLayout.initialize(displayRect, windowRect, this.mTaskStackBounds, StackState.getStackStateForStack(stack));
            this.mDummyStackView.setTasks(stack, false);
            Rect taskViewBounds = stackLayout.getUntransformedTaskViewBounds();
            if (!taskViewBounds.isEmpty()) {
                int taskViewWidth = taskViewBounds.width();
                synchronized (this.mHeaderBarLock) {
                    if (!(this.mHeaderBar.getMeasuredWidth() == taskViewWidth && this.mHeaderBar.getMeasuredHeight() == this.mTaskBarHeight)) {
                        this.mHeaderBar.measure(MeasureSpec.makeMeasureSpec(taskViewWidth, 1073741824), MeasureSpec.makeMeasureSpec(this.mTaskBarHeight, 1073741824));
                    }
                    this.mHeaderBar.layout(0, 0, taskViewWidth, this.mTaskBarHeight);
                }
                if (this.mThumbTransitionBitmapCache != null && this.mThumbTransitionBitmapCache.getWidth() == taskViewWidth) {
                    if (this.mThumbTransitionBitmapCache.getHeight() == this.mTaskBarHeight) {
                        return;
                    }
                }
                this.mThumbTransitionBitmapCache = Bitmap.createBitmap(taskViewWidth, this.mTaskBarHeight, Config.ARGB_8888);
            }
        }
    }

    private void calculateWindowStableInsets(Rect inOutInsets, Rect windowRect) {
        Rect appRect = new Rect(Recents.getSystemServices().getDisplayRect());
        appRect.inset(inOutInsets);
        Rect windowRectWithInsets = new Rect(windowRect);
        windowRectWithInsets.intersect(appRect);
        inOutInsets.left = windowRectWithInsets.left - windowRect.left;
        inOutInsets.top = windowRectWithInsets.top - windowRect.top;
        inOutInsets.right = windowRect.right - windowRectWithInsets.right;
        inOutInsets.bottom = windowRect.bottom - windowRectWithInsets.bottom;
    }

    private void preloadIcon(int runningTaskId) {
        Options launchOpts = new Options();
        launchOpts.runningTaskId = runningTaskId;
        launchOpts.loadThumbnails = false;
        launchOpts.onlyLoadForCache = true;
        Recents.getTaskLoader().loadTasks(this.mContext, sInstanceLoadPlan, launchOpts);
    }

    protected ActivityOptions getUnknownTransitionActivityOptions() {
        return ActivityOptions.makeCustomAnimation(this.mContext, R.anim.recents_from_unknown_enter, R.anim.recents_from_unknown_exit, this.mHandler, null);
    }

    private ActivityOptions getThumbnailTransitionActivityOptions(RunningTaskInfo runningTask, TaskStackView stackView, Rect windowOverrideRect) {
        Bitmap thumbnail;
        if (runningTask == null || runningTask.stackId != 2) {
            Task toTask = new Task();
            TaskViewTransform toTransform = getThumbnailTransitionTransform(stackView, toTask, windowOverrideRect);
            thumbnail = drawThumbnailTransitionBitmap(toTask, toTransform, this.mThumbTransitionBitmapCache);
            if (thumbnail == null) {
                return getUnknownTransitionActivityOptions();
            }
            RectF toTaskRect = toTransform.rect;
            return ActivityOptions.makeThumbnailAspectScaleDownAnimation(this.mDummyStackView, thumbnail, (int) toTaskRect.left, (int) toTaskRect.top, (int) toTaskRect.width(), (int) toTaskRect.height(), this.mHandler, null);
        }
        ArrayList<AppTransitionAnimationSpec> specs = new ArrayList();
        ArrayList<Task> tasks = stackView.getStack().getStackTasks();
        TaskStackLayoutAlgorithm stackLayout = stackView.getStackAlgorithm();
        TaskStackViewScroller stackScroller = stackView.getScroller();
        stackView.updateLayoutAlgorithm(true);
        stackView.updateToInitialState();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task task = (Task) tasks.get(i);
            if (task.isFreeformTask()) {
                this.mTmpTransform = stackLayout.getStackTransformScreenCoordinates(task, stackScroller.getStackScroll(), this.mTmpTransform, null, windowOverrideRect);
                thumbnail = drawThumbnailTransitionBitmap(task, this.mTmpTransform, this.mThumbTransitionBitmapCache);
                Rect toTaskRect2 = new Rect();
                this.mTmpTransform.rect.round(toTaskRect2);
                specs.add(new AppTransitionAnimationSpec(task.key.id, thumbnail, toTaskRect2));
            }
        }
        AppTransitionAnimationSpec[] specsArray = new AppTransitionAnimationSpec[specs.size()];
        specs.toArray(specsArray);
        return ActivityOptions.makeThumbnailAspectScaleDownAnimation(this.mDummyStackView, specsArray, this.mHandler, null, this);
    }

    private TaskViewTransform getThumbnailTransitionTransform(TaskStackView stackView, Task runningTaskOut, Rect windowOverrideRect) {
        TaskStack stack = stackView.getStack();
        Task launchTask = stack.getLaunchTarget();
        if (launchTask != null) {
            runningTaskOut.copyFrom(launchTask);
        } else {
            launchTask = stack.getStackFrontMostTask(true);
            runningTaskOut.copyFrom(launchTask);
        }
        stackView.updateLayoutAlgorithm(true);
        stackView.updateToInitialState();
        stackView.getStackAlgorithm().getStackTransformScreenCoordinates(launchTask, stackView.getScroller().getStackScroll(), this.mTmpTransform, null, windowOverrideRect);
        return this.mTmpTransform;
    }

    private Bitmap drawThumbnailTransitionBitmap(Task toTask, TaskViewTransform toTransform, Bitmap thumbnail) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (toTransform == null || toTask.key == null) {
            return null;
        }
        synchronized (this.mHeaderBarLock) {
            boolean isInSafeMode = !toTask.isSystemApp ? ssp.isInSafeMode() : false;
            this.mHeaderBar.onTaskViewSizeChanged((int) toTransform.rect.width(), (int) toTransform.rect.height());
            thumbnail.eraseColor(0);
            Canvas c = new Canvas(thumbnail);
            Drawable icon = this.mHeaderBar.getIconView().getDrawable();
            if (icon != null) {
                icon.setCallback(null);
            }
            this.mHeaderBar.bindToTask(toTask, false, isInSafeMode);
            this.mHeaderBar.onTaskDataLoaded();
            this.mHeaderBar.setDimAlpha(toTransform.dimAlpha);
            this.mHeaderBar.draw(c);
            c.setBitmap(null);
        }
        return thumbnail.createAshmemBitmap();
    }

    protected void startRecentsActivity(RunningTaskInfo runningTask, boolean isHomeStackVisible, boolean animate, int growTarget) {
        int runningTaskId;
        boolean useThumbnailTransition;
        RecentsTaskLoader loader = Recents.getTaskLoader();
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (this.mLaunchedWhileDocking || runningTask == null) {
            runningTaskId = -1;
        } else {
            runningTaskId = runningTask.id;
        }
        if (this.mLaunchedWhileDocking || this.mTriggeredFromAltTab || sInstanceLoadPlan == null) {
            sInstanceLoadPlan = loader.createLoadPlan(this.mContext);
        }
        if (this.mLaunchedWhileDocking || this.mTriggeredFromAltTab || !sInstanceLoadPlan.hasTasks()) {
            loader.preloadTasks(sInstanceLoadPlan, runningTaskId, !isHomeStackVisible);
        }
        TaskStack stack = sInstanceLoadPlan.getTaskStack();
        boolean hasRecentTasks = stack.getTaskCount() > 0;
        if (runningTask == null || isHomeStackVisible) {
            useThumbnailTransition = false;
        } else {
            useThumbnailTransition = hasRecentTasks;
        }
        boolean z = (useThumbnailTransition || this.mLaunchedWhileDocking) ? false : true;
        launchState.launchedFromHome = z;
        launchState.launchedFromApp = !useThumbnailTransition ? this.mLaunchedWhileDocking : true;
        launchState.launchedViaDockGesture = this.mLaunchedWhileDocking;
        launchState.launchedViaDragGesture = this.mDraggingInRecents;
        launchState.launchedToTaskId = runningTaskId;
        launchState.launchedWithAltTab = this.mTriggeredFromAltTab;
        preloadIcon(runningTaskId);
        Rect windowOverrideRect = getWindowRectOverride(growTarget);
        updateHeaderBarLayout(stack, windowOverrideRect);
        VisibilityReport stackVr = this.mDummyStackView.computeStackVisibilityReport();
        launchState.launchedNumVisibleTasks = stackVr.numVisibleTasks;
        launchState.launchedNumVisibleThumbnails = stackVr.numVisibleThumbnails;
        if (animate) {
            ActivityOptions opts;
            if (useThumbnailTransition) {
                opts = getThumbnailTransitionActivityOptions(runningTask, this.mDummyStackView, windowOverrideRect);
            } else {
                opts = ActivityOptions.makeCustomAnimation(this.mContext, -1, -1);
                PerfDebugUtils.perfRecentsLaunchElapsedTimeEnd(1);
                PerfDebugUtils.perfRecentsLaunchElapsedTimeBegin(2);
            }
            startRecentsActivity(opts);
            this.mLastToggleTime = SystemClock.elapsedRealtime();
            return;
        }
        startRecentsActivity(ActivityOptions.makeCustomAnimation(this.mContext, -1, -1));
    }

    private Rect getWindowRectOverride(int growTarget) {
        if (growTarget == -1) {
            return null;
        }
        Rect result = new Rect();
        Rect displayRect = Recents.getSystemServices().getDisplayRect();
        DockedDividerUtils.calculateBoundsForPosition(growTarget, 4, result, displayRect.width(), displayRect.height(), Recents.getSystemServices().getDockedDividerSize(this.mContext));
        return result;
    }

    private void startRecentsActivity(ActivityOptions opts) {
        PerfDebugUtils.beginSystraceSection("RecentsImpl.startRecentsActivity");
        long startTime = System.currentTimeMillis();
        Intent intent = new Intent();
        intent.setClassName("com.android.systemui", "com.android.systemui.recents.RecentsActivity");
        intent.setFlags(276840448);
        if (opts != null) {
            this.mContext.startActivityAsUser(intent, opts.toBundle(), UserHandle.CURRENT);
        } else {
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        }
        PerfDebugUtils.keyOperationTimeConsumed("CALL_START_RECENTS_ACTIVITY", startTime);
        PerfDebugUtils.endSystraceSection();
        EventBus.getDefault().send(new RecentsActivityStartingEvent());
    }

    public void onAnimationFinished() {
        EventBus.getDefault().post(new EnterRecentsWindowLastAnimationFrameEvent());
    }
}
