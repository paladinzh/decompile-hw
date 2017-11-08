package com.android.systemui.recents.model;

import android.app.ActivityManager;
import android.app.ActivityManager.TaskDescription;
import android.app.ActivityManager.TaskThumbnailInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.LruCache;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.events.activity.PackagesChangedEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.RecentsTaskLoadPlan.Options;
import com.android.systemui.recents.model.Task.TaskKey;
import com.android.systemui.recents.model.TaskKeyLruCache.EvictionCallback;
import com.android.systemui.utils.badgedicon.BadgedIconHelper;
import fyusion.vislib.BuildConfig;
import java.io.PrintWriter;

public class RecentsTaskLoader {
    private final LruCache<ComponentName, ActivityInfo> mActivityInfoCache;
    private final TaskKeyLruCache<String> mActivityLabelCache;
    private EvictionCallback mClearActivityInfoOnEviction = new EvictionCallback() {
        public void onEntryEvicted(TaskKey key) {
            if (key != null) {
                RecentsTaskLoader.this.mActivityInfoCache.remove(key.getComponent());
            }
        }
    };
    private final TaskKeyLruCache<String> mContentDescriptionCache;
    BitmapDrawable mDefaultIcon;
    int mDefaultTaskBarBackgroundColor;
    int mDefaultTaskViewBackgroundColor;
    Bitmap mDefaultThumbnail;
    private final TaskKeyLruCache<Drawable> mIconCache;
    private final TaskResourceLoadQueue mLoadQueue;
    private final BackgroundTaskLoader mLoader;
    private final int mMaxIconCacheSize;
    private final int mMaxThumbnailCacheSize;
    private int mNumVisibleTasksLoaded;
    private int mNumVisibleThumbnailsLoaded;
    private final TaskKeyLruCache<ThumbnailData> mThumbnailCache;

    public RecentsTaskLoader(Context context) {
        Resources res = context.getResources();
        this.mDefaultTaskBarBackgroundColor = context.getColor(R.color.recents_task_bar_default_background_color);
        this.mDefaultTaskViewBackgroundColor = context.getColor(R.color.recents_task_view_default_background_color);
        this.mMaxThumbnailCacheSize = res.getInteger(R.integer.config_recents_max_thumbnail_count);
        this.mMaxIconCacheSize = res.getInteger(R.integer.config_recents_max_icon_count);
        int iconCacheSize = this.mMaxIconCacheSize;
        int thumbnailCacheSize = this.mMaxThumbnailCacheSize;
        Bitmap icon = Bitmap.createBitmap(1, 1, Config.ALPHA_8);
        icon.eraseColor(0);
        this.mDefaultThumbnail = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
        this.mDefaultThumbnail.setHasAlpha(false);
        this.mDefaultThumbnail.eraseColor(-1);
        this.mDefaultIcon = new BitmapDrawable(context.getResources(), icon);
        int numRecentTasks = ActivityManager.getMaxRecentTasksStatic();
        this.mLoadQueue = new TaskResourceLoadQueue();
        this.mIconCache = new TaskKeyLruCache(iconCacheSize, this.mClearActivityInfoOnEviction);
        this.mThumbnailCache = new TaskKeyLruCache(thumbnailCacheSize);
        this.mActivityLabelCache = new TaskKeyLruCache(numRecentTasks, this.mClearActivityInfoOnEviction);
        this.mContentDescriptionCache = new TaskKeyLruCache(numRecentTasks, this.mClearActivityInfoOnEviction);
        this.mActivityInfoCache = new LruCache(numRecentTasks);
        this.mLoader = new BackgroundTaskLoader(this.mLoadQueue, this.mIconCache, this.mThumbnailCache, this.mDefaultThumbnail, this.mDefaultIcon);
    }

    public int getIconCacheSize() {
        return this.mMaxIconCacheSize;
    }

    public int getThumbnailCacheSize() {
        return this.mMaxThumbnailCacheSize;
    }

    public RecentsTaskLoadPlan createLoadPlan(Context context) {
        return new RecentsTaskLoadPlan(context);
    }

    public void preloadTasks(RecentsTaskLoadPlan plan, int runningTaskId, boolean includeFrontMostExcludedTask) {
        plan.preloadPlan(this, runningTaskId, includeFrontMostExcludedTask);
    }

