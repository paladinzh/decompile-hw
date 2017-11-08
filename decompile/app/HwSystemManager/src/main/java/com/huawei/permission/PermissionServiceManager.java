package com.huawei.permission;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import com.huawei.hsm.permission.StubController;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.util.HwLog;

public class PermissionServiceManager {
    private static final String EXTRA_MONITOR = "shouldMonitor";
    private static final String EXTRA_PACKAGE = "packageName";
    private static final String METHOD_SHOUD_MONITOR = "checkShoudMonitor";
    private static final int MONITOR = 1;
    private static final String TAG = "PermissionServiceManager";
    private static PermissionServiceManager sInstance = null;
    private DeathRecipient mDeathRecepient = null;
    private IHoldService mService = null;

    private class MyDeathRecipient implements DeathRecipient {
        private MyDeathRecipient() {
        }

        public void binderDied() {
            try {
                PermissionServiceManager.this.mService.asBinder().unlinkToDeath(PermissionServiceManager.this.mDeathRecepient, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            PermissionServiceManager.this.mDeathRecepient = null;
            PermissionServiceManager.this.mService = null;
        }
    }

    private PermissionServiceManager() {
        initHoldService();
        if (this.mService == null) {
            HwLog.w(TAG, "Hold service is not avalaible.");
        }
    }

    public static synchronized PermissionServiceManager getInstance() {
        PermissionServiceManager permissionServiceManager;
        synchronized (PermissionServiceManager.class) {
            if (sInstance == null) {
                sInstance = new PermissionServiceManager();
            }
            permissionServiceManager = sInstance;
        }
        return permissionServiceManager;
    }

    public boolean shouldMonitor(Context context, String pkgName) {
        boolean z = true;
        initHoldService();
        if (this.mService == null) {
            return GRuleManager.getInstance().shouldMonitor(context, MonitorScenario.SCENARIO_PERMISSION, pkgName);
        }
        Bundle params = new Bundle();
        params.putString("packageName", pkgName);
        try {
            Bundle result = this.mService.callHsmService(METHOD_SHOUD_MONITOR, params);
            if (result == null) {
                HwLog.i(TAG, "shouldMonitor: Get an invalid result");
                return false;
            }
            if (result.getInt(EXTRA_MONITOR) != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }

    private void initHoldService() {
        if (this.mService == null) {
            try {
                this.mService = StubController.getHoldService();
                HwLog.i(TAG, "Get hold service:" + this.mService);
                if (this.mService == null) {
                    HwLog.w(TAG, "Hold service not avalaible.");
                } else {
                    this.mDeathRecepient = new MyDeathRecipient();
                    this.mService.asBinder().linkToDeath(this.mDeathRecepient, 0);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}
