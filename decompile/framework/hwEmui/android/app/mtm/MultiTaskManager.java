package android.app.mtm;

import android.app.mtm.IMultiTaskManagerService.Stub;
import android.app.mtm.iaware.RSceneData;
import android.os.Bundle;
import android.os.FreezeScreenScene;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.rms.iaware.RPolicyData;
import android.util.Log;
import android.util.Slog;
import com.huawei.hsm.permission.StubController;

public class MultiTaskManager {
    static final boolean DEBUG = false;
    static final String TAG = "MultiTaskManager";
    private static MultiTaskManager instance = null;
    static final Object mLock = new Object[0];
    private IMultiTaskManagerService mService;

    private MultiTaskManager() {
        IMultiTaskManagerService service = getService();
        if (service == null) {
            Slog.e(TAG, "multi task service is null in constructor");
        }
        this.mService = service;
    }

    public static MultiTaskManager getInstance() {
        synchronized (mLock) {
            if (SystemProperties.getBoolean("persist.sys.enable_iaware", false)) {
                if (instance == null) {
                    if (Log.HWINFO) {
                        Slog.i(TAG, "first time to initialize MultiTaskManager, this log should not appear again!");
                    }
                    instance = new MultiTaskManager();
                    if (instance.mService == null) {
                        instance = null;
                    }
                }
                MultiTaskManager multiTaskManager = instance;
                return multiTaskManager;
            }
            Slog.e(TAG, "multitask service is not running because prop is false, so getInstance return null");
            return null;
        }
    }

    public MultiTaskPolicy getMultiTaskPolicy(int resourcetype, String resourceextend, int resourcestatus, Bundle args) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.getMultiTaskPolicy(resourcetype, resourceextend, resourcestatus, args);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
        return null;
    }

    public void notifyResourceStatusOverload(int resourcetype, String resourceextend, int resourcestatus, Bundle args) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                this.mService.notifyResourceStatusOverload(resourcetype, resourceextend, resourcestatus, args);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public int getMultiTaskProcessGroup(int schedGroup, int pid, int uid, int clientpid, int clientuid, String adjType) {
        String TAG_POLICYVALUE = "policyvalue23";
        Bundle args = new Bundle();
        args.putInt(FreezeScreenScene.PID_PARAM, pid);
        args.putInt(StubController.TABLE_COLUM_UID, uid);
        args.putInt("clientpid", clientpid);
        args.putInt("clientuid", clientuid);
        MultiTaskPolicy mpolicy = getMultiTaskPolicy(23, adjType, schedGroup, args);
        if (mpolicy == null) {
            Slog.e(TAG, "get null policy in getMultiTaskProcessGroup");
            return schedGroup;
        } else if (mpolicy.getPolicy() == 512) {
            return mpolicy.getPolicyData().getInt(TAG_POLICYVALUE, schedGroup);
        } else {
            return schedGroup;
        }
    }

    public void registerObserver(IMultiTaskProcessObserver observer) {
        try {
            if (this.mService != null) {
                this.mService.registerObserver(observer);
                return;
            }
            this.mService = getService();
            if (this.mService != null) {
                this.mService.registerObserver(observer);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public void unregisterObserver(IMultiTaskProcessObserver observer) {
        try {
            if (this.mService != null) {
                this.mService.unregisterObserver(observer);
                return;
            }
            this.mService = getService();
            if (this.mService != null) {
                this.mService.unregisterObserver(observer);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public void notifyProcessGroupChange(int pid, int uid) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                this.mService.notifyProcessGroupChange(pid, uid);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public boolean killProcess(int pid, boolean restartservice) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.killProcess(pid, restartservice);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
        return false;
    }

    public boolean forcestopApps(int pid) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.forcestopApps(pid);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
        return false;
    }

    public boolean reportScene(int featureId, RSceneData scene) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.reportScene(featureId, scene);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "reportScene can not connect to MultiTaskManagerService");
        }
        return false;
    }

    public RPolicyData acquirePolicyData(int featureId, RSceneData scene) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.acquirePolicyData(featureId, scene);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "acquirePolicyData can not connect to MultiTaskManagerService");
        }
        return null;
    }

    private IMultiTaskManagerService getService() {
        return Stub.asInterface(ServiceManager.getService("multi_task"));
    }
}
