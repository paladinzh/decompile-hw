package com.huawei.powergenie.core.device;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.ISdkService;
import com.huawei.powergenie.integration.eventhub.HookEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class BluetoothState {
    private long mBluethoothDisconnectTime = 0;
    private final ArrayList<BtActiveRecord> mBtActiveRecord = new ArrayList();
    private final IAppManager mIAppManager;
    private final ICoreContext mICoreContext;
    private final ISdkService mISdkService;
    private boolean mIsBluethoothConnected = false;
    private boolean mIsBtOn = false;

    private static final class BtActiveRecord {
        public final int mPid;
        public final String mReason;
        public final int mType;
        public final int mUid;

        public BtActiveRecord(int uid, int pid, int type, String reason) {
            this.mUid = uid;
            this.mPid = pid;
            this.mType = type;
            this.mReason = reason;
        }

        public boolean match(int uid, int pid) {
            return this.mUid == uid || this.mPid == pid;
        }
    }

    protected BluetoothState(ICoreContext coreContext) {
        this.mICoreContext = coreContext;
        this.mIAppManager = (IAppManager) this.mICoreContext.getService("appmamager");
        this.mISdkService = (ISdkService) coreContext.getService("sdk");
        this.mIsBtOn = isBluetoothEnable();
    }

    private void updateAppBluetoothState(boolean btStart, int uid) {
        this.mISdkService.handleStateChanged(8, btStart ? 1 : 2, 0, null, uid);
    }

    private boolean isBluetoothEnable() {
        BluetoothAdapter localBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (localBluetoothAdapter != null) {
            return localBluetoothAdapter.isEnabled();
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean isActiveBtApp(String pkg, int uid, int pid) {
        if (pkg == null) {
            Log.e("BluetoothState", "error argument pkg is null");
            return false;
        }
        if (this.mBtActiveRecord.size() > 0) {
            synchronized (this.mBtActiveRecord) {
                if (isBluetoothEnable()) {
                    if (uid <= 0) {
                        uid = this.mIAppManager.getUidByPkg(pkg);
                    }
                    for (BtActiveRecord record : this.mBtActiveRecord) {
                        if (record.match(uid, pid)) {
                            Log.d("BluetoothState", "active bt app: " + pkg + ",uid:" + uid + ",pid:" + pid + " reason:" + record.mReason);
                            if ("BREDR_DISCOVERY".equals(record.mReason) || "BLE_SCAN".equals(record.mReason)) {
                            } else {
                                return true;
                            }
                        }
                    }
                }
                Log.i("BluetoothState", "Bluetooth is off and clear Active BT Records!");
                this.mBtActiveRecord.clear();
                return false;
            }
        } else if (isBluethoothConnected() && isBluetoothEnable() && this.mIAppManager.isBleApp(this.mICoreContext.getContext(), pkg)) {
            Log.d("BluetoothState", "check feature ble connected, uid: " + uid + " pkg: " + pkg);
            return true;
        }
        return false;
    }

    protected void processBtSockect(HookEvent event) {
        try {
            int uid = Integer.parseInt(event.getPkgName());
            int type = Integer.parseInt(event.getValue1());
            int port = Integer.parseInt(event.getValue2());
            int eventId = event.getEventId();
            if (172 == eventId) {
                synchronized (this.mBtActiveRecord) {
                    this.mBtActiveRecord.add(new BtActiveRecord(uid, -1, type, "sck_connect"));
                    updateAppBluetoothState(true, uid);
                }
                Log.i("BluetoothState", "new bluetooth sockect connected, uid: " + uid + ", type:" + type + ", port:" + port);
            } else if (173 == eventId) {
                ArrayList<BtActiveRecord> removedItems = new ArrayList();
                synchronized (this.mBtActiveRecord) {
                    for (BtActiveRecord item : this.mBtActiveRecord) {
                        if (item != null && item.mUid == uid && item.mType == type) {
                            removedItems.add(item);
                            updateAppBluetoothState(false, uid);
                            Log.i("BluetoothState", "remove bluetooth sockect connected, uid: " + uid + ", type:" + type + ", port:" + port);
                        }
                    }
                    this.mBtActiveRecord.removeAll(removedItems);
                }
            }
        } catch (Exception e) {
            Log.e("BluetoothState", "BluetoothConnected Error:", e);
        }
    }

    protected void initBtActiveApps(Intent data) {
        if (data == null) {
            Log.e("BluetoothState", "initBtActiveApps data is null");
            return;
        }
        Bundle bundle = data.getExtras();
        if (bundle != null) {
            HashMap allActiveApps = (HashMap) bundle.getSerializable("btFreezeProcess");
            if (allActiveApps == null) {
                Log.e("BluetoothState", "initBtActiveApps no any active apps");
                return;
            }
            for (Entry entry : allActiveApps.entrySet()) {
                String strUid = (String) entry.getKey();
                String strPid = (String) entry.getValue();
                Log.i("BluetoothState", "init bt active app uid: " + strUid + ",pid: " + strPid);
                try {
                    int uid = Integer.parseInt(strUid);
                    int pid = Integer.parseInt(strPid);
                    synchronized (this.mBtActiveRecord) {
                        this.mBtActiveRecord.add(new BtActiveRecord(uid, pid, -1, "unknown"));
                        updateAppBluetoothState(true, uid);
                    }
                } catch (Exception e) {
                    Log.e("BluetoothState", "initBtActiveApps Error:", e);
                    return;
                }
            }
        }
        Log.e("BluetoothState", "initBtActiveApps no bundle!");
    }

    protected void processActiveBtApp(HookEvent event) {
        String reason = event.getValue2();
        if ("BLUETOOTH_SOCKET".equals(reason)) {
            Log.d("BluetoothState", "not process ble socket from bt app.");
            return;
        }
        try {
            int uid = Integer.parseInt(event.getPkgName());
            int pid = Integer.parseInt(event.getValue1());
            int eventId = event.getEventId();
            if (181 == eventId) {
                synchronized (this.mBtActiveRecord) {
                    this.mBtActiveRecord.add(new BtActiveRecord(uid, pid, -1, reason));
                    updateAppBluetoothState(true, uid);
                }
                Log.i("BluetoothState", "new active bluetooth uid: " + uid + ", pid:" + pid + ", reason:" + reason);
            } else if (182 == eventId) {
                ArrayList<BtActiveRecord> removedItems = new ArrayList();
                synchronized (this.mBtActiveRecord) {
                    for (BtActiveRecord item : this.mBtActiveRecord) {
                        if (item != null && item.mPid == pid && reason != null && reason.equals(item.mReason)) {
                            removedItems.add(item);
                            updateAppBluetoothState(false, uid);
                            Log.i("BluetoothState", "inactive bluetooth uid: " + uid + ", pid:" + pid + ", reason:" + reason);
                        }
                    }
                    if (removedItems.size() > 0) {
                        this.mBtActiveRecord.removeAll(removedItems);
                    } else {
                        Log.d("BluetoothState", "not find active bluetooth for inactive event uid: " + uid + ", pid:" + pid + ", reason:" + reason);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("BluetoothState", "processBluetoothApp Error:", e);
        }
    }

    protected void updateBtState(int state) {
        switch (state) {
            case Integer.MIN_VALUE:
            case 10:
                this.mIsBtOn = false;
                this.mIsBluethoothConnected = false;
                this.mBluethoothDisconnectTime = SystemClock.elapsedRealtime();
                Log.i("BluetoothState", "bluetooth state off");
                synchronized (this.mBtActiveRecord) {
                    this.mBtActiveRecord.clear();
                }
                return;
            case 12:
                this.mIsBtOn = true;
                Log.i("BluetoothState", "bluetooth state on");
                return;
            default:
                return;
        }
    }

    protected boolean isBluethoothOn() {
        return this.mIsBtOn;
    }

    protected void updateBtConnected(boolean isConnected) {
        this.mIsBluethoothConnected = isConnected;
        this.mBluethoothDisconnectTime = isConnected ? 0 : SystemClock.elapsedRealtime();
        Log.i("BluetoothState", "bluetooth connected: " + isConnected);
    }

    protected boolean isBluethoothConnected() {
        return this.mIsBluethoothConnected;
    }

    protected long getBtDisConnectedTime() {
        if (this.mIsBluethoothConnected) {
            return 0;
        }
        return SystemClock.elapsedRealtime() - this.mBluethoothDisconnectTime;
    }
}
