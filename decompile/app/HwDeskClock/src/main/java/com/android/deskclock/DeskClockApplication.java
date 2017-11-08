package com.android.deskclock;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.os.StrictMode.VmPolicy;
import com.android.deskclock.alarmclock.Alarms;
import com.android.deskclock.provider.DataLoadHelper;
import com.android.deskclock.timer.TimerPage;
import com.android.deskclock.worldclock.TimeZoneUtils;
import com.android.util.HwLog;
import com.android.util.Log;
import com.android.util.UIUtils;
import com.android.util.Utils;
import java.util.Locale;

public class DeskClockApplication extends Application {
    private static DeskClockApplication deskClockApplication = null;
    private static boolean sIsBTVPadDevice = false;
    boolean DEVELOPER_MODE = false;
    private String fontPath = "/data/skin/fonts";
    private boolean mFontDirEmpty = true;

    public static boolean isBtvPadDevice() {
        return sIsBTVPadDevice;
    }

    public static void setIsBtvPadDevice(boolean isBtvPadDevice) {
        sIsBTVPadDevice = isBtvPadDevice;
    }

    public void onCreate() {
        if (this.DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new Builder().detectDiskReads().detectDiskWrites().detectNetwork().detectCustomSlowCalls().penaltyLog().build());
            StrictMode.setVmPolicy(new VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().detectActivityLeaks().detectLeakedRegistrationObjects().penaltyLog().penaltyDeath().build());
        }
        setDeskClockApplication(this);
        UIUtils.getDeviceSize(this);
        setIsBtvPadDevice(UIUtils.isBtvPadScreenDevice());
        super.onCreate();
        Log.iRelease("DeskClockApplication", "onCreate");
        checkFontDir();
        TimerPage.setIsFromCTS(false);
        DataLoadHelper.loadCalendarData(this);
        Alarms.saveAlertStatus(this, false);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Utils.setsIsZhArea(this);
        Log.dRelease("DeskClockApplication", "DeskClockApplication : onConfigurationChanged " + Utils.isChinaRegionalVersion());
        checkFontDir();
        Utils.initTypeface(this);
    }

    public static DeskClockApplication getDeskClockApplication() {
        return deskClockApplication;
    }

    public static void setDeskClockApplication(DeskClockApplication app) {
        deskClockApplication = app;
    }

    private void checkFontDir() {
        if (!"zh".equals(Locale.getDefault().getLanguage()) || Utils.hasAvailableFile(this.fontPath)) {
            this.mFontDirEmpty = false;
        } else {
            this.mFontDirEmpty = true;
        }
        Log.dRelease("DeskClockApplication", "checkFontDir : mFontDirEmpty = " + this.mFontDirEmpty);
    }

    public boolean isSystemDefaultFont() {
        return this.mFontDirEmpty;
    }

    public void setTranslucentStatus(boolean on, Activity activity) {
    }

    public void openAccelerated(boolean isOpen, Activity activity) {
        int wpflags = isOpen ? 16777216 : 0;
        if (wpflags != (activity.getWindow().getAttributes().flags & 16777216)) {
            activity.getWindow().setFlags(wpflags, 16777216);
        }
    }

    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        HwLog.i("DeskClockApplication", "onTrimMemory" + level);
        TimeZoneUtils.clearCityData();
    }
}
