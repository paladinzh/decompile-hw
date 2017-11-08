package com.huawei.powergenie.core.battery;

import android.net.TrafficStats;
import android.os.SystemClock;
import android.util.Log;
import com.android.internal.os.PowerProfile;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.Utils;
import com.huawei.powergenie.core.BaseService;
import com.huawei.powergenie.core.policy.DBWrapper;
import com.huawei.powergenie.core.policy.DBWrapper.ScrOffItem;
import com.huawei.powergenie.debugtest.DbgUtils;
import com.huawei.powergenie.integration.eventhub.MsgEvent;
import java.util.ArrayList;

public final class BatteryMonitor extends BaseService {
    private int mBatteryCapacity;
    private int mBatteryLevelWhenScrOff = 0;
    private ScrOffItem mCurScrOffItem = null;
    private final DBWrapper mDBWrapper;
    private long mElapsedTimeWhenScrOff = 0;
    private final ArrayList<ScrOffItem> mHistoryScrOffItems = new ArrayList();
    private final IAppManager mIAppManager;
    private final ICoreContext mICoreContext;
    private final IDeviceState mIDeviceState;
    private int mLastBatteryLevel = 0;
    private long mUptimeWhenScrOff = 0;

    public BatteryMonitor(ICoreContext coreContext) {
        this.mICoreContext = coreContext;
        this.mIAppManager = (IAppManager) coreContext.getService("appmamager");
        this.mIDeviceState = (IDeviceState) coreContext.getService("device");
        this.mDBWrapper = new DBWrapper(coreContext.getContext());
        this.mBatteryCapacity = (int) new PowerProfile(coreContext.getContext()).getBatteryCapacity();
        Log.i("BatteryMonitor", "device battery capacity:" + this.mBatteryCapacity);
        if (this.mBatteryCapacity < 2000) {
            this.mBatteryCapacity = 3000;
        }
    }

    public void onInputMsgEvent(MsgEvent evt) {
        switch (evt.getEventId()) {
            case 300:
                handleScreenOn();
                return;
            case 301:
                handleScreenOff();
                return;
            case 303:
                writeScrOffItemsDB();
                return;
            case 308:
                int level = evt.getIntent().getIntExtra("level", 0);
                if (level != this.mLastBatteryLevel) {
                    handleBatteryLevelChanged(level);
                    this.mLastBatteryLevel = level;
                    return;
                }
                return;
            default:
                return;
        }
    }

    protected void handleBatteryLevelChanged(int level) {
        if (this.mIDeviceState.isScreenOff() && !this.mIDeviceState.isCharging()) {
            if (this.mIDeviceState.isCalling() || this.mIDeviceState.isPlayingSound()) {
                this.mCurScrOffItem = null;
                Log.i("BatteryMonitor", "calling or playing screen off battery level : " + level);
                return;
            }
            if (level < this.mBatteryLevelWhenScrOff) {
                Log.i("BatteryMonitor", "screen off battery level : " + level);
                if (this.mCurScrOffItem == null) {
                    this.mCurScrOffItem = new ScrOffItem();
                    initScrOffUsage(this.mCurScrOffItem, level);
                }
            }
        }
    }

    private void handleScreenOff() {
        this.mElapsedTimeWhenScrOff = SystemClock.elapsedRealtime();
        this.mUptimeWhenScrOff = SystemClock.uptimeMillis();
        this.mBatteryLevelWhenScrOff = this.mIDeviceState.getBatteryLevel();
    }

