package com.huawei.systemmanager.applock.taskstack;

import android.app.Activity;
import java.util.ArrayList;
import java.util.List;

class ActivityTaskStack {
    private static ActivityTaskStack sInstance = null;
    private List<Activity> mActivityList = new ArrayList();

    ActivityTaskStack() {
    }

    static synchronized ActivityTaskStack getInstance() {
        ActivityTaskStack activityTaskStack;
        synchronized (ActivityTaskStack.class) {
            if (sInstance == null) {
                sInstance = new ActivityTaskStack();
            }
            activityTaskStack = sInstance;
        }
        return activityTaskStack;
    }

    static synchronized void release() {
        synchronized (ActivityTaskStack.class) {
            if (sInstance != null) {
                sInstance.clearAll();
            }
            sInstance = null;
        }
    }

    void addToStack(Activity activity) {
        this.mActivityList.add(activity);
    }

    void removeFromStack(Activity activity) {
        this.mActivityList.remove(activity);
    }

    void clearAll() {
        this.mActivityList.clear();
    }

    void finishAll() {
        for (Activity finish : this.mActivityList) {
            finish.finish();
        }
        clearAll();
    }
}
