package com.android.systemui.recents.tv;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.RecentsImpl;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.LaunchTaskFailedEvent;
import com.android.systemui.recents.events.activity.ToggleRecentsEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.DeleteTaskDataEvent;
import com.android.systemui.recents.events.ui.UserInteractionEvent;
import com.android.systemui.recents.events.ui.focus.DismissFocusedTaskViewEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.RecentsPackageMonitor;
import com.android.systemui.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.recents.model.RecentsTaskLoadPlan.Options;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.tv.animations.HomeRecentsEnterExitAnimationHolder;
import com.android.systemui.recents.tv.views.RecentsTvView;
import com.android.systemui.recents.tv.views.TaskCardView;
import com.android.systemui.recents.tv.views.TaskStackHorizontalGridView;
import com.android.systemui.recents.tv.views.TaskStackHorizontalViewAdapter;
import com.android.systemui.tv.pip.PipManager;
import com.android.systemui.tv.pip.PipManager.Listener;
import com.android.systemui.tv.pip.PipRecentsOverlayManager;
import com.android.systemui.tv.pip.PipRecentsOverlayManager.Callback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecentsTvActivity extends Activity implements OnPreDrawListener {
    private FinishRecentsRunnable mFinishLaunchHomeRunnable;
    private boolean mFinishedOnStartup;
    private HomeRecentsEnterExitAnimationHolder mHomeRecentsEnterExitAnimationHolder;
    private boolean mIgnoreAltTabRelease;
    private boolean mLaunchedFromHome;
    private RecentsPackageMonitor mPackageMonitor;
    private final Listener mPipListener = new Listener() {
        public void onPipEntered() {
            RecentsTvActivity.this.updatePipUI();
        }

        public void onPipActivityClosed() {
            RecentsTvActivity.this.updatePipUI();
        }

        public void onShowPipMenu() {
            RecentsTvActivity.this.updatePipUI();
        }

        public void onMoveToFullscreen() {
            RecentsTvActivity.this.dismissRecentsToLaunchTargetTaskOrHome(false);
        }

        public void onPipResizeAboutToStart() {
        }
    };
    private final PipManager mPipManager = PipManager.getInstance();
    private PipRecentsOverlayManager mPipRecentsOverlayManager;
    private final Callback mPipRecentsOverlayManagerCallback = new Callback() {
        public void onClosed() {
            RecentsTvActivity.this.dismissRecentsToLaunchTargetTaskOrHome(true);
        }

        public void onBackPressed() {
            RecentsTvActivity.this.onBackPressed();
        }

        public void onRecentsFocused() {
            if (RecentsTvActivity.this.mTalkBackEnabled) {
                RecentsTvActivity.this.mTaskStackHorizontalGridView.requestFocus();
                RecentsTvActivity.this.mTaskStackHorizontalGridView.sendAccessibilityEvent(8);
            }
            RecentsTvActivity.this.mTaskStackHorizontalGridView.startFocusGainAnimation();
        }
    };
    private View mPipView;
    private final OnFocusChangeListener mPipViewFocusChangeListener = new OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                RecentsTvActivity.this.requestPipControlsFocus();
            }
        }
    };
    private RecentsTvView mRecentsView;
    private boolean mTalkBackEnabled;
    private TaskStackHorizontalGridView mTaskStackHorizontalGridView;
    private TaskStackHorizontalViewAdapter mTaskStackViewAdapter;

    class FinishRecentsRunnable implements Runnable {
        Intent mLaunchIntent;

        public FinishRecentsRunnable(Intent launchIntent) {
            this.mLaunchIntent = launchIntent;
        }

        public void run() {
            try {
                RecentsTvActivity.this.startActivityAsUser(this.mLaunchIntent, ActivityOptions.makeCustomAnimation(RecentsTvActivity.this, R.anim.recents_to_launcher_enter, R.anim.recents_to_launcher_exit).toBundle(), UserHandle.CURRENT);
            } catch (Exception e) {
                Log.e("RecentsTvActivity", RecentsTvActivity.this.getString(R.string.recents_launch_error_message, new Object[]{"Home"}), e);
            }
        }
    }

    private void updateRecentsTasks() {
        RecentsTaskLoader loader = Recents.getTaskLoader();
        RecentsTaskLoadPlan plan = RecentsImpl.consumeInstanceLoadPlan();
        if (plan == null) {
            plan = loader.createLoadPlan(this);
        }
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (!plan.hasTasks()) {
            loader.preloadTasks(plan, -1, !launchState.launchedFromHome);
        }
        int numVisibleTasks = TaskCardView.getNumberOfVisibleTasks(getApplicationContext());
        this.mLaunchedFromHome = launchState.launchedFromHome;
        TaskStack stack = plan.getTaskStack();
        Options loadOpts = new Options();
        loadOpts.runningTaskId = launchState.launchedToTaskId;
        loadOpts.numVisibleTasks = numVisibleTasks;
        loadOpts.numVisibleTaskThumbnails = numVisibleTasks;
        loader.loadTasks(this, plan, loadOpts);
        List stackTasks = stack.getStackTasks();
        Collections.reverse(stackTasks);
        if (this.mTaskStackViewAdapter == null) {
            this.mTaskStackViewAdapter = new TaskStackHorizontalViewAdapter(stackTasks);
            this.mTaskStackHorizontalGridView = this.mRecentsView.setTaskStackViewAdapter(this.mTaskStackViewAdapter);
            this.mHomeRecentsEnterExitAnimationHolder = new HomeRecentsEnterExitAnimationHolder(getApplicationContext(), this.mTaskStackHorizontalGridView);
        } else {
            this.mTaskStackViewAdapter.setNewStackTasks(stackTasks);
        }
        this.mRecentsView.init(stack);
        if (launchState.launchedToTaskId != -1) {
            ArrayList<Task> tasks = stack.getStackTasks();
            int taskCount = tasks.size();
            for (int i = 0; i < taskCount; i++) {
                Task t = (Task) tasks.get(i);
                if (t.key.id == launchState.launchedToTaskId) {
                    t.isLaunchTarget = true;
                    return;
                }
            }
        }
    }

    boolean dismissRecentsToLaunchTargetTaskOrHome(boolean animate) {
        if (Recents.getSystemServices().isRecentsActivityVisible()) {
            if (this.mRecentsView.launchPreviousTask(animate)) {
                return true;
            }
            dismissRecentsToHome(animate);
        }
        return false;
    }

    boolean dismissRecentsToFocusedTaskOrHome() {
        if (!Recents.getSystemServices().isRecentsActivityVisible()) {
            return false;
        }
        if (this.mRecentsView.launchFocusedTask()) {
            return true;
        }
        dismissRecentsToHome(true);
        return true;
    }

    void dismissRecentsToHome(boolean animateTaskViews) {
        Runnable closeSystemWindows = new Runnable() {
            public void run() {
                Recents.getSystemServices().sendCloseSystemWindows("homekey");
            }
        };
        DismissRecentsToHomeAnimationStarted dismissEvent = new DismissRecentsToHomeAnimationStarted(animateTaskViews);
        dismissEvent.addPostAnimationCallback(this.mFinishLaunchHomeRunnable);
        dismissEvent.addPostAnimationCallback(closeSystemWindows);
        if (this.mTaskStackHorizontalGridView.getChildCount() <= 0 || !animateTaskViews) {
            closeSystemWindows.run();
            this.mFinishLaunchHomeRunnable.run();
            return;
        }
        this.mHomeRecentsEnterExitAnimationHolder.startExitAnimation(dismissEvent);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFinishedOnStartup = false;
        if (Recents.getSystemServices() == null) {
            this.mFinishedOnStartup = true;
            finish();
            return;
        }
        this.mPipRecentsOverlayManager = PipManager.getInstance().getPipRecentsOverlayManager();
        EventBus.getDefault().register(this, 2);
        this.mPackageMonitor = new RecentsPackageMonitor();
        this.mPackageMonitor.register(this);
        setContentView(R.layout.recents_on_tv);
        this.mRecentsView = (RecentsTvView) findViewById(R.id.recents_view);
        this.mRecentsView.setSystemUiVisibility(1792);
        this.mPipView = findViewById(R.id.pip);
        this.mPipView.setOnFocusChangeListener(this.mPipViewFocusChangeListener);
        Rect pipBounds = this.mPipManager.getRecentsFocusedPipBounds();
        LayoutParams lp = (LayoutParams) this.mPipView.getLayoutParams();
        lp.width = pipBounds.width();
        lp.height = pipBounds.height();
        lp.leftMargin = pipBounds.left;
        lp.topMargin = pipBounds.top;
        this.mPipView.setLayoutParams(lp);
        this.mPipRecentsOverlayManager.setCallback(this.mPipRecentsOverlayManagerCallback);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.privateFlags |= 16384;
        Intent homeIntent = new Intent("android.intent.action.MAIN", null);
        homeIntent.addCategory("android.intent.category.HOME");
        homeIntent.addFlags(270532608);
        homeIntent.putExtra("com.android.systemui.recents.tv.RecentsTvActivity.RECENTS_HOME_INTENT_EXTRA", true);
        this.mFinishLaunchHomeRunnable = new FinishRecentsRunnable(homeIntent);
        this.mPipManager.addListener(this.mPipListener);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        if (this.mLaunchedFromHome) {
            this.mHomeRecentsEnterExitAnimationHolder.startEnterAnimation(this.mPipManager.isPipShown());
        }
        EventBus.getDefault().send(new EnterRecentsWindowAnimationCompletedEvent());
    }

    public void onResume() {
        boolean z = true;
        super.onResume();
        this.mPipRecentsOverlayManager.onRecentsResumed();
        updateRecentsTasks();
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        boolean wasLaunchedByAm = !launchState.launchedFromHome ? !launchState.launchedFromApp : false;
        if (wasLaunchedByAm) {
            EventBus.getDefault().send(new EnterRecentsWindowAnimationCompletedEvent());
        }
        SystemServicesProxy ssp = Recents.getSystemServices();
        EventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, true));
        if (this.mTaskStackHorizontalGridView.getStack().getTaskCount() <= 1 || this.mLaunchedFromHome) {
            this.mTaskStackHorizontalGridView.setSelectedPosition(0);
        } else {
            this.mTaskStackHorizontalGridView.setSelectedPosition(1);
        }
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        View dismissPlaceholder = findViewById(R.id.dismiss_placeholder);
        this.mTalkBackEnabled = ssp.isTouchExplorationEnabled();
        if (this.mTalkBackEnabled) {
            dismissPlaceholder.setAccessibilityTraversalBefore(R.id.task_list);
            dismissPlaceholder.setAccessibilityTraversalAfter(R.id.dismiss_placeholder);
            this.mTaskStackHorizontalGridView.setAccessibilityTraversalAfter(R.id.dismiss_placeholder);
            this.mTaskStackHorizontalGridView.setAccessibilityTraversalBefore(R.id.pip);
            dismissPlaceholder.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    RecentsTvActivity.this.mTaskStackHorizontalGridView.requestFocus();
                    RecentsTvActivity.this.mTaskStackHorizontalGridView.sendAccessibilityEvent(8);
                    Task focusedTask = RecentsTvActivity.this.mTaskStackHorizontalGridView.getFocusedTask();
                    if (focusedTask != null) {
                        RecentsTvActivity.this.mTaskStackViewAdapter.removeTask(focusedTask);
                        EventBus.getDefault().send(new DeleteTaskDataEvent(focusedTask));
                    }
                }
            });
        }
        if (this.mPipManager.isPipShown()) {
            if (this.mTalkBackEnabled) {
                this.mPipView.setVisibility(0);
            } else {
                this.mPipView.setVisibility(8);
            }
            PipRecentsOverlayManager pipRecentsOverlayManager = this.mPipRecentsOverlayManager;
            if (this.mTaskStackViewAdapter.getItemCount() <= 0) {
                z = false;
            }
            pipRecentsOverlayManager.requestFocus(z);
            return;
        }
        this.mPipView.setVisibility(8);
        this.mPipRecentsOverlayManager.removePipRecentsOverlayView();
    }

    public void onPause() {
        super.onPause();
        this.mPipRecentsOverlayManager.onRecentsPaused();
    }

    protected void onStop() {
        super.onStop();
        this.mIgnoreAltTabRelease = false;
        EventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, false));
        Recents.getConfiguration().getLaunchState().reset();
        finish();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mPipManager.removeListener(this.mPipListener);
        if (!this.mFinishedOnStartup) {
            this.mPackageMonitor.unregister();
            EventBus.getDefault().unregister(this);
        }
    }

    public void onTrimMemory(int level) {
        RecentsTaskLoader loader = Recents.getTaskLoader();
        if (loader != null) {
            loader.onTrimMemory(level);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 67:
            case 112:
                EventBus.getDefault().send(new DismissFocusedTaskViewEvent());
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    public void onUserInteraction() {
        EventBus.getDefault().send(new UserInteractionEvent());
    }

    public void onBackPressed() {
        EventBus.getDefault().send(new ToggleRecentsEvent());
    }

    public final void onBusEvent(ToggleRecentsEvent event) {
        if (Recents.getConfiguration().getLaunchState().launchedFromHome) {
            dismissRecentsToHome(true);
        } else {
            dismissRecentsToLaunchTargetTaskOrHome(true);
        }
    }

    public final void onBusEvent(HideRecentsEvent event) {
        if (event.triggeredFromAltTab) {
            if (!this.mIgnoreAltTabRelease) {
                dismissRecentsToFocusedTaskOrHome();
            }
        } else if (event.triggeredFromHomeKey) {
            dismissRecentsToHome(true);
        }
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

    public final void onBusEvent(DeleteTaskDataEvent event) {
        Recents.getTaskLoader().deleteTaskData(event.task, false);
        Recents.getSystemServices().removeTask(event.task.key.id);
    }

    public final void onBusEvent(AllTaskViewsDismissedEvent event) {
        if (this.mPipManager.isPipShown()) {
            this.mRecentsView.showEmptyView();
            this.mPipRecentsOverlayManager.requestFocus(false);
            return;
        }
        dismissRecentsToHome(false);
    }

    public final void onBusEvent(LaunchTaskFailedEvent event) {
        dismissRecentsToHome(true);
    }

    public boolean onPreDraw() {
        this.mRecentsView.getViewTreeObserver().removeOnPreDrawListener(this);
        if (this.mLaunchedFromHome) {
            this.mHomeRecentsEnterExitAnimationHolder.setEnterFromHomeStartingAnimationValues(this.mPipManager.isPipShown());
        } else {
            this.mHomeRecentsEnterExitAnimationHolder.setEnterFromAppStartingAnimationValues(this.mPipManager.isPipShown());
        }
        this.mRecentsView.post(new Runnable() {
            public void run() {
                Recents.getSystemServices().endProlongedAnimations();
            }
        });
        return true;
    }

    private void updatePipUI() {
        if (this.mPipManager.isPipShown()) {
            Log.w("RecentsTvActivity", "An activity entered PIP mode while Recents is shown");
            return;
        }
        this.mPipRecentsOverlayManager.removePipRecentsOverlayView();
        this.mTaskStackHorizontalGridView.startFocusLossAnimation();
    }

    public void requestPipControlsFocus() {
        boolean z = false;
        if (this.mPipManager.isPipShown()) {
            this.mTaskStackHorizontalGridView.startFocusLossAnimation();
            PipRecentsOverlayManager pipRecentsOverlayManager = this.mPipRecentsOverlayManager;
            if (this.mTaskStackViewAdapter.getItemCount() > 0) {
                z = true;
            }
            pipRecentsOverlayManager.requestFocus(z);
        }
    }
}
