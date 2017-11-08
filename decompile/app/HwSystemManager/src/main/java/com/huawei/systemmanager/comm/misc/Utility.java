package com.huawei.systemmanager.comm.misc;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.power.model.RemainingTimeSceneHelper;
import com.huawei.systemmanager.power.service.DarkThemeChanageService;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.sdk.tmsdk.TMSdkEngine;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import com.huawei.systemmanager.widget.OneKeyCleanActivity;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;

public class Utility {
    public static final float ALPHA_MAX = 1.0f;
    private static final float AlphaDisable = 0.3f;
    private static final float AlphaDisableInDarkTheme = 0.7f;
    public static final long FIVE_YEAR = 157680000000L;
    public static final String HSM_PACKAGE_NAME = "com.huawei.systemmanager";
    public static final long ONE_HOUR = 3600000;
    public static final int OPEN_FILE_MSG_FAILED = 2;
    public static final int OPEN_FILE_MSG_FAILED_NOT_EXIT = 3;
    public static final int OPEN_FILE_MSG_SUCCESSED = 1;
    private static final String REGISTED_FILE_NAME = "push_registe_token";
    private static final String REGISTED_KEY = "isTokenRegistered";
    private static final String REGISTED_TOKEN_KEY = "registeToken";
    private static final String ROM_VERSION = "rom_vesion";
    public static final String SDK_API_PERMISSION = "com.huawei.permission.sec.MDM_PHONE_MANAGER";
    private static final boolean SUPER_POWER_MODE_ENABLE = SystemProperties.getBoolean("ro.config.show_superpower", true);
    public static final String SYSTEM_MANAGER_PERMISSION = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
    public static final String TAG = "Utility";
    private static boolean isDataOnly = false;
    private static boolean isWifiOnly = false;
    private static final Object sObject = new Object();

    private static float getAlphaDisable(Context context) {
        if (context == null) {
            HwLog.i(TAG, "context is null");
            return AlphaDisable;
        }
        ContentResolver cr = context.getContentResolver();
        if (cr == null) {
            HwLog.i(TAG, "ContentResolver is null");
            return AlphaDisable;
        } else if (System.getIntForUser(cr, DarkThemeChanageService.DB_DARK_THEME, 0, -2) == 1) {
            return AlphaDisableInDarkTheme;
        } else {
            return AlphaDisable;
        }
    }

    public static boolean ifFloatEqual(float f1, float f2) {
        return Float.compare(f1, f2) == 0;
    }

