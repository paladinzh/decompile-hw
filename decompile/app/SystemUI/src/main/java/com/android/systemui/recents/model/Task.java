package com.android.systemui.recents.model;

import android.app.ActivityManager.TaskDescription;
import android.app.ActivityManager.TaskThumbnailInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.ViewDebug.ExportedProperty;
import com.android.systemui.recents.HwRecentsLockUtils;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;

public class Task {
    @ExportedProperty(category = "recents")
    public int affiliationColor;
    @ExportedProperty(category = "recents")
    public int affiliationTaskId;
    @ExportedProperty(category = "recents")
    public String appInfoDescription;
    @ExportedProperty(category = "recents")
    public Rect bounds;
    @ExportedProperty(category = "recents")
    public int colorBackground;
    @ExportedProperty(category = "recents")
    public int colorPrimary;
    @ExportedProperty(category = "recents")
    public String dismissDescription;
    @ExportedProperty(deepExport = true, prefix = "group_")
    public TaskGrouping group;
    public Drawable icon;
    @ExportedProperty(category = "recents")
    public boolean isDockable;
    @ExportedProperty(category = "recents")
    public boolean isLaunchTarget;
    @ExportedProperty(category = "recents")
    public boolean isLocked;
    @ExportedProperty(category = "recents")
    public boolean isStackTask;
    @ExportedProperty(category = "recents")
    public boolean isSystemApp;
    @ExportedProperty(deepExport = true, prefix = "key_")
    public TaskKey key;
    @ExportedProperty(category = "recents")
    public String lockDescription;
    private ArrayList<TaskCallbacks> mCallbacks = new ArrayList();
    @ExportedProperty(category = "recents")
    public String packageName;
    @ExportedProperty(category = "recents")
    public int resizeMode;
    public TaskDescription taskDescription;
    public int temporarySortIndexInStack;
    public Bitmap thumbnail;
    @ExportedProperty(category = "recents")
    public String title;
    @ExportedProperty(category = "recents")
    public String titleDescription;
    @ExportedProperty(category = "recents")
    public ComponentName topActivity;
    @ExportedProperty(category = "recents")
    public boolean useLightOnPrimaryColor;

    public interface TaskCallbacks {
        void onTaskDataLoaded(Task task, TaskThumbnailInfo taskThumbnailInfo);

        void onTaskDataUnloaded();

        void onTaskStackIdChanged();
    }

    public static class TaskKey {
        @ExportedProperty(category = "recents")
        public final Intent baseIntent;
        @ExportedProperty(category = "recents")
        public long firstActiveTime;
        @ExportedProperty(category = "recents")
        public final int id;
        @ExportedProperty(category = "recents")
        public long lastActiveTime;
        private int mHashCode;
        @ExportedProperty(category = "recents")
        public int stackId;
        @ExportedProperty(category = "recents")
        public final int userId;

        public TaskKey(int id, int stackId, Intent intent, int userId, long firstActiveTime, long lastActiveTime) {
            this.id = id;
            this.stackId = stackId;
            this.baseIntent = intent;
            this.userId = userId;
            this.firstActiveTime = firstActiveTime;
            this.lastActiveTime = lastActiveTime;
            updateHashCode();
        }

        public void setStackId(int stackId) {
            this.stackId = stackId;
            updateHashCode();
        }