    public void loadTasks(Context context, RecentsTaskLoadPlan plan, Options opts) {
        if (opts == null) {
            throw new RuntimeException("Requires load options");
        }
        plan.executePlan(opts, this, this.mLoadQueue);
        if (!opts.onlyLoadForCache) {
            this.mNumVisibleTasksLoaded = opts.numVisibleTasks;
            this.mNumVisibleThumbnailsLoaded = opts.numVisibleTaskThumbnails;
            this.mLoader.start(context);
        }
    }

    public void loadTaskData(Task t) {
        Drawable icon = (Drawable) this.mIconCache.getAndInvalidateIfModified(t.key);
        Bitmap bitmap = null;
        TaskThumbnailInfo thumbnailInfo = null;
        ThumbnailData thumbnailData = (ThumbnailData) this.mThumbnailCache.getAndInvalidateIfModified(t.key);
        if (thumbnailData != null) {
            bitmap = thumbnailData.thumbnail;
            thumbnailInfo = thumbnailData.thumbnailInfo;
        }
        boolean requiresLoad = icon == null || bitmap == null;
        if (icon == null) {
            icon = this.mDefaultIcon;
        }
        if (requiresLoad) {
            this.mLoadQueue.addTask(t);
        }
        if (bitmap == this.mDefaultThumbnail) {
            bitmap = null;
        }
        t.notifyTaskDataLoaded(bitmap, icon, thumbnailInfo);
    }

    public void unloadTaskData(Task t) {
        this.mLoadQueue.removeTask(t);
        t.notifyTaskDataUnloaded(null, this.mDefaultIcon);
    }

    public void deleteTaskData(Task t, boolean notifyTaskDataUnloaded) {
        this.mLoadQueue.removeTask(t);
        this.mThumbnailCache.remove(t.key);
        this.mIconCache.remove(t.key);
        this.mActivityLabelCache.remove(t.key);
        this.mContentDescriptionCache.remove(t.key);
        if (notifyTaskDataUnloaded) {
            t.notifyTaskDataUnloaded(null, this.mDefaultIcon);
        }
    }

    public void onTrimMemory(int level) {
        RecentsConfiguration config = Recents.getConfiguration();
        switch (level) {
            case 5:
            case 40:
                this.mThumbnailCache.trimToSize(Math.max(1, this.mMaxThumbnailCacheSize / 2));
                this.mIconCache.trimToSize(Math.max(1, this.mMaxIconCacheSize / 2));
                this.mActivityInfoCache.trimToSize(Math.max(1, ActivityManager.getMaxRecentTasksStatic() / 2));
                return;
            case 10:
            case 60:
                this.mThumbnailCache.trimToSize(Math.max(1, this.mMaxThumbnailCacheSize / 4));
                this.mIconCache.trimToSize(Math.max(1, this.mMaxIconCacheSize / 4));
                this.mActivityInfoCache.trimToSize(Math.max(1, ActivityManager.getMaxRecentTasksStatic() / 4));
                return;
            case 15:
            case 80:
                this.mThumbnailCache.evictAll();
                this.mIconCache.evictAll();
                this.mActivityInfoCache.evictAll();
                this.mActivityLabelCache.evictAll();
                this.mContentDescriptionCache.evictAll();
                return;
            case 20:
                stopLoader();
                if (config.svelteLevel == 0) {
                    this.mThumbnailCache.trimToSize(Math.max(this.mNumVisibleTasksLoaded, this.mMaxThumbnailCacheSize / 2));
                } else if (config.svelteLevel == 1) {
                    this.mThumbnailCache.trimToSize(this.mNumVisibleThumbnailsLoaded);
                } else if (config.svelteLevel >= 2) {
                    this.mThumbnailCache.evictAll();
                }
                this.mIconCache.trimToSize(Math.max(this.mNumVisibleTasksLoaded, this.mMaxIconCacheSize / 2));
                return;
            default:
                return;
        }
    }

