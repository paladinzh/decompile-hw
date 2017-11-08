package com.huawei.systemmanager.netassistant.task;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IProcessObserver;
import android.app.IProcessObserver.Stub;
import android.content.ComponentName;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.HwNetworkPolicyManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import com.huawei.systemmanager.Task;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.NetAppConst;
import com.huawei.systemmanager.netassistant.NetAssistantManager;
import com.huawei.systemmanager.util.HwLog;

public class ForegroundAppChangeMonitor extends Task {
    public static final String TAG = "NetControllService";
    private IProcessObserver mProcessObserver = new Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (foregroundActivities) {
                ForegroundAppChangeMonitor.this.mHandler.obtainMessage(uid, pid, pid).sendToTarget();
            }
        }

        public void onProcessDied(int pid, int uid) {
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
        }
    };

    public String getName() {
        return "NetControllService";
    }

    public void registerListener() {
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                am.registerProcessObserver(this.mProcessObserver);
            }
        } catch (RemoteException e) {
            HwLog.e("NetControllService", "registerProcessObserver RemoteException!");
        }
    }

    public void unRegisterListener() {
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                am.unregisterProcessObserver(this.mProcessObserver);
            }
        } catch (RemoteException e) {
            HwLog.e("NetControllService", "unregisterProcessObserver RemoteException!");
        }
    }

    public void onHandleMessage(Message msg) {
        int uid = msg.what;
        int pid = msg.arg1;
        if (UserHandle.getUserId(uid) == 0 && GlobalContext.getContext().getSharedPreferences("note_preferences", 4).getString(String.valueOf(uid), "").isEmpty()) {
            checkShowNetworkToast(uid, pid);
        }
    }

    private void checkShowNetworkToast(int uid, int pid) {
        if (HwNetworkPolicyManager.from(GlobalContext.getContext()).getHwUidPolicy(uid) != 0) {
            switch (getNetworkType()) {
                case 0:
                    if (!NetAssistantManager.isNetworkAccessInState(uid, 1)) {
                        showDialog(uid, pid, 1);
                        break;
                    }
                    break;
                case 1:
                    if (!NetAssistantManager.isNetworkAccessInState(uid, 2)) {
                        showDialog(uid, pid, 2);
                        break;
                    }
                    break;
            }
        }
    }

    private void showDialog(int uid, int pid, int networkType) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.netassistant.netapp.service.NetControllService"));
        intent.putExtra("uid", uid);
        intent.putExtra(NetAppConst.EXT_PID, pid);
        intent.putExtra(NetAppConst.EXT_NETWORKTYPE, networkType);
        GlobalContext.getContext().startService(intent);
    }

    private int getNetworkType() {
        NetworkInfo activeNetInfo = ((ConnectivityManager) GlobalContext.getContext().getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetInfo != null) {
            if (activeNetInfo.getType() == 0) {
                return 0;
            }
            if (activeNetInfo.getType() == 1) {
                return 1;
            }
        }
        return -1;
    }
}
