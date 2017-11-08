package com.android.systemui.stackdivider;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.ArraySet;
import com.android.systemui.R;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.AppTransitionFinishedEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.SystemServicesProxy.TaskStackListener;
import com.android.systemui.stackdivider.events.StartedDragingEvent;
import com.android.systemui.stackdivider.events.StoppedDragingEvent;
import com.android.systemui.utils.SystemUiUtil;

public class ForcedResizableInfoActivityController {
    private final Context mContext;
    private boolean mDividerDraging;
    private final Handler mHandler = new Handler();
    private final ArraySet<String> mPackagesShownInSession = new ArraySet();
    private final ArraySet<Integer> mPendingTaskIds = new ArraySet();
    private final Runnable mTimeoutRunnable = new Runnable() {
        public void run() {
            ForcedResizableInfoActivityController.this.showPending();
        }
    };

    public ForcedResizableInfoActivityController(Context context) {
        this.mContext = context;
        EventBus.getDefault().register(this);
        SystemServicesProxy.getInstance(context).registerTaskStackListener(new TaskStackListener() {
            public void onActivityForcedResizable(String packageName, int taskId) {
                ForcedResizableInfoActivityController.this.activityForcedResizable(packageName, taskId);
            }

            public void onActivityDismissingDockedStack() {
                ForcedResizableInfoActivityController.this.activityDismissingDockedStack();
            }
        });
    }

    public void notifyDockedStackExistsChanged(boolean exists) {
        if (!exists) {
            this.mPackagesShownInSession.clear();
        }
    }

    public final void onBusEvent(AppTransitionFinishedEvent event) {
        if (!this.mDividerDraging) {
            showPending();
        }
    }

    public final void onBusEvent(StartedDragingEvent event) {
        this.mDividerDraging = true;
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
    }

    public final void onBusEvent(StoppedDragingEvent event) {
        this.mDividerDraging = false;
        showPending();
    }

    private void activityForcedResizable(String packageName, int taskId) {
        if (!debounce(packageName)) {
            this.mPendingTaskIds.add(Integer.valueOf(taskId));
            postTimeout();
        }
    }

    private void activityDismissingDockedStack() {
        SystemUiUtil.showToastForAllUser(this.mContext, R.string.recents_incompatible_app_message);
    }

    private void showPending() {
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
        for (int i = this.mPendingTaskIds.size() - 1; i >= 0; i--) {
            Intent intent = new Intent(this.mContext, ForcedResizableInfoActivity.class);
            ActivityOptions options = ActivityOptions.makeBasic();
            options.setLaunchTaskId(((Integer) this.mPendingTaskIds.valueAt(i)).intValue());
            options.setTaskOverlay(true);
            this.mContext.startActivity(intent, options.toBundle());
        }
        this.mPendingTaskIds.clear();
    }

    private void postTimeout() {
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
        this.mHandler.postDelayed(this.mTimeoutRunnable, 1000);
    }

    private boolean debounce(String packageName) {
        if (packageName == null) {
            return false;
        }
        if ("com.android.systemui".equals(packageName)) {
            return true;
        }
        boolean debounce = this.mPackagesShownInSession.contains(packageName);
        this.mPackagesShownInSession.add(packageName);
        return debounce;
    }
}
