package com.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.icu.util.ULocale;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build.VERSION;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.support.v4.os.BuildCompat;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import com.android.deskclock.DeskClockApplication;
import java.io.File;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static final boolean VR_SWITCH = SystemProperties.getBoolean("ro.vr.surport", false);
    static String fontPath = "/data/skin/fonts";
    private static Typeface mClockTypeface;
    private static Typeface mRobotoLightTypeface;
    private static Typeface mRobotoRegularTypeface;
    private static Typeface mRobotoThinTypeface;
    private static Typeface mRobotoXianBlackTypeface;
    private static String sClockTypeface = "/system/fonts/AndroidClock.ttf";
    private static boolean sDeskClockForeground = true;
    private static boolean sIsZhArea = false;
    private static boolean sUpdata = false;

    public static long getTimeNow() {
        return SystemClock.elapsedRealtime();
    }

    public static void clearSwSharedPref(SharedPreferences prefs) {
        Editor editor = prefs.edit();
        editor.remove("shouldcount_stopwatch");
        editor.remove("sw_start_time");
        editor.remove("sw_accum_time");
        editor.remove("sw_accum_interval_time");
        editor.remove("sw_min_time");
        editor.remove("sw_state");
        int lapNum = prefs.getInt("sw_lap_num", 0);
        for (int i = 0; i < lapNum; i++) {
            editor.remove("sw_lap_time_" + Integer.toString(i));
        }
        editor.remove("sw_lap_num");
        editor.remove("swring_count");
        editor.remove("swring_count_text_pre");
        editor.remove("swring_count_text_back");
        editor.remove("swring_count_text_show");
        editor.remove("swring_isstartrun");
        editor.remove("swring_chazhi");
        editor.remove("swring_pauseTime");
        editor.remove("swring_degree");
        editor.remove("swring_startTime");
        editor.remove("swring_statusType");
        editor.commit();
    }

    public static void clearTimerSharedPref(Context context) {
        HwLog.i("clear", "clearTimerSharedPref");
        SharedPreferences prefs = getBootSharedPreferences(context, "timer", 0);
        if (prefs.contains("mRightDegree")) {
            Editor editor = prefs.edit();
            editor.clear();
            editor.commit();
        }
    }

    public static void clearStopWatchPref(Context context) {
        HwLog.i("clear", "clearStopWatchPref");
        SharedPreferences prefs = getDefaultSharedPreferences(context);
        if (prefs.contains("swring_current_degree")) {
            Editor editor = prefs.edit();
            editor.clear();
            editor.commit();
        }
    }

    public static boolean isLandScreen(Context context) {
        return context != null && 2 == context.getResources().getConfiguration().orientation;
    }

    public static int getCurrentDisplayMetricsDensity() {
        return (int) DeskClockApplication.getDeskClockApplication().getResources().getDisplayMetrics().density;
    }

    public static boolean isKitKatOrLater() {
        return VERSION.SDK_INT > 18;
    }

    public static boolean isMutiUser() {
        if (VERSION.SDK_INT >= 21) {
            return true;
        }
        return false;
    }

    public static boolean isNOrLater() {
        return BuildCompat.isAtLeastN();
    }

    public static void updateSystemVolume(Context context, int volume) {
        Editor editor = getDefaultSharedPreferences(context).edit();
        editor.putInt("systemAlarmVolume", volume);
        editor.commit();
    }

    public static synchronized void initTypeface(Context context) {
        synchronized (Utils.class) {
            if (mRobotoLightTypeface == null) {
                mRobotoLightTypeface = Typeface.create("sans-serif-light", 0);
            }
            if (mRobotoRegularTypeface == null) {
                mRobotoRegularTypeface = Typeface.DEFAULT;
            }
            if (mRobotoThinTypeface == null) {
                mRobotoThinTypeface = Typeface.create("sans-serif-thin", 0);
            }
            if (mClockTypeface == null) {
                File fontFile = new File(sClockTypeface);
                if (fontFile.exists()) {
                    try {
                        mClockTypeface = Typeface.createFromFile(fontFile);
                    } catch (RuntimeException e) {
                        Log.w("Utils", "Font is not exist");
                        mClockTypeface = Typeface.DEFAULT;
                    }
                } else {
                    mClockTypeface = Typeface.DEFAULT;
                }
            }
            if (!DeskClockApplication.getDeskClockApplication().isSystemDefaultFont()) {
                mRobotoXianBlackTypeface = null;
                return;
            } else if (mRobotoXianBlackTypeface == null) {
                try {
                    mRobotoXianBlackTypeface = Typeface.create("chnfzxh", 0);
                } catch (RuntimeException e2) {
                    Log.w("Utils", "Font is not exist");
                    mRobotoXianBlackTypeface = Typeface.DEFAULT;
                }
            }
        }
    }

    public static synchronized Typeface getmRobotoLightTypeface() {
        Typeface typeface;
        synchronized (Utils.class) {
            if (mRobotoLightTypeface == null) {
                mRobotoLightTypeface = Typeface.create("sans-serif-light", 0);
            }
            typeface = mRobotoLightTypeface;
        }
        return typeface;
    }

    public static synchronized Typeface getmRobotoRegularTypeface() {
        Typeface typeface;
        synchronized (Utils.class) {
            if (mRobotoRegularTypeface == null) {
                mRobotoRegularTypeface = Typeface.DEFAULT;
            }
            typeface = mRobotoRegularTypeface;
        }
        return typeface;
    }

    public static synchronized Typeface getmRobotoXianBlackTypeface() {
        synchronized (Utils.class) {
            if (isExistCustomFont() || isChineseLanguage()) {
                Typeface typeface = getchnfzxhFont();
                return typeface;
            }
            if (mRobotoXianBlackTypeface == null) {
                isGetXiangBlack();
            }
            typeface = mRobotoXianBlackTypeface;
            return typeface;
        }
    }

    public static synchronized Typeface getmRobotoThinTypeface() {
        Typeface typeface;
        synchronized (Utils.class) {
            if (mRobotoThinTypeface == null) {
                mRobotoThinTypeface = Typeface.create("sans-serif-thin", 0);
            }
            typeface = mRobotoThinTypeface;
        }
        return typeface;
    }

    private static void isGetXiangBlack() {
        if (!"zh".equals(Locale.getDefault().getLanguage()) || hasAvailableFile(fontPath)) {
            try {
                mRobotoXianBlackTypeface = Typeface.create("chnfzxh", 0);
            } catch (RuntimeException e) {
                Log.w("Utils", "isGetXiangBlack->Font is not exist");
                mRobotoXianBlackTypeface = null;
            }
        }
    }

    public static boolean hasAvailableFile(String fontPath) {
        File file = new File(fontPath);
        File[] files = file.listFiles();
        if (!file.exists() || !file.isDirectory() || files == null || files.length <= 0) {
            return false;
        }
        return true;
    }

    public static boolean isChinaRegionalVersion() {
        return sIsZhArea;
    }

    public static void setsIsZhArea(Context context) {
        boolean isSimpleChinese;
        String currentVersion = SystemProperties.get("ro.product.locale.language") + "_" + SystemProperties.get("ro.product.locale.region");
        Locale locale = Locale.getDefault();
        if (!isNOrLater()) {
            isSimpleChinese = Locale.CHINA.equals(locale);
        } else if ("Hans".equals(ULocale.addLikelySubtags(ULocale.forLanguageTag(locale.toLanguageTag())).getScript())) {
            isSimpleChinese = "zh".equals(locale.getLanguage());
        } else {
            isSimpleChinese = false;
        }
        if (!"zh_CN".equals(currentVersion)) {
            isSimpleChinese = false;
        }
        sIsZhArea = isSimpleChinese;
    }

    public static int checkNetworkStatus(Context context) {
        ConnectivityManager localConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo localNetworkInfo = localConnectivityManager.getActiveNetworkInfo();
        if (localNetworkInfo == null || !localNetworkInfo.isConnected()) {
            NetworkInfo[] arrayOfNetworkInfo = localConnectivityManager.getAllNetworkInfo();
            if (arrayOfNetworkInfo != null) {
                for (NetworkInfo networkInfo : arrayOfNetworkInfo) {
                    if (networkInfo.getState() == State.CONNECTED) {
                        int networkType = networkInfo.getType();
                        Log.d("Utils", "checkNetworkStatus success All networkType = " + networkType);
                        return networkType;
                    }
                }
            }
            Log.d("Utils", "checkNetworkStatus fail networkType = " + -1);
            return -1;
        }
        networkType = localNetworkInfo.getType();
        Log.d("Utils", "checkNetworkStatus success Active networkType = " + networkType);
        return networkType;
    }

    public static boolean getNavigationbarStatus(Context context) {
        boolean status = Global.getInt(context.getContentResolver(), "navigationbar_is_min", 0) != 1;
        Log.i("Utils", "getNavigationbarStatus : status =" + status);
        return status;
    }

    public static boolean isExistCustomFont() {
        boolean z = false;
        File file = new File(fontPath);
        if (!file.exists() || !file.isDirectory()) {
            return false;
        }
        String[] childFiles = file.list();
        if (!(childFiles == null || childFiles.length == 0)) {
            z = true;
        }
        return z;
    }

    public static boolean isChineseLanguage() {
        return "zh".equals(Locale.getDefault().getLanguage());
    }

    public static Typeface getDefaultFont() {
        return Typeface.DEFAULT;
    }

    public static Typeface getchnfzxhFont() {
        return Typeface.create("chnfzxh", 0);
    }

    public static Typeface getSmallWindowFont() {
        return Typeface.create("sans-serif", 0);
    }

    public static String getLocalDigitString(long count) {
        String text = String.valueOf(count);
        try {
            text = NumberFormat.getInstance(Locale.getDefault()).format(count);
        } catch (NumberFormatException e) {
            Log.w("locald_digit", "NumberFormatException : " + e.getMessage());
        } catch (Exception e2) {
            Log.w("locald_digit", "Exception: " + e2.getMessage());
        }
        return text;
    }

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        Context storageContext;
        if (isNOrLater()) {
            Context deviceContext = context.createDeviceProtectedStorageContext();
            if (!deviceContext.moveSharedPreferencesFrom(context, PreferenceManager.getDefaultSharedPreferencesName(context))) {
                HwLog.i("locald_digit", "Failed to migrate shared preferences");
            }
            storageContext = deviceContext;
        } else {
            storageContext = context;
        }
        return PreferenceManager.getDefaultSharedPreferences(storageContext);
    }

    public static void setVersionUpdate(boolean bUpdate) {
        sUpdata = bUpdate;
    }

    public static SharedPreferences getBootSharedPreferences(Context context, String sharedPreferencesName, int sharedPreferencesMode) {
        Context storageContext;
        if (isNOrLater()) {
            Context deviceContext = context.createDeviceProtectedStorageContext();
            if (!deviceContext.moveSharedPreferencesFrom(context, sharedPreferencesName)) {
                HwLog.i("locald_digit", "Failed to migrate shared preferences");
            }
            storageContext = deviceContext;
        } else {
            HwLog.i("update", "getSharedPreferences common context");
            storageContext = context;
        }
        return storageContext.getSharedPreferences(sharedPreferencesName, sharedPreferencesMode);
    }

    public static SharedPreferences getSharedPreferences(Context context, String sharedPreferencesName, int sharedPreferencesMode) {
        Context storageContext;
        if (!isNOrLater()) {
            storageContext = context;
        } else if (sUpdata) {
            storageContext = context;
            HwLog.i("update", "database is updating!");
        } else {
            Context deviceContext = context.createDeviceProtectedStorageContext();
            if (!deviceContext.moveSharedPreferencesFrom(context, sharedPreferencesName)) {
                HwLog.i("locald_digit", "Failed to migrate shared preferences");
            }
            storageContext = deviceContext;
        }
        return storageContext.getSharedPreferences(sharedPreferencesName, sharedPreferencesMode);
    }

    public static Icon getBitampIcon(Context context, int resId) {
        Config config;
        Drawable drawable = context.getResources().getDrawable(resId);
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if (drawable.getOpacity() != -1) {
            config = Config.ARGB_8888;
        } else {
            config = Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return Icon.createWithBitmap(bitmap);
    }

    public static String[] getTimePoint(Context context) {
        String[] string = new String[2];
        Matcher matcher = Pattern.compile("(\\w+)(.|:)(\\w+)(.|:)(\\w+)").matcher(DateFormat.getBestDateTimePattern(context.getResources().getConfiguration().locale, "Hms"));
        if (matcher.find()) {
            string[0] = matcher.group(2);
            string[1] = matcher.group(4);
        }
        return string;
    }

    public static void updateContentDescription(View view, Calendar time) {
        view.setContentDescription(DateUtils.formatDateTime(view.getContext(), time.getTimeInMillis(), 129));
    }

    public static boolean isDeskClockForeground() {
        HwLog.i("locald_digit", "isDeskClockForeground = " + sDeskClockForeground);
        return sDeskClockForeground;
    }

    public static void setDeskClockForeground(boolean isForegroud) {
        HwLog.i("locald_digit", "setDeskClockForeground = " + sDeskClockForeground);
        sDeskClockForeground = isForegroud;
    }

    public static boolean isLockScreenSupportLand() {
        return SystemProperties.getBoolean("lockscreen.rot_override", false);
    }

    public static int dip2px(Context context, int dipValue) {
        return (int) ((((float) dipValue) * context.getResources().getDisplayMetrics().density) + 0.5f);
    }
}
