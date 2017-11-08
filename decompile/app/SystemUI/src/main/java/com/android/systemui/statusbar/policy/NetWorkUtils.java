package com.android.systemui.statusbar.policy;

import android.util.Log;

public class NetWorkUtils {
    private static final String TAG = NetWorkUtils.class.getSimpleName();
    static boolean is3GCalling = false;
    static int mCdmaSlot = -1;
    static int mDefaultSlot = -1;
    static int mVSimCurCardType = -1;
    static int mVsimId = -1;

    public static int getVSimCurCardType() {
        Log.d(TAG, "mVSimCurCardType is " + mVSimCurCardType);
        return mVSimCurCardType;
    }

    public static void setVSimCurCardType(int vsimcurcardtype) {
        Log.d(TAG, "set mVSimCurCardType " + mVSimCurCardType);
        mVSimCurCardType = vsimcurcardtype;
    }

    public static int getVSimSubId() {
        return mVsimId;
    }

    public static void setVSimSubId(int vsimid) {
        Log.d(TAG, "set mVsimId " + mVsimId);
        mVsimId = vsimid;
    }

    public static boolean get3GCallingState(int sub) {
        Log.d(TAG, "is3GCalling is " + is3GCalling + ",mCdmaSlot is " + mCdmaSlot);
        if (is3GCalling && mCdmaSlot == sub) {
            return true;
        }
        return false;
    }

    public static void set3GCallingState(boolean is3Gcall, int sub) {
        is3GCalling = is3Gcall;
        mCdmaSlot = sub;
    }

    public static int getDefaultSlot() {
        Log.d(TAG, "mDefaultSlot is " + mDefaultSlot);
        return mDefaultSlot;
    }

    public static void setDefaultSlot(int defaultslot) {
        mDefaultSlot = defaultslot;
    }

    public static int getTjtIcons(int inetcon, int length) {
        if (getVSimCurCardType() == 1) {
            return HwTelephonyIcons.TELEPHONY_SIGNAL_TYPE_TJT[0][0];
        }
        if (inetcon < 0 || inetcon >= 2 || length < 0 || length >= 5) {
            return HwTelephonyIcons.TELEPHONY_SIGNAL_TYPE_TJT[0][0];
        }
        return HwTelephonyIcons.TELEPHONY_SIGNAL_TYPE_TJT[inetcon][length];
    }
}
