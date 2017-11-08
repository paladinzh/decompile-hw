package com.android.systemui.recents.model;

import android.app.ActivityManager.RecentTaskInfo;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.UserManager;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.recents.HwRecentTaskRemove;
import com.android.systemui.recents.HwRecentsHelper;
import com.android.systemui.recents.HwRecentsLockUtils;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.Task.TaskKey;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RecentsTaskLoadPlan {
    private static int MIN_NUM_TASKS = 5;
    private static int SESSION_BEGIN_TIME = 21600000;
    Context mContext;
    ArraySet<Integer> mCurrentQuietProfiles = new ArraySet();
    List<RecentTaskInfo> mRawTasks;
    TaskStack mStack;

    public static class Options {
        public boolean loadIcons = true;
        public boolean loadThumbnails = true;
        public int numVisibleTaskThumbnails = 0;
        public int numVisibleTasks = 0;
        public boolean onlyLoadForCache = false;
        public boolean onlyLoadPausedActivities = false;
        public int runningTaskId = -1;
    }

    RecentsTaskLoadPlan(Context context) {
        this.mContext = context;
    }

    private void updateCurrentQuietProfilesCache(int currentUserId) {
        this.mCurrentQuietProfiles.clear();
        if (currentUserId == -2) {
            currentUserId = UserSwitchUtils.getCurrentUser();
        }
        List<UserInfo> profiles = ((UserManager) this.mContext.getSystemService("user")).getProfiles(currentUserId);
        if (profiles != null) {
            for (int i = 0; i < profiles.size(); i++) {
                UserInfo user = (UserInfo) profiles.get(i);
                if (user.isManagedProfile() && user.isQuietModeEnabled()) {
                    this.mCurrentQuietProfiles.add(Integer.valueOf(user.id));
                }
            }
        }
    }

    public synchronized void preloadRawTasks(boolean includeFrontMostExcludedTask) {
        updateCurrentQuietProfilesCache(-2);
        this.mRawTasks = Recents.getSystemServices().getRecentTasks(20, -2, includeFrontMostExcludedTask, this.mCurrentQuietProfiles);
        Collections.reverse(this.mRawTasks);
    }

    public synchronized void preloadPlan(RecentsTaskLoader loader, int runningTaskId, boolean includeFrontMostExcludedTask) {
        HwRecentsLockUtils.refreshToCache();
        HwRecentsHelper.refreshPlayingMusicUidSet();
        Map<String, Boolean> map = HwRecentsLockUtils.search(this.mContext);
        Resources res = this.mContext.getResources();
        ArrayList<Task> allTasks = new ArrayList();
        if (this.mRawTasks == null) {
            preloadRawTasks(includeFrontMostExcludedTask);
        }
        SparseArray<TaskKey> affiliatedTasks = new SparseArray();
        SparseIntArray affiliatedTaskCounts = new SparseIntArray();
        String dismissDescFormat = this.mContext.getString(R.string.accessibility_recents_item_will_be_dismissed);
        String appInfoDescFormat = this.mContext.getString(R.string.accessibility_recents_item_open_app_info);
        String lockDescFormat = this.mContext.getString(R.string.accessibility_recents_item_will_be_locked);
        long lastStackActiveTime = Prefs.getLong(this.mContext, "OverviewLastStackTaskActiveTime", 0);
        long newLastStackActiveTime = -1;
        int taskCount = this.mRawTasks.size();
        int i = 0;
        while (i < taskCount) {
            Drawable andUpdateActivityIcon;
            RecentTaskInfo t = (RecentTaskInfo) this.mRawTasks.get(i);
            TaskKey taskKey = new TaskKey(t.persistentId, t.stackId, t.baseIntent, t.userId, t.firstActiveTime, t.lastActiveTime);
            boolean isStackTask = (SystemServicesProxy.isFreeformStack(t.stackId) || !isHistoricalTask(t)) ? true : t.lastActiveTime >= lastStackActiveTime && i >= taskCount - MIN_NUM_TASKS;
            boolean isLaunchTarget = taskKey.id == runningTaskId;
            if (isStackTask && newLastStackActiveTime < 0) {
                newLastStackActiveTime = t.lastActiveTime;
            }
            ActivityInfo info = loader.getAndUpdateActivityInfo(taskKey);
            String title = loader.getAndUpdateActivityTitle(taskKey, t.taskDescription);
            String dismissDescription = String.format(dismissDescFormat, new Object[]{loader.getAndUpdateContentDescription(taskKey, res)});
            String lockDescription = String.format(lockDescFormat, new Object[]{titleDescription});
            String appInfoDescription = String.format(appInfoDescFormat, new Object[]{titleDescription});
            if (isStackTask) {
                andUpdateActivityIcon = loader.getAndUpdateActivityIcon(taskKey, t.taskDescription, res, false);
            } else {
                andUpdateActivityIcon = null;
            }
            Bitmap thumbnail = loader.getAndUpdateThumbnail(taskKey, false);
            int activityColor = loader.getActivityPrimaryColor(t.taskDescription);
            int backgroundColor = loader.getActivityBackgroundColor(t.taskDescription);
            boolean isSystemApp = info != null ? (info.applicationInfo.flags & 1) != 0 : false;
            Task task = new Task(taskKey, t.affiliatedTaskId, t.affiliatedTaskColor, andUpdateActivityIcon, thumbnail, title, titleDescription, dismissDescription, appInfoDescription, lockDescription, activityColor, backgroundColor, isLaunchTarget, isStackTask, isSystemApp, t.isDockable, t.bounds, t.taskDescription, t.resizeMode, t.topActivity, info != null ? info.packageName : BuildConfig.FLAVOR);
            if (!shouldSkipLoadTask(map, t, task)) {
                allTasks.add(task);
                affiliatedTaskCounts.put(taskKey.id, affiliatedTaskCounts.get(taskKey.id, 0) + 1);
                affiliatedTasks.put(taskKey.id, taskKey);
            }
            i++;
        }
        if (newLastStackActiveTime != -1) {
            Prefs.putLong(this.mContext, "OverviewLastStackTaskActiveTime", newLastStackActiveTime);
        }
        HwLog.i("RecentsTaskLoadPlan", "to show tasks size is " + allTasks.size());
        this.mStack = new TaskStack();
        this.mStack.setTasks(this.mContext, allTasks, false);
    }

    public synchronized void executePlan(Options opts, RecentsTaskLoader loader, TaskResourceLoadQueue loadQueue) {
        RecentsConfiguration config = Recents.getConfiguration();
        Resources res = this.mContext.getResources();
        Task rawTask = this.mStack.getStackFrontMostTask(false);
        List<RecentTaskInfo> curTasks = Recents.getSystemServices().getRecentTasks(1, 0, false, this.mCurrentQuietProfiles);
        if (!(curTasks == null || curTasks.size() <= 0 || rawTask == null)) {
            rawTask.key.lastActiveTime = ((RecentTaskInfo) curTasks.get(0)).lastActiveTime;
        }
        ArrayList<Task> tasks = this.mStack.getStackTasks();
        int taskCount = tasks.size();
        int i = 0;
        while (i < taskCount) {
            Task task = (Task) tasks.get(i);
            TaskKey taskKey = task.key;
            boolean isRunningTask = task.key.id == opts.runningTaskId;
            boolean isVisibleTask = i >= taskCount - opts.numVisibleTasks;
            boolean isVisibleThumbnail = i >= taskCount - opts.numVisibleTaskThumbnails;
            if (!opts.onlyLoadPausedActivities || !isRunningTask) {
                if (opts.loadIcons && ((isRunningTask || isVisibleTask) && task.icon == null)) {
                    task.icon = loader.getAndUpdateActivityIcon(taskKey, task.taskDescription, res, true);
                }
                if (opts.loadThumbnails && ((isRunningTask || isVisibleThumbnail) && (task.thumbnail == null || isRunningTask))) {
                    if (config.svelteLevel <= 1) {
                        task.thumbnail = loader.getAndUpdateThumbnail(taskKey, true);
                    } else if (config.svelteLevel == 2) {
                        loadQueue.addTask(task);
                    }
                }
            }
            i++;
        }
    }

    public TaskStack getTaskStack() {
        return this.mStack;
    }

    public boolean hasTasks() {
        boolean z = false;
        if (this.mStack == null) {
            return false;
        }
        if (this.mStack.getTaskCount() > 0) {
            z = true;
        }
        return z;
    }

    private boolean isHistoricalTask(RecentTaskInfo t) {
        return t.lastActiveTime < System.currentTimeMillis() - ((long) SESSION_BEGIN_TIME);
    }

    private boolean shouldSkipLoadTask(Map<String, Boolean> lockMap, RecentTaskInfo recentInfo, Task task) {
        if (!HwRecentTaskRemove.getInstance(this.mContext).willRemovedTask(recentInfo)) {
            return false;
        }
        if (lockMap.get(task.packageName) == null ? false : ((Boolean) lockMap.get(task.packageName)).booleanValue()) {
            Log.d("RecentsTaskLoadPlan", task.packageName + "is locked, so need load it");
            return false;
        } else if (HwRecentsHelper.getPlayingMusicUid(this.mContext, task)) {
            Log.d("RecentsTaskLoadPlan", task.packageName + "is music, so need load it");
            return false;
        } else {
            HwLog.i("RecentsTaskLoadPlan", "in removing, will remove task: " + task.packageName);
            return true;
        }
    }
}
