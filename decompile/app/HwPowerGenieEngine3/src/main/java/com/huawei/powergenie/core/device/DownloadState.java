package com.huawei.powergenie.core.device;

import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.ISdkService;
import com.huawei.powergenie.debugtest.DbgUtils;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;

public class DownloadState {
    private static final boolean DEBUG_USB = DbgUtils.DBG_USB;
    private final HashMap<Integer, Long> mCurUidBytes = new HashMap();
    private final DeviceStateService mDeviceState;
    private boolean mEnable = false;
    private final ISdkService mISdkService;
    private final TrafficStatsRunnable mRunnable;
    private final HashMap<Integer, Long> mUidBytes = new HashMap();
    private final HashMap<Integer, Boolean> mUidDownloadState = new HashMap();
    private final HashMap<Integer, Integer> mUidStopTimes = new HashMap();

    private class TrafficStatsRunnable implements Runnable {
        private TrafficStatsRunnable() {
        }

        public void run() {
            DownloadState.this.translateArrayToHashMap(NativeAdapter.updateTrafficStats(), DownloadState.this.mUidBytes);
            while (DownloadState.this.mEnable) {
                try {
                    synchronized (this) {
                        int t = DownloadState.this.is2GNetwork() ? 3000 : 5000;
                        wait((long) t);
                        if (DownloadState.this.mEnable) {
                            DownloadState.this.updateSpeed(t);
                        }
                    }
                } catch (Exception e) {
                    Log.e("DownloadState", "Unexpected exception", e);
                }
            }
            Log.d("DownloadState", "traffic stats thread quit");
            DownloadState.this.mUidBytes.clear();
            DownloadState.this.mUidStopTimes.clear();
            for (Entry entry : DownloadState.this.mUidDownloadState.entrySet()) {
                Integer uid = (Integer) entry.getKey();
                if (((Boolean) entry.getValue()).booleanValue()) {
                    DownloadState.this.updateDownloadState(uid.intValue(), false);
                }
            }
            DownloadState.this.mUidDownloadState.clear();
        }
    }

    protected DownloadState(ICoreContext coreContext, DeviceStateService deviceState) {
        this.mISdkService = (ISdkService) coreContext.getService("sdk");
        this.mDeviceState = deviceState;
        this.mRunnable = new TrafficStatsRunnable();
    }

    protected void enable(boolean enable) {
        if (this.mEnable != enable) {
            Log.i("DownloadState", "traffic enable:" + enable);
            if (enable) {
                this.mEnable = true;
                new Thread(this.mRunnable, "traffic stats").start();
            } else {
                this.mEnable = false;
                synchronized (this.mRunnable) {
                    this.mRunnable.notify();
                }
            }
        }
    }

    protected boolean isDlUploading(int uid) {
        Boolean state = (Boolean) this.mUidDownloadState.get(Integer.valueOf(uid));
        return state == null ? false : state.booleanValue();
    }

    private void updateDownloadState(int uid, boolean download) {
        Log.i("DownloadState", "@@@@@@@@@@@@ uid :" + uid + " download :" + download);
        this.mUidDownloadState.put(Integer.valueOf(uid), Boolean.valueOf(download));
        if (download) {
            this.mISdkService.handleStateChanged(5, 1, 0, null, uid);
        } else {
            this.mISdkService.handleStateChanged(5, 2, 0, null, uid);
        }
    }

    private void updateSpeed(int t) {
        translateArrayToHashMap(NativeAdapter.updateTrafficStats(), this.mCurUidBytes);
        if (this.mCurUidBytes.size() != 0) {
            for (Entry entry : this.mCurUidBytes.entrySet()) {
                Integer uid = (Integer) entry.getKey();
                long bytes = ((Long) entry.getValue()).longValue();
                long speed = ((bytes - (this.mUidBytes.containsKey(uid) ? ((Long) this.mUidBytes.get(uid)).longValue() : 0)) * 1000) / ((long) t);
                boolean lastState = this.mUidDownloadState.containsKey(uid) ? ((Boolean) this.mUidDownloadState.get(uid)).booleanValue() : false;
                boolean curState = false;
                boolean update = false;
                if (DEBUG_USB && speed > 0) {
                    Log.i("DownloadState", uid + " transmitting data speed : " + speed + " bytes/s");
                }
                if (isDlUploadingStartSpeed(speed)) {
                    curState = true;
                    update = true;
                    resetStopTimes(uid);
                } else if (lastState && isDlUploadingStopSpeed(uid, speed)) {
                    curState = false;
                    update = true;
                    resetStopTimes(uid);
                }
                if (update && lastState != curState) {
                    updateDownloadState(uid.intValue(), curState);
                }
                this.mUidBytes.put(uid, Long.valueOf(bytes));
            }
        }
    }

    private boolean isDlUploadingStartSpeed(long speed) {
        if (speed >= 8192) {
            return true;
        }
        if (speed < 1024 || !is2GNetwork()) {
            return false;
        }
        return true;
    }

    private boolean is2GNetwork() {
        return this.mDeviceState.isMobileConnected() ? this.mDeviceState.is2GNetworkClass() : false;
    }

    private boolean isDlUploadingStopSpeed(Integer uid, long speed) {
        if (speed == 0) {
            return true;
        }
        if (is2GNetwork()) {
            return false;
        }
        if (speed < 256) {
            return true;
        }
        if (speed >= 6144) {
            if (DEBUG_USB) {
                Log.i("DownloadState", uid + " download speed > 6k/s, reset");
            }
            resetStopTimes(uid);
        } else if (((long) updateStopTimes(uid)) >= 3) {
            if (DEBUG_USB) {
                Log.i("DownloadState", uid + " download speed < 6k/s for 3 times");
            }
            return true;
        }
        return false;
    }

    private int updateStopTimes(Integer uid) {
        int count;
        Integer times = (Integer) this.mUidStopTimes.get(uid);
        if (times == null) {
            count = 1;
        } else {
            count = times.intValue() + 1;
            times = Integer.valueOf(count);
        }
        this.mUidStopTimes.put(uid, Integer.valueOf(count));
        return count;
    }

    private void resetStopTimes(Integer uid) {
        this.mUidStopTimes.remove(uid);
    }

    private void translateArrayToHashMap(long[] keyValue, HashMap<Integer, Long> hasMap) {
        if (keyValue != null) {
            hasMap.clear();
            int len = keyValue.length;
            for (int i = 0; i < len / 2; i++) {
                hasMap.put(Integer.valueOf((int) keyValue[i * 2]), Long.valueOf(keyValue[(i * 2) + 1]));
            }
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        pw.println("\nDownload STATE");
        HashMap<Integer, Long> uidBytes = (HashMap) this.mUidBytes.clone();
        HashMap<Integer, Boolean> uidDownloadState = (HashMap) this.mUidDownloadState.clone();
        pw.println("TrafficStatsRunnable: " + this.mEnable + " History Size:" + uidBytes.size() + " State Size:" + uidDownloadState.size());
        for (Entry entry : uidDownloadState.entrySet()) {
            Integer uid = (Integer) entry.getKey();
            Boolean state = (Boolean) entry.getValue();
            if (state.booleanValue()) {
                pw.println("UID: " + uid + "\t Download: " + state + " Bytes:" + uidBytes.get(uid));
            }
        }
    }
}
