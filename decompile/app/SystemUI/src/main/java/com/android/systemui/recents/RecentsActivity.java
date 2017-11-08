package com.android.systemui.recents;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager.LayoutParams;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DebugFlagsChangedEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.DockedFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowLastAnimationFrameEvent;
import com.android.systemui.recents.events.activity.ExitRecentsWindowFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.IterateRecentsEvent;
import com.android.systemui.recents.events.activity.LaunchTaskFailedEvent;
import com.android.systemui.recents.events.activity.LaunchTaskSucceededEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.ToggleRecentsEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.component.ScreenPinningRequestEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.DeleteTaskDataEvent;
import com.android.systemui.recents.events.ui.HideIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.RecentsDrawnEvent;
import com.android.systemui.recents.events.ui.ShowApplicationInfoEvent;
import com.android.systemui.recents.events.ui.ShowIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.StackViewScrolledEvent;
import com.android.systemui.recents.events.ui.UpdateFreeformTaskViewVisibilityEvent;
import com.android.systemui.recents.events.ui.UserInteractionEvent;
import com.android.systemui.recents.events.ui.focus.DismissFocusedTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusNextTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusPreviousTaskViewEvent;
import com.android.systemui.recents.misc.DozeTrigger;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.RecentsPackageMonitor;
import com.android.systemui.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.recents.model.RecentsTaskLoadPlan.Options;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.HwRecentsView;
import com.android.systemui.recents.views.SystemBarScrimViews;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.analyze.JanklogUtils;
import com.android.systemui.utils.analyze.PerfDebugUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class RecentsActivity extends Activity implements OnPreDrawListener {
    private boolean mFinishedOnStartup;
    private int mFocusTimerDuration;
    private Handler mHandler = new Handler();
    private Intent mHomeIntent;
    private boolean mIgnoreAltTabRelease;
    private View mIncompatibleAppOverlay;
    private boolean mIsVisible;
    private DozeTrigger mIterateTrigger;
    private int mLastDeviceOrientation = 0;
    private int mLastDisplayDensity;
    private long mLastTabKeyEventTime;
    private RecentsPackageMonitor mPackageMonitor;
    private boolean mReceivedNewIntent;
    private final OnPreDrawListener mRecentsDrawnEventListener = new OnPreDrawListener() {
        public boolean onPreDraw() {
            RecentsActivity.this.mRecentsView.getViewTreeObserver().removeOnPreDrawListener(this);
            EventBus.getDefault().post(new RecentsDrawnEvent());
            return true;
        }
    };
    private HwRecentsView mRecentsView;
    private SystemBarScrimViews mScrimViews;
    private final Runnable mSendEnterWindowAnimationCompleteRunnable = new -void__init___LambdaImpl0();
    final BroadcastReceiver mSystemBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_OFF")) {
                RecentsActivity.this.dismissRecentsToHomeIfVisible(false);
            } else if (action.equals("android.intent.action.TIME_SET")) {
                Prefs.putLong(RecentsActivity.this, "OverviewLastStackTaskActiveTime", 0);
            }
        }
    };
    private final UserInteractionEvent mUserInteractionEvent = new UserInteractionEvent();
    Thread refreshMemory = new Thread() {
        public void run() {
            RecentsActivity.this.writeMemorySize();
        }
    };

    final /* synthetic */ class -void__init___LambdaImpl0 implements Runnable {
        public void run() {
            RecentsActivity.-com_android_systemui_recents_RecentsActivity_lambda$2();
        }
    }

    class LaunchHomeRunnable implements Runnable {
        Intent mLaunchIntent;
        ActivityOptions mOpts;

        final /* synthetic */ class -void_run__LambdaImpl0 implements Runnable {
            private /* synthetic */ LaunchHomeRunnable val$this;

            public /* synthetic */ -void_run__LambdaImpl0(LaunchHomeRunnable launchHomeRunnable) {
                this.val$this = launchHomeRunnable;
            }

            public void run() {
                this.val$this.-com_android_systemui_recents_RecentsActivity$LaunchHomeRunnable_lambda$1();
            }
        }

        public LaunchHomeRunnable(Intent launchIntent, ActivityOptions opts) {
            this.mLaunchIntent = launchIntent;
            this.mOpts = opts;
        }

        public void run() {
            RecentsActivity.this.mHandler.postAtFrontOfQueue(new -void_run__LambdaImpl0());
        }

        /* synthetic */ void -com_android_systemui_recents_RecentsActivity$LaunchHomeRunnable_lambda$1() {
            try {
                ActivityOptions opts = this.mOpts;
                if (opts == null) {
                    if (HwRecentsHelper.getAllTaskRemovingAllFlag()) {
                        opts = ActivityOptions.makeCustomAnimation(RecentsActivity.this, -1, -1);
                    } else {
                        opts = ActivityOptions.makeCustomAnimation(RecentsActivity.this, R.anim.recents_to_launcher_enter, R.anim.recents_to_launcher_exit);
                    }
                }
                long startTime = System.currentTimeMillis();
                RecentsActivity.this.startActivityAsUser(this.mLaunchIntent, opts.toBundle(), UserHandle.CURRENT);
                PerfDebugUtils.keyOperationTimeConsumed("CALL_START_LAUNCHER_ACTIVITY", startTime);
                PerfDebugUtils.perfRecentsRemoveAllElapsedTimeBegin(4);
            } catch (Exception e) {
                Log.e("RecentsActivity", RecentsActivity.this.getString(R.string.recents_launch_error_message, new Object[]{"Home"}), e);
            }
        }
    }

    static /* synthetic */ void -com_android_systemui_recents_RecentsActivity_lambda$2() {
        PerfDebugUtils.perfRecentsLaunchElapsedTimeEnd(5);
        EventBus.getDefault().send(new EnterRecentsWindowAnimationCompletedEvent(Recents.getConfiguration().getLaunchState()));
    }

    boolean dismissRecentsToFocusedTask(int logCategory) {
        if (Recents.getSystemServices().isRecentsActivityVisible() && this.mRecentsView.launchFocusedTask(logCategory)) {
            return true;
        }
        return false;
    }

    boolean dismissRecentsToLaunchTargetTaskOrHome() {
        if (Recents.getSystemServices().isRecentsActivityVisible()) {
            if (this.mRecentsView.launchPreviousTask()) {
                return true;
            }
            dismissRecentsToHome(true);
        }
        return false;
    }

    boolean dismissRecentsToFocusedTaskOrHome() {
        if (!Recents.getSystemServices().isRecentsActivityVisible()) {
            return false;
        }
        if (this.mRecentsView.launchFocusedTask(0)) {
            return true;
        }
        dismissRecentsToHome(true);
        return true;
    }

    void dismissRecentsToHome(boolean animateTaskViews) {
        dismissRecentsToHome(animateTaskViews, null);
    }

    void dismissRecentsToHome(boolean animateTaskViews, ActivityOptions overrideAnimation) {
        DismissRecentsToHomeAnimationStarted dismissEvent = new DismissRecentsToHomeAnimationStarted(animateTaskViews);
        dismissEvent.addPostAnimationCallback(new LaunchHomeRunnable(this.mHomeIntent, overrideAnimation));
        Recents.getSystemServices().sendCloseSystemWindows("homekey");
        EventBus.getDefault().send(dismissEvent);
        this.mRecentsView.updateClearBox(false, true);
    }

    boolean dismissRecentsToHomeIfVisible(boolean animated) {
        if (!Recents.getSystemServices().isRecentsActivityVisible()) {
            return false;
        }
        dismissRecentsToHome(animated);
        return true;
    }

    public void onCreate(Bundle savedInstanceState) {
        HwLog.i("RecentsActivity", "onCreate");
        try {
            PerfDebugUtils.beginSystraceSection("RecentsActivity.onCreate");
            super.onCreate(savedInstanceState);
            this.mFinishedOnStartup = false;
            PerfDebugUtils.perfRecentsLaunchElapsedTimeEnd(2);
            PerfDebugUtils.perfRecentsLaunchElapsedTimeBegin(3);
            if (Recents.getSystemServices() == null) {
                this.mFinishedOnStartup = true;
                finish();
                return;
            }
            this.refreshMemory.start();
            EventBus.getDefault().register(this, 2);
            this.mPackageMonitor = new RecentsPackageMonitor();
            this.mPackageMonitor.register(this);
            setContentView(R.layout.recents);
            takeKeyEvents(true);
            this.mRecentsView = (HwRecentsView) findViewById(R.id.recents_view);
            this.mRecentsView.setSystemUiVisibility(1792);
            this.mScrimViews = new SystemBarScrimViews(this);
            LayoutParams attributes = getWindow().getAttributes();
            attributes.privateFlags |= 16384;
            Configuration appConfiguration = Utilities.getAppConfiguration(this);
            this.mLastDeviceOrientation = appConfiguration.orientation;
            this.mLastDisplayDensity = appConfiguration.densityDpi;
            this.mFocusTimerDuration = getResources().getInteger(R.integer.recents_auto_advance_duration);
            this.mIterateTrigger = new DozeTrigger(this.mFocusTimerDuration, new Runnable() {
                public void run() {
                    RecentsActivity.this.dismissRecentsToFocusedTask(288);
                }
            });
            getWindow().setBackgroundDrawable(this.mRecentsView.getBackgroundScrim());
            this.mHomeIntent = new Intent("android.intent.action.MAIN", null);
            this.mHomeIntent.addCategory("android.intent.category.HOME");
            this.mHomeIntent.addFlags(270532608);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction("android.intent.action.TIME_SET");
            registerReceiver(this.mSystemBroadcastReceiver, filter);
            getWindow().addPrivateFlags(64);
            reloadStackView();
            PerfDebugUtils.perfRecentsLaunchElapsedTimeEnd(3);
            PerfDebugUtils.endSystraceSection();
        } finally {
            PerfDebugUtils.endSystraceSection();
        }
    }

    protected void onStart() {
        super.onStart();
        HwLog.i("RecentsActivity", "onStart");
        EventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, true));
        MetricsLogger.visible(this, 224);
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this.mRecentsDrawnEventListener);
    }

    protected void onNewIntent(Intent intent) {
        HwLog.i("RecentsActivity", "onNewIntent");
        PerfDebugUtils.beginSystraceSection("RecentsActivity.onNewIntent");
        super.onNewIntent(intent);
        this.mReceivedNewIntent = true;
        if (!this.mFinishedOnStartup) {
            PerfDebugUtils.perfRecentsLaunchElapsedTimeEnd(2);
        }
        PerfDebugUtils.perfRecentsLaunchElapsedTimeBegin(4);
        reloadStackView();
        PerfDebugUtils.perfRecentsLaunchElapsedTimeEnd(4);
        PerfDebugUtils.endSystraceSection();
    }

    private void reloadStackView() {
        RecentsTaskLoader loader = Recents.getTaskLoader();
        RecentsTaskLoadPlan loadPlan = RecentsImpl.consumeInstanceLoadPlan();
        if (loadPlan == null) {
            loadPlan = loader.createLoadPlan(this);
        }
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (!loadPlan.hasTasks()) {
            loader.preloadTasks(loadPlan, launchState.launchedToTaskId, !launchState.launchedFromHome);
        }
        Options loadOpts = new Options();
        loadOpts.runningTaskId = launchState.launchedToTaskId;
        loadOpts.numVisibleTasks = launchState.launchedNumVisibleTasks;
        loadOpts.numVisibleTaskThumbnails = launchState.launchedNumVisibleThumbnails;
        loader.loadTasks(this, loadPlan, loadOpts);
        TaskStack stack = loadPlan.getTaskStack();
        this.mRecentsView.onReload(this.mIsVisible, stack.getTaskCount() == 0);
        this.mRecentsView.updateStack(stack, true);
        this.mScrimViews.updateNavBarScrim(!launchState.launchedViaDockGesture, stack.getTaskCount() > 0, null);
        boolean wasLaunchedByAm = !launchState.launchedFromHome ? !launchState.launchedFromApp : false;
        if (wasLaunchedByAm) {
            EventBus.getDefault().send(new EnterRecentsWindowAnimationCompletedEvent());
        }
        if (launchState.launchedWithAltTab) {
            MetricsLogger.count(this, "overview_trigger_alttab", 1);
        } else {
            MetricsLogger.count(this, "overview_trigger_nav_btn", 1);
        }
        if (launchState.launchedFromApp) {
            int launchTaskIndexInStack;
            Task launchTarget = stack.getLaunchTarget();
            if (launchTarget != null) {
                launchTaskIndexInStack = stack.indexOfStackTask(launchTarget);
            } else {
                launchTaskIndexInStack = 0;
            }
            MetricsLogger.count(this, "overview_source_app", 1);
            MetricsLogger.histogram(this, "overview_source_app_index", launchTaskIndexInStack);
        } else {
            MetricsLogger.count(this, "overview_source_home", 1);
        }
        MetricsLogger.histogram(this, "overview_task_count", this.mRecentsView.getStack().getTaskCount());
        this.mIsVisible = true;
    }

    public void onEnterAnimationComplete() {
        HwLog.i("RecentsActivity", "onEnterAnimationComplete");
        PerfDebugUtils.beginSystraceSection("RecentsActivity.onEnterAnimationComplete");
        super.onEnterAnimationComplete();
        this.mHandler.removeCallbacks(this.mSendEnterWindowAnimationCompleteRunnable);
        if (this.mReceivedNewIntent) {
            this.mSendEnterWindowAnimationCompleteRunnable.run();
        } else {
            this.mHandler.post(this.mSendEnterWindowAnimationCompleteRunnable);
        }
        PerfDebugUtils.endSystraceSection();
    }

    protected void onResume() {
        super.onResume();
        HwLog.i("RecentsActivity", "onResume");
        this.mHandler.postDelayed(this.mSendEnterWindowAnimationCompleteRunnable, 100);
        JanklogUtils.eventEnd(135, "Start recent activity");
        PerfDebugUtils.perfRecentsLaunchElapsedTimeBegin(5);
    }

    private void writeMemorySize() {
        if (getResources().getBoolean(R.bool.config_task_manager)) {
            HwRecentsHelper.setTotalMemorySize(this);
        }
    }

    protected void onPause() {
        HwLog.i("RecentsActivity", "onPause");
        PerfDebugUtils.beginSystraceSection("RecentsActivity.onPause");
        super.onPause();
        PerfDebugUtils.perfRecentsRemoveAllElapsedTimeEnd(4);
        this.mIgnoreAltTabRelease = false;
        this.mIterateTrigger.stopDozing();
        EventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, false));
        HwRecentTaskRemove.getInstance(this).notifyRemoveStart(this.mRecentsView.getRemoveAllClickTime());
        PerfDebugUtils.perfRecentsRemoveAllElapsedTimeBegin(5);
        PerfDebugUtils.endSystraceSection();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        boolean z = true;
        super.onConfigurationChanged(newConfig);
        if (this.mRecentsView == null) {
            HwLog.e("RecentsActivity", "onConfigurationChanged mRecentsView == null");
            return;
        }
        boolean z2;
        Configuration newDeviceConfiguration = Utilities.getAppConfiguration(this);
        int numStackTasks = this.mRecentsView.getStack().getStackTaskCount();
        EventBus eventBus = EventBus.getDefault();
        boolean z3 = this.mLastDeviceOrientation != newDeviceConfiguration.orientation;
        if (this.mLastDisplayDensity != newDeviceConfiguration.densityDpi) {
            z2 = true;
        } else {
            z2 = false;
        }
        if (numStackTasks <= 0) {
            z = false;
        }
        eventBus.send(new ConfigurationChangedEvent(false, z3, z2, z));
        this.mLastDeviceOrientation = newDeviceConfiguration.orientation;
        this.mLastDisplayDensity = newDeviceConfiguration.densityDpi;
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        boolean z;
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        RecentsTaskLoader loader = Recents.getTaskLoader();
        RecentsTaskLoadPlan loadPlan = loader.createLoadPlan(this);
        loader.preloadTasks(loadPlan, -1, false);
        Options loadOpts = new Options();
        loadOpts.numVisibleTasks = launchState.launchedNumVisibleTasks;
        loadOpts.numVisibleTaskThumbnails = launchState.launchedNumVisibleThumbnails;
        loader.loadTasks(this, loadPlan, loadOpts);
        TaskStack stack = loadPlan.getTaskStack();
        int numStackTasks = stack.getStackTaskCount();
        boolean showDeferredAnimation = numStackTasks > 0;
        EventBus eventBus = EventBus.getDefault();
        if (numStackTasks > 0) {
            z = true;
        } else {
            z = false;
        }
        eventBus.send(new ConfigurationChangedEvent(true, false, false, z));
        EventBus.getDefault().send(new MultiWindowStateChangedEvent(isInMultiWindowMode, showDeferredAnimation, stack));
    }

    protected void onStop() {
        HwLog.i("RecentsActivity", "onStop");
        PerfDebugUtils.beginSystraceSection("RecentsActivity.onStop");
        super.onStop();
        PerfDebugUtils.perfRecentsRemoveAllElapsedTimeEnd(5);
        this.mIsVisible = false;
        this.mReceivedNewIntent = false;
        MetricsLogger.hidden(this, 224);
        Recents.getConfiguration().getLaunchState().reset();
        PerfDebugUtils.dumpRecentsRemoveAllTime();
        PerfDebugUtils.endSystraceSection();
    }

    protected void onDestroy() {
        super.onDestroy();
        HwLog.i("RecentsActivity", "onDestroy");
        if (!this.mFinishedOnStartup) {
            unregisterReceiver(this.mSystemBroadcastReceiver);
            this.mPackageMonitor.unregister();
            EventBus.getDefault().unregister(this);
        }
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this.mScrimViews, 2);
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this.mScrimViews);
    }

    public void onTrimMemory(int level) {
        RecentsTaskLoader loader = Recents.getTaskLoader();
        if (loader != null) {
            loader.onTrimMemory(level);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 19:
                EventBus.getDefault().send(new FocusNextTaskViewEvent(0));
                return true;
            case 20:
                EventBus.getDefault().send(new FocusPreviousTaskViewEvent());
                return true;
            case 61:
                boolean hasRepKeyTimeElapsed = SystemClock.elapsedRealtime() - this.mLastTabKeyEventTime > ((long) getResources().getInteger(R.integer.recents_alt_tab_key_delay));
                if (event.getRepeatCount() <= 0 || hasRepKeyTimeElapsed) {
                    if (event.isShiftPressed()) {
                        EventBus.getDefault().send(new FocusPreviousTaskViewEvent());
                    } else {
                        EventBus.getDefault().send(new FocusNextTaskViewEvent(0));
                    }
                    this.mLastTabKeyEventTime = SystemClock.elapsedRealtime();
                    if (event.isAltPressed()) {
                        this.mIgnoreAltTabRelease = false;
                    }
                }
                return true;
            case 67:
            case 112:
                if (event.getRepeatCount() <= 0) {
                    EventBus.getDefault().send(new DismissFocusedTaskViewEvent());
                    MetricsLogger.histogram(this, "overview_task_dismissed_source", 0);
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onUserInteraction() {
        EventBus.getDefault().send(this.mUserInteractionEvent);
    }

    public void onBackPressed() {
        EventBus.getDefault().send(new ToggleRecentsEvent());
    }

    public final void onBusEvent(ToggleRecentsEvent event) {
        if (Recents.getConfiguration().getLaunchState().launchedFromHome) {
            dismissRecentsToHome(true);
        } else {
            dismissRecentsToLaunchTargetTaskOrHome();
        }
    }

    public final void onBusEvent(IterateRecentsEvent event) {
        int timerIndicatorDuration = 0;
        if (Recents.getDebugFlags().isFastToggleRecentsEnabled()) {
            timerIndicatorDuration = getResources().getInteger(R.integer.recents_subsequent_auto_advance_duration);
            this.mIterateTrigger.setDozeDuration(timerIndicatorDuration);
            if (this.mIterateTrigger.isDozing()) {
                this.mIterateTrigger.poke();
            } else {
                this.mIterateTrigger.startDozing();
            }
        }
        EventBus.getDefault().send(new FocusNextTaskViewEvent(timerIndicatorDuration));
        MetricsLogger.action(this, 276);
    }

    public final void onBusEvent(UserInteractionEvent event) {
        this.mIterateTrigger.stopDozing();
    }

    public final void onBusEvent(HideRecentsEvent event) {
        if (event.triggeredFromAltTab) {
            if (!this.mIgnoreAltTabRelease) {
                dismissRecentsToFocusedTaskOrHome();
            }
        } else if (event.triggeredFromHomeKey) {
            dismissRecentsToHome(true);
            EventBus.getDefault().send(this.mUserInteractionEvent);
        }
    }

    public final void onBusEvent(EnterRecentsWindowLastAnimationFrameEvent event) {
        EventBus.getDefault().send(new UpdateFreeformTaskViewVisibilityEvent(true));
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        this.mRecentsView.invalidate();
    }

    public final void onBusEvent(ExitRecentsWindowFirstAnimationFrameEvent event) {
        if (this.mRecentsView.isLastTaskLaunchedFreeform()) {
            EventBus.getDefault().send(new UpdateFreeformTaskViewVisibilityEvent(false));
        }
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        this.mRecentsView.invalidate();
    }

    public final void onBusEvent(DockedFirstAnimationFrameEvent event) {
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        this.mRecentsView.invalidate();
    }

    public final void onBusEvent(CancelEnterRecentsWindowAnimationEvent event) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        int launchToTaskId = launchState.launchedToTaskId;
        if (launchToTaskId == -1) {
            return;
        }
        if (event.launchTask == null || launchToTaskId != event.launchTask.key.id) {
            SystemServicesProxy ssp = Recents.getSystemServices();
            ssp.cancelWindowTransition(launchState.launchedToTaskId);
            ssp.cancelThumbnailTransition(getTaskId());
        }
    }

    public final void onBusEvent(ShowApplicationInfoEvent event) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.fromParts("package", event.task.key.getComponent().getPackageName(), null));
        intent.setComponent(intent.resolveActivity(getPackageManager()));
        TaskStackBuilder.create(this).addNextIntentWithParentStack(intent).startActivities(null, new UserHandle(event.task.key.userId));
        MetricsLogger.count(this, "overview_app_info", 1);
    }

    public final void onBusEvent(ShowIncompatibleAppOverlayEvent event) {
        if (this.mIncompatibleAppOverlay == null) {
            this.mIncompatibleAppOverlay = Utilities.findViewStubById((Activity) this, (int) R.id.incompatible_app_overlay_stub).inflate();
            this.mIncompatibleAppOverlay.setWillNotDraw(false);
            this.mIncompatibleAppOverlay.setVisibility(0);
        }
        this.mIncompatibleAppOverlay.animate().alpha(1.0f).setDuration(150).setInterpolator(Interpolators.ALPHA_IN).start();
    }

    public final void onBusEvent(HideIncompatibleAppOverlayEvent event) {
        if (this.mIncompatibleAppOverlay != null) {
            this.mIncompatibleAppOverlay.animate().alpha(0.0f).setDuration(150).setInterpolator(Interpolators.ALPHA_OUT).start();
        }
    }

    public final void onBusEvent(DeleteTaskDataEvent event) {
        Recents.getTaskLoader().deleteTaskData(event.task, false);
        if (!event.isRemoveAll) {
            Recents.getSystemServices().removeTask(event.task.key.id);
            HwRecentTaskRemove.getInstance(getApplicationContext()).sendRemoveTaskToSystemManager(event.task);
        }
    }

    public final void onBusEvent(AllTaskViewsDismissedEvent event) {
        if (Recents.getSystemServices().hasDockedTask()) {
            this.mRecentsView.showEmptyView(event.msgResId);
        } else if (this.mIsVisible) {
            dismissRecentsToHome(false);
        } else {
            HwLog.i("RecentsActivity", "RecentsActivity is not visible,do not send to Home");
        }
        MetricsLogger.count(this, "overview_task_all_dismissed", 1);
    }

    public final void onBusEvent(LaunchTaskSucceededEvent event) {
        MetricsLogger.histogram(this, "overview_task_launch_index", event.taskIndexFromStackFront);
    }

    public final void onBusEvent(LaunchTaskFailedEvent event) {
        dismissRecentsToHome(true);
        MetricsLogger.count(this, "overview_task_launch_failed", 1);
    }

    public final void onBusEvent(ScreenPinningRequestEvent event) {
        MetricsLogger.count(this, "overview_screen_pinned", 1);
    }

    public final void onBusEvent(DebugFlagsChangedEvent event) {
        finish();
    }

    public final void onBusEvent(StackViewScrolledEvent event) {
        this.mIgnoreAltTabRelease = true;
    }

    public final void onBusEvent(DockedTopTaskEvent event) {
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this.mRecentsDrawnEventListener);
        this.mRecentsView.invalidate();
    }

    @FindBugsSuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    public boolean onPreDraw() {
        this.mRecentsView.getViewTreeObserver().removeOnPreDrawListener(this);
        this.mRecentsView.post(new Runnable() {
            public void run() {
                Recents.getSystemServices().endProlongedAnimations();
            }
        });
        return true;
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        EventBus.getDefault().dump(prefix, writer);
        Recents.getTaskLoader().dump(prefix, writer);
        String id = Integer.toHexString(System.identityHashCode(this));
        writer.print(prefix);
        writer.print("RecentsActivity");
        writer.print(" visible=");
        writer.print(this.mIsVisible ? "Y" : "N");
        writer.print(" [0x");
        writer.print(id);
        writer.print("]");
        writer.println();
        if (this.mRecentsView != null) {
            this.mRecentsView.dump(prefix, writer);
        }
    }
}
