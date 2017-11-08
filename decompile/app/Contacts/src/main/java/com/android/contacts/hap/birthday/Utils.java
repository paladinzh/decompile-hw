package com.android.contacts.hap.birthday;

import android.content.Context;
import android.content.Intent;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.util.HwLog;

public class Utils {
    private static final String[] mBirthdayCalendarSelectionArgs = new String[]{"Phone", "com.android.huawei.birthday", "Birthday calendar"};

    public static void createBirthdayAccount(Context aContext) {
        if (QueryUtil.isHAPProviderInstalled()) {
            if (HwLog.HWDBG) {
                HwLog.d("BirthdayUtils", "createBirthdayAccount");
            }
            Intent lBirthdayServiceIntent = new Intent();
            lBirthdayServiceIntent.setAction("com.huawei.create.birthday.calendar");
            lBirthdayServiceIntent.setPackage("com.android.providers.contacts");
            aContext.startService(lBirthdayServiceIntent);
        }
    }
}
