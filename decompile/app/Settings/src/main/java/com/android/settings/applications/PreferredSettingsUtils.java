package com.android.settings.applications;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.telecom.DefaultDialerManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.telephony.SmsApplication.SmsApplicationData;
import com.android.settings.MLog;
import com.android.settings.fingerprint.HwCustFingerprintSettingsFragmentImpl;
import com.huawei.android.content.pm.ApplicationInfoEx;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PreferredSettingsUtils {

    public enum PreferredApplication {
        PREFERRED_HOME,
        PREFERRED_DAILER,
        PREFERRED_MMS,
        PREFERRED_CAMERA,
        PREFERRED_GALLERY,
        PREFERRED_MUSIC,
        PREFERRED_MAIL,
        PREFERRED_BROWSER
    }

    public static boolean isSystemAndUnRemovable(ApplicationInfo info) {
        boolean isSystem = (info.flags & 1) != 0;
        boolean isUnRemovable = true;
        try {
            isUnRemovable = (new ApplicationInfoEx(info).getHwFlags() & 100663296) == 0;
        } catch (Exception ex) {
            Log.e("PreferredSettingsUtils", "Error happened when checking removablity, msg is " + ex.getMessage());
        }
        if (isSystem) {
            return isUnRemovable;
        }
        return false;
    }

    public static boolean isHomeApplication(IntentFilter filter) {
        if (filter == null || !filter.hasCategory("android.intent.category.HOME")) {
            return false;
        }
        return true;
    }

    public static boolean isMmsApplication(IntentFilter filter) {
        if (filter == null || !filter.hasAction("android.intent.action.SENDTO")) {
            return false;
        }
        return true;
    }

    public static boolean isMusicApplication(IntentFilter filter) {
        if (filter == null || filter.countDataTypes() <= 0) {
            return false;
        }
        String dataType = filter.getDataType(0);
        if (dataType == null || !dataType.startsWith("audio")) {
            return false;
        }
        return true;
    }

    public static boolean isIllegalMMSApplication(Context context, String packageName) {
        if (context == null || packageName == null || "".equals(packageName)) {
            return true;
        }
        for (SmsApplicationData smsApplicationData : SmsApplication.getApplicationCollection(context)) {
            if (packageName.equals(smsApplicationData.mPackageName)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isTelephoneyOperationsSupported(Context contenxt) {
        if (((TelephonyManager) contenxt.getSystemService("phone")).getPhoneType() != 0) {
            return true;
        }
        return false;
    }

    public static void changePreferredApplication(PackageManager manager, String oldPackageName, ComponentName newComponentName, Context context, int match, IntentFilter filter, ComponentName[] set) {
        if (manager != null && context != null && filter != null && newComponentName != null) {
            if (isMmsApplication(filter)) {
                SmsApplication.setDefaultApplication(newComponentName.getPackageName(), context);
                MLog.d("PreferredSettingsUtils", "This is MMS application. User select " + newComponentName.getPackageName() + "as preferred.");
            } else {
                if (oldPackageName != null) {
                    manager.clearPackagePreferredActivities(oldPackageName);
                    MLog.d("PreferredSettingsUtils", "Clear the old preferred application: " + oldPackageName);
                }
                if (isDialerApplication(filter)) {
                    Log.i("PreferredSettingsUtils", "changePreferredApplication packageName = " + newComponentName.getPackageName());
                    TelecomManager.from(context).setDefaultDialer(newComponentName.getPackageName());
                }
                IntentFilter preferredFilter = new IntentFilter(filter);
                preferredFilter.addCategory("android.intent.category.DEFAULT");
                manager.addPreferredActivity(preferredFilter, match, set, newComponentName);
            }
        }
    }

    public static boolean isDialerApplication(IntentFilter filter) {
        if (filter == null || !filter.hasAction("android.intent.action.DIAL")) {
            return false;
        }
        return true;
    }

    public static String getPreferredPackageName(PackageManager manager, List<ResolveInfo> resolveInfoList, IntentFilter filter, Intent preferredAppIntent, Context context) {
        if (manager == null || context == null || filter == null || resolveInfoList == null || resolveInfoList.size() == 0) {
            return null;
        }
        if (resolveInfoList.size() == 1) {
            return ((ResolveInfo) resolveInfoList.get(0)).activityInfo.packageName;
        }
        String preferredAppPkgName = null;
        if (!isMmsApplication(filter)) {
            if (!isDialerApplication(filter)) {
                List<ComponentName> activities = new ArrayList();
                ArrayList<IntentFilter> filters = new ArrayList();
                for (ResolveInfo info : resolveInfoList) {
                    activities.clear();
                    filters.clear();
                    manager.getPreferredActivities(filters, activities, info.activityInfo.packageName);
                    int size = filters.size();
                    int i = 0;
                    while (i < size) {
                        if (info.activityInfo.packageName.equals(((ComponentName) activities.get(i)).getPackageName()) && info.activityInfo.name.equals(((ComponentName) activities.get(i)).getClassName()) && isFilterMatch(filter, (IntentFilter) filters.get(i))) {
                            preferredAppPkgName = info.activityInfo.packageName;
                            continue;
                            break;
                        }
                        i++;
                    }
                    if (preferredAppPkgName != null) {
                        break;
                    }
                }
            }
            preferredAppPkgName = DefaultDialerManager.getDefaultDialerApplication(context);
        } else {
            ComponentName appName = SmsApplication.getDefaultSmsApplication(context, true);
            if (appName != null) {
                preferredAppPkgName = appName.getPackageName();
                MLog.d("PreferredSettingsUtils", "This is MMS application, preferred application is: " + preferredAppPkgName);
            }
        }
        if (preferredAppPkgName != null) {
            return preferredAppPkgName;
        }
        return getSystemPreferredPackageName(preferredAppIntent, resolveInfoList);
    }

    public static String getSystemPreferredPackageName(Intent preferredAppIntent, List<ResolveInfo> resolveInfoList) {
        if (preferredAppIntent == null || preferredAppIntent.getAction() == null || resolveInfoList == null || resolveInfoList.size() == 0) {
            return null;
        }
        String action = preferredAppIntent.getAction();
        for (ResolveInfo info : resolveInfoList) {
            String packageName = info.activityInfo.applicationInfo.packageName;
            if ("android.media.action.IMAGE_CAPTURE".equals(action)) {
                if ("com.huawei.camera".equals(packageName)) {
                    return info.activityInfo.packageName;
                }
            } else if ("android.intent.action.VIEW".equals(action)) {
                if (preferredAppIntent.getType() != null && preferredAppIntent.getType().startsWith("image/") && "com.android.gallery3d".equals(packageName)) {
                    return info.activityInfo.packageName;
                }
                if (preferredAppIntent.getType() != null && preferredAppIntent.getType().startsWith("audio/") && "com.android.mediacenter".equals(packageName)) {
                    return info.activityInfo.packageName;
                }
                if (preferredAppIntent.getData() != null && preferredAppIntent.getData().getScheme() != null && "mailto".equals(preferredAppIntent.getData().getScheme()) && "com.android.email".equals(packageName)) {
                    return info.activityInfo.packageName;
                }
                if (!(preferredAppIntent.getData() == null || preferredAppIntent.getData().getScheme() == null || !"http".equals(preferredAppIntent.getData().getScheme())) || ("https".equals(preferredAppIntent.getData().getScheme()) && "com.android.browser".equals(packageName))) {
                    return info.activityInfo.packageName;
                }
            } else if ("android.intent.action.DIAL".equals(action)) {
                if (preferredAppIntent.getData() != null && preferredAppIntent.getData().getScheme() != null && preferredAppIntent.getData().getScheme().equals(HwCustFingerprintSettingsFragmentImpl.TEL_PATTERN) && "com.android.contacts".equals(packageName)) {
                    return info.activityInfo.packageName;
                }
            } else if ("android.intent.action.MAIN".equals(action)) {
                Set<String> categories = preferredAppIntent.getCategories();
                if (categories != null && categories.contains("android.intent.category.HOME") && "com.huawei.android.launcher".equals(packageName)) {
                    return info.activityInfo.packageName;
                }
            } else {
                continue;
            }
        }
        Uri data = preferredAppIntent.getData();
        String scheme = "";
        if (data != null) {
            scheme = data.getScheme();
        }
        for (ResolveInfo info2 : resolveInfoList) {
            if (isSystemAndUnRemovable(info2.activityInfo.applicationInfo)) {
                Log.e("PreferredSettingsUtils", "getSystemPreferredPackageName.consider the system pre-installed one as preferred, packageName:" + info2.activityInfo.packageName + "|action:" + action + "|type:" + preferredAppIntent.getType() + "|scheme:" + scheme);
                return info2.activityInfo.packageName;
            }
        }
        Log.e("PreferredSettingsUtils", "getSystemPreferredPackageName:No preferred packageName was found.|action:" + action + "|type:" + preferredAppIntent.getType() + "|scheme:" + scheme);
        return null;
    }

    public static boolean isFilterMatch(IntentFilter first, IntentFilter second) {
        boolean dataTypeMatches = true;
        int firstSchemeCount = first.countDataSchemes();
        int secondSchemeCount = second.countDataSchemes();
        if (firstSchemeCount > 0 && secondSchemeCount > 0 && firstSchemeCount == secondSchemeCount) {
            int i = 0;
            while (i < firstSchemeCount) {
                if (first.getDataScheme(i) == null || second.getDataScheme(i) == null) {
                    dataTypeMatches = false;
                    break;
                } else if (!first.getDataScheme(i).equals(second.getDataScheme(i))) {
                    dataTypeMatches = false;
                    break;
                } else {
                    i++;
                }
            }
        }
        if (!dataTypeMatches || first.countDataTypes() <= 0 || second.countDataTypes() <= 0 || TextUtils.isEmpty(first.getDataType(0)) || TextUtils.isEmpty(second.getDataType(0))) {
            return dataTypeMatches;
        }
        return first.getDataType(0).equalsIgnoreCase(second.getDataType(0));
    }

    public static CharSequence getApplicationlabel(PackageManager manager, IntentFilter filter, String pkgName, String label) {
        if (manager == null || pkgName == null) {
            return label;
        }
        CharSequence appPkgName = label;
        if (!isMmsApplication(filter)) {
            try {
                appPkgName = manager.getApplicationInfo(pkgName, 0).loadLabel(manager);
            } catch (NameNotFoundException e) {
                MLog.e("PreferredSettingsUtils", "Can not find package " + pkgName);
            }
        }
        return appPkgName;
    }
}