    String getAndUpdateActivityTitle(TaskKey taskKey, TaskDescription td) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (td != null && td.getLabel() != null) {
            return td.getLabel();
        }
        String label = (String) this.mActivityLabelCache.getAndInvalidateIfModified(taskKey);
        if (label != null) {
            return label;
        }
        ActivityInfo activityInfo = getAndUpdateActivityInfo(taskKey);
        if (activityInfo == null) {
            return BuildConfig.FLAVOR;
        }
        label = ssp.getBadgedActivityLabel(activityInfo, taskKey.userId);
        this.mActivityLabelCache.put(taskKey, label);
        return label;
    }

    String getAndUpdateContentDescription(TaskKey taskKey, Resources res) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        String label = (String) this.mContentDescriptionCache.getAndInvalidateIfModified(taskKey);
        if (label != null) {
            return label;
        }
        ActivityInfo activityInfo = getAndUpdateActivityInfo(taskKey);
        if (activityInfo == null) {
            return BuildConfig.FLAVOR;
        }
        label = ssp.getBadgedContentDescription(activityInfo, taskKey.userId, res);
        this.mContentDescriptionCache.put(taskKey, label);
        return label;
    }

    Drawable getAndUpdateActivityIcon(TaskKey taskKey, TaskDescription td, Resources res, boolean loadIfNotCached) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        Drawable icon = (Drawable) this.mIconCache.getAndInvalidateIfModified(taskKey);
        if (icon != null) {
            return icon;
        }
        if (loadIfNotCached) {
            int badgedIconType = BadgedIconHelper.getBadgedIconType(taskKey.baseIntent.getHwFlags(), taskKey.getComponent().getPackageName());
            icon = ssp.getBadgedTaskDescriptionIcon(td, taskKey.userId, res, badgedIconType);
            if (icon != null) {
                this.mIconCache.put(taskKey, icon);
                return icon;
            }
            ActivityInfo activityInfo = getAndUpdateActivityInfo(taskKey);
            if (activityInfo != null) {
                icon = ssp.getBadgedActivityIcon(activityInfo, taskKey.userId, badgedIconType);
                if (icon != null) {
                    this.mIconCache.put(taskKey, icon);
                    return icon;
                }
            }
        }
        return null;
    }

    Bitmap getAndUpdateThumbnail(TaskKey taskKey, boolean loadIfNotCached) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        ThumbnailData thumbnailData = (ThumbnailData) this.mThumbnailCache.getAndInvalidateIfModified(taskKey);
        if (thumbnailData != null) {
            return thumbnailData.thumbnail;
        }
        if (loadIfNotCached && Recents.getConfiguration().svelteLevel < 3) {
            thumbnailData = ssp.getTaskThumbnail(taskKey.id);
            if (thumbnailData.thumbnail != null) {
                this.mThumbnailCache.put(taskKey, thumbnailData);
                return thumbnailData.thumbnail;
            }
        }
        return null;
    }

    int getActivityPrimaryColor(TaskDescription td) {
        if (td == null || td.getPrimaryColor() == 0) {
            return this.mDefaultTaskBarBackgroundColor;
        }
        return td.getPrimaryColor();
    }

    int getActivityBackgroundColor(TaskDescription td) {
        if (td == null || td.getBackgroundColor() == 0) {
            return this.mDefaultTaskViewBackgroundColor;
        }
        return td.getBackgroundColor();
    }

    ActivityInfo getAndUpdateActivityInfo(TaskKey taskKey) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        ComponentName cn = taskKey.getComponent();
        ActivityInfo activityInfo = (ActivityInfo) this.mActivityInfoCache.get(cn);
        if (activityInfo == null) {
            activityInfo = ssp.getActivityInfo(cn, taskKey.userId);
            if (cn == null || activityInfo == null) {
                Log.e("RecentsTaskLoader", "Unexpected null component name or activity info: " + cn + ", " + activityInfo);
                return null;
            }
            this.mActivityInfoCache.put(cn, activityInfo);
        }
        return activityInfo;
    }

    private void stopLoader() {
        this.mLoader.stop();
        this.mLoadQueue.clearTasks();
    }

    public final void onBusEvent(PackagesChangedEvent event) {
        for (ComponentName cn : this.mActivityInfoCache.snapshot().keySet()) {
            if (cn.getPackageName().equals(event.packageName)) {
                this.mActivityInfoCache.remove(cn);
            }
        }
    }

    public void dump(String prefix, PrintWriter writer) {
        String innerPrefix = prefix + "  ";
        writer.print(prefix);
        writer.println("RecentsTaskLoader");
        writer.print(prefix);
        writer.println("Icon Cache");
        this.mIconCache.dump(innerPrefix, writer);
        writer.print(prefix);
        writer.println("Thumbnail Cache");
        this.mThumbnailCache.dump(innerPrefix, writer);
    }
}
