package com.android.contacts;

import android.content.Context;

public class HwCustSpecialCharSequenceMgr {
    public boolean handleSimUnLockBroadcast(Context context, String input) {
        return false;
    }

    public String getMeidStr(Context context) {
        return null;
    }

    public String getImeiStr(Context context) {
        return null;
    }

    public boolean handleCustSpecialCharSequence(Context context, String input) {
        return false;
    }

    public boolean isEnableCustomSwitch(String lSubString, Context context) {
        return false;
    }

    public String customizedMeidDisplay(Context context, String meidStr) {
        return meidStr;
    }

    public String customizedMeidTitle(Context context, String lTitleString, String meidStr) {
        return lTitleString;
    }

    public String customizedImeiDisplay(Context context, String imeiStr) {
        return imeiStr;
    }

    public String customizedImeiTitle(Context context, String lTitleString, String imeiStr) {
        return lTitleString;
    }

    public String getCustomizedMEID(Context context, String meidStr) {
        return meidStr;
    }

    public boolean checkForDisableHiddenMenuItems(Context context, String inputString) {
        return false;
    }
}
