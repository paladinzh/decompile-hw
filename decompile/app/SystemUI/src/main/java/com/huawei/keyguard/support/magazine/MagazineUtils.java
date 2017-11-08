package com.huawei.keyguard.support.magazine;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import com.android.keyguard.R$drawable;
import com.android.keyguard.R$string;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.theme.HwThemeParser;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.OsUtils;
import fyusion.vislib.BuildConfig;
import java.io.File;

public class MagazineUtils {
    public static final String MAGAZINEDIR = ("/storage/emulated/0" + File.separator + "MagazineUnlock" + File.separator);
    private static final String OLD_MAGAZINEDIR = ("/storage/emulated/0" + File.separator + "Magazine unlock" + File.separator);
    private static boolean mBlockSwitchByPagerBusy = false;

    public static String getAppDownloadDialogText(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return BuildConfig.FLAVOR;
        }
        if ("com.android.mediacenter".equalsIgnoreCase(packageName)) {
            return context.getString(R$string.keyguard_download_dialog_hwmusic_message);
        }
        if ("com.huawei.hwvplayer.youku".equalsIgnoreCase(packageName)) {
            return context.getString(R$string.keyguard_download_dialog_video_youku_message);
        }
        if ("com.huawei.himovie".equalsIgnoreCase(packageName)) {
            return context.getString(R$string.keyguard_download_dialog_hwvideo_message);
        }
        if ("com.huawei.hwireader".equalsIgnoreCase(packageName)) {
            return context.getString(R$string.keyguard_download_dialog_hwreader_message);
        }
        return BuildConfig.FLAVOR;
    }

    public static String getAppUpdateDialogText(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return BuildConfig.FLAVOR;
        }
        if ("com.android.mediacenter".equalsIgnoreCase(packageName)) {
            return context.getString(R$string.keyguard_update_dialog_hwmusic_message);
        }
        if ("com.huawei.hwvplayer.youku".equalsIgnoreCase(packageName)) {
            return context.getString(R$string.keyguard_update_dialog_video_youku_message);
        }
        if ("com.huawei.himovie".equalsIgnoreCase(packageName)) {
            return context.getString(R$string.keyguard_update_dialog_hwvideo_message);
        }
        if ("com.huawei.hwireader".equalsIgnoreCase(packageName)) {
            return context.getString(R$string.keyguard_update_dialog_hwreader_message);
        }
        return BuildConfig.FLAVOR;
    }

    public static Drawable getAppIcon(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return null;
        }
        if ("com.android.mediacenter".equalsIgnoreCase(packageName)) {
            return context.getResources().getDrawable(R$drawable.icon_keyguard_hwmusic);
        }
        if ("com.huawei.hwvplayer.youku".equalsIgnoreCase(packageName)) {
            return context.getResources().getDrawable(R$drawable.icon_keyguard_hwvideo);
        }
        if ("com.huawei.himovie".equalsIgnoreCase(packageName)) {
            return context.getResources().getDrawable(R$drawable.icon_keyguard_himovie);
        }
        if ("com.huawei.hwireader".equalsIgnoreCase(packageName)) {
            return context.getResources().getDrawable(R$drawable.icon_keyguard_hwreader);
        }
        return null;
    }

    public static Drawable getKeyguardResTitleIcon(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return null;
        }
        if ("com.android.mediacenter".equalsIgnoreCase(packageName)) {
            return context.getResources().getDrawable(R$drawable.icon_keyguard_music);
        }
        if ("com.huawei.hwvplayer.youku".equalsIgnoreCase(packageName) || "com.huawei.himovie".equalsIgnoreCase(packageName)) {
            return context.getResources().getDrawable(R$drawable.icon_keyguard_video);
        }
        if ("com.huawei.hwireader".equalsIgnoreCase(packageName)) {
            return context.getResources().getDrawable(R$drawable.icon_keyguard_book);
        }
        return null;
    }

    public static boolean compareAppVersion(int localAppVersion, String minAppVersion) {
        boolean z = false;
        if (localAppVersion < 0 || TextUtils.isEmpty(minAppVersion)) {
            HwLog.e("MagazineUtils", "param is error!");
            return false;
        } else if (TextUtils.isDigitsOnly(minAppVersion)) {
            try {
                if (Integer.parseInt(minAppVersion) > localAppVersion) {
                    z = true;
                }
                return z;
            } catch (NumberFormatException ex) {
                HwLog.e("MagazineUtils", "compareAppVersion():NumberFormatException=" + ex);
                return false;
            }
        } else {
            HwLog.e("MagazineUtils", "version is not only digits! localAppVersion=" + localAppVersion + ",minAppVersion=" + minAppVersion);
            return false;
        }
    }

    public static int getApkVersionCode(Context context, String packageName) {
        if (context == null || packageName == null) {
            return 0;
        }
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            return packageInfo.versionCode;
        }
        return 0;
    }

    public static boolean hasTranslateDatas(Context context) {
        return context.getSharedPreferences("magazine_preferences", 0).getBoolean("update_translate_legacy_data", false);
    }

    public static boolean isMagazineEnable(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        boolean re = false;
        try {
            SharedPreferences sp = context.createPackageContext("com.android.keyguard", 0).getSharedPreferences("com.android.keyguard_preferences", 0);
            if (sp != null) {
                re = sp.getBoolean("enable_magazinelock_feature", false);
            }
        } catch (NameNotFoundException e) {
            HwLog.e("MagazineUtils", "isMagazineEnable exception: " + e.toString());
        }
        if (re && !isMagazineUnlockForbidden(context)) {
            z = true;
        }
        return z;
    }

    public static void setMagazineEnable(Context context, boolean enable) {
        if (context != null) {
            try {
                SharedPreferences sp = context.createPackageContext("com.android.keyguard", 0).getSharedPreferences("com.android.keyguard_preferences", 0);
                if (sp != null) {
                    Editor editor = sp.edit();
                    if (editor != null) {
                        editor.putBoolean("enable_magazinelock_feature", enable).commit();
                    }
                }
            } catch (NameNotFoundException e) {
                HwLog.e("MagazineUtils", "isMagazineEnable exception: " + e.toString());
            }
        }
    }

    private static boolean isMagazineUnlockForbidden(Context context) {
        boolean z = true;
        if (context == null) {
            return true;
        }
        if (Global.getInt(context.getContentResolver(), "magazine_unlock_enabled", 0) == 1) {
            z = false;
        }
        return z;
    }

    public static ContextThemeWrapper getHwThemeContext(Context context, String theme) {
        if (context == null) {
            HwLog.e("MagazineUtils", "error getHwThemeContext context is null");
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

    public static boolean isMagazineUnlockOn(Context context) {
        if (KeyguardCfg.isExtremePowerSavingMode()) {
            HwLog.i("MagazineUtils", "is magazine unlock on, now is extreme power saving mode");
            return false;
        } else if (KeyguardCfg.isSimpleModeOn()) {
            HwLog.i("MagazineUtils", "is magazine unlock on, now is simple mode");
            return false;
        } else if (context == null) {
            return false;
        } else {
            if (((KeyguardManager) context.getSystemService("keyguard")).isKeyguardSecure() && !KeyguardCfg.isDoubleLockOn(context)) {
                HwLog.i("MagazineUtils", "is magazine unlock on, now is only security mode");
                return false;
            } else if (HwKeyguardUpdateMonitor.getInstance(context).isLockScreenDisabled(context)) {
                HwLog.i("MagazineUtils", "is magazine unlock on, now is lock screen diabled mode");
                return false;
            } else if (!isBiometricUnlockEnabled(context)) {
                return isMagazineLockStyle();
            } else {
                HwLog.i("MagazineUtils", "is magazine unlock on, now is biometric mode");
                return false;
            }
        }
    }

    public static boolean isMagazineLockStyle() {
        if (SystemProperties.getBoolean("ro.config.lockscreen_magazine", true)) {
            return "magazine".equalsIgnoreCase(HwThemeParser.getInstance().getStyle(null));
        }
        HwLog.i("MagazineUtils", "is magazine unlock on, in prop settings,  magazine style is closed");
        return false;
    }

    private static boolean isBiometricUnlockEnabled(Context context) {
        return false;
    }

    public static String getLandPicName(String picPath) {
        if (picPath == null) {
            HwLog.w("MagazineUtils", "there is no land picture, picPath is null");
            return null;
        }
        String picFilename = picPath.substring(0, picPath.lastIndexOf(46));
        return picFilename + "_land" + picPath.substring(picPath.lastIndexOf(46));
    }

    public static void setAutoSwitchMagazine(Context context, boolean auto, String reason) {
        Editor editor = context.getSharedPreferences("magazine_preferences", 0).edit();
        if (OsUtils.getCurrentUser() == 0) {
            editor.putBoolean("auto_switch", auto).apply();
        } else {
            editor.putBoolean("auto_switch_" + OsUtils.getCurrentUserSN() + "_", auto).apply();
        }
        HwLog.w("MagazineUtils", "Magazine AutoSwitch state chagned. " + auto + "; as " + reason);
    }

    public static void setScrollingBlockFlag(boolean block) {
        mBlockSwitchByPagerBusy = block;
    }

    public static boolean isAutoSwitchMagazine(Context context, boolean buttonStateShow) {
        SharedPreferences sp = context.getSharedPreferences("magazine_preferences", 0);
        if (!mBlockSwitchByPagerBusy || buttonStateShow) {
            return sp.getBoolean("auto_switch", true);
        }
        HwLog.w("MagazineUtils", "AutoSwitch Blocked!");
        return false;
    }

    public static boolean isUserCustomedWallpaper(Context context) {
        if (HwKeyguardUpdateMonitor.getInstance(context).isFirstTimeStartupAndEncrypted()) {
            return false;
        }
        boolean ret;
        SharedPreferences sp = context.getSharedPreferences("magazine_preferences", 0);
        int id = OsUtils.getCurrentUser();
        if (id == 0) {
            ret = sp.getBoolean("user_set_wallpaper", false);
        } else {
            ret = sp.getBoolean("user_set_wallpaper_" + OsUtils.getCurrentUserSN() + "_", false);
        }
        HwLog.v("MagazineUtils", "Is User Assigned Wallpaper? : " + id + "  " + ret);
        return ret;
    }

    public static void setUserCustomedWallpaper(Context context, boolean userAssigned) {
        Editor editor = context.getSharedPreferences("magazine_preferences", 0).edit();
        int id = OsUtils.getCurrentUser();
        if (id == 0) {
            editor.putBoolean("user_set_wallpaper", userAssigned).apply();
        } else {
            editor.putBoolean("user_set_wallpaper_" + OsUtils.getCurrentUserSN() + "_", userAssigned).apply();
        }
        HwLog.i("MagazineUtils", "User assigned wallpaper " + id + " - " + userAssigned);
    }

    public static boolean isDataInited(Context context) {
        return context.getSharedPreferences("magazine_preferences", 0).getBoolean("update_theme", false);
    }

    public static int getCurrentPicId(Context context) {
        return context.getSharedPreferences("magazine_preferences", 0).getInt("current_picId", -1);
    }

    public static void setCurrentPicId(Context context, int key) {
        context.getSharedPreferences("magazine_preferences", 0).edit().putInt("current_picId", key).apply();
    }

    public static boolean getFirstLiftDetailViewFlag(Context context) {
        return context.getSharedPreferences("magazine_preferences", 0).getBoolean("first_lift", true);
    }

    public static void setFirstLiftDetailViewFlag(Context context, boolean key) {
        context.getSharedPreferences("magazine_preferences", 0).edit().putBoolean("first_lift", key).apply();
    }

    public static boolean isRepeatCheck(Context context, long duration) {
        if (context == null) {
            return true;
        }
        try {
            long currentTime = System.currentTimeMillis();
            SharedPreferences sp = context.createPackageContext("com.android.keyguard", 0).getSharedPreferences("magazine_preferences", 4);
            if (sp == null) {
                return true;
            }
            long lastAutoCheckTime = sp.getLong("last_success_check_time", 0);
            HwLog.d("MagazineUtils", "currentTime = " + currentTime + ", lastAutoCheckTime = " + lastAutoCheckTime);
            return currentTime - lastAutoCheckTime <= duration && currentTime >= lastAutoCheckTime;
        } catch (NameNotFoundException e) {
            HwLog.e("MagazineUtils", "isRepeatCheck exception: " + e.toString());
        }
    }

    public static boolean isWifiEnable(Context context) {
        boolean z = true;
        int state = -1;
        if (context == null) {
            HwLog.w("MagazineUtils", "check active connect, context is null");
            return false;
        }
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo != null) {
            state = networkInfo.getType();
        }
        HwLog.w("MagazineUtils", "check active connect, state = " + state);
        if (1 != state) {
            z = false;
        }
        return z;
    }

    public static void sendConnectivityActionToMagazine(Context context) {
        if (context == null) {
            HwLog.w("MagazineUtils", "sendConnectivityAction2Magazine, context is null");
            return;
        }
        boolean isMagazineUnlockOn = isMagazineUnlockOn(context);
        if (isWifiEnable(context) && isMagazineUnlockOn && !isRepeatCheck(context, 10800000)) {
            Intent connectIntent = new Intent("com.android.keyguard.magazineunlock.SEND_CONNECTIVITY");
            connectIntent.setPackage("com.android.keyguard");
            HwLog.w("MagazineUtils", "send connectivity broadcaset : ");
            context.sendBroadcastAsUser(connectIntent, UserHandle.OWNER, "com.android.huawei.magazineunlock.permission.WRITE");
        }
    }
}
