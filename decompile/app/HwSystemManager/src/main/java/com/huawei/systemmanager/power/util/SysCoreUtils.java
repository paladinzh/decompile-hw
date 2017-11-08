package com.huawei.systemmanager.power.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.SQLException;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.IWindowManager.Stub;
import android.view.WindowManager;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hsm.am.M2NAdapter;
import com.huawei.android.app.AlarmManagerEx;
import com.huawei.csp.util.MmsInfo;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.PowerSavingMgr;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant.AdBlockColumns;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.data.battery.BatteryInfo;
import com.huawei.systemmanager.power.data.battery.DiskFileReader;
import com.huawei.systemmanager.power.data.stats.PowerStatsException;
import com.huawei.systemmanager.power.data.stats.PowerStatsHelper;
import com.huawei.systemmanager.power.data.stats.UidAndPower;
import com.huawei.systemmanager.power.model.BatteryStatisticsHelper;
import com.huawei.systemmanager.power.model.PowerModeControl;
import com.huawei.systemmanager.power.provider.ProviderWrapper;
import com.huawei.systemmanager.power.service.DarkThemeChanageService;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import com.huawei.systemmanager.util.procpolicy.ProcessUtil;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SysCoreUtils {
    private static final String AMOLED = "AMOLED";
    private static final double[] AMOLED_DARK_THEME_PROFIT = new double[]{1.0d, 1.026d};
    private static final String CLASS_NAME = "android.view.SurfaceControl";
    private static String FILEPATH = "/sys/class/graphics/fb0/panel_info";
    private static final String FLASHLIGHT_CURRENT_STATE = "flashlight_current_state";
    public static final int INVALID_UID = -1;
    public static final boolean IS_ATT;
    private static final String METHOD_NAME = "isRogSupport";
    private static final double[] POWER_PROFIT = new double[]{1.0d, 1.063d};
    private static final double[] POWER_PROFIT_16_TO_10 = new double[]{1.0d, 1.0d, 1.04d};
    private static final String PREX = "lcdtype:";
    public static final int ROG_MODE_OFF = 0;
    public static final int ROG_MODE_ON = 1;
    public static final int SHARED_UID = 1;
    private static final Point[] SIZE_TYPES = new Point[]{new Point(720, 1280), new Point(1080, 1920), new Point(Events.E_POWER_POWERMODE_SWITCH_STATUS, 2560)};
    private static final Point[] SIZE_TYPES_16_TO_10 = new Point[]{new Point(800, 1280), new Point(Events.E_OPTMIZE_VIRUS_VIEW, 1920), new Point(Events.E_SECURITY_PATCH_FUNC_DESC, 2560)};
    private static final String TAG = SysCoreUtils.class.getSimpleName();
    public static final int UNSHARED_UID = 0;

    static {
        boolean z = false;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("07")) {
            z = SystemProperties.get("ro.config.hw_optb", "0").equals("840");
        }
        IS_ATT = z;
    }

    public static boolean checkIsBackground(Context context, String pkgName) {
        if (isScreenOn(context) && isTopActivity(context, pkgName)) {
            return false;
        }
        return true;
    }

    public static boolean isTopActivity(Context context, String pkgName) {
        List<RunningTaskInfo> tasksInfo = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
        return tasksInfo != null && tasksInfo.size() > 0 && pkgName.equals(((RunningTaskInfo) tasksInfo.get(0)).topActivity.getPackageName());
    }

    public static boolean isScreenOn(Context context) {
        return ((PowerManager) context.getSystemService(BatteryStatisticsHelper.DB_POWER)).isScreenOn();
    }

    public static Set<Integer> getRunningUids(Context context) {
        Set<Integer> cacheUidSet = Sets.newHashSet();
        List<RunningAppProcessInfo> runningList = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
        if (runningList == null || runningList.isEmpty()) {
            return cacheUidSet;
        }
        cacheUidSet.addAll(Collections2.transform(runningList, new Function<RunningAppProcessInfo, Integer>() {
            public Integer apply(RunningAppProcessInfo arg0) {
                if (arg0 != null) {
                    return Integer.valueOf(arg0.uid);
                }
                return Integer.valueOf(0);
            }
        }));
        return cacheUidSet;
    }

    public static void forceStopPackageAndSyncSaving(Context context, List<String> packages) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        for (String pkg : packages) {
            HwLog.d(TAG, "Force stop package: " + pkg);
            activityManager.forceStopPackage(pkg);
            ProviderWrapper.updateWakeupNumDBSingle(context, pkg);
        }
    }

    public static List<ApplicationInfo> getAppInfoByUid(Context context, int uid) {
        List<ApplicationInfo> resultApp = Lists.newArrayList();
        PackageManager pm = context.getPackageManager();
        String[] pkaName = pm.getPackagesForUid(uid);
        if (pkaName != null && pkaName.length > 0) {
            for (String applicationInfo : pkaName) {
                try {
                    resultApp.add(pm.getApplicationInfo(applicationInfo, 0));
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultApp;
    }

    public static ApplicationInfo getAppInfoByPackageName(Context context, String pkgName) {
        try {
            return context.getPackageManager().getApplicationInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            HwLog.e(TAG, "getAppInfoByPackageName NameNotFoundException: " + pkgName);
            e.printStackTrace();
            return null;
        } catch (Exception e2) {
            HwLog.e(TAG, "getAppInfoByPackageName Exception: " + pkgName);
            e2.printStackTrace();
            return null;
        }
    }

    public static int getGoogleSharedUid(Context ctx) {
        try {
            return ctx.getPackageManager().getUidForSharedUser("com.google.uid.shared");
        } catch (NameNotFoundException e) {
            HwLog.w(TAG, "getGoogleSharedUid NameNotFoundException!");
            return -1;
        } catch (Exception e2) {
            HwLog.e(TAG, "getGoogleSharedUid Exception!");
            e2.printStackTrace();
            return -1;
        }
    }

    public static int getAppWakeUpNum(Context ctx, String pkgName) {
        try {
            return AlarmManagerEx.getWakeUpNum((AlarmManager) ctx.getSystemService("alarm"), PackageManagerWrapper.getPackageInfo(ctx.getPackageManager(), pkgName, 0).applicationInfo.uid, pkgName);
        } catch (NoSuchMethodError e) {
            HwLog.e(TAG, "Warning!!! java.lang.NoSuchMethodError: com.huawei.android.app.AlarmManagerEx.getWakeUpNum");
            e.printStackTrace();
            HwLog.w(TAG, "getAppWakeUpNum empty return");
            return 0;
        } catch (NullPointerException e2) {
            HwLog.e(TAG, "getAppWakeUpNum NullPointerException");
            e2.printStackTrace();
            HwLog.w(TAG, "getAppWakeUpNum empty return");
            return 0;
        } catch (Exception e3) {
            HwLog.e(TAG, "getAppWakeUpNum catch Exception!");
            e3.printStackTrace();
            HwLog.w(TAG, "getAppWakeUpNum empty return");
            return 0;
        }
    }

    public static List<String> getAppLabelByUid(Context context, int uid) {
        HwLog.e(TAG, "getAppLabelByUid: " + uid);
        String[] packages = context.getPackageManager().getPackagesForUid(uid);
        if (packages == null) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(Collections2.filter(Collections2.transform(Arrays.asList(packages), new Function<String, String>() {
            public String apply(String input) {
                try {
                    return HsmPackageManager.getInstance().getLabel(input);
                } catch (Exception e) {
                    HwLog.e(SysCoreUtils.TAG, "getAppLabelByUid catch exception when calling getLabel: " + input);
                    return null;
                }
            }
        }), new Predicate<String>() {
            public boolean apply(String input) {
                return !Strings.isNullOrEmpty(input);
            }
        }));
    }

    public static int getUidType(int uid) {
        String[] packages = GlobalContext.getContext().getPackageManager().getPackagesForUid(uid);
        if (packages == null || packages.length == 0) {
            HwLog.e(TAG, "getUidType invalid uid: " + uid);
            return -1;
        } else if (1 == packages.length) {
            return 0;
        } else {
            return 1;
        }
    }

    public static ExtAppInfo getExtAppInfoByUid(Context ctx, int uid) {
        ExtAppInfo extAppInfo = new ExtAppInfo();
        try {
            extAppInfo.setmUid(uid);
            PackageManager pm = ctx.getPackageManager();
            String[] packages = pm.getPackagesForUid(uid);
            if (packages == null || packages.length == 0) {
                HwLog.e(TAG, "getExtAppInfoByUid invalid uid: " + uid);
                return null;
            } else if (1 == packages.length) {
                ApplicationInfo appInfo = pm.getApplicationInfo(packages[0], 0);
                extAppInfo.setmIcon(null);
                extAppInfo.setmIsShareUid(false);
                extAppInfo.setmPkgLabel(pm.getApplicationLabel(appInfo).toString());
                extAppInfo.setmPkgName(packages[0]);
                return extAppInfo;
            } else {
                ApplicationInfo mApp;
                for (String pkg : packages) {
                    PackageInfo pi = PackageManagerWrapper.getPackageInfo(pm, pkg, 0);
                    if (pi.sharedUserLabel != 0) {
                        mApp = pi.applicationInfo;
                        if (mApp != null) {
                            extAppInfo.setmPkgLabel(mApp.loadLabel(pm).toString().replaceAll("\\s", ""));
                            extAppInfo.setmIsShareUid(true);
                            extAppInfo.setmIcon(null);
                            extAppInfo.setmPkgName(pkg);
                            return extAppInfo;
                        }
                    }
                }
                if (1 < packages.length) {
                    HwLog.i(TAG, "the shared uid apps have not pi.sharedUserLabel != 0,uid= " + uid);
                    Collections.sort(Arrays.asList(packages));
                    mApp = PackageManagerWrapper.getPackageInfo(pm, packages[packages.length - 1], 0).applicationInfo;
                    if (mApp != null) {
                        extAppInfo.setmPkgLabel(mApp.loadLabel(pm).toString().replaceAll("\\s", ""));
                        extAppInfo.setmIsShareUid(true);
                        extAppInfo.setmIcon(null);
                        extAppInfo.setmPkgName(packages[packages.length - 1]);
                        return extAppInfo;
                    }
                }
                return null;
            }
        } catch (NameNotFoundException ex) {
            HwLog.e(TAG, "getExtAppInfoByUid catch NameNotFoundException failed uid: " + uid);
            ex.printStackTrace();
        } catch (Exception ex2) {
            HwLog.e(TAG, "getExtAppInfoByUid catch Exception failed uid: " + uid);
            ex2.printStackTrace();
        }
    }

    public static Drawable getExtAppIconByUid(Context ctx, int uid) {
        try {
            PackageManager pm = ctx.getPackageManager();
            String[] packages = pm.getPackagesForUid(uid);
            if (packages == null || packages.length == 0) {
                HwLog.e(TAG, "getExtAppInfoByUid invalid uid: " + uid);
                return null;
            } else if (1 == packages.length) {
                return pm.getApplicationIcon(pm.getApplicationInfo(packages[0], 0));
            } else {
                PackageInfo pi;
                for (String pkg : packages) {
                    pi = PackageManagerWrapper.getPackageInfo(pm, pkg, 0);
                    if (pi.sharedUserLabel != 0 && pm.getText(pkg, pi.sharedUserLabel, pi.applicationInfo) != null) {
                        return pi.applicationInfo.loadIcon(pm);
                    }
                }
                if (1 < packages.length) {
                    HwLog.i(TAG, "the shared uid apps have not pi.sharedUserLabel != 0,uid= " + uid);
                    Collections.sort(Arrays.asList(packages));
                    pi = PackageManagerWrapper.getPackageInfo(pm, packages[packages.length - 1], 0);
                    if (pi.applicationInfo != null) {
                        return pi.applicationInfo.loadIcon(pm);
                    }
                }
                return null;
            }
        } catch (NameNotFoundException ex) {
            HwLog.e(TAG, "getExtAppInfoByUid catch NameNotFoundException failed uid: " + uid);
            ex.printStackTrace();
        } catch (Exception ex2) {
            HwLog.e(TAG, "getExtAppInfoByUid catch Exception failed uid: " + uid);
            ex2.printStackTrace();
        }
    }

    public static Point getCurrentPoint() {
        Point point = new Point();
        try {
            Stub.asInterface(ServiceManager.checkService("window")).getBaseDisplaySize(0, point);
            HwLog.d(TAG, "get curSize:" + point + ")");
        } catch (Exception e) {
            HwLog.e(TAG, e.toString(), e);
        }
        return point;
    }

    public static int getMaxPointIdx() {
        Point max = getMaxSuportPoint();
        HwLog.d(TAG, "enter getMaxPointIdx(max:" + max + ")");
        int idx = 0;
        for (int i = 0; i < SIZE_TYPES.length; i++) {
            if (max.y == SIZE_TYPES[i].y) {
                idx = i;
                break;
            }
        }
        HwLog.d(TAG, "getMaxPointIdx return:" + idx);
        return idx;
    }

    public static Point getMaxSuportPoint() {
        Point maxPoint = new Point();
        try {
            Stub.asInterface(ServiceManager.checkService("window")).getInitialDisplaySize(0, maxPoint);
            HwLog.e(TAG, "maxPoint:" + maxPoint);
        } catch (Exception e) {
            HwLog.e(TAG, e.toString(), e);
        }
        return maxPoint;
    }

    public static boolean getCurrentRogStatus() {
        if (getCurrentPoint().y >= getMaxSuportPoint().y) {
            return false;
        }
        return true;
    }

    public static Point getToSwitchPoint(int toSwitchMode) {
        Point maxPoint = getMaxSuportPoint();
        if (toSwitchMode == 0) {
            return maxPoint;
        }
        int targetHeight;
        int targetWidth;
        switch (maxPoint.y) {
            case 1920:
                targetHeight = 1280;
                targetWidth = (maxPoint.x * 1280) / maxPoint.y;
                break;
            case 2560:
                targetHeight = 1920;
                targetWidth = (maxPoint.x * 1920) / maxPoint.y;
                break;
            default:
                targetHeight = 1920;
                targetWidth = 1080;
                HwLog.i(TAG, "defaultHeight=" + maxPoint.y + " is illegal! use default.");
                break;
        }
        HwLog.i(TAG, "targetWidth=" + targetWidth + ";targetHeight=" + targetHeight);
        return new Point(targetWidth, targetHeight);
    }

    public static int getToSwitchMode() {
        return getCurrentRogStatus() ? 0 : 1;
    }

    public static Point getPersistentPoint() {
        return SIZE_TYPES[getMaxPointIdx() - 1];
    }

    public static boolean isSupportRog() {
        try {
            Integer value = (Integer) Class.forName(CLASS_NAME).getDeclaredMethod(METHOD_NAME, new Class[0]).invoke(null, null);
            HwLog.i(TAG, "isSupportRog  value ==" + value);
            if (value.intValue() == 1) {
                return true;
            }
            return false;
        } catch (ClassNotFoundException e) {
            HwLog.e(TAG, e.toString(), e);
            return false;
        } catch (SecurityException e2) {
            HwLog.e(TAG, e2.toString(), e2);
            return false;
        } catch (NoSuchMethodException e3) {
            HwLog.e(TAG, e3.toString(), e3);
            return false;
        } catch (IllegalArgumentException e4) {
            HwLog.e(TAG, e4.toString(), e4);
            return false;
        } catch (IllegalAccessException e5) {
            HwLog.e(TAG, e5.toString(), e5);
            return false;
        } catch (InvocationTargetException e6) {
            HwLog.e(TAG, e6.toString(), e6);
            return false;
        } catch (Exception e7) {
            HwLog.e(TAG, e7.toString(), e7);
            return false;
        }
    }

    public static boolean is16To10Device() {
        Point maxPoint = getMaxSuportPoint();
        int i = 0;
        while (i < SIZE_TYPES_16_TO_10.length) {
            if (maxPoint.x == SIZE_TYPES_16_TO_10[i].x && maxPoint.y == SIZE_TYPES_16_TO_10[i].y) {
                return true;
            }
            i++;
        }
        return false;
    }

    public static double getPowerProfit() {
        if (getLowResolutionSwitchState(GlobalContext.getContext())) {
            return POWER_PROFIT[1];
        }
        return POWER_PROFIT[0];
    }

    public static int getToSwitchROGDensity(int toSwitchMode) {
        int dpi = SystemProperties.getInt("persist.sys.dpi", 0);
        int realdpi = SystemProperties.getInt("ro.sf.lcd_density", 0);
        int defaultRogdpi = (getToSwitchPoint(1).y * realdpi) / getToSwitchPoint(0).y;
        if (toSwitchMode == 0) {
            if (dpi <= 0) {
                dpi = realdpi;
            }
            return dpi;
        }
        if (dpi > 0) {
            defaultRogdpi = (defaultRogdpi * dpi) / realdpi;
        }
        return defaultRogdpi;
    }

    public static boolean isCharging(int mBatteryStatus) {
        if (mBatteryStatus == 2 || mBatteryStatus == 5) {
            return true;
        }
        return false;
    }

    public static double format2decimal(double value) {
        return ((double) Math.round(value * 100.0d)) / 100.0d;
    }

    public static int getScreenWidth(Context mAppContext) {
        Display display = ((WindowManager) mAppContext.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        return displayMetrics.widthPixels < displayMetrics.heightPixels ? displayMetrics.widthPixels : displayMetrics.heightPixels;
    }

    public static void enterSuperPowerSavingMode(Context mAppContext) {
        String batteryCapacityLevel = String.valueOf(BatteryInfo.getBatteryLevelValue()) + mAppContext.getString(R.string.percent_identifier);
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.valueOf(System.currentTimeMillis()));
        HsmStat.statE(PowerSavingMgr.ACTION_ENTER_SUPER_POWER_NOTIFICATION, "p", batteryCapacityLevel, "t", currentTime);
        SystemProperties.set("sys.super_power_save", "true");
        HwLog.d(TAG, " sys.super_power_save = " + SystemProperties.getBoolean("sys.super_power_save", false));
        PowerModeControl.getInstance(mAppContext).recordBatteryPercentStatusForSuperMode();
        System.putInt(mAppContext.getContentResolver(), PowerModeControl.DB_BATTERY_PERCENT_SWITCH, 1);
        Intent mIntent = new Intent(ActionConst.INTENT_CHANGE_POWER_MODE);
        mIntent.putExtra("power_mode", 3);
        mIntent.addFlags(ShareCfg.PERMISSION_MODIFY_CALENDAR);
        mAppContext.sendBroadcast(mIntent, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
        Intent callPowerGenieIntent = new Intent(ActionConst.INTENT_USE_POWER_GENIE_CHANGE_MODE);
        callPowerGenieIntent.putExtra(AdBlockColumns.COLUMN_ENABLE, true);
        mAppContext.sendBroadcast(callPowerGenieIntent, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
        Global.putInt(mAppContext.getContentResolver(), FLASHLIGHT_CURRENT_STATE, 0);
        AppOpsManager appOps = (AppOpsManager) mAppContext.getSystemService("appops");
        String mmsPkgName = MmsInfo.getSmsAppName(mAppContext);
        HwLog.i(TAG, "mmsPkgName = " + mmsPkgName);
        try {
            appOps.setMode(15, mAppContext.getPackageManager().getApplicationInfo(mmsPkgName, 8192).uid, mmsPkgName, 0);
        } catch (NameNotFoundException e) {
        }
        Secure.putString(mAppContext.getContentResolver(), "sms_default_application", mmsPkgName);
        try {
            int[] runningUsers = ActivityManagerNative.getDefault().getRunningUserIds();
            for (int userId : runningUsers) {
                if (userId != 0) {
                    M2NAdapter.stopUser(ActivityManagerNative.getDefault(), userId, true);
                    HwLog.i(TAG, "User = " + userId + " has been stopped .");
                }
            }
        } catch (RemoteException e2) {
            e2.printStackTrace();
        }
        PowerNotificationUtils.canclePowerModeNotification(mAppContext);
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "2");
        HsmStat.statE(Events.E_POWER_POWERMODE_SELECT, statParam);
    }

    private static int getScreenHeight(Context mAppContext) {
        Display display = ((WindowManager) mAppContext.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        return displayMetrics.widthPixels > displayMetrics.heightPixels ? displayMetrics.widthPixels : displayMetrics.heightPixels;
    }

    public static boolean getLowResolutionSwitchState(Context mContext) {
        int state;
        if (SystemProperties.getBoolean("sys.aps.2kenablesdrdefault", false)) {
            state = Global.getInt(mContext.getContentResolver(), ApplicationConstant.LOW_RESOLUTION_SWITCH_STATUS, -1);
            if (state == -1) {
                if (getScreenHeight(mContext) >= Events.E_PERMISSION_RECOMMEND_CLICK) {
                    state = 1;
                } else {
                    state = 0;
                }
                Global.putInt(mContext.getContentResolver(), ApplicationConstant.LOW_RESOLUTION_SWITCH_STATUS, state);
            }
        } else {
            state = Global.getInt(mContext.getContentResolver(), ApplicationConstant.LOW_RESOLUTION_SWITCH_STATUS, 0);
        }
        if (state == 0) {
            return false;
        }
        return true;
    }

    public static void setLowResolutionSwitchState(Context mContext, boolean state) {
        int i = 1;
        ContentResolver contentResolver = mContext.getContentResolver();
        String str = ApplicationConstant.LOW_RESOLUTION_SWITCH_STATUS;
        if (!state) {
            i = 0;
        }
        Global.putInt(contentResolver, str, i);
        try {
            Class<?> hwNSDImplClass = Class.forName("android.view.HwNsdImpl");
            Object hwNsdImplObject = hwNSDImplClass.getMethod("getDefault", new Class[0]).invoke(null, new Object[0]);
            hwNSDImplClass.getDeclaredMethod("setLowResolutionMode", new Class[]{Context.class, Boolean.TYPE}).invoke(hwNsdImplObject, new Object[]{mContext, Boolean.valueOf(state)});
            HwLog.i(TAG, "setLowResolutionSwitchState, state = " + state);
        } catch (ClassNotFoundException e) {
            HwLog.e(TAG, e.toString(), e);
        } catch (NoSuchMethodException e2) {
            HwLog.e(TAG, e2.toString(), e2);
        } catch (IllegalAccessException e3) {
            HwLog.e(TAG, e3.toString(), e3);
        } catch (IllegalArgumentException e4) {
            HwLog.e(TAG, e4.toString(), e4);
        } catch (InvocationTargetException e5) {
            HwLog.e(TAG, e5.toString(), e5);
        } catch (Exception e6) {
            HwLog.e(TAG, e6.toString(), e6);
        }
    }

    public static boolean isLowResolutionSupported() {
        try {
            Class<?> hwNSDImplClass = Class.forName("android.view.HwNsdImpl");
            boolean state = ((Boolean) hwNSDImplClass.getDeclaredMethod("isLowResolutionSupported", new Class[0]).invoke(hwNSDImplClass.getMethod("getDefault", new Class[0]).invoke(null, new Object[0]), new Object[0])).booleanValue();
            HwLog.i(TAG, "isLowResolutionSupported  = " + state);
            return state;
        } catch (ClassNotFoundException e) {
            HwLog.e(TAG, e.toString(), e);
            HwLog.i(TAG, "isRogSupported function is not existed, do not support Rog");
            return false;
        } catch (NoSuchMethodException e2) {
            HwLog.e(TAG, e2.toString(), e2);
            HwLog.i(TAG, "isRogSupported function is not existed, do not support Rog");
            return false;
        } catch (IllegalAccessException e3) {
            HwLog.e(TAG, e3.toString(), e3);
            HwLog.i(TAG, "isRogSupported function is not existed, do not support Rog");
            return false;
        } catch (IllegalArgumentException e4) {
            HwLog.e(TAG, e4.toString(), e4);
            HwLog.i(TAG, "isRogSupported function is not existed, do not support Rog");
            return false;
        } catch (InvocationTargetException e5) {
            HwLog.e(TAG, e5.toString(), e5);
            HwLog.i(TAG, "isRogSupported function is not existed, do not support Rog");
            return false;
        } catch (Exception e6) {
            HwLog.e(TAG, e6.toString(), e6);
            HwLog.i(TAG, "isRogSupported function is not existed, do not support Rog");
            return false;
        }
    }

    public static List<String> getBackgroundConsumeData(Context mContext) {
        List<String> tmpDataList = Lists.newArrayList();
        try {
            List<UidAndPower> bgAppConsumpList = PowerStatsHelper.newInstance(mContext, true).computerBackgroundConsumption(mContext, true);
            Integer isWakeUpApp = Integer.valueOf(0);
            Integer rogueType = Integer.valueOf(0);
            for (UidAndPower uap : bgAppConsumpList) {
                List<ApplicationInfo> appInfos = getAppInfoByUid(mContext, uap.getUid());
                HwLog.e(TAG, "getBackgroundConsumeData loop print UidPower: " + uap);
                if (appInfos.size() < 1) {
                    HwLog.w(TAG, "the size of appInfos less than one");
                } else {
                    if (appInfos.size() > 1) {
                        for (int i = 0; i < appInfos.size(); i++) {
                            if (SavingSettingUtil.isPkgNameExistInRogueDB(mContext.getContentResolver(), ((ApplicationInfo) appInfos.get(i)).packageName)) {
                                HwLog.e(TAG, "swap sharedUid appInfos:  0  and  " + i);
                                Collections.swap(appInfos, 0, i);
                                break;
                            }
                        }
                    }
                    try {
                        isWakeUpApp = (Integer) SavingSettingUtil.getRogue(mContext.getContentResolver(), ((ApplicationInfo) appInfos.get(0)).packageName, 4);
                        rogueType = (Integer) SavingSettingUtil.getRogue(mContext.getContentResolver(), ((ApplicationInfo) appInfos.get(0)).packageName, 7);
                    } catch (SQLException e) {
                        HwLog.e(TAG, "The HwSystemManager db RuntimeException!!");
                        e.printStackTrace();
                    }
                    if (rogueType == null) {
                        rogueType = Integer.valueOf(0);
                    }
                    if (isWakeUpApp == null) {
                        isWakeUpApp = Integer.valueOf(0);
                    }
                    HwLog.i(TAG, "getBackgroundConsumeData UID = " + uap.getUid() + "###isWakeUpApp = " + isWakeUpApp + "packageName = " + ((ApplicationInfo) appInfos.get(0)).packageName + ", rogueType: " + rogueType);
                    if (isInValidType(rogueType.intValue(), uap.getUid())) {
                        HwLog.d(TAG, "not show uap.uid= " + uap.getUid());
                    } else {
                        ApplicationInfo appInfo = (ApplicationInfo) appInfos.get(0);
                        if (appInfo != null) {
                            tmpDataList.add(appInfo.packageName);
                        }
                    }
                }
            }
            return tmpDataList;
        } catch (PowerStatsException e2) {
            HwLog.e(TAG, "getBackgroundConsumeData catch PowerStatsException");
            e2.printStackTrace();
            return tmpDataList;
        }
    }

    public static boolean isInValidType(int rogueType, int uid) {
        if (rogueType == 0 || rogueType == 4 || uid == 1000) {
            return true;
        }
        return false;
    }

    public static boolean isAmoledPanel() {
        String lineValue = DiskFileReader.readFileByString(FILEPATH);
        boolean resutl = false;
        if (!lineValue.equals("")) {
            for (String temp : lineValue.trim().split(ConstValues.SEPARATOR_KEYWORDS_EN)) {
                if (temp.startsWith(PREX) && temp.split(":")[1].equalsIgnoreCase(AMOLED)) {
                    resutl = true;
                }
            }
        }
        HwLog.i(TAG, "isAmoledPanel = " + resutl);
        return resutl;
    }

    public static boolean isDarkThemeApplied() {
        if (System.getIntForUser(GlobalContext.getContext().getContentResolver(), DarkThemeChanageService.DB_DARK_THEME, 0, -2) == 0) {
            return false;
        }
        return true;
    }

    public static void initDarkThemeStateForAppStart() {
        if (ProcessUtil.getInstance().isServiceProcess()) {
            SharePrefWrapper.setPrefValue(GlobalContext.getContext(), SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.DARK_THEME_SWITCH_KEY, true);
        }
    }

    public static double getAmoledProfit() {
        if (!isAmoledPanel()) {
            return AMOLED_DARK_THEME_PROFIT[0];
        }
        if (isDarkThemeApplied()) {
            return AMOLED_DARK_THEME_PROFIT[1];
        }
        return AMOLED_DARK_THEME_PROFIT[0];
    }

    public static void setSuperHighPowerSwitchState(Context mContext, boolean state) {
        System.putInt(mContext.getContentResolver(), SharedPrefKeyConst.SUPER_HIGH_POWER_SWITCH_KEY, state ? 1 : 0);
    }

    public static boolean getSuperHighPowerSwitchState(Context mContext) {
        if (System.getInt(mContext.getContentResolver(), SharedPrefKeyConst.SUPER_HIGH_POWER_SWITCH_KEY, 1) == 1) {
            return true;
        }
        return false;
    }

    public static boolean isOtherUserProcess() {
        int procUserId = UserHandle.myUserId();
        int currUserId = ActivityManager.getCurrentUser();
        HwLog.i(TAG, "procUserId= " + procUserId + " currUserId= " + currUserId);
        if (currUserId == procUserId) {
            return false;
        }
        HwLog.i(TAG, "procUserId is not equal with currUserId,procUserId = " + procUserId + " currUserId =" + currUserId);
        return true;
    }
}