    private void handleScreenOn() {
        if (this.mCurScrOffItem != null) {
            if (SystemClock.elapsedRealtime() - this.mCurScrOffItem.totalTime >= 1200000) {
                collectScrOffUsage(this.mCurScrOffItem);
                this.mHistoryScrOffItems.add(this.mCurScrOffItem);
                if (this.mHistoryScrOffItems.size() >= 5) {
                    writeScrOffItemsDB();
                }
            }
            this.mCurScrOffItem = null;
        }
        long screenOffDuration = SystemClock.elapsedRealtime() - this.mElapsedTimeWhenScrOff;
        long awakeDuration = SystemClock.uptimeMillis() - this.mUptimeWhenScrOff;
        long batterylevel = 0;
        if (this.mBatteryLevelWhenScrOff > 0) {
            batterylevel = (long) (this.mIDeviceState.getBatteryLevel() - this.mBatteryLevelWhenScrOff);
        }
        String title = "screen off: " + Utils.formatDuration(screenOffDuration) + ", awake: " + Utils.formatDuration(awakeDuration);
        String usage = " wakeups: " + this.mIAppManager.getTotalWakeupsSinceScrOff() + ", battery changed: " + batterylevel;
        Log.i("BatteryMonitor", title + usage);
        if (screenOffDuration >= 1200000) {
            DbgUtils.sendNotification(title, usage);
        }
    }

    private void initScrOffUsage(ScrOffItem item, int level) {
        Log.i("BatteryMonitor", "init usage battery level:" + level);
        item.startTime = Utils.formatDate(System.currentTimeMillis());
        item.totalTime = SystemClock.elapsedRealtime();
        item.wkTime = SystemClock.uptimeMillis();
        item.powerUsage = level;
        item.wakeups = this.mIAppManager.getTotalWakeupsSinceScrOff();
        item.mobileRx = TrafficStats.getMobileRxBytes();
        item.mobileTx = TrafficStats.getMobileTxBytes();
        item.wifiRx = TrafficStats.getTotalRxBytes() - item.mobileRx;
        item.wifiTx = TrafficStats.getTotalTxBytes() - item.mobileTx;
        item.wifiScan = this.mIDeviceState.getWifiScanTime();
        item.gpsTime = this.mIDeviceState.getGpsTime();
        item.reason = "normal";
    }

    private void collectScrOffUsage(ScrOffItem item) {
        item.totalTime = SystemClock.elapsedRealtime() - item.totalTime;
        item.wkTime = SystemClock.uptimeMillis() - item.wkTime;
        item.powerUsage -= this.mIDeviceState.getBatteryLevel();
        if (item.totalTime > 1000) {
            item.avgPower = (int) (((long) (((this.mBatteryCapacity * item.powerUsage) / 100) * 3600)) / (item.totalTime / 1000));
        } else {
            item.avgPower = 0;
        }
        item.wakeups = this.mIAppManager.getTotalWakeupsSinceScrOff() - item.wakeups;
        item.mobileRx = TrafficStats.getMobileRxBytes() - item.mobileRx;
        item.mobileTx = TrafficStats.getMobileTxBytes() - item.mobileTx;
        item.wifiRx = (TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes()) - item.wifiRx;
        item.wifiTx = (TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes()) - item.wifiTx;
        item.wifiScan = this.mIDeviceState.getWifiScanTime() - item.wifiScan;
        item.gpsTime = this.mIDeviceState.getGpsTime() - item.gpsTime;
        Log.i("BatteryMonitor", "collect usage battery changed level:" + item.powerUsage);
        if (DbgUtils.DBG_TIPS) {
            StringBuilder usage = new StringBuilder();
            usage.append("time: ").append(Utils.formatDuration(item.totalTime));
            usage.append(", awake: ").append(Utils.formatDuration(item.wkTime));
            usage.append("\n wakeups: ").append(item.wakeups);
            usage.append(" battery usage: ").append(item.powerUsage);
            usage.append("\n wifisan: ").append(Utils.formatDuration(item.wifiScan));
            usage.append(", gps: ").append(Utils.formatDuration(item.gpsTime));
            usage.append(", \n mobileRxTx: ").append(item.mobileRx + item.mobileTx);
            usage.append("bytes, wifiRxTx: ").append(item.wifiRx + item.wifiTx).append("bytes");
            Log.i("BatteryMonitor", "usage battery:" + usage.toString());
            DbgUtils.sendNotification("screen off usage", usage.toString());
        }
    }

    private void writeScrOffItemsDB() {
        Log.i("BatteryMonitor", "write the pendding screen off usages to db.");
        for (ScrOffItem item : this.mHistoryScrOffItems) {
            this.mDBWrapper.addScrOffItem(item);
        }
        this.mHistoryScrOffItems.clear();
    }
}
