package com.android.systemui.linkplus;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.telephony.ServiceState;

public class RoamPlus {
    public static final boolean IS_SUPPORT_ROAMING_PLUS = isSupportRoamingPlus();
    private static final String TAG = RoamPlus.class.getSimpleName();
    private static Context mContext;
    private static RoamPlus mRoamPlus = new RoamPlus();
    private Handler mHandler;
    private boolean mIsRegister;
    private Runnable mRunnable;
    private ContentObserver mStateContentObserver;

    private RoamPlus() {
    }

    public static RoamPlus getInstance(Context context) {
        mContext = context;
        return mRoamPlus;
    }

    public void register() {
        if (IS_SUPPORT_ROAMING_PLUS) {
            init();
            if (!this.mIsRegister) {
                this.mIsRegister = true;
                mContext.getContentResolver().registerContentObserver(System.getUriFor("showing_roaming_plus_dialog"), true, this.mStateContentObserver);
            }
        }
    }

    private void init() {
        if (this.mHandler == null) {
            this.mHandler = new Handler();
        }
        if (this.mStateContentObserver == null) {
            this.mStateContentObserver = new ContentObserver(this.mHandler) {
                public void onChange(boolean selfChange) {
                    if (RoamPlus.this.isShowingRoamPlusDialog(RoamPlus.mContext)) {
                        RoamPlus.this.mHandler.removeCallbacks(RoamPlus.this.mRunnable);
                        RoamPlus.this.mHandler.postDelayed(RoamPlus.this.mRunnable, 30000);
                    }
                }
            };
        }
        if (this.mRunnable == null) {
            this.mRunnable = new Runnable() {
                public void run() {
                    if (RoamPlus.mContext != null) {
                        RoamPlus.this.setRoamPlusDialogValue(RoamPlus.mContext, 0);
                    }
                }
            };
        }
    }

    public void unRegister() {
        if (IS_SUPPORT_ROAMING_PLUS && this.mIsRegister) {
            this.mIsRegister = false;
            mContext.getContentResolver().unregisterContentObserver(this.mStateContentObserver);
        }
    }

    private boolean isShowingRoamPlusDialog(Context context) {
        return System.getInt(context.getContentResolver(), "showing_roaming_plus_dialog", 0) == 1;
    }

    private void setRoamPlusDialogValue(Context context, int value) {
        System.putInt(context.getContentResolver(), "showing_roaming_plus_dialog", value);
    }

    public static void resetAlreadyShowRoaming(Context context) {
        System.putInt(context.getContentResolver(), "already_show_roaming_plus_dialog", 0);
    }

    public static boolean isSupportRoamingPlus() {
        return SystemProperties.get("ro.config.linkplus.roaming", "false").equals("true");
    }

    public static void searchServiceSuccess(ServiceState serviceState, Context context) {
        boolean roamPlusShowing = true;
        if (IS_SUPPORT_ROAMING_PLUS && serviceState != null && serviceState.getState() == 0) {
            if (System.getInt(context.getContentResolver(), "showing_roaming_plus_dialog", 0) != 1) {
                roamPlusShowing = false;
            }
            if (roamPlusShowing) {
                Intent intent = new Intent("huawei.intent.action.ROAMING_PLUS_REGISTER_SUCCEEDED");
                intent.setPackage("com.android.settings");
                context.sendBroadcastAsUser(intent, new UserHandle(-2), "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM");
            }
        }
    }
}