    public static <T> boolean isNullOrEmptyList(List<T> list) {
        if (list == null || list.size() <= 0) {
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmptyCursor(Cursor cursor, boolean isCloseIfEmpty) {
        if (cursor == null) {
            return true;
        }
        if (cursor.getCount() > 0) {
            return false;
        }
        if (isCloseIfEmpty) {
            try {
                cursor.close();
            } catch (Exception e) {
                HwLog.e(TAG, "", e);
            }
        }
        return true;
    }

    public static int dip2px(float dipValue) {
        return dip2px(GlobalContext.getContext(), dipValue);
    }

    public static int px2dip(float pxValue) {
        return px2dip(GlobalContext.getContext(), pxValue);
    }

    public static int dip2px(Context context, float dipValue) {
        return (int) ((dipValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static int getDisplayMetricsWidth() {
        return getDisplayMetricsWidth(GlobalContext.getContext());
    }

    public static int getDisplayMetricsWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService("window");
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static int px2dip(Context context, float pxValue) {
        return (int) ((pxValue / context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static boolean isUidSystem(int uid) {
        int appid = UserHandle.getAppId(uid);
        if (appid == 1000 || appid == 1001 || uid == 0) {
            return true;
        }
        return false;
    }

    public static boolean isLowRamDevice() {
        if (SystemProperties.getBoolean("ro.config.low_ram", false)) {
            return true;
        }
        return SystemProperties.getBoolean("ro.config.hw_low_ram", false);
    }

    public static boolean superPowerEntryEnable() {
        return SUPER_POWER_MODE_ENABLE;
    }

    public static boolean checkBroadcast(Context context, Intent intent) {
        if (context == null || intent == null || TextUtils.isEmpty(intent.getAction())) {
            return false;
        }
        return true;
    }

    public static Intent openReceivedFile(Context context, String fileName, String mimetype) {
        HwLog.d(TAG, "fileName= " + fileName + "mimetype= " + mimetype);
        if (fileName == null || mimetype == null) {
            HwLog.e(TAG, "ERROR: Para fileName ==null, or mimetype == null");
            return null;
        } else if (!new File(fileName).exists()) {
            return null;
        } else {
            Uri path = Uri.parse(fileName);
            if (path.getScheme() == null) {
                path = Uri.fromFile(new File(fileName));
            }
            if (!isRecognizedFileType(context, path, mimetype)) {
                return null;
            }
            Intent activityIntent = new Intent("android.intent.action.VIEW");
            activityIntent.setDataAndTypeAndNormalize(path, mimetype);
            HwLog.i(TAG, "ACTION_VIEW intent sent out: " + path + " / " + mimetype);
            return activityIntent;
        }
    }

    public static boolean isRecognizedFileType(Context context, Uri fileUri, String mimetype) {
        HwLog.d(TAG, "RecognizedFileType() fileUri: " + fileUri + " mimetype: " + mimetype);
        Intent mimetypeIntent = new Intent("android.intent.action.VIEW");
        mimetypeIntent.setDataAndTypeAndNormalize(fileUri, mimetype);
        if (PackageManagerWrapper.queryIntentActivities(context.getPackageManager(), mimetypeIntent, 65536).size() != 0) {
            return true;
        }
        HwLog.d(TAG, "NO application to handle MIME type " + mimetype);
        return false;
    }

    public static boolean checkIntentAlivable(Context ctx, Intent intent) {
        boolean z = false;
        if (ctx == null || intent == null) {
            return false;
        }
        if (intent.resolveActivity(ctx.getPackageManager()) != null) {
            z = true;
        }
        return z;
    }

    public static int getStaticIntFiled(String clazzName, String fieldName) {
        try {
            return Class.forName(clazzName).getField(fieldName).getInt(null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch (NoSuchFieldException e2) {
            e2.printStackTrace();
            return 0;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return 0;
        } catch (Exception e4) {
            e4.printStackTrace();
            return 0;
        }
    }

    public static void initWifiDataOnlyStatus() {
        boolean z = false;
        boolean z2 = (isVoiceCapable() || isSmsCapable()) ? false : true;
        isWifiOnly = z2;
        if (!isVoiceCapable()) {
            z = isSmsCapable();
        }
        isDataOnly = z;
    }

    public static boolean isWifiOnlyMode() {
        return isWifiOnly;
    }

    public static boolean isTokenRegistered(Context context) {
        SharedPreferences sf = context.getSharedPreferences(REGISTED_FILE_NAME, 0);
        if (sf.getBoolean(REGISTED_KEY, false)) {
            return Build.DISPLAY.equals(sf.getString(ROM_VERSION, ""));
        }
        return false;
    }

    public static void updateRegistedKey(Context context) {
        SharedPreferences sf = context.getSharedPreferences(REGISTED_FILE_NAME, 0);
        if (!Build.DISPLAY.equals(sf.getString(ROM_VERSION, ""))) {
            sf.edit().putBoolean(REGISTED_KEY, false).commit();
        }
    }

    public static boolean setTokenRegistered(Context context, boolean registered) {
        SharedPreferences sf = context.getSharedPreferences(REGISTED_FILE_NAME, 0);
        sf.edit().putBoolean(REGISTED_KEY, registered).commit();
        return sf.edit().putString(ROM_VERSION, Build.DISPLAY).commit();
    }

    public static String getReisteToken(Context context) {
        return context.getSharedPreferences(REGISTED_FILE_NAME, 0).getString(REGISTED_TOKEN_KEY, "");
    }

    public static boolean setRegisteToken(Context context, String token) {
        return context.getSharedPreferences(REGISTED_FILE_NAME, 0).edit().putString(REGISTED_TOKEN_KEY, token).commit();
    }

    public static boolean isDataOnlyMode() {
        return isDataOnly;
    }

    public static boolean isVoiceCapable() {
        Resources res = GlobalContext.getContext().getResources();
        if (res == null) {
            HwLog.i(TAG, "Resources is null in VoiceCapable");
            return false;
        }
        int resid = res.getIdentifier("config_voice_capable", "bool", "com.android.internal");
        if (resid > 0) {
            return res.getBoolean(resid);
        }
        resid = res.getIdentifier("config_voice_capable", "bool", "android");
        return resid > 0 ? res.getBoolean(resid) : true;
    }

    public static boolean isSmsCapable() {
        Resources res = GlobalContext.getContext().getResources();
        if (res == null) {
            HwLog.i(TAG, "Resources is null in SmsCapable");
            return false;
        }
        int resid = res.getIdentifier("config_sms_capable", "bool", "com.android.internal");
        if (resid > 0) {
            return res.getBoolean(resid);
        }
        resid = res.getIdentifier("config_sms_capable", "bool", "android");
        return resid > 0 ? res.getBoolean(resid) : true;
    }

    public static boolean isFirstBoot(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SystemManagerConst.SYSTEM_MANAGER_PERFERENCE, 4);
        boolean result = sp.getBoolean(SystemManagerConst.KEY_FIRST_BOOT, true);
        if (result) {
            sp.edit().putBoolean(SystemManagerConst.KEY_FIRST_BOOT, false).commit();
        }
        return result;
    }

    public static String getLocalPath(String path) {
        if (path.startsWith(Constant.LOCAL_FILE_PREFIXION)) {
            return path;
        }
        return Constant.LOCAL_FILE_PREFIXION + path;
    }

    public static String getLocalPath(int path) {
        return Constant.DRAWABLE_FILE_PREFIXION + String.valueOf(path);
    }

    public static int getScreenSmallWidth(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        HwLog.i(TAG, "getScreenSmallWidth, x:" + point.x + ", y:" + point.y + ", orientation:" + context.getResources().getConfiguration().orientation);
        return Math.min(point.x, point.y);
    }

    public static int getScreenLongHeight(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        HwLog.i(TAG, "getScreenSmallWidth, x:" + point.x + ", y:" + point.y + ", orientation:" + context.getResources().getConfiguration().orientation);
        return Math.max(point.x, point.y);
    }

    public static int getScreenHeight(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point.y;
    }

    public static boolean isOwnerUser() {
        return isOwnerUser(true);
    }

    public static boolean isOwnerUser(boolean showToast) {
        Context context = GlobalContext.getContext();
        if (UserHandle.myUserId() == 0) {
            HwLog.d(TAG, "The current user is owner , userid =  " + UserHandle.myUserId());
            return true;
        }
        HwLog.d(TAG, "The current user is not owner , userid =  " + UserHandle.myUserId());
        if (showToast) {
            Toast.makeText(context, context.getString(R.string.alert_toast_multi_users), 1).show();
        }
        return false;
    }

    public static boolean isOwner() {
        return UserHandle.myUserId() == 0;
    }

    public static boolean isSupportOrientation() {
        return GlobalContext.getContext().getResources().getBoolean(R.bool.IsSupportOrientation);
    }

    public static boolean isSupportSystemTheme() {
        return GlobalContext.getContext().getResources().getBoolean(R.bool.IsSupportSystemTheme);
    }

    public static boolean isNavBarOnBottomWhenLand() {
        return GlobalContext.getContext().getResources().getBoolean(R.bool.is_navbar_on_bottom_when_landscape);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isTestApp(String pkg) {
        IBinder iPM = ServiceManager.getService("package");
        if (iPM == null || pkg == null) {
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("huawei.com.android.server.IPackageManager");
            data.writeString(pkg);
            iPM.transact(Events.E_STARTUPMGR_AWAKED_STARTUP_PERIOD_STATISTICS, data, reply, 0);
            reply.readException();
            boolean test = reply.readInt() == 1;
            if (test) {
                HwLog.i(TAG, "Installed granted pkg:" + pkg);
            }
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
            return test;
        } catch (RemoteException e) {
            HwLog.e(TAG, "remote exception.", e);
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
        } catch (Exception e2) {
            HwLog.e(TAG, "remote exception.", e2);
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
        }
    }

    public static String getDefaultInputMethod(Context context) {
        String str = null;
        if (context == null) {
            return null;
        }
        String defInput = Secure.getString(context.getContentResolver(), "default_input_method");
        if (defInput == null) {
            return null;
        }
        String[] temp = defInput.split("/");
        if (temp.length > 0) {
            str = temp[0];
        }
        return str;
    }

    public static String getLocaleNumber(int number) {
        return NumberFormat.getInstance().format((long) number);
    }

    public static boolean isPhoto(String mimeType) {
        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }
        return mimeType.contains("image");
    }

    @FindBugsSuppressWarnings({"REC_CATCH_EXCEPTION"})
    public static void startSimpleVideoView(Context ct, String path) {
        if (TextUtils.isEmpty(path) || ct == null) {
            HwLog.i(TAG, "viewPhoto, arg is wrong");
            ToastUtils.toastShortMsg(GlobalContext.getString(R.string.space_clean_photo_not_exits_tip, path));
        } else if (new File(path).exists()) {
            try {
                Uri uri = Uri.parse(Constant.LOCAL_FILE_PREFIXION + path);
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setDataAndTypeAndNormalize(uri, "video/*");
                ct.startActivity(intent);
            } catch (Exception e) {
                HwLog.i(TAG, "viewPhoto, exception");
                ToastUtils.toastShortMsg(GlobalContext.getString(R.string.space_clean_photo_not_exits_tip, path));
            }
        } else {
            HwLog.i(TAG, "viewPhoto, file is null");
            ToastUtils.toastShortMsg(GlobalContext.getString(R.string.space_clean_photo_not_exits_tip, path));
        }
    }

    public static void setViewEnabled(View view, boolean enable) {
        if (view != null) {
            if (view instanceof ViewGroup) {
                setViewGroupEnabled((ViewGroup) view, enable);
            } else {
                view.setEnabled(enable);
                view.setAlpha(enable ? ALPHA_MAX : getAlphaDisable(view.getContext()));
            }
        }
    }

    public static void setViewGroupEnabled(ViewGroup vg, boolean enable) {
        if (vg != null) {
            vg.setEnabled(enable);
            vg.setAlpha(enable ? ALPHA_MAX : getAlphaDisable(vg.getContext()));
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                child.setEnabled(enable);
                child.setAlpha(enable ? ALPHA_MAX : getAlphaDisable(child.getContext()));
                if (child instanceof ViewGroup) {
                    setViewGroupEnabled((ViewGroup) child, enable);
                }
            }
        }
    }

    public static void initSDK(Context context) {
        synchronized (sObject) {
            if (TMSEngineFeature.shouldInitTmsEngine() && !TMSEngineFeature.isSupportTMS()) {
                HwLog.i(TAG, "service init SDK");
                try {
                    TMSdkEngine.initTMSDK(context);
                    TMSEngineFeature.setSupportTMS(true);
                } catch (Exception e) {
                    HwLog.e(TAG, "init tms5 engine failed");
                    e.printStackTrace();
                } catch (Error error) {
                    HwLog.e(TAG, "init tms5 engine failed:" + error);
                }
            }
        }
    }

    public static boolean hasUnparcelException(Intent intent) {
        if (intent == null) {
            return false;
        }
        try {
            intent.getStringExtra("test");
            return false;
        } catch (RuntimeException e) {
            HwLog.w(TAG, "hasUnparcelException", e);
            return true;
        }
    }

    public static Context getEmuiContext(Context ctx) {
        Resources res = ctx.getResources();
        if (res != null) {
            int themeId = res.getIdentifier(OneKeyCleanActivity.EMUI_THEME, null, null);
            if (themeId != 0) {
                return new ContextThemeWrapper(ctx, themeId);
            }
        }
        HwLog.w(TAG, "setEMUITheme got null Resources!");
        return ctx;
    }

    public static boolean isTablet(Context context) {
        return "tablet".equals(SystemProperties.get("ro.build.characteristics", RemainingTimeSceneHelper.DB_RECORD_DATE_DEFAULT));
    }
}
