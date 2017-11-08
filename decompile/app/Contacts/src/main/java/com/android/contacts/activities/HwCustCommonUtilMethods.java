package com.android.contacts.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.util.LogConfig;

public class HwCustCommonUtilMethods {
    public static void startAABClient(Context context, boolean startFromSettings) {
        Intent lIntent = new Intent(HwCustCommonConstants.START_AAB_INIT_ACTIVITY_ACTION);
        lIntent.setPackage(HwCustCommonConstants.AAB_PACKAGE_NAME);
        lIntent.putExtra(HwCustCommonConstants.AAB_STARTUP_KEY, startFromSettings);
        lIntent.setFlags(268435456);
        if (LogConfig.HWDBG) {
            Log.d(HwCustCommonConstants.TAG_AAB, "Send intent to start AAB StartupActivity");
        }
        try {
            context.startActivity(lIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void startVVM(Activity activity) {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.att.mobile.android.vvm", "com.att.mobile.android.vvm.screen.WelcomeActivity");
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(HwCustCommonConstants.TAG_AAB, "HwCustDialpadFragmentHelperImpl, Application is not normal to open");
        }
    }

    public static void startShareContacts(Context context) {
        try {
            Intent lIntent = new Intent();
            lIntent.setAction("android.intent.action.HAP_SHARE_CONTACTS");
            context.startActivity(lIntent);
        } catch (ActivityNotFoundException e) {
            Log.d(HwCustCommonConstants.TAG_AAB, "HwCustDialpadFragmentHelperImpl, Activity not found to open Share Contacts");
        }
    }
}
