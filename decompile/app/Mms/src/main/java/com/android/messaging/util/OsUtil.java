package com.android.messaging.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.os.Build.VERSION;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.util.Log;
import com.android.mms.MmsApp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class OsUtil {
    public static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    private static boolean sIsAtLeastICS_MR1;
    private static boolean sIsAtLeastJB;
    private static boolean sIsAtLeastJB_MR1;
    private static boolean sIsAtLeastJB_MR2;
    private static boolean sIsAtLeastKLP;
    private static boolean sIsAtLeastL;
    private static boolean sIsAtLeastL_MR1;
    private static boolean sIsAtLeastM;
    private static boolean sIsFirstPermissionCheck = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("sIsFirstPermissionCheck", true);
    private static Boolean sIsSecondaryUser = null;
    private static boolean sIsStartApp = false;
    private static Hashtable<String, Integer> sPermissions = new Hashtable();
    private static String[] sRequiredPermissions = new String[]{"android.permission.READ_SMS", "android.permission.READ_PHONE_STATE", "android.permission.READ_CONTACTS", "android.permission.READ_EXTERNAL_STORAGE"};
    private static String[] sStrictRequiredPermissions = new String[]{"android.permission.READ_SMS", "android.permission.READ_PHONE_STATE", "android.permission.READ_CONTACTS", "android.permission.READ_EXTERNAL_STORAGE"};

    static {
        boolean z;
        int v = getApiVersion();
        sIsAtLeastICS_MR1 = v >= 15;
        if (v >= 16) {
            z = true;
        } else {
            z = false;
        }
        sIsAtLeastJB = z;
        if (v >= 17) {
            z = true;
        } else {
            z = false;
        }
        sIsAtLeastJB_MR1 = z;
        if (v >= 18) {
            z = true;
        } else {
            z = false;
        }
        sIsAtLeastJB_MR2 = z;
        if (v >= 19) {
            z = true;
        } else {
            z = false;
        }
        sIsAtLeastKLP = z;
        if (v >= 21) {
            z = true;
        } else {
            z = false;
        }
        sIsAtLeastL = z;
        if (v >= 22) {
            z = true;
        } else {
            z = false;
        }
        sIsAtLeastL_MR1 = z;
        if (v >= 23) {
            z = true;
        } else {
            z = false;
        }
        sIsAtLeastM = z;
    }

    public static boolean isAtLeastKLP() {
        return sIsAtLeastKLP;
    }

    public static boolean isAtLeastL() {
        return sIsAtLeastL;
    }

    public static boolean isAtLeastM() {
        return sIsAtLeastM;
    }

    public static int getApiVersion() {
        return VERSION.SDK_INT;
    }

    public static Context getApplicationContext() {
        return MmsApp.getApplication();
    }

    public static final boolean isSupportMultiUser(Context context) {
        UserManager userManager = (UserManager) context.getSystemService("user");
        if (!UserManager.supportsMultipleUsers() || userManager.getUserCount() <= 1) {
            return false;
        }
        return true;
    }

    public static final boolean isOwner() {
        return UserHandle.myUserId() == 0;
    }

    public static final boolean isForgroundOwner() {
        return ActivityManager.getCurrentUser() == 0;
    }

    public static final boolean isSmsDisabledForLoginUser(Context context) {
        try {
            UserInfo ui = ActivityManagerNative.getDefault().getCurrentUser();
            boolean ret = isSmsDisabledForUser(context, ui.id);
            Log.e("OsUtil", (ret ? "Sms disabled for LoginUser " : "Sms enabled for LoginUser ") + ui.id);
            return ret;
        } catch (RemoteException e) {
            Log.e("OsUtil", "Can't get Current User Info", e);
            return true;
        }
    }

    public static final boolean isSmsDisabledForUser(Context context, int uid) {
        boolean ret = ((UserManager) context.getSystemService("user")).hasUserRestriction("no_sms", new UserHandle(uid));
        Log.e("OsUtil", (ret ? "Sms disabled for user" : "Sms enabled for user") + uid);
        return ret;
    }

    public static final boolean isSmsDisabledForMe(Context context) {
        boolean ret = ((UserManager) context.getSystemService("user")).hasUserRestriction("no_sms");
        Log.e("OsUtil", ret ? "Sms disabled for me" : "Sms enabled for me");
        return ret;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isSecondaryUser() {
        synchronized (OsUtil.class) {
            if (sIsSecondaryUser != null) {
                boolean booleanValue = sIsSecondaryUser.booleanValue();
                return booleanValue;
            }
        }
    }

    public static boolean isOwnerLogin() {
        boolean z = true;
        try {
            if (ActivityManagerNative.getDefault().getCurrentUser().id != 0) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.e("OsUtil", "isInLoginUser got RemoteException", e);
            return true;
        }
    }

    public static boolean isInLoginUser() {
        boolean z = true;
        try {
            if (UserHandle.myUserId() != ActivityManagerNative.getDefault().getCurrentUser().id) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.e("OsUtil", "isInLoginUser got RemoteException", e);
            return true;
        }
    }

    public static boolean hasPermission(String permission) {
        if (!isAtLeastM()) {
            return true;
        }
        if (!sPermissions.containsKey(permission) || ((Integer) sPermissions.get(permission)).intValue() == -1) {
            sPermissions.put(permission, Integer.valueOf(getApplicationContext().checkSelfPermission(permission)));
        }
        return ((Integer) sPermissions.get(permission)).intValue() == 0;
    }

    public static boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasLocationPermission() {
        return hasPermission("android.permission.ACCESS_FINE_LOCATION");
    }

    public static boolean hasCalendarPermission() {
        return hasPermission("android.permission.READ_CALENDAR");
    }

    public static boolean hasChooseSubScriptionPermission() {
        return hasPermission("com.huawei.contacts.permission.CHOOSE_SUBSCRIPTION");
    }

    public static boolean hasStoragePermission() {
        return hasPermission("android.permission.READ_EXTERNAL_STORAGE");
    }

    public static boolean hasRecordAudioPermission() {
        return hasPermission("android.permission.RECORD_AUDIO");
    }

    public static boolean hasCameraPermission() {
        return hasPermission("android.permission.CAMERA");
    }

    public static String[] getMissingPermissions(String[] permissions) {
        ArrayList<String> missingList = new ArrayList();
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                missingList.add(permission);
            }
        }
        String[] missingArray = new String[missingList.size()];
        missingList.toArray(missingArray);
        return missingArray;
    }

    public static boolean hasRequiredPermissions() {
        return isFirstLaunch() ? hasAllRequiredPermissions() : hasStrictRequiredPermissions();
    }

    public static boolean hasAllRequiredPermissions() {
        long start = System.currentTimeMillis();
        boolean ret = hasPermissions(sRequiredPermissions);
        Log.d("OsUtil", "hasRequiredPermissions run " + ret + " " + (System.currentTimeMillis() - start));
        return ret;
    }

    public static boolean hasStrictRequiredPermissions() {
        boolean ret = hasPermissions(sStrictRequiredPermissions);
        Log.d("OsUtil", "hasStrictRequiredPermissions run " + ret);
        return ret;
    }

    public static String[] getMissingRequiredPermissions() {
        long start = System.currentTimeMillis();
        String[] ret = getMissingPermissions(sRequiredPermissions);
        Log.d("OsUtil", "getMissingRequiredPermissions run " + ret.length + " " + (System.currentTimeMillis() - start));
        return ret;
    }

    public static void setFirstLaunch(boolean first) {
        sIsFirstPermissionCheck = first;
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("sIsFirstPermissionCheck", first).commit();
    }

    public static boolean isFirstLaunch() {
        return sIsFirstPermissionCheck;
    }

    public static void setAppStart(boolean isStartApp) {
        sIsStartApp = isStartApp;
    }

    public static boolean isAppStart() {
        return sIsStartApp;
    }

    public static void requestPermission(Activity activity, String[] permissionList, int mPermissionRequestCode) {
        if (VERSION.SDK_INT < 23) {
            Log.i("OsUtil", "requestPermission android version < 23, return : " + VERSION.SDK_INT);
        } else {
            requestPermissions(permissionList, activity, mPermissionRequestCode);
        }
    }

    public static void requestPermissions(String[] permissionList, Activity activity, int mPermissionRequestCode) {
        List<String> NeedRequestPermissionList = new ArrayList();
        for (String permission : permissionList) {
            if (activity.checkSelfPermission(permission) != 0) {
                NeedRequestPermissionList.add(permission);
            }
        }
        if (NeedRequestPermissionList.size() != 0) {
            try {
                activity.requestPermissions((String[]) NeedRequestPermissionList.toArray(new String[NeedRequestPermissionList.size()]), mPermissionRequestCode);
            } catch (Exception e) {
                Log.e("OsUtil", "permission requestPermissions e= " + e);
            }
        }
    }

    public static Context getContextOfCurrentUser(Context context) {
        Context currentContext = context;
        int currentUser = ActivityManager.getCurrentUser();
        int myUser = UserManager.get(context).getUserHandle();
        Log.i("OsUtil", "myUser=" + myUser + ", currentUser=" + currentUser);
        if (myUser != currentUser) {
            try {
                currentContext = context.createPackageContextAsUser(context.getPackageName(), 0, new UserHandle(currentUser));
            } catch (NameNotFoundException e) {
                Log.e("OsUtil", "Can't find self package", e);
            }
        }
        return currentContext;
    }
}