        public ComponentName getComponent() {
            return this.baseIntent.getComponent();
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof TaskKey)) {
                return false;
            }
            TaskKey otherKey = (TaskKey) o;
            if (this.id == otherKey.id && this.stackId == otherKey.stackId && this.userId == otherKey.userId) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return this.mHashCode;
        }

        public String toString() {
            return "id=" + this.id + " stackId=" + this.stackId + " user=" + this.userId + " lastActiveTime=" + this.lastActiveTime;
        }

        private void updateHashCode() {
            this.mHashCode = Objects.hash(new Object[]{Integer.valueOf(this.id), Integer.valueOf(this.stackId), Integer.valueOf(this.userId)});
        }
    }

    public Task(TaskKey key, int affiliationTaskId, int affiliationColor, Drawable icon, Bitmap thumbnail, String title, String titleDescription, String dismissDescription, String appInfoDescription, String lockDescription, int colorPrimary, int colorBackground, boolean isLaunchTarget, boolean isStackTask, boolean isSystemApp, boolean isDockable, Rect bounds, TaskDescription taskDescription, int resizeMode, ComponentName topActivity, String packageName) {
        boolean hasAffiliationGroupColor = (affiliationTaskId != key.id) && affiliationColor != 0;
        this.key = key;
        this.affiliationTaskId = affiliationTaskId;
        this.affiliationColor = affiliationColor;
        this.icon = icon;
        this.thumbnail = thumbnail;
        this.title = title;
        this.titleDescription = titleDescription;
        this.dismissDescription = dismissDescription;
        this.lockDescription = lockDescription;
        this.appInfoDescription = appInfoDescription;
        if (!hasAffiliationGroupColor) {
            affiliationColor = colorPrimary;
        }
        this.colorPrimary = affiliationColor;
        this.colorBackground = colorBackground;
        this.useLightOnPrimaryColor = Utilities.computeContrastBetweenColors(this.colorPrimary, -1) > 3.0f;
        this.bounds = bounds;
        this.taskDescription = taskDescription;
        this.isLaunchTarget = isLaunchTarget;
        this.isStackTask = isStackTask;
        this.isSystemApp = isSystemApp;
        this.isDockable = isDockable;
        this.resizeMode = resizeMode;
        this.topActivity = topActivity;
        this.packageName = packageName;
        this.isLocked = HwRecentsLockUtils.isLocked(packageName, false);
    }

    public void copyFrom(Task o) {
        this.key = o.key;
        this.group = o.group;
        this.affiliationTaskId = o.affiliationTaskId;
        this.affiliationColor = o.affiliationColor;
        this.icon = o.icon;
        this.thumbnail = o.thumbnail;
        this.title = o.title;
        this.titleDescription = o.titleDescription;
        this.dismissDescription = o.dismissDescription;
        this.lockDescription = o.lockDescription;
        this.appInfoDescription = o.appInfoDescription;
        this.colorPrimary = o.colorPrimary;
        this.colorBackground = o.colorBackground;
        this.useLightOnPrimaryColor = o.useLightOnPrimaryColor;
        this.bounds = o.bounds;
        this.taskDescription = o.taskDescription;
        this.isLaunchTarget = o.isLaunchTarget;
        this.isStackTask = o.isStackTask;
        this.isSystemApp = o.isSystemApp;
        this.isDockable = o.isDockable;
        this.resizeMode = o.resizeMode;
        this.topActivity = o.topActivity;
        this.isLocked = o.isLocked;
        this.packageName = o.packageName;
    }

    public void addCallback(TaskCallbacks cb) {
        if (!this.mCallbacks.contains(cb)) {
            this.mCallbacks.add(cb);
        }
    }

    public void removeCallback(TaskCallbacks cb) {
        this.mCallbacks.remove(cb);
    }

    public void setGroup(TaskGrouping group) {
        this.group = group;
    }

    public void setStackId(int stackId) {
        this.key.setStackId(stackId);
        int callbackCount = this.mCallbacks.size();
        for (int i = 0; i < callbackCount; i++) {
            ((TaskCallbacks) this.mCallbacks.get(i)).onTaskStackIdChanged();
        }
    }

    public boolean isFreeformTask() {
        return Recents.getSystemServices().hasFreeformWorkspaceSupport() ? SystemServicesProxy.isFreeformStack(this.key.stackId) : false;
    }

    public void notifyTaskDataLoaded(Bitmap thumbnail, Drawable applicationIcon, TaskThumbnailInfo thumbnailInfo) {
        this.icon = applicationIcon;
        this.thumbnail = thumbnail;
        int callbackCount = this.mCallbacks.size();
        for (int i = 0; i < callbackCount; i++) {
            ((TaskCallbacks) this.mCallbacks.get(i)).onTaskDataLoaded(this, thumbnailInfo);
        }
    }

    public void notifyTaskDataUnloaded(Bitmap defaultThumbnail, Drawable defaultApplicationIcon) {
        this.icon = defaultApplicationIcon;
        this.thumbnail = defaultThumbnail;
        for (int i = this.mCallbacks.size() - 1; i >= 0; i--) {
            ((TaskCallbacks) this.mCallbacks.get(i)).onTaskDataUnloaded();
        }
    }

    public boolean isAffiliatedTask() {
        return this.key.id != this.affiliationTaskId;
    }

    public ComponentName getTopComponent() {
        if (this.topActivity != null) {
            return this.topActivity;
        }
        return this.key.baseIntent.getComponent();
    }

    public boolean equals(Object o) {
        return this.key.equals(((Task) o).key);
    }

    public String toString() {
        return "[" + this.key.toString() + "] " + this.title;
    }

    public void dump(String prefix, PrintWriter writer) {
        writer.print(prefix);
        writer.print(this.key);
        if (isAffiliatedTask()) {
            writer.print(" ");
            writer.print("affTaskId=" + this.affiliationTaskId);
        }
        if (!this.isDockable) {
            writer.print(" dockable=N");
        }
        if (this.isLaunchTarget) {
            writer.print(" launchTarget=Y");
        }
        if (isFreeformTask()) {
            writer.print(" freeform=Y");
        }
        writer.print(" ");
        writer.print(this.title);
        writer.println();
    }
}
