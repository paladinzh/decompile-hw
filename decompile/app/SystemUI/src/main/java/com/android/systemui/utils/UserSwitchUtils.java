package com.android.systemui.utils;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.ArrayList;

public class UserSwitchUtils {
    private static int INVALID_USER = -1;
    private static int mCurrentUser = INVALID_USER;
    private static BroadcastReceiver mUserSwitcvhedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                UserSwitchUtils.mCurrentUser = ActivityManager.getCurrentUser();
                UserSwitchUtils.notifyChanged();
            }
        }
    };
    private static Object sSyncObj = new Object();
    private static ArrayList<UserSwitchedListener> sUserSwitchedListeners = new ArrayList();

    public interface UserSwitchedListener {
        void onUserChanged();
    }

    private static void registerUserSwitchedReceiver(Context ctx) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_SWITCHED");
        ctx.registerReceiver(mUserSwitcvhedReceiver, filter);
    }

    public static void init(Context ctx) {
        mCurrentUser = ActivityManager.getCurrentUser();
        registerUserSwitchedReceiver(ctx);
    }

    public static int getCurrentUser() {
        if (mCurrentUser == INVALID_USER) {
            mCurrentUser = ActivityManager.getCurrentUser();
        }
        return mCurrentUser;
    }

    public static void addListener(UserSwitchedListener listener) {
        synchronized (sSyncObj) {
            sUserSwitchedListeners.add(listener);
            HwLog.i("UserSwitchUtils", "addListener " + sUserSwitchedListeners.size());
        }
    }

    public static void removeListener(UserSwitchedListener listener) {
        synchronized (sSyncObj) {
            sUserSwitchedListeners.remove(listener);
            HwLog.i("UserSwitchUtils", "removeListener " + sUserSwitchedListeners.size());
        }
    }

    private static void notifyChanged() {
        ArrayList<UserSwitchedListener> tempList;
        synchronized (sSyncObj) {
            tempList = (ArrayList) sUserSwitchedListeners.clone();
        }
        HwLog.i("UserSwitchUtils", "notifyChanged::list size=" + tempList.size());
        for (UserSwitchedListener listener : tempList) {
            listener.onUserChanged();
        }
    }
}
