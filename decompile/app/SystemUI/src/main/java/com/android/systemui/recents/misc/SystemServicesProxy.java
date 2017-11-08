package com.android.systemui.recents.misc;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager.StackInfo;
import android.app.ActivityManager.TaskDescription;
import android.app.ActivityManager.TaskThumbnail;
import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.ITaskStackListener.Stub;
import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.ArraySet;
import android.util.Log;
import android.util.MutableBoolean;
import android.view.Display;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.IDockedStackListener;
import android.view.WindowManager;
import android.view.WindowManager.KeyboardShortcutsReceiver;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.app.AssistUtils;
import com.android.internal.os.BackgroundThread;
import com.android.systemui.R;
import com.android.systemui.recents.model.Task.TaskKey;
import com.android.systemui.recents.model.ThumbnailData;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;
import com.android.systemui.utils.analyze.PerfDebugUtils;
import com.android.systemui.utils.badgedicon.BadgedIconHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SystemServicesProxy {
    static final Options sBitmapOptions = new Options();
    static final List<String> sRecentsBlacklist = new ArrayList();
    private static SystemServicesProxy sSystemServicesProxy;
    AccessibilityManager mAccm;
    ActivityManager mAm;
    ComponentName mAssistComponent;
    AssistUtils mAssistUtils;
    Canvas mBgProtectionCanvas;
    Paint mBgProtectionPaint;
    Display mDisplay;
    int mDummyThumbnailHeight;
    int mDummyThumbnailWidth;
    private final Handler mHandler = new H();
    boolean mHasFreeformWorkspaceSupport;
    IActivityManager mIam;
    IPackageManager mIpm;
    boolean mIsSafeMode;
    PackageManager mPm;
    String mRecentsPackage;
    Resources mRes;
    private Stub mTaskStackListener = new Stub() {
        public void onTaskStackChanged() throws RemoteException {
            SystemServicesProxy.this.mHandler.removeMessages(1);
            SystemServicesProxy.this.mHandler.sendEmptyMessage(1);
        }

        public void onActivityPinned() throws RemoteException {
            SystemServicesProxy.this.mHandler.removeMessages(2);
            SystemServicesProxy.this.mHandler.sendEmptyMessage(2);
        }

        public void onPinnedActivityRestartAttempt() throws RemoteException {
            SystemServicesProxy.this.mHandler.removeMessages(3);
            SystemServicesProxy.this.mHandler.sendEmptyMessage(3);
        }

        public void onPinnedStackAnimationEnded() throws RemoteException {
            SystemServicesProxy.this.mHandler.removeMessages(4);
            SystemServicesProxy.this.mHandler.sendEmptyMessage(4);
        }

        public void onActivityForcedResizable(String packageName, int taskId) throws RemoteException {
            SystemServicesProxy.this.mHandler.obtainMessage(5, taskId, 0, packageName).sendToTarget();
        }

        public void onActivityDismissingDockedStack() throws RemoteException {
            SystemServicesProxy.this.mHandler.sendEmptyMessage(6);
        }
    };
    private List<TaskStackListener> mTaskStackListeners = new ArrayList();
    UserManager mUm;
    WindowManager mWm;

    public static abstract class TaskStackListener {
        public void onTaskStackChanged() {
        }

        public void onActivityPinned() {
        }

        public void onPinnedActivityRestartAttempt() {
        }

        public void onPinnedStackAnimationEnded() {
        }

        public void onActivityForcedResizable(String packageName, int taskId) {
        }

        public void onActivityDismissingDockedStack() {
        }
    }

    private final class H extends Handler {
        private H() {
        }

        public void handleMessage(Message msg) {
            int i;
            switch (msg.what) {
                case 1:
                    for (i = SystemServicesProxy.this.mTaskStackListeners.size() - 1; i >= 0; i--) {
                        ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(i)).onTaskStackChanged();
                    }
                    return;
                case 2:
                    for (i = SystemServicesProxy.this.mTaskStackListeners.size() - 1; i >= 0; i--) {
                        ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(i)).onActivityPinned();
                    }
                    return;
                case 3:
                    for (i = SystemServicesProxy.this.mTaskStackListeners.size() - 1; i >= 0; i--) {
                        ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(i)).onPinnedActivityRestartAttempt();
                    }
                    return;
                case 4:
                    for (i = SystemServicesProxy.this.mTaskStackListeners.size() - 1; i >= 0; i--) {
                        ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(i)).onPinnedStackAnimationEnded();
                    }
                    return;
                case 5:
                    for (i = SystemServicesProxy.this.mTaskStackListeners.size() - 1; i >= 0; i--) {
                        ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(i)).onActivityForcedResizable((String) msg.obj, msg.arg1);
                    }
                    return;
                case 6:
                    for (i = SystemServicesProxy.this.mTaskStackListeners.size() - 1; i >= 0; i--) {
                        ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(i)).onActivityDismissingDockedStack();
                    }
                    return;
                default:
                    return;
            }
        }
    }

    static {
        sBitmapOptions.inMutable = true;
        sBitmapOptions.inPreferredConfig = Config.RGB_565;
    }

    private SystemServicesProxy(Context context) {
        boolean z;
        this.mAccm = AccessibilityManager.getInstance(context);
        this.mAm = (ActivityManager) context.getSystemService("activity");
        this.mIam = ActivityManagerNative.getDefault();
        this.mPm = context.getPackageManager();
        this.mIpm = AppGlobals.getPackageManager();
        this.mAssistUtils = new AssistUtils(context);
        this.mWm = (WindowManager) context.getSystemService("window");
        this.mUm = UserManager.get(context);
        this.mDisplay = this.mWm.getDefaultDisplay();
        this.mRecentsPackage = context.getPackageName();
        if (this.mPm.hasSystemFeature("android.software.freeform_window_management")) {
            z = true;
        } else if (Global.getInt(context.getContentResolver(), "enable_freeform_support", 0) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mHasFreeformWorkspaceSupport = z;
        this.mIsSafeMode = this.mPm.isSafeMode();
        this.mRes = context.getResources();
        this.mDummyThumbnailWidth = this.mRes.getDimensionPixelSize(17104898);
        this.mDummyThumbnailHeight = this.mRes.getDimensionPixelSize(17104897);
        this.mBgProtectionPaint = new Paint();
        this.mBgProtectionPaint.setXfermode(new PorterDuffXfermode(Mode.DST_ATOP));
        this.mBgProtectionPaint.setColor(-1);
        this.mBgProtectionCanvas = new Canvas();
        this.mAssistComponent = this.mAssistUtils.getAssistComponentForUser(UserHandle.myUserId());
        if (((UiModeManager) context.getSystemService("uimode")).getCurrentModeType() == 4) {
            Collections.addAll(sRecentsBlacklist, this.mRes.getStringArray(R.array.recents_tv_blacklist_array));
        }
    }

    public static SystemServicesProxy getInstance(Context context) {
        if (Looper.getMainLooper().isCurrentThread()) {
            if (sSystemServicesProxy == null) {
                sSystemServicesProxy = new SystemServicesProxy(context);
            }
            return sSystemServicesProxy;
        }
        throw new RuntimeException("Must be called on the UI thread");
    }

    public List<RecentTaskInfo> getRecentTasks(int numLatestTasks, int userId, boolean includeFrontMostExcludedTask, ArraySet<Integer> quietProfileIds) {
        if (this.mAm == null) {
            return null;
        }
        int numTasksToQuery = Math.max(10, numLatestTasks);
        int flags = 62;
        if (includeFrontMostExcludedTask) {
            flags = 63;
        }
        List list = null;
        try {
            list = this.mAm.getRecentTasksForUser(numTasksToQuery, flags, userId);
            HwLog.i("SystemServicesProxy", "get the task from ActivityManager size is " + (list == null ? null : Integer.valueOf(list.size())) + " AMS:getRecentTasksForUser,args:numTasksToQuery=" + numTasksToQuery + ",flags=" + flags + ",userId=" + userId);
        } catch (Exception e) {
            Log.e("SystemServicesProxy", "Failed to get recent tasks", e);
        }
        if (list == null) {
            return new ArrayList();
        }
        Iterator<RecentTaskInfo> iter = list.iterator();
        while (iter.hasNext()) {
            RecentTaskInfo t = (RecentTaskInfo) iter.next();
            if (t.realActivity == null || sRecentsBlacklist.contains(t.realActivity.getClassName()) || sRecentsBlacklist.contains(t.realActivity.getPackageName())) {
                iter.remove();
                HwLog.i("SystemServicesProxy", "t.realActivity is null or is in recents black list,ignore!");
            } else {
                if ((((t.baseIntent.getFlags() & 8388608) == 8388608) | quietProfileIds.contains(Integer.valueOf(t.userId))) && !(false && includeFrontMostExcludedTask)) {
                    iter.remove();
                    HwLog.i("SystemServicesProxy", t.realActivity.getClassName() + " is excluded,ignore!");
                }
            }
        }
        return list.subList(0, Math.min(list.size(), numLatestTasks));
    }

    public RunningTaskInfo getRunningTask() {
        List<RunningTaskInfo> tasks = this.mAm.getRunningTasks(1);
        if (tasks == null || tasks.isEmpty()) {
            return null;
        }
        return (RunningTaskInfo) tasks.get(0);
    }

    public boolean isRecentsActivityVisible() {
        return isRecentsActivityVisible(null);
    }

    public boolean isRecentsActivityVisible(MutableBoolean isHomeStackVisible) {
        boolean z = true;
        if (this.mIam == null) {
            return false;
        }
        try {
            StackInfo stackInfo = this.mIam.getStackInfo(0);
            if (stackInfo == null) {
                return false;
            }
            StackInfo fullscreenStackInfo = this.mIam.getStackInfo(1);
            ComponentName topActivity = stackInfo.topActivity;
            boolean homeStackVisibleNotOccluded = stackInfo.visible;
            if (fullscreenStackInfo != null) {
                int i;
                boolean isFullscreenStackOccludingHome = fullscreenStackInfo.visible ? fullscreenStackInfo.position > stackInfo.position : false;
                if (isFullscreenStackOccludingHome) {
                    i = 0;
                } else {
                    i = 1;
                }
                homeStackVisibleNotOccluded &= i;
            }
            if (isHomeStackVisible != null) {
                isHomeStackVisible.value = homeStackVisibleNotOccluded;
            }
            if (!homeStackVisibleNotOccluded || topActivity == null || !topActivity.getPackageName().equals("com.android.systemui")) {
                z = false;
            } else if (!topActivity.getClassName().equals("com.android.systemui.recents.RecentsActivity")) {
                z = topActivity.getClassName().equals("com.android.systemui.recents.tv.RecentsTvActivity");
            }
            return z;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasFreeformWorkspaceSupport() {
        return this.mHasFreeformWorkspaceSupport;
    }

    public boolean isInSafeMode() {
        return this.mIsSafeMode;
    }

    public boolean startTaskInDockedMode(int taskId, int createMode) {
        if (this.mIam == null) {
            return false;
        }
        try {
            ActivityOptions options = ActivityOptions.makeBasic();
            options.setDockCreateMode(createMode);
            options.setLaunchStackId(3);
            this.mIam.startActivityFromRecents(taskId, options.toBundle());
            return true;
        } catch (Exception e) {
            Log.e("SystemServicesProxy", "Failed to dock task: " + taskId + " with createMode: " + createMode, e);
            return false;
        }
    }

    public boolean moveTaskToDockedStack(int taskId, int createMode, Rect initialBounds) {
        if (this.mIam == null) {
            return false;
        }
        try {
            return this.mIam.moveTaskToDockedStack(taskId, createMode, true, false, initialBounds, true);
        } catch (RemoteException e) {
            HwLog.e("SystemServicesProxy", "moveTaskToDockedStack::RemoteException=" + e);
            return false;
        } catch (IllegalStateException e2) {
            HwLog.e("SystemServicesProxy", "moveTaskToDockedStack::IllegalStateException=" + e2);
            return false;
        } catch (Exception e3) {
            HwLog.e("SystemServicesProxy", "moveTaskToDockedStack::Exception=" + e3);
            return false;
        }
    }

    public static boolean isHomeStack(int stackId) {
        return stackId == 0;
    }

    public static boolean isFreeformStack(int stackId) {
        return stackId == 2;
    }

    public boolean hasDockedTask() {
        if (this.mIam == null) {
            return false;
        }
        StackInfo stackInfo = null;
        try {
            stackInfo = this.mIam.getStackInfo(3);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (stackInfo == null) {
            return false;
        }
        int userId = getCurrentUser();
        boolean hasUserTask = false;
        for (int i = stackInfo.taskUserIds.length - 1; i >= 0 && !hasUserTask; i--) {
            hasUserTask = stackInfo.taskUserIds[i] == userId;
        }
        return hasUserTask;
    }

    public boolean hasSoftNavigationBar() {
        try {
            return WindowManagerGlobal.getWindowManagerService().hasNavigationBar();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasTransposedNavigationBar() {
        Rect insets = new Rect();
        getStableInsets(insets);
        if (insets.right > 0) {
            return true;
        }
        return false;
    }

    public void cancelWindowTransition(int taskId) {
        if (this.mWm != null) {
            try {
                WindowManagerGlobal.getWindowManagerService().cancelTaskWindowTransition(taskId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void cancelThumbnailTransition(int taskId) {
        if (this.mWm != null) {
            try {
                WindowManagerGlobal.getWindowManagerService().cancelTaskThumbnailTransition(taskId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public ThumbnailData getTaskThumbnail(int taskId) {
        if (this.mAm == null) {
            return null;
        }
        ThumbnailData thumbnailData = new ThumbnailData();
        getThumbnail(taskId, thumbnailData);
        if (thumbnailData.thumbnail != null) {
            thumbnailData.thumbnail.setHasAlpha(false);
            if (Color.alpha(thumbnailData.thumbnail.getPixel(0, 0)) == 0) {
                this.mBgProtectionCanvas.setBitmap(thumbnailData.thumbnail);
                this.mBgProtectionCanvas.drawRect(0.0f, 0.0f, (float) thumbnailData.thumbnail.getWidth(), (float) thumbnailData.thumbnail.getHeight(), this.mBgProtectionPaint);
                this.mBgProtectionCanvas.setBitmap(null);
                Log.e("SystemServicesProxy", "Invalid screenshot detected from getTaskThumbnail()");
            }
        }
        return thumbnailData;
    }

    public void getThumbnail(int taskId, ThumbnailData thumbnailDataOut) {
        if (this.mAm != null) {
            TaskThumbnail taskThumbnail = this.mAm.getTaskThumbnail(taskId);
            if (taskThumbnail != null) {
                Bitmap thumbnail = taskThumbnail.mainThumbnail;
                ParcelFileDescriptor descriptor = taskThumbnail.thumbnailFileDescriptor;
                if (thumbnail == null && descriptor != null) {
                    thumbnail = BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor(), null, sBitmapOptions);
                }
                if (descriptor != null) {
                    try {
                        descriptor.close();
                    } catch (IOException e) {
                    }
                }
                thumbnailDataOut.thumbnail = thumbnail;
                thumbnailDataOut.thumbnailInfo = taskThumbnail.thumbnailInfo;
            }
        }
    }

    public void moveTaskToStack(int taskId, int stackId) {
        if (this.mIam != null) {
            try {
                this.mIam.positionTaskInStack(taskId, stackId, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeTask(final int taskId) {
        if (this.mAm != null) {
            BackgroundThread.getHandler().post(new Runnable() {
                public void run() {
                    SystemServicesProxy.this.mAm.removeTask(taskId);
                }
            });
        }
    }

    public void sendCloseSystemWindows(final String reason) {
        new Thread() {
            public void run() {
                PerfDebugUtils.beginSystraceSection("SystemServicesProxy.sendCloseSystemWindows");
                if (ActivityManagerNative.isSystemReady()) {
                    try {
                        SystemServicesProxy.this.mIam.closeSystemDialogs(reason);
                    } catch (RemoteException e) {
                    }
                }
                PerfDebugUtils.endSystraceSection();
            }
        }.start();
    }

    public ActivityInfo getActivityInfo(ComponentName cn, int userId) {
        if (this.mIpm == null) {
            return null;
        }
        try {
            return this.mIpm.getActivityInfo(cn, 128, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getBadgedActivityLabel(ActivityInfo info, int userId) {
        if (this.mPm == null) {
            return null;
        }
        return getBadgedLabel(info.loadLabel(this.mPm).toString(), userId);
    }

    public String getBadgedContentDescription(ActivityInfo info, int userId, Resources res) {
        String activityLabel = info.loadLabel(this.mPm).toString();
        String applicationLabel = info.applicationInfo.loadLabel(this.mPm).toString();
        String badgedApplicationLabel = getBadgedLabel(applicationLabel, userId);
        if (applicationLabel.equals(activityLabel)) {
            return badgedApplicationLabel;
        }
        return res.getString(R.string.accessibility_recents_task_header, new Object[]{badgedApplicationLabel, activityLabel});
    }

    public Drawable getBadgedActivityIcon(ActivityInfo info, int userId, int badgedIconType) {
        if (this.mPm == null) {
            return null;
        }
        return getBadgedIcon(info.loadIcon(this.mPm), userId, badgedIconType);
    }

    public Drawable getBadgedTaskDescriptionIcon(TaskDescription taskDescription, int userId, Resources res, int badgedIconType) {
        Bitmap tdIcon = taskDescription.getInMemoryIcon();
        if (tdIcon == null) {
            try {
                tdIcon = TaskDescription.loadTaskDescriptionIcon(taskDescription.getIconFilename(), userId);
            } catch (Exception e) {
                Log.e("SystemServicesProxy", "loadTaskDescriptionIcon exception!");
            }
        }
        if (tdIcon != null) {
            return getBadgedIcon(new BitmapDrawable(res, tdIcon), userId, badgedIconType);
        }
        return null;
    }

    private Drawable getBadgedIcon(Drawable icon, int userId, int badgedIconType) {
        if (userId != UserHandle.myUserId()) {
            return this.mPm.getUserBadgedIcon(icon, new UserHandle(userId));
        }
        if (badgedIconType == 1) {
            return this.mPm.getUserBadgedIcon(icon, new UserHandle(2147383647));
        }
        if (badgedIconType == 2) {
            return BadgedIconHelper.getTrustSpaceBadgedDrawable(this.mRes, icon);
        }
        return icon;
    }

    private String getBadgedLabel(String label, int userId) {
        if (userId != UserHandle.myUserId()) {
            return this.mPm.getUserBadgedLabel(label, new UserHandle(userId)).toString();
        }
        return label;
    }

    public boolean isSystemUser(int userId) {
        return userId == 0;
    }

    public int getCurrentUser() {
        if (this.mAm == null) {
            return 0;
        }
        return UserSwitchUtils.getCurrentUser();
    }

    public int getProcessUser() {
        if (this.mUm == null) {
            return 0;
        }
        return this.mUm.getUserHandle();
    }

    public boolean isTouchExplorationEnabled() {
        boolean z = false;
        if (this.mAccm == null) {
            return false;
        }
        if (this.mAccm.isEnabled()) {
            z = this.mAccm.isTouchExplorationEnabled();
        }
        return z;
    }

    public boolean isScreenPinningActive() {
        if (this.mIam == null) {
            return false;
        }
        try {
            return this.mIam.isInLockTaskMode();
        } catch (RemoteException e) {
            return false;
        }
    }

    public int getSystemSetting(Context context, String setting) {
        return System.getInt(context.getContentResolver(), setting, 0);
    }

    public int getDeviceSmallestWidth() {
        if (this.mDisplay == null) {
            return 0;
        }
        Point smallestSizeRange = new Point();
        this.mDisplay.getCurrentSizeRange(smallestSizeRange, new Point());
        return smallestSizeRange.x;
    }

    public Rect getDisplayRect() {
        Rect displayRect = new Rect();
        if (this.mDisplay == null) {
            return displayRect;
        }
        Point p = new Point();
        this.mDisplay.getRealSize(p);
        displayRect.set(0, 0, p.x, p.y);
        return displayRect;
    }

    public Rect getWindowRect() {
        Rect windowRect = new Rect();
        if (this.mIam == null) {
            return windowRect;
        }
        try {
            StackInfo stackInfo = this.mIam.getStackInfo(0);
            if (stackInfo != null) {
                windowRect.set(stackInfo.bounds);
            }
            return windowRect;
        } catch (RemoteException e) {
            e.printStackTrace();
            return windowRect;
        } catch (Throwable th) {
            return windowRect;
        }
    }

    public boolean startActivityFromRecents(Context context, TaskKey taskKey, String taskName, ActivityOptions options) {
        Bundle bundle = null;
        if (this.mIam != null) {
            HwLog.i("SystemServicesProxy", "startActivityFromRecents: " + taskName);
            try {
                if (taskKey.stackId == 3) {
                    if (options == null) {
                        options = ActivityOptions.makeBasic();
                    }
                    options.setLaunchStackId(1);
                }
                long startTime = System.currentTimeMillis();
                IActivityManager iActivityManager = this.mIam;
                int i = taskKey.id;
                if (options != null) {
                    bundle = options.toBundle();
                }
                iActivityManager.startActivityFromRecents(i, bundle);
                PerfDebugUtils.keyOperationTimeConsumed("startActivityFromRecents", startTime);
                return true;
            } catch (Exception e) {
                Log.e("SystemServicesProxy", context.getString(R.string.recents_launch_error_message, new Object[]{taskName}), e);
            }
        }
        return false;
    }

    public void startInPlaceAnimationOnFrontMostApplication(ActivityOptions opts) {
        if (this.mIam != null) {
            try {
                this.mIam.startInPlaceAnimationOnFrontMostApplication(opts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerTaskStackListener(TaskStackListener listener) {
        if (this.mIam != null) {
            this.mTaskStackListeners.add(listener);
            if (this.mTaskStackListeners.size() == 1) {
                try {
                    this.mIam.registerTaskStackListener(this.mTaskStackListener);
                } catch (Exception e) {
                    Log.w("SystemServicesProxy", "Failed to call registerTaskStackListener", e);
                }
            }
        }
    }

    public void endProlongedAnimations() {
        if (this.mWm != null) {
            try {
                WindowManagerGlobal.getWindowManagerService().endProlongedAnimations();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerDockedStackListener(IDockedStackListener listener) {
        if (this.mWm != null) {
            try {
                WindowManagerGlobal.getWindowManagerService().registerDockedStackListener(listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getDockedDividerSize(Context context) {
        Resources res = context.getResources();
        return res.getDimensionPixelSize(17104929) - (res.getDimensionPixelSize(17104930) * 2);
    }

    public void requestKeyboardShortcuts(Context context, KeyboardShortcutsReceiver receiver, int deviceId) {
        this.mWm.requestAppKeyboardShortcuts(receiver, deviceId);
    }

    public void getStableInsets(Rect outStableInsets) {
        if (this.mWm != null) {
            try {
                WindowManagerGlobal.getWindowManagerService().getStableInsets(outStableInsets);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture future, IRemoteCallback animStartedListener, boolean scaleUp) {
        try {
            WindowManagerGlobal.getWindowManagerService().overridePendingAppTransitionMultiThumbFuture(future, animStartedListener, scaleUp);
        } catch (RemoteException e) {
            Log.w("SystemServicesProxy", "Failed to override transition: " + e);
        }
    }

    public boolean isCurrentHomeActivity() {
        ActivityInfo homeInfo = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").resolveActivityInfo(this.mPm, 0);
        RunningTaskInfo topTask = getRunningTask();
        if (topTask == null || homeInfo == null) {
            return false;
        }
        return homeInfo.packageName.equals(topTask.topActivity.getPackageName());
    }
}
