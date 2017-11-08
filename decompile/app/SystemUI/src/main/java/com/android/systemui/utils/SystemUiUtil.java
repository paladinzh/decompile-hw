package com.android.systemui.utils;

import android.app.ActivityManager;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.StatusBarManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.service.notification.StatusBarNotification;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.widget.Toast;
import com.android.systemui.HwSystemUIApplication;
import com.android.systemui.R;
import com.android.systemui.utils.analyze.PerfDebugUtils;
import com.android.systemui.utils.badgedicon.BadgedIconHelper;
import fyusion.vislib.BuildConfig;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SystemUiUtil {
    private static final boolean IS_LOW_RAMDEVICE;
    public static final boolean NAVBAR_REMOVABLE = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final String OPTA = SystemProperties.get("ro.config.hw_opta", "0");
    private static final String OPTB = SystemProperties.get("ro.config.hw_optb", "0");
    private static final boolean SKYTONE_SWITCH = SystemProperties.getBoolean("ro.radio.vsim_support", false);
    private static boolean hiddenfrombutton = false;
    private static final boolean isChinaMobile;
    private static final boolean isChinaTelecom = (SystemProperties.get("ro.config.hw_opta", "0").equals("92") ? isChinaArea() : false);
    private static final boolean isChinaUnicom;
    static int[] mCurrentNetWorkType = new int[3];
    private static final boolean mIsChina = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", BuildConfig.FLAVOR));
    static Boolean sMultiSim = null;

    static {
        boolean z;
        if (ActivityManager.isLowRamDeviceStatic()) {
            z = true;
        } else {
            z = SystemProperties.getBoolean("ro.config.hw_low_ram", false);
        }
        IS_LOW_RAMDEVICE = z;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("17")) {
            z = isChinaArea();
        } else {
            z = false;
        }
        isChinaUnicom = z;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("01")) {
            z = isChinaArea();
        } else {
            z = false;
        }
        isChinaMobile = z;
    }

    public static String subZeroAndDot(String s) {
        if (s == null) {
            return null;
        }
        if (s.contains(".")) {
            s = s.replaceAll("0+?$", BuildConfig.FLAVOR).replaceAll("[.]$", BuildConfig.FLAVOR);
        }
        return s;
    }

    public static String getMemoryString(double size) {
        NumberFormat nf = NumberFormat.getInstance();
        int value = (int) size;
        if (value < 1024) {
            return String.valueOf(nf.format((long) value));
        }
        nf.setMaximumFractionDigits(2);
        return subZeroAndDot(String.valueOf(nf.format(size / 1024.0d)));
    }

    public static void setNavigationBarHiddenbyButton(boolean staus) {
        hiddenfrombutton = staus;
    }

    public static boolean isFaAr(String s) {
        return ("ar".equals(s) || "fa".equals(s) || "iw".equals(s)) ? true : "ur".equals(s);
    }

    public static boolean isSimCardReady(Context context) {
        if (isMulityCard(context)) {
            TelephonyManager mSimTelManager = TelephonyManager.from(context);
            if (mSimTelManager.getSimState(0) == 5 || mSimTelManager.getSimState(1) == 5) {
                return true;
            }
        } else if (TelephonyManager.from(context).getSimState() == 5) {
            return true;
        }
        return false;
    }

    public static int getCurrentNetWorkTypeBySlotId(int slot) {
        if (slot >= 0 && slot < mCurrentNetWorkType.length) {
            return mCurrentNetWorkType[slot];
        }
        HwLog.e("SystemUiUtil", "getCurrentNetWorkTypeBySlotId::Beyond the scope of array index");
        return 16;
    }

    public static void setCurrentNetWorkTypeBySlotId(int slot, int type) {
        if (slot < 0 || slot >= mCurrentNetWorkType.length) {
            HwLog.e("SystemUiUtil", "setCurrentNetWorkTypeBySlotId::Beyond the scope of array index");
        } else {
            mCurrentNetWorkType[slot] = type;
        }
    }

    public static boolean isFloatequal(float x, float y) {
        return ((double) Math.abs(x - y)) < 1.0E-7d;
    }

    public static void addWindowView(WindowManager mWindowManager, View view, LayoutParams params) {
        HwLog.i("SystemUiUtil", "addWindowView:" + view.getClass().getSimpleName() + ", width=" + params.width + ", height=" + params.height + ", flag=0x" + Integer.toHexString(params.flags));
        try {
            mWindowManager.addView(view, params);
        } catch (Exception e) {
            Log.e("SystemUiUtil", "the exception happen in addWindowView, e=" + e.getMessage());
        }
    }

    public static void updateWindowView(WindowManager mWindowManager, View view, LayoutParams params) {
        HwLog.i("SystemUiUtil", "updateWindowView:" + view.getClass().getSimpleName() + ", width=" + params.width + ", height=" + params.height + ", flag=0x" + Integer.toHexString(params.flags));
        PerfDebugUtils.beginSystraceSection("SystemUiUtil_updateWindowView");
        try {
            mWindowManager.updateViewLayout(view, params);
        } catch (Exception e) {
            Log.e("SystemUiUtil", "the exception happen in updateWindowView, e=" + e.getMessage());
        }
        PerfDebugUtils.endSystraceSection();
    }

    public static void dismissKeyguard() throws RemoteException {
        WindowManagerGlobal.getWindowManagerService().dismissKeyguard();
    }

    public static boolean isMonkeyRunning() {
        return ActivityManager.isUserAMonkey();
    }

    public static void startActivityDismissingKeyguard(Context context, Intent intent) {
        try {
            dismissKeyguard();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        StatusBarManager statusBarManager = (StatusBarManager) context.getSystemService("statusbar");
        if (statusBarManager != null) {
            try {
                statusBarManager.collapsePanels();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        intent.setFlags(335544320);
        try {
            context.startActivityAsUser(intent, new UserHandle(-2));
        } catch (ActivityNotFoundException e3) {
            e3.printStackTrace();
        }
    }

    public static boolean isChina() {
        return mIsChina;
    }

    public static boolean isIntentExist(Context context, Intent intent) {
        List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentActivities(intent, 65536);
        if (resolveInfo == null || resolveInfo.size() <= 0) {
            return false;
        }
        return true;
    }

    public static boolean isMulityCard(Context context) {
        if (sMultiSim != null) {
            return sMultiSim.booleanValue();
        }
        sMultiSim = Boolean.valueOf(TelephonyManager.from(context).isMultiSimEnabled());
        return sMultiSim.booleanValue();
    }

    public static RectF generateRectF(float left, float top, float right, float bottom, RectF defautRectF) {
        if (defautRectF == null) {
            return new RectF(left, top, right, bottom);
        }
        defautRectF.set(left, top, right, bottom);
        return defautRectF;
    }

    private static float getPrettyNumber(double number, int scale) {
        return new BigDecimal(number).setScale(scale, 4).stripTrailingZeros().floatValue();
    }

    public static String formatFileSize(Context context, long number) {
        if (context == null) {
            return BuildConfig.FLAVOR;
        }
        float value;
        String formatStr;
        float result = (float) number;
        int suffix = 17039497;
        if (result > 900.0f) {
            suffix = 17039498;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = 17039499;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = 17039500;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = 17039501;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = 17039502;
            result /= 1024.0f;
        }
        if (((double) result) < 0.01d) {
            value = getPrettyNumber((double) result, 0);
        } else if (result < 1.0f) {
            value = getPrettyNumber((double) result, 2);
        } else if (result < 10.0f) {
            value = getPrettyNumber((double) result, 1);
        } else {
            value = getPrettyNumber((double) result, 0);
        }
        Locale locale = context.getResources().getConfiguration().locale;
        if (locale == null) {
            HwLog.w("SystemUiUtil", "formatFileSize::locale is null!");
            formatStr = NumberLocationPercent.getFormatnumberString(value);
        } else {
            formatStr = NumberLocationPercent.getFormatnumberString(value, locale);
        }
        return context.getResources().getString(17039503, new Object[]{formatStr, context.getString(suffix)});
    }

    public static boolean isCalling(Context context) {
        int phoneState = TelephonyManager.from(context).getCallState();
        HwLog.d("SystemUiUtil", "isRinging phoneState:" + phoneState);
        if (phoneState != 0) {
            return true;
        }
        return false;
    }

    public static boolean isChinaArea() {
        return SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    }

    public static PackageManager getPackageManagerForUser(Context context, int userId) {
        return getContextForUser(context, userId).getPackageManager();
    }

    public static Context getContextForUser(Context context, int userId) {
        return getContextForUser(context, userId, context.getPackageName());
    }

    public static Context getContextForUser(Context context, int userId, String pkgName) {
        Context contextForUser = context;
        if (userId >= 0) {
            try {
                contextForUser = context.createPackageContextAsUser("android", 4, new UserHandle(userId));
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return contextForUser;
    }

    public static boolean isChinaTelecomArea() {
        return isChinaTelecom;
    }

    public static boolean isChinaUnicomArea() {
        return isChinaUnicom;
    }

    public static boolean isChinaMobileArea() {
        return isChinaMobile;
    }

    public static Context getContextCurrentUser(Context context) {
        return getContextForUser(context, UserSwitchUtils.getCurrentUser());
    }

    public static boolean isOwner() {
        return UserSwitchUtils.getCurrentUser() == 0;
    }

    public static boolean isSupportVSim() {
        return SKYTONE_SWITCH;
    }

    public static boolean isNeedRemove4GSwitch() {
        return SystemProperties.getBoolean("ro.config.hw_remove_networkmode", true);
    }

    public static boolean isWifiOnly(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm == null || cm.isNetworkSupported(0)) {
            return false;
        }
        return true;
    }

    public static boolean isVoiceCapable(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService("phone");
        return telephony != null ? telephony.isVoiceCapable() : false;
    }

    public static boolean isIntentAvailable(Context context, Intent intent) {
        boolean z = true;
        try {
            if (context.getPackageManager().queryIntentActivities(intent, 1).size() <= 0) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            HwLog.e("SystemUiUtil", "isIntentAvailable::occur exception: " + e);
            return false;
        }
    }

    public static boolean isPackageExist(Context context, String pkgName) {
        try {
            HwLog.i("SystemUiUtil", pkgName + " package is exist, pInfo=" + context.getPackageManager().getPackageInfo(pkgName, 0));
            return true;
        } catch (NameNotFoundException e) {
            HwLog.i("SystemUiUtil", pkgName + " package is not exist of NameNotFoundException: " + e);
            return false;
        } catch (Exception e2) {
            HwLog.i("SystemUiUtil", pkgName + " package is not exist: " + e2);
            return false;
        }
    }

    public static boolean isLandscape() {
        return 2 == HwSystemUIApplication.getContext().getResources().getConfiguration().orientation;
    }

    public static boolean getNavibarAlignLeftWhenLand(Context mContext) {
        String status = getFieldFromDB(mContext, "content://com.huawei.vdrive.mirrorlinktogo/mirrorlink_status", "connected");
        if (status == null || !status.equals("1")) {
            return false;
        }
        return true;
    }

    public static String getFieldFromDB(Context context, String uri, String field) {
        Cursor cursor = null;
        String str = null;
        try {
            cursor = context.getContentResolver().query(Uri.parse(uri), new String[]{field}, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                str = cursor.getString(0);
            }
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    HwLog.i("SystemUiUtil", " close Cursor Exception: " + e);
                }
            }
        } catch (Exception e2) {
            HwLog.i("SystemUiUtil", " getFieldFromDB Exception: " + e2);
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e22) {
                    HwLog.i("SystemUiUtil", " close Cursor Exception: " + e22);
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e222) {
                    HwLog.i("SystemUiUtil", " close Cursor Exception: " + e222);
                }
            }
        }
        return str;
    }

    public static boolean isCurrentSupportLTE(Context context) {
        int subscription = HwTelephonyManager.getDefault().getPreferredDataSubscription();
        int slot = System.getInt(context.getContentResolver(), "switch_dual_card_slots", 0);
        HwLog.i("SystemUiUtil", "isCurrentSupportLTE : subscription = " + subscription + ", slot = " + slot);
        if (subscription == slot) {
            return true;
        }
        return false;
    }

    public static ContextThemeWrapper getHwThemeContext(Context context, String theme) {
        if (context == null) {
            HwLog.e("SystemUiUtil", "error getHwThemeContext context is null");
            return null;
        } else if (TextUtils.isEmpty(theme)) {
            return (ContextThemeWrapper) context;
        } else {
            Context context2 = null;
            int themeID = context.getResources().getIdentifier(theme, null, null);
            if (themeID > 0) {
                context2 = new ContextThemeWrapper(context, themeID);
            }
            return (ContextThemeWrapper) (context2 == null ? context : context2);
        }
    }

    public static ContextThemeWrapper getHwThemeLightContext(Context context) {
        return getHwThemeContext(context, "androidhwext:style/Theme.Emui");
    }

    public static boolean allowLogEvent(MotionEvent event) {
        return event.getAction() == 0 || event.getAction() == 3 || event.getAction() == 1;
    }

    private static boolean isWakeupWhenReceiveNotificationEnabled(Context context) {
        boolean z = false;
        if (context == null) {
            HwLog.i("SystemUiUtil", "isWakeupWhenReceiveNotificationEnabled context is null");
            return false;
        }
        int currentMode = 0;
        try {
            currentMode = Global.getInt(context.getContentResolver(), "zen_mode");
        } catch (SettingNotFoundException e) {
            HwLog.e("SystemUiUtil", "Get current zen mode fail, default mode is OFF");
        }
        if (currentMode != 0) {
            HwLog.i("SystemUiUtil", "zen_mode_on");
            return false;
        }
        if (getSecureInt(context, "wakeup_when_receive_notification", 0) != 0) {
            z = true;
        }
        return z;
    }

    private static int getSecureInt(Context context, String name, int def) {
        return Secure.getIntForUser(context.getContentResolver(), name, def, UserSwitchUtils.getCurrentUser());
    }

    public static float getX(View child, View root) {
        if (child == null || root == null || child == root) {
            return 0.0f;
        }
        float x = child.getX();
        Object parent = child.getParent();
        while (parent != null && (parent instanceof View) && ((View) parent) != root) {
            x += ((View) parent).getX();
            parent = parent.getParent();
        }
        if (parent != root) {
            HwLog.w("SystemUiUtil", "Can not find root! child = " + child.toString() + " root = " + root.toString());
        }
        return x;
    }

    public static float getY(View child, View root) {
        if (child == null || root == null || child == root) {
            return 0.0f;
        }
        float y = child.getY();
        Object parent = child.getParent();
        while (parent != null && (parent instanceof View) && ((View) parent) != root) {
            y += ((View) parent).getY();
            parent = parent.getParent();
        }
        if (parent != root) {
            HwLog.w("SystemUiUtil", "Can not find root! child = " + child.toString() + " root = " + root.toString());
        }
        return y;
    }

    public static void wakeScreenOnIfNeeded(Context context) {
        if (isWakeupWhenReceiveNotificationEnabled(context)) {
            PowerManager pm = (PowerManager) context.getSystemService("power");
            if (!(pm == null || pm.isScreenOn())) {
                pm.wakeUp(SystemClock.uptimeMillis());
                HwLog.i("SystemUiUtil", "ScreenOn waked by notification");
            }
        }
    }

    public static boolean isMarketPlaceVersion() {
        if ("735".equals(OPTA) && "156".equals(OPTB)) {
            return true;
        }
        return false;
    }

    public static void sendForMarketPlaceNotifcation(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
        CharSequence title = context.getString(R.string.market_demo_notification_title);
        CharSequence text = context.getString(R.string.market_demo_notification_content);
        Builder builder = new Builder(context).setSmallIcon(BadgedIconHelper.getBitampIcon(context, R.drawable.ic_notify_market)).setContentTitle(title).setContentText(text).setStyle(new BigTextStyle().bigText(text)).setOngoing(true);
        Bundle extras = new Bundle();
        extras.putString("android.substName", context.getString(R.string.market_demo_notification_name));
        builder.addExtras(extras);
        mNotificationManager.cancelAsUser(null, R.id.market_place_notification, UserHandle.ALL);
        mNotificationManager.notifyAsUser(null, R.id.market_place_notification, builder.build(), UserHandle.ALL);
        HwLog.i("SystemUiUtil", "SendForMallShowNotifcation");
    }

    public static boolean isMarketPlaceSbn(StatusBarNotification sbn) {
        if (sbn != null && "com.android.systemui".equals(sbn.getPackageName()) && R.id.market_place_notification == sbn.getId() && isMarketPlaceVersion()) {
            return true;
        }
        return false;
    }

    public static boolean isCalibrationSbn(StatusBarNotification sbn) {
        if (sbn != null && "com.android.systemui".equals(sbn.getPackageName()) && 110 == sbn.getId()) {
            return true;
        }
        return false;
    }

    public static boolean isAndroidSecureNotification(StatusBarNotification sbn) {
        if ("android".equals(sbn.getPackageName()) && sbn.getId() == 0) {
            return true;
        }
        return false;
    }

    public static void showToastForAllUser(Context mContext, int message) {
        if (mContext != null) {
            Toast toast = Toast.makeText(mContext, message, 0);
            LayoutParams windowParams = toast.getWindowParams();
            windowParams.privateFlags |= 16;
            toast.show();
        }
    }

    public static void setViewVisibility(View view, int visibility) {
        if (view != null && visibility != view.getVisibility()) {
            view.setVisibility(visibility);
        }
    }

    public static boolean isDefaultLandOrientationProduct() {
        int defaultOrientation = SystemProperties.getInt("ro.panel.hw_orientation", 0);
        if (defaultOrientation == 90 || defaultOrientation == 270) {
            return true;
        }
        return false;
    }

    public static String getAppName(StatusBarNotification sbn) {
        PackageManager pmUser = getPackageManagerForUser(HwSystemUIApplication.getContext(), sbn.getUser().getIdentifier());
        try {
            return String.valueOf(pmUser.getApplicationLabel(pmUser.getApplicationInfo(sbn.getPackageName(), 8704)));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return BuildConfig.FLAVOR;
        }
    }

    public static boolean isTablet(Context context) {
        return "tablet".equals(SystemProperties.get("ro.build.characteristics", "default"));
    }

    public static boolean isShowNavigationBarFootView() {
        return SystemProperties.getBoolean("ro.config.navbar_support_slide", false);
    }

    public static boolean isNFCEnable(Context context) {
        NfcAdapter nfcAdapter = null;
        try {
            nfcAdapter = NfcAdapter.getNfcAdapter(context);
        } catch (UnsupportedOperationException e) {
            HwLog.e("SystemUiUtil", "isNFCEnable::UnsupportedOperationException " + e);
        } catch (Exception e2) {
            HwLog.e("SystemUiUtil", "isNFCEnable::Exception " + e2);
        }
        if (nfcAdapter != null) {
            return nfcAdapter.isEnabled();
        }
        Log.i("SystemUiUtil", "isNFCEnable::system nfc adapter is null");
        return false;
    }
}
