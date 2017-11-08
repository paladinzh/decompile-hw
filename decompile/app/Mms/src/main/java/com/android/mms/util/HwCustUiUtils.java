package com.android.mms.util;

import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.mms.HwCustMmsConfigImpl;

public class HwCustUiUtils {
    private static final String TAG = "HwCustUiUtils";
    public static final boolean THUMBNAIL_SUPPORT = SystemProperties.getBoolean("ro.config.thmbnail_msglist", false);

    public static String updateForwardSubject(String aFwdSubject, String aMsgSubject) {
        if (HwCustMmsConfigImpl.supportEmptyFWDSubject() || (aMsgSubject != null && TextUtils.getTrimmedLength(aMsgSubject) != 0)) {
            return aFwdSubject;
        }
        return aMsgSubject;
    }

    public static boolean isLocalTimeRight(long aTimeNow, long aServerTime) {
        if (Math.abs((aTimeNow - aServerTime) / 1000) < 60 || aServerTime == 0) {
            return true;
        }
        return false;
    }
}
