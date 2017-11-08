package com.huawei.powergenie.core.device;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.IAppType;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IScenario;
import com.huawei.powergenie.api.ISdkService;
import com.huawei.powergenie.debugtest.DbgUtils;
import com.huawei.powergenie.integration.eventhub.HookEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class DeviceMonitor {
    private static final boolean DEBUG_USB = DbgUtils.DBG_USB;
    private Context mContext;
    private DeviceStats mDeviceStats;
    private String mFrontPkgWhenScreenOff = null;
    private GpsStats mGpsStats;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    if (DeviceMonitor.this.mIDeviceState.isPlayingSound()) {
                        Log.i("DeviceMonitor", "clock app :" + msg.obj);
                        DeviceMonitor.this.mIAppType.updateAppType(10, msg.obj);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private IAppManager mIAppManager;
    private IAppType mIAppType;
    private ICoreContext mICoreContext;
    private IDeviceState mIDeviceState;
    private IScenario mIScenario;
    private ISdkService mISdkService;
    private boolean mIsScreenOff = false;
    private long mScrOffTime = 0;
    private long mScrOnTime = 0;
    private WakelockStats mWakelockStats;
    private WifiScanStats mWifiScanStats;

    private abstract class DeviceStats {
        protected DeviceStats mNextStats;

        protected abstract boolean handleStatsEvent(HookEvent hookEvent);

        private DeviceStats() {
        }

        protected void setNextHandleStats(DeviceStats nextStats) {
            this.mNextStats = nextStats;
        }

        protected boolean nextHandleStats(HookEvent event) {
            if (this.mNextStats != null) {
                return this.mNextStats.handleStatsEvent(event);
            }
            return false;
        }
    }

    private class GpsStats extends DeviceStats {
        private long mGpsTotalTime;
        private HashMap<Integer, Long> mGpsUidsScrOffStats;
        private HashMap<Integer, Long> mGpsUidsStart;
        private HashMap<Integer, Long> mGpsUidsStats;

        private GpsStats() {
            super();
            this.mGpsTotalTime = 0;
            this.mGpsUidsStart = new HashMap();
            this.mGpsUidsStats = new HashMap();
            this.mGpsUidsScrOffStats = new HashMap();
        }

        protected boolean handleStatsEvent(HookEvent event) {
            int eventId = event.getEventId();
            int uid;
            if (eventId == 156) {
                uid = Integer.parseInt(event.getPkgName());
                if (!this.mGpsUidsStart.containsKey(Integer.valueOf(uid))) {
                    this.mGpsUidsStart.put(Integer.valueOf(uid), Long.valueOf(SystemClock.elapsedRealtime()));
                    DeviceMonitor.this.mISdkService.handleStateChanged(3, 1, 0, null, uid);
                }
                return true;
            } else if (eventId != 157) {
                return nextHandleStats(event);
            } else {
                uid = Integer.parseInt(event.getPkgName());
                if (this.mGpsUidsStart.containsKey(Integer.valueOf(uid))) {
                    long nowMs = SystemClock.elapsedRealtime();
                    long startTime = ((Long) this.mGpsUidsStart.remove(Integer.valueOf(uid))).longValue();
                    long incTime = nowMs - startTime;
                    this.mGpsTotalTime += incTime;
                    if (this.mGpsUidsStats.containsKey(Integer.valueOf(uid))) {
                        this.mGpsUidsStats.put(Integer.valueOf(uid), Long.valueOf(incTime + ((Long) this.mGpsUidsStats.get(Integer.valueOf(uid))).longValue()));
                    } else {
                        this.mGpsUidsStats.put(Integer.valueOf(uid), Long.valueOf(incTime));
                    }
                    updateScrOffGpsTime(uid, startTime, nowMs);
                    if (DeviceMonitor.this.mIsScreenOff) {
                        Log.i("DeviceMonitor", "GPS_END uid:" + uid + " time:" + incTime + " total:" + this.mGpsUidsStats.get(Integer.valueOf(uid)) + "currentScrnOff : " + this.mGpsUidsScrOffStats.get(Integer.valueOf(uid)));
                    }
                }
                DeviceMonitor.this.mISdkService.handleStateChanged(3, 2, 0, null, uid);
                return true;
            }
        }

        private boolean hasActiveGps(int uid) {
            if (this.mGpsUidsStart.containsKey(Integer.valueOf(uid))) {
                return true;
            }
            return false;
        }

        private void updateScrOffGpsTime(int uid, long startTime, long nowMs) {
            if (DeviceMonitor.this.mIsScreenOff) {
                long scrOffIncGpsTime = nowMs - (DeviceMonitor.this.mScrOffTime < startTime ? startTime : DeviceMonitor.this.mScrOffTime);
                Long scrOffGpsTime = (Long) this.mGpsUidsScrOffStats.get(Integer.valueOf(uid));
                if (scrOffGpsTime != null) {
                    this.mGpsUidsScrOffStats.put(Integer.valueOf(uid), Long.valueOf(scrOffIncGpsTime + scrOffGpsTime.longValue()));
                } else {
                    this.mGpsUidsScrOffStats.put(Integer.valueOf(uid), Long.valueOf(scrOffIncGpsTime));
                }
            }
        }

        private boolean hasActiveGps() {
            return this.mGpsUidsStart.size() > 0;
        }

        private long getGpsTime(int uid) {
            return getHistoryTime(uid) + getCurrentActiveTime(uid);
        }

        private long getHistoryTime(int uid) {
            if (uid == 0) {
                return this.mGpsTotalTime;
            }
            if (this.mGpsUidsStats.containsKey(Integer.valueOf(uid))) {
                return ((Long) this.mGpsUidsStats.get(Integer.valueOf(uid))).longValue();
            }
            return 0;
        }

        private long getCurrentActiveTime(int uid) {
            if (this.mGpsUidsStart.containsKey(Integer.valueOf(uid))) {
                Long time = (Long) this.mGpsUidsStart.get(Integer.valueOf(uid));
                if (time != null) {
                    return SystemClock.elapsedRealtime() - time.longValue();
                }
            }
            return 0;
        }

        private long getScrOffCurrentActiveTime(int uid) {
            if (DeviceMonitor.this.mIsScreenOff) {
                Long startTime = (Long) this.mGpsUidsStart.get(Integer.valueOf(uid));
                if (startTime != null) {
                    return SystemClock.elapsedRealtime() - (DeviceMonitor.this.mScrOffTime < startTime.longValue() ? startTime.longValue() : DeviceMonitor.this.mScrOffTime);
                }
            }
            return 0;
        }

        private long getScrOffGpsTime(int uid) {
            long gpsTime = getScrOffCurrentActiveTime(uid);
            Long scrOffGpsTime = (Long) this.mGpsUidsScrOffStats.get(Integer.valueOf(uid));
            if (scrOffGpsTime != null) {
                return gpsTime + scrOffGpsTime.longValue();
            }
            return gpsTime;
        }

        private void handleScrState(boolean isScrnOn) {
            this.mGpsUidsScrOffStats.clear();
        }
    }

    private class WakelockStats extends DeviceStats {
        private final HashMap<Integer, Integer> mActiveAudioInWL;
        private final HashMap<Integer, Integer> mActiveAudioMixWL;
        private long mAoTagTime;
        private ArrayList<String> mFilterNotClocks;
        private boolean mHasWkEvent;
        private int mLastReleaseAudioInUid;
        private int mLastReleaseAudioMixUid;
        private int mUid;
        private ArrayList<WakelockItem> mWakelockItemList;
        private long mWbFlagTime;

        class WakelockItem {
            long mAcquireTime = 0;
            int mFlags = -1;
            boolean mIsReleased = false;
            int mPid = -1;
            String mTag = null;
            long mTotalHeldTime = 0;
            long mTotalNoOverlapHeldTime = 0;
            int mUid = -1;

            public WakelockItem(int flags, String tag, int uid, int pid) {
                this.mFlags = flags;
                this.mTag = tag;
                this.mUid = uid;
                this.mPid = pid;
                this.mAcquireTime = SystemClock.elapsedRealtime();
            }

            private void releaseWklock(long noOverlapEndTime) {
                if (!this.mIsReleased) {
                    long nowMs = SystemClock.elapsedRealtime();
                    if (this.mAcquireTime > DeviceMonitor.this.mScrOffTime) {
                        this.mTotalHeldTime += nowMs - this.mAcquireTime;
                    } else {
                        this.mTotalHeldTime += nowMs - DeviceMonitor.this.mScrOffTime;
                    }
                    if (noOverlapEndTime > DeviceMonitor.this.mScrOffTime && noOverlapEndTime > this.mAcquireTime) {
                        this.mTotalNoOverlapHeldTime += noOverlapEndTime - this.mAcquireTime;
                    }
                    this.mIsReleased = true;
                }
            }

            private void acquireWkItem() {
                if (this.mIsReleased) {
                    this.mAcquireTime = SystemClock.elapsedRealtime();
                    this.mIsReleased = false;
                }
            }

            private boolean hasSameWakelock(int flags, String tag, int uid, int pid) {
                boolean z = false;
                if (tag == null) {
                    return false;
                }
                if (this.mFlags == flags && this.mPid == pid && this.mUid == uid) {
                    z = tag.equals(this.mTag);
                }
                return z;
            }

            private long getNoOverlapWkTime() {
                return this.mTotalNoOverlapHeldTime;
            }

            private long getWkTime() {
                long runTime = this.mTotalHeldTime;
                if (this.mIsReleased) {
                    return runTime;
                }
                long nowMs = SystemClock.elapsedRealtime();
                if (this.mAcquireTime > DeviceMonitor.this.mScrOffTime) {
                    runTime += nowMs - this.mAcquireTime;
                } else {
                    runTime += nowMs - DeviceMonitor.this.mScrOffTime;
                }
                return runTime;
            }

            private boolean matched(int uid, int pid) {
                if (uid != -1 && this.mUid != uid) {
                    return false;
                }
                if (pid == -1 || this.mPid == -2 || this.mPid == pid) {
                    return true;
                }
                return false;
            }
        }

        private WakelockStats() {
            super();
            this.mWakelockItemList = new ArrayList();
            this.mLastReleaseAudioMixUid = -1;
            this.mLastReleaseAudioInUid = -1;
            this.mActiveAudioMixWL = new HashMap();
            this.mActiveAudioInWL = new HashMap();
            this.mHasWkEvent = false;
            this.mWbFlagTime = 0;
            this.mAoTagTime = 0;
            this.mUid = -1;
            this.mFilterNotClocks = new ArrayList<String>() {
                {
                    add("sh.lilith.dgame");
                    add("com.tencent.tmgp.rxcq");
                    add("com.baidu.BaiduMap");
                    add("com.jingdong.app.mall");
                }
            };
        }

        private void handleScrState(boolean scrOn) {
            if (scrOn) {
                long interval = SystemClock.elapsedRealtime() - DeviceMonitor.this.mScrOffTime;
                if (DeviceMonitor.DEBUG_USB || interval >= 3600000) {
                    synchronized (this.mWakelockItemList) {
                        for (WakelockItem item : this.mWakelockItemList) {
                            long heldTime = item.getWkTime();
                            if (heldTime >= 30000) {
                                Log.i("DeviceMonitor", "Top wakelock UID=" + item.mUid + " PID=" + item.mPid + " wkTime=" + heldTime);
                            }
                        }
                    }
                }
                removeAllReleaseWklocks();
                clockAppIdentify();
                return;
            }
            DeviceMonitor.this.mFrontPkgWhenScreenOff = DeviceMonitor.this.mIScenario.getFrontPkg();
        }

        private long getNoOverlapEndTime(WakelockItem wklock) {
            long endTime = SystemClock.elapsedRealtime();
            synchronized (this.mWakelockItemList) {
                for (WakelockItem item : this.mWakelockItemList) {
                    if (!item.mIsReleased && wklock.mUid == item.mUid && wklock.mPid == item.mPid && !item.hasSameWakelock(wklock.mFlags, wklock.mTag, wklock.mUid, wklock.mPid) && endTime > item.mAcquireTime) {
                        endTime = item.mAcquireTime;
                    }
                }
            }
            return endTime;
        }

        private WakelockItem findWakeLock(int flags, String tag, int uid, int pid) {
            synchronized (this.mWakelockItemList) {
                for (WakelockItem item : this.mWakelockItemList) {
                    if (item.hasSameWakelock(flags, tag, uid, pid)) {
                        return item;
                    }
                }
                return null;
            }
        }

        private WakelockItem removeWakeLock(int flags, String tag, int uid, int pid) {
            synchronized (this.mWakelockItemList) {
                for (WakelockItem item : this.mWakelockItemList) {
                    if (item.hasSameWakelock(flags, tag, uid, pid)) {
                        this.mWakelockItemList.remove(item);
                        return item;
                    }
                }
                return null;
            }
        }

        private void removeAllReleaseWklocks() {
            ArrayList<WakelockItem> tempWakelockStats = new ArrayList();
            synchronized (this.mWakelockItemList) {
                for (WakelockItem item : this.mWakelockItemList) {
                    if (item.mIsReleased) {
                        tempWakelockStats.add(item);
                    }
                }
                this.mWakelockItemList.removeAll(tempWakelockStats);
            }
        }

        protected boolean handleStatsEvent(HookEvent event) {
            int eventId = event.getEventId();
            if (eventId != 160 && eventId != 161) {
                return nextHandleStats(event);
            }
            this.mHasWkEvent = true;
            int flags = Integer.parseInt(event.getValue1());
            int uid = Integer.parseInt(event.getPkgName());
            int pid = Integer.parseInt(event.getValue2());
            String tag = event.getValue3();
            WakelockItem wklock;
            HashMap hashMap;
            if (eventId == 160) {
                wklock = findWakeLock(flags, tag, uid, pid);
                if (wklock == null) {
                    synchronized (this.mWakelockItemList) {
                        this.mWakelockItemList.add(new WakelockItem(flags, tag, uid, pid));
                    }
                } else {
                    wklock.acquireWkItem();
                }
                if (pid == -2) {
                    if ("AudioMix".equals(tag)) {
                        hashMap = this.mActiveAudioMixWL;
                        synchronized (hashMap) {
                            this.mActiveAudioMixWL.put(Integer.valueOf(uid), Integer.valueOf(pid));
                        }
                    } else if ("AudioIn".equals(tag)) {
                        hashMap = this.mActiveAudioInWL;
                        synchronized (hashMap) {
                            this.mActiveAudioInWL.put(Integer.valueOf(uid), Integer.valueOf(pid));
                            Log.i("DeviceMonitor", "Add AudioIn uid:" + uid + " pid:" + pid);
                        }
                    }
                }
                collectClockWkTime(uid, flags, tag);
            } else {
                if (DeviceMonitor.this.mIsScreenOff) {
                    wklock = findWakeLock(flags, tag, uid, pid);
                    if (wklock != null) {
                        wklock.releaseWklock(getNoOverlapEndTime(wklock));
                        long heldTime = wklock.getWkTime();
                        if (heldTime >= 60000) {
                            Log.i("DeviceMonitor", " uid:" + uid + " pid:" + pid + " flags:" + flags + " tag:" + tag + " wakeup time:" + heldTime);
                        }
                    }
                } else {
                    removeWakeLock(flags, tag, uid, pid);
                }
                if (pid == -2) {
                    if ("AudioMix".equals(tag)) {
                        hashMap = this.mActiveAudioMixWL;
                        synchronized (hashMap) {
                            this.mActiveAudioMixWL.remove(Integer.valueOf(uid));
                        }
                    } else if ("AudioIn".equals(tag)) {
                        hashMap = this.mActiveAudioInWL;
                        synchronized (hashMap) {
                            this.mActiveAudioInWL.remove(Integer.valueOf(uid));
                            Log.i("DeviceMonitor", "Remove AudioIn uid:" + uid);
                        }
                    }
                }
                if ("AudioMix".equals(tag) && UserHandle.getAppId(uid) >= 10000) {
                    this.mLastReleaseAudioMixUid = uid;
                    Log.i("DeviceMonitor", "last release audio mix uid:" + uid + " pid:" + pid);
                } else if ("AudioIn".equals(tag) && UserHandle.getAppId(uid) >= 10000) {
                    this.mLastReleaseAudioInUid = uid;
                    Log.i("DeviceMonitor", "last release audio in uid:" + uid + " pid:" + pid);
                }
            }
            return true;
        }

        private long getWkTimeByUidPid(int uid, int pid) {
            if (!this.mHasWkEvent) {
                return -1;
            }
            long wkTime = 0;
            long earliestAqTime = SystemClock.elapsedRealtime();
            synchronized (this.mWakelockItemList) {
                for (WakelockItem item : this.mWakelockItemList) {
                    if (item.matched(uid, pid)) {
                        wkTime += item.getNoOverlapWkTime();
                        if (!item.mIsReleased && earliestAqTime > item.mAcquireTime) {
                            earliestAqTime = item.mAcquireTime;
                        }
                    }
                }
                if (earliestAqTime < DeviceMonitor.this.mScrOffTime) {
                    earliestAqTime = DeviceMonitor.this.mScrOffTime;
                }
                wkTime += SystemClock.elapsedRealtime() - earliestAqTime;
            }
            return wkTime;
        }

        private String getWkTagByUidPid(int uid, int pid) {
            WakelockItem wkItem = null;
            synchronized (this.mWakelockItemList) {
                for (WakelockItem item : this.mWakelockItemList) {
                    if (uid == item.mUid && pid == item.mPid) {
                        if (wkItem == null) {
                            wkItem = item;
                        } else if (item.getWkTime() > wkItem.getWkTime()) {
                            wkItem = item;
                        }
                    }
                }
            }
            if (wkItem != null) {
                return wkItem.mTag;
            }
            return null;
        }

        private Set<Integer> getWkUidsByTag(String tag) {
            if (tag == null) {
                return null;
            }
            Set<Integer> keySet;
            if ("AudioMix".equals(tag)) {
                synchronized (this.mActiveAudioMixWL) {
                    keySet = this.mActiveAudioMixWL.keySet();
                }
                return keySet;
            } else if (!"AudioIn".equals(tag)) {
                return null;
            } else {
                synchronized (this.mActiveAudioInWL) {
                    keySet = this.mActiveAudioInWL.keySet();
                }
                return keySet;
            }
        }

        private Set<Integer> getWkPidsByTag(String tag) {
            if (tag == null) {
                return null;
            }
            HashSet pidSet = new HashSet();
            int pid;
            Integer uid;
            if ("AudioMix".equals(tag)) {
                synchronized (this.mActiveAudioMixWL) {
                    for (Entry entry : this.mActiveAudioMixWL.entrySet()) {
                        pid = ((Integer) entry.getValue()).intValue();
                        if (pid > 0) {
                            pidSet.add(Integer.valueOf(pid));
                        } else {
                            uid = (Integer) entry.getKey();
                            pid = DeviceMonitor.this.mIAppManager.getPidByUid(uid.intValue());
                            if (pid > 0) {
                                pidSet.add(Integer.valueOf(pid));
                                this.mActiveAudioMixWL.put(uid, Integer.valueOf(pid));
                            }
                        }
                    }
                }
                return pidSet;
            } else if (!"AudioIn".equals(tag)) {
                return null;
            } else {
                synchronized (this.mActiveAudioInWL) {
                    for (Entry entry2 : this.mActiveAudioInWL.entrySet()) {
                        pid = ((Integer) entry2.getValue()).intValue();
                        if (pid > 0) {
                            pidSet.add(Integer.valueOf(pid));
                        } else {
                            uid = (Integer) entry2.getKey();
                            pid = DeviceMonitor.this.mIAppManager.getPidByUid(uid.intValue());
                            if (pid > 0) {
                                pidSet.add(Integer.valueOf(pid));
                                this.mActiveAudioInWL.put(uid, Integer.valueOf(pid));
                            }
                        }
                    }
                }
                return pidSet;
            }
        }

        private int getLastReleaseAudioMixUid() {
            return this.mLastReleaseAudioMixUid;
        }

        private int getLastReleaseAudioInUid() {
            return this.mLastReleaseAudioInUid;
        }

        private boolean isWindowBrightFlag(int flag) {
            if (536870922 == flag || 805306394 == flag || 268435457 == flag || 268435482 == flag || 268435466 == flag || 268435462 == flag) {
                return true;
            }
            return false;
        }

        private void collectClockWkTime(int uid, int flag, String tag) {
            if (UserHandle.getAppId(uid) >= 10000 && isWindowBrightFlag(flag)) {
                this.mWbFlagTime = SystemClock.elapsedRealtime();
                this.mUid = uid;
                clockAppIdentify();
            } else if (UserHandle.getAppId(uid) >= 10000) {
            } else {
                if ("AudioOut_5".equals(tag) || "AudioMix".equals(tag)) {
                    this.mAoTagTime = SystemClock.elapsedRealtime();
                    clockAppIdentify();
                }
            }
        }

        private void clockAppIdentify() {
            if (Math.max(Math.max(this.mWbFlagTime, this.mAoTagTime), DeviceMonitor.this.mScrOnTime) - Math.min(Math.min(this.mWbFlagTime, this.mAoTagTime), DeviceMonitor.this.mScrOnTime) < 2000 && DeviceMonitor.this.mIDeviceState.isKeyguardPresent()) {
                ArrayList<String> pkgList = DeviceMonitor.this.mIAppManager.getPkgNameByUid(DeviceMonitor.this.mContext, this.mUid);
                ArrayList<String> pkgScreenLockList = null;
                String frontPkg = DeviceMonitor.this.mIScenario.getFrontPkg();
                if (frontPkg == null || !frontPkg.equals(DeviceMonitor.this.mFrontPkgWhenScreenOff)) {
                    for (String pkgName : pkgList) {
                        for (String skipPkg : this.mFilterNotClocks) {
                            if (pkgName != null && pkgName.startsWith(skipPkg)) {
                                Log.i("DeviceMonitor", "not clock app:" + pkgName);
                                return;
                            }
                        }
                        if (pkgName == null || frontPkg == null || pkgName.startsWith(frontPkg)) {
                            if (pkgScreenLockList == null) {
                                pkgScreenLockList = DeviceMonitor.this.mIAppType.getAppsByType(9);
                            }
                            if (pkgScreenLockList != null && pkgScreenLockList.contains(pkgName)) {
                                Log.i("DeviceMonitor", "not clock app, it is screen lock:" + pkgName);
                                this.mFilterNotClocks.add(pkgName);
                            } else if (DeviceMonitor.this.mIAppType.getAppType(pkgName) == 11) {
                                Log.i("DeviceMonitor", "not clock app, it is im app:" + pkgName);
                                this.mFilterNotClocks.add(pkgName);
                            } else {
                                int appType = DeviceMonitor.this.mIAppType.getAppType(pkgName);
                                if (appType != -1) {
                                    Log.i("DeviceMonitor", pkgName + " not unknown app,it has been set type as:" + appType);
                                } else {
                                    Message msg = DeviceMonitor.this.mHandler.obtainMessage(200);
                                    msg.obj = pkgName;
                                    DeviceMonitor.this.mHandler.sendMessageDelayed(msg, 3000);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private class WifiScanStats extends DeviceStats {
        private long mWifiScanTotalTime;
        private HashMap<Integer, Long> mWifiScanUidsStart;
        private HashMap<Integer, Long> mWifiScanUidsStats;

        private WifiScanStats() {
            super();
            this.mWifiScanTotalTime = 0;
            this.mWifiScanUidsStart = new HashMap();
            this.mWifiScanUidsStats = new HashMap();
        }

        protected boolean handleStatsEvent(HookEvent event) {
            int eventId = event.getEventId();
            int uid;
            if (eventId == 158) {
                uid = Integer.parseInt(event.getPkgName());
                if (!this.mWifiScanUidsStart.containsKey(Integer.valueOf(uid))) {
                    this.mWifiScanUidsStart.put(Integer.valueOf(uid), Long.valueOf(SystemClock.elapsedRealtime()));
                }
                return true;
            } else if (eventId != 159) {
                return nextHandleStats(event);
            } else {
                uid = Integer.parseInt(event.getPkgName());
                if (this.mWifiScanUidsStart.containsKey(Integer.valueOf(uid))) {
                    long incTime = SystemClock.elapsedRealtime() - ((Long) this.mWifiScanUidsStart.remove(Integer.valueOf(uid))).longValue();
                    this.mWifiScanTotalTime += incTime;
                    if (this.mWifiScanUidsStats.containsKey(Integer.valueOf(uid))) {
                        this.mWifiScanUidsStats.put(Integer.valueOf(uid), Long.valueOf(incTime + ((Long) this.mWifiScanUidsStats.get(Integer.valueOf(uid))).longValue()));
                    } else {
                        this.mWifiScanUidsStats.put(Integer.valueOf(uid), Long.valueOf(incTime));
                    }
                    if (DeviceMonitor.this.mIsScreenOff && incTime > 60000) {
                        Log.i("DeviceMonitor", "waste power wifi scanning uid:" + uid + " time:" + incTime);
                    }
                }
                return true;
            }
        }

        private long getWifiScanTime(int uid) {
            return getHistoryTime(uid) + getCurrentActiveTime(uid);
        }

        private long getHistoryTime(int uid) {
            if (uid == 0) {
                return this.mWifiScanTotalTime;
            }
            if (this.mWifiScanUidsStats.containsKey(Integer.valueOf(uid))) {
                return ((Long) this.mWifiScanUidsStats.get(Integer.valueOf(uid))).longValue();
            }
            return 0;
        }

        private long getCurrentActiveTime(int uid) {
            if (this.mWifiScanUidsStart.containsKey(Integer.valueOf(uid))) {
                return SystemClock.elapsedRealtime() - ((Long) this.mWifiScanUidsStart.get(Integer.valueOf(uid))).longValue();
            }
            return 0;
        }
    }

    protected DeviceMonitor(Context context, ICoreContext coreContext) {
        this.mContext = context;
        this.mICoreContext = coreContext;
        this.mDeviceStats = createAllStats();
        this.mIAppManager = (IAppManager) this.mICoreContext.getService("appmamager");
        this.mIScenario = (IScenario) this.mICoreContext.getService("scenario");
        this.mIDeviceState = (IDeviceState) this.mICoreContext.getService("device");
        this.mISdkService = (ISdkService) this.mICoreContext.getService("sdk");
        this.mIAppType = (IAppType) this.mICoreContext.getService("appmamager");
    }

    private DeviceStats createAllStats() {
        this.mGpsStats = new GpsStats();
        this.mWifiScanStats = new WifiScanStats();
        this.mWakelockStats = new WakelockStats();
        this.mWakelockStats.setNextHandleStats(this.mWifiScanStats);
        this.mWifiScanStats.setNextHandleStats(this.mGpsStats);
        return this.mWakelockStats;
    }

    protected boolean processStatsEvent(HookEvent event) {
        if (this.mDeviceStats != null) {
            return this.mDeviceStats.handleStatsEvent(event);
        }
        return false;
    }

    public boolean hasActiveGps(int uid) {
        if (this.mGpsStats != null) {
            return this.mGpsStats.hasActiveGps(uid);
        }
        return false;
    }

    public boolean hasActiveGps() {
        if (this.mGpsStats != null) {
            return this.mGpsStats.hasActiveGps();
        }
        return false;
    }

    protected long getGpsTime(int uid) {
        if (this.mGpsStats != null) {
            return this.mGpsStats.getGpsTime(uid);
        }
        return 0;
    }

    protected long getCurrentGpsTime(int uid) {
        if (this.mGpsStats != null) {
            return this.mGpsStats.getCurrentActiveTime(uid);
        }
        return 0;
    }

    protected long getScrOffGpsTime(int uid) {
        if (this.mGpsStats != null) {
            return this.mGpsStats.getScrOffGpsTime(uid);
        }
        return 0;
    }

    protected long getWifiScanTime(int uid) {
        if (this.mWifiScanStats != null) {
            return this.mWifiScanStats.getWifiScanTime(uid);
        }
        return 0;
    }

    protected long getWkTimeByUidPid(int uid, int pid) {
        if (this.mWakelockStats != null) {
            return this.mWakelockStats.getWkTimeByUidPid(uid, pid);
        }
        return 0;
    }

    protected String getWkTagByUidPid(int uid, int pid) {
        if (this.mWakelockStats != null) {
            return this.mWakelockStats.getWkTagByUidPid(uid, pid);
        }
        return null;
    }

    protected Set<Integer> getWkUidsByTag(String tag) {
        if (this.mWakelockStats != null) {
            return this.mWakelockStats.getWkUidsByTag(tag);
        }
        return null;
    }

    protected Set<Integer> getWkPidsByTag(String tag) {
        if (this.mWakelockStats != null) {
            return this.mWakelockStats.getWkPidsByTag(tag);
        }
        return null;
    }

    protected int getLastReleaseAudioMixUid() {
        if (this.mWakelockStats != null) {
            return this.mWakelockStats.getLastReleaseAudioMixUid();
        }
        return -1;
    }

    protected int getLastReleaseAudioInUid() {
        if (this.mWakelockStats != null) {
            return this.mWakelockStats.getLastReleaseAudioInUid();
        }
        return -1;
    }

    protected void handleScrState(boolean isScrOn) {
        this.mIsScreenOff = !isScrOn;
        if (isScrOn) {
            this.mScrOnTime = SystemClock.elapsedRealtime();
        } else {
            this.mScrOffTime = SystemClock.elapsedRealtime();
        }
        if (this.mWakelockStats != null) {
            this.mWakelockStats.handleScrState(isScrOn);
        }
        if (this.mGpsStats != null) {
            this.mGpsStats.handleScrState(isScrOn);
        }
    }
}
