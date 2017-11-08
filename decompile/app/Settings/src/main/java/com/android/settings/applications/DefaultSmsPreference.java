package com.android.settings.applications;

import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.telephony.SmsApplication.SmsApplicationData;
import com.android.settings.AppListPreference;
import com.android.settings.SelfAvailablePreference;
import java.util.Collection;
import java.util.Objects;

public class DefaultSmsPreference extends AppListPreference implements SelfAvailablePreference {
    public DefaultSmsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadSmsApps();
    }

    private void loadSmsApps() {
        Collection<SmsApplicationData> smsApplications = SmsApplication.getApplicationCollection(getContext());
        String[] packageNames = new String[smsApplications.size()];
        int i = 0;
        for (SmsApplicationData smsApplicationData : smsApplications) {
            int i2 = i + 1;
            packageNames[i] = smsApplicationData.mPackageName;
            i = i2;
        }
        setPackageNames(packageNames, getDefaultPackage());
    }

    private String getDefaultPackage() {
        ComponentName appName = SmsApplication.getDefaultSmsApplication(getContext(), true);
        if (appName != null) {
            return appName.getPackageName();
        }
        return null;
    }

    protected boolean persistString(String value) {
        if (!(TextUtils.isEmpty(value) || Objects.equals(value, getDefaultPackage()))) {
            SmsApplication.setDefaultApplication(value, getContext());
        }
        setSummary(getEntry());
        return true;
    }

    public boolean isAvailable(Context context) {
        return !UserManager.get(context).getUserInfo(UserHandle.myUserId()).isRestricted() ? ((TelephonyManager) context.getSystemService("phone")).isSmsCapable() : false;
    }

    public static boolean hasSmsPreference(String pkg, Context context) {
        for (SmsApplicationData data : SmsApplication.getApplicationCollection(context)) {
            if (data.mPackageName.equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSmsDefault(String pkg, Context context) {
        ComponentName appName = SmsApplication.getDefaultSmsApplication(context, true);
        return appName != null ? appName.getPackageName().equals(pkg) : false;
    }
}
