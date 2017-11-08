package com.android.systemui;

import android.content.Context;
import com.android.systemui.floattask.FloatTask;
import com.android.systemui.observer.SystemUIObserver;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryControllerImpl;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HwNetworkControllerImpl;
import com.android.systemui.time.TimeManager;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.UserSwitchUtils;
import com.android.systemui.utils.analyze.MemUtils;
import com.android.systemui.utils.analyze.PerfDebugUtils;

public class HwSystemUIApplication extends SystemUIApplication {
    private static HwSystemUIApplication mInstance;
    private BatteryController mBatteryController = null;
    private FlashlightController mFlashlightController = null;
    private FloatTask mFloatTask = new FloatTask(this);
    private long mLastDumpMeminfoTime = -1;

    public HwSystemUIApplication() {
        setInstance(this);
    }

    private static void setInstance(HwSystemUIApplication instance) {
        mInstance = instance;
    }

    public static HwSystemUIApplication getInstance() {
        return mInstance;
    }

    public static Context getContext() {
        return mInstance;
    }

    public void onCreate() {
        HwLog.i("HwSystemUIApplication", "onCreate");
        PerfDebugUtils.setThreadPolicy();
        UserSwitchUtils.init(this);
        setHwTheme(this);
        if (!isScreenshotProcess()) {
            SystemUIThread.init();
            SystemUIObserver.init();
            TimeManager.getInstance().registerTimeReceiver(this);
        }
        super.onCreate();
        if (!isScreenshotProcess()) {
            if (HwPhoneStatusBar.getInstance() != null) {
                this.mFlashlightController = (FlashlightController) HwPhoneStatusBar.getInstance().getFlashlightController();
                this.mBatteryController = HwPhoneStatusBar.getInstance().getBatteryController();
            } else {
                this.mFlashlightController = new FlashlightController(this);
                this.mBatteryController = new BatteryControllerImpl(this);
            }
        }
    }

    public void startServicesIfNeeded() {
        HwLog.i("HwSystemUIApplication", "startServicesIfNeeded:" + this.mServicesStarted);
        if (!this.mServicesStarted) {
            this.mFloatTask.init();
        }
        super.startServicesIfNeeded();
        HwNetworkControllerImpl.sendSimInactiveBroadcast(this);
    }

    public void setHwTheme(Context context) {
        int themeID = context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dark", null, null);
        if (themeID != 0) {
            context.setTheme(themeID);
        }
    }

    public FlashlightController getFlashlightController() {
        return this.mFlashlightController;
    }

    public BatteryController getBatteryController() {
        return this.mBatteryController;
    }

    public void onLowMemory() {
        long currentTime = System.currentTimeMillis();
        if (this.mLastDumpMeminfoTime <= 0 || currentTime - this.mLastDumpMeminfoTime >= 86400000) {
            this.mLastDumpMeminfoTime = currentTime;
            MemUtils.logCurrentMemoryInfo();
            return;
        }
        HwLog.i("HwSystemUIApplication", "onLowMemory::last meminfo dump in:" + this.mLastDumpMeminfoTime + ", and now in: " + currentTime + ", it is less than one day, no need log info again!");
    }
}
