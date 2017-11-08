package com.huawei.systemmanager.applock.taskstack;

import android.app.Activity;

public class ActivityStackUtils {
    public static void addActivity(Activity activity) {
        ActivityTaskStack stack = ActivityTaskStack.getInstance();
        if (stack != null) {
            stack.addToStack(activity);
        }
    }

    public static void removeFromStack(Activity activity) {
        ActivityTaskStack stack = ActivityTaskStack.getInstance();
        if (stack != null) {
            stack.removeFromStack(activity);
        }
    }

    public static void finishAllActivity() {
        ActivityTaskStack stack = ActivityTaskStack.getInstance();
        if (stack != null) {
            stack.finishAll();
        }
    }

    public static void release() {
        ActivityTaskStack.release();
    }
}
