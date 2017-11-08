package com.huawei.systemui;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.text.TextUtils;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.dynamiclockscreen.DynamicUnlockUtils;
import com.huawei.keyguard.events.EventCenter;
import com.huawei.keyguard.support.magazine.HwFyuseUtils;
import com.huawei.keyguard.support.magazine.MagazineWallpaper;
import com.huawei.keyguard.theme.HwThemeParser;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.MultiDpiUtil;
import fyusion.vislib.BuildConfig;
import java.util.List;

public abstract class BaseApplication extends Application {
    private String mProcessName = BuildConfig.FLAVOR;

    public abstract <T> T getComponent(Class<T> cls);

    public abstract void startServicesIfNeeded();

    public void onCreate() {
        super.onCreate();
        Context appContext = getApplicationContext();
        setCurrenProcessName(appContext);
        if (isScreenshotProcess()) {
            HwLog.w("BaseApplication", "onCreate for screenshot process, just do nothing!");
            return;
        }
        MultiDpiUtil.loadRes(GlobalContext.getBackgroundHandler(), appContext);
        KeyguardCfg.init(appContext);
        GlobalContext.setContext(appContext);
        EventCenter.getInst().init(appContext);
        HwFyuseUtils.initFyuseSDK(appContext);
        if ("magazine".equals(HwThemeParser.getInstance().getStyle(appContext))) {
            MagazineWallpaper.getInst(appContext).initDatas();
        }
        DynamicUnlockUtils.init(appContext);
        HwKeyguardUpdateMonitor.getInstance(appContext).setCurrenProcessName();
    }

    public boolean isScreenshotProcess() {
        boolean z = false;
        if (TextUtils.isEmpty(this.mProcessName)) {
            return false;
        }
        if (!"com.android.systemui".equals(this.mProcessName)) {
            z = true;
        }
        return z;
    }

    private void setCurrenProcessName(Context context) {
        if (context == null) {
            HwLog.e("BaseApplication", "setCurrenProcessName with null context!");
            return;
        }
        int pid = Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        if (activityManager == null) {
            HwLog.e("BaseApplication", "setCurrenProcessName with activityManager wrong!");
            return;
        }
        List<RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        if (processInfos == null || processInfos.isEmpty()) {
            HwLog.e("BaseApplication", "setCurrenProcessName with processInfos wrong!");
            return;
        }
        for (RunningAppProcessInfo process : processInfos) {
            if (process.pid == pid) {
                this.mProcessName = process.processName;
                break;
            }
        }
        HwLog.i("BaseApplication", "setCurrenProcessName with mProcessName is : " + this.mProcessName);
    }
}
