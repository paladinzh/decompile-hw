package com.huawei.powergenie.modules.apppower.restrict.cleanup;

import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import java.util.ArrayList;

public final class ExtremeClean extends BaseClean {
    private static String mCurScreenLock = null;
    private static String mExtrPrevLauncher;
    private static String mExtrPrevScreenLock;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    ExtremeClean.this.startExtremeClean();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mNextExtremeClean = false;
    private int mProcessNumInExtreme = 0;

    public ExtremeClean(ICoreContext coreContext) {
        super(coreContext);
    }

    protected void handlePowerModeChange(int newMode) {
        if (newMode == 4) {
            mExtrPrevLauncher = getUsingLauncher();
            mExtrPrevScreenLock = mCurScreenLock;
            Message msg = this.mHandler.obtainMessage(100);
            this.mHandler.removeMessages(100);
            this.mHandler.sendMessageDelayed(msg, 50);
            this.mNextExtremeClean = true;
            this.mProcessNumInExtreme = 0;
        } else if (this.mIPolicy.getOldPowerMode() == 4) {
            this.mHandler.removeMessages(100);
            mExtrPrevLauncher = null;
            mExtrPrevScreenLock = null;
        }
    }

    protected void handleScreenState(boolean sreenOn) {
        if (!sreenOn && this.mIPolicy.getPowerMode() == 4 && this.mActivityManager.getRunningAppProcesses().size() > this.mProcessNumInExtreme) {
            this.mHandler.removeMessages(100);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100), 3000);
        }
    }

    protected void handleAddTopView(boolean add, int uid) {
        if (UserHandle.getAppId(uid) > 10000 && add && this.mIPolicy.getPowerMode() == 4 && this.mHandler.hasMessages(100)) {
            ArrayList<String> pkg = this.mIAppManager.getPkgNameByUid(this.mContext, uid);
            if (pkg != null) {
                ArrayList<String> forceProtectApps = getForceProtectApps();
                for (String name : pkg) {
                    if (!(forceProtectApps.contains(name) || isExtrModeV2ProtectApp(name))) {
                        Log.d("ExtremeClean", "Force stop the added top view app : " + pkg);
                        forceStopApp(name, "Extreme Clean");
                    }
                }
            }
        }
    }

    protected void handleReserveAppsChange() {
        Message msg = this.mHandler.obtainMessage(100);
        this.mHandler.removeMessages(100);
        this.mHandler.sendMessageDelayed(msg, 2000);
    }

    private void startExtremeClean() {
        int cleanNum = 0;
        for (String appPkg : getExtremeCleanApps()) {
            if (isExtrModeV2ProtectApp(appPkg)) {
                Log.i("ExtremeClean", "Extreme v2 reserve :" + appPkg);
            } else {
                cleanNum++;
                forceStopApp(appPkg, "Extreme Clean");
            }
        }
        Log.i("ExtremeClean", "Extreme Clean Apps Num:" + cleanNum);
        if (this.mNextExtremeClean) {
            Message msg = this.mHandler.obtainMessage(100);
            this.mHandler.removeMessages(100);
            this.mHandler.sendMessageDelayed(msg, 15000);
            this.mNextExtremeClean = false;
        }
        this.mProcessNumInExtreme = this.mActivityManager.getRunningAppProcesses().size();
    }

    private boolean isExtrModeV2ProtectApp(String pkgName) {
        if (this.mIPolicy.isExtremeModeV2() && (this.mIAppManager.isExtrModeV2ReserveApp(pkgName) || "com.huawei.parentcontrol".equals(pkgName))) {
            return true;
        }
        return false;
    }

    private ArrayList<String> getExtremeCleanApps() {
        ArrayList<String> resultApps = new ArrayList();
        ArrayList<String> forceProtectApps = getForceProtectApps();
        ArrayList<String> importentService = getImportentServices();
        ArrayList<String> cleanBlackList = getCleanBlackList();
        ArrayList<String> protectApps = this.mIAppManager.getCleanProtectApps();
        ArrayList<String> unprotectApps = this.mIAppManager.getCleanUnprotectApps();
        for (String pkg : this.mIAppManager.getRuningApp(this.mContext)) {
            if (cleanBlackList.contains(pkg) && !resultApps.contains(pkg)) {
                resultApps.add(pkg);
            } else if (!(importentService.contains(pkg) || forceProtectApps.contains(pkg))) {
                if ((protectApps.contains(pkg) || unprotectApps.contains(pkg)) && !resultApps.contains(pkg)) {
                    resultApps.add(pkg);
                }
            }
        }
        return resultApps;
    }

    private ArrayList<String> getForceProtectApps() {
        ArrayList<String> forceProtectList = new ArrayList();
        String usingLauncher = getUsingLauncher();
        String defaultLauncher = getDefaultLauncher();
        String curLiveWallpaper = getCurLiveWallpaper();
        String defaultInputmethod = getDefaultInputMethod();
        forceProtectList.addAll(mForceProtectList);
        if (!(usingLauncher == null || forceProtectList.contains(usingLauncher))) {
            forceProtectList.add(usingLauncher);
        }
        if (!(defaultLauncher == null || forceProtectList.contains(defaultLauncher))) {
            forceProtectList.add(defaultLauncher);
        }
        if (!(curLiveWallpaper == null || forceProtectList.contains(curLiveWallpaper))) {
            forceProtectList.add(curLiveWallpaper);
        }
        if (!(defaultInputmethod == null || forceProtectList.contains(defaultInputmethod))) {
            forceProtectList.add(defaultInputmethod);
        }
        if (!(mExtrPrevLauncher == null || forceProtectList.contains(mExtrPrevLauncher))) {
            forceProtectList.add(mExtrPrevLauncher);
        }
        if (mCurScreenLock == null) {
            for (String screenLock : this.mIAppType.getAppsByType(9)) {
                if (!forceProtectList.contains(screenLock)) {
                    forceProtectList.add(screenLock);
                }
            }
        } else {
            forceProtectList.add(mCurScreenLock);
        }
        if (!(mExtrPrevScreenLock == null || forceProtectList.contains(mExtrPrevScreenLock))) {
            forceProtectList.add(mExtrPrevScreenLock);
        }
        forceProtectList.add("com.huawei.devicetest");
        forceProtectList.add("com.huawei.android.thememanager");
        return forceProtectList;
    }

    private ArrayList<String> getCleanBlackList() {
        ArrayList<String> cleanBlackList = new ArrayList();
        cleanBlackList.add("com.huawei.android.FMRadio");
        cleanBlackList.add("com.mediatek.FMRadio");
        cleanBlackList.add("com.android.mediacenter");
        cleanBlackList.add("com.huawei.KoBackup");
        cleanBlackList.add("com.huawei.KoBackup.CMCC");
        cleanBlackList.add("com.android.email");
        cleanBlackList.add("com.android.soundrecorder");
        cleanBlackList.add("com.mediatek.fmradio");
        ArrayList<String> audioPlayingPkg = getAudioPlayingPkg();
        if (audioPlayingPkg != null) {
            for (String pkg : audioPlayingPkg) {
                if (!cleanBlackList.contains(pkg)) {
                    cleanBlackList.add(pkg);
                    Log.d("ExtremeClean", "Extreme clean playing audio : " + pkg);
                }
            }
        }
        return cleanBlackList;
    }
}
