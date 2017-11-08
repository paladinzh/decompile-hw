package com.android.server.pg;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.WorkSource;
import android.util.Log;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.am.ActivityManagerService;
import com.android.server.power.PowerManagerService;
import com.huawei.pgmng.api.IPGManager.Stub;
import java.util.ArrayList;
import java.util.List;

public class PGManagerService extends Stub {
    public static final int CODE_CLOSE_SOCKETS_FOR_UID = 1107;
    public static final String DESCRIPTOR_NETWORKMANAGEMENT_SERVICE = "android.os.INetworkManagementService";
    private static final String TAG = "PGManagerService";
    private static final Object mLock = new Object();
    private static PGManagerService sInstance = null;
    private ActivityManagerService mAM;
    private final Context mContext;
    private LocationManagerService mLMS;
    private PowerManagerService mPms;
    private ProcBatteryStats mProcStats = null;
    private boolean mSystemReady;

    class LocalService extends PGManagerInternal {
        LocalService() {
        }

        public void noteStartWakeLock(String tag, WorkSource ws, String pkgName, int uid) {
            PGManagerService.this.mProcStats.processWakeLock(160, tag, ws, pkgName, uid);
        }

        public void noteStopWakeLock(String tag, WorkSource ws, String pkgName, int uid) {
            PGManagerService.this.mProcStats.processWakeLock(161, tag, ws, pkgName, uid);
        }

        public void noteChangeWakeLock(String tag, WorkSource ws, String pkgName, int uid, String newTag, WorkSource newWs, String newPkgName, int newUid) {
            PGManagerService.this.mProcStats.processWakeLock(160, newTag, newWs, newPkgName, newUid);
            PGManagerService.this.mProcStats.processWakeLock(161, tag, ws, pkgName, uid);
        }
    }

    public PGManagerService(Context context) {
        this.mContext = context;
        this.mProcStats = new ProcBatteryStats(this.mContext);
        LocalServices.addService(PGManagerInternal.class, new LocalService());
    }

    public static PGManagerService getInstance(Context context) {
        PGManagerService pGManagerService;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new PGManagerService(context);
                ServiceManager.addService("pgservice", sInstance);
            }
            pGManagerService = sInstance;
        }
        return pGManagerService;
    }

    public void systemReady(ActivityManagerService activityManagerService, PowerManagerService powerManagerService, LocationManagerService location) {
        synchronized (mLock) {
            this.mAM = activityManagerService;
            this.mPms = powerManagerService;
            this.mLMS = location;
            this.mSystemReady = true;
            this.mProcStats.onSystemReady();
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (this.mProcStats.onTransact(code, data, reply, flags)) {
            return true;
        }
        return super.onTransact(code, data, reply, flags);
    }

    public long proxyBroadcast(List<String> pkgs, boolean proxy) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for proxyBroadcast:" + pkgs + " proxy:" + proxy);
            return -1;
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "proxy broadcast permission not allowed. uid = " + Binder.getCallingUid());
            return -1;
        } else {
            Log.i(TAG, "proxyBroadcast:" + pkgs + " proxy:" + proxy);
            return this.mAM.proxyBroadcast(pkgs, proxy);
        }
    }

    public long proxyBroadcastByPid(List<String> pids, boolean proxy) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for proxyBroadcastByPid:" + pids + " proxy:" + proxy);
            return -1;
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "proxy broadcast permission not allowed. uid = " + Binder.getCallingUid());
            return -1;
        } else {
            Log.i(TAG, "proxyBroadcastByPid:" + pids + " proxy:" + proxy);
            List ipids = new ArrayList();
            if (pids != null) {
                for (String pid : pids) {
                    ipids.add(Integer.valueOf(Integer.parseInt(pid)));
                }
            } else {
                ipids = null;
            }
            return this.mAM.proxyBroadcastByPid(ipids, proxy);
        }
    }

    public void setProxyBCActions(List<String> actions) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for setProxyBCActions:" + actions);
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "setProxyBCActions permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            Log.i(TAG, "proxy BC Actions:" + actions);
            this.mAM.setProxyBCActions(actions);
        }
    }

    public void setActionExcludePkg(String action, String pkg) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for setActionExcludePkg action:" + action + " pkg:" + pkg);
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "setActionExcludePkg permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            Log.i(TAG, "set action:" + action + " pkg:" + pkg);
            this.mAM.setActionExcludePkg(action, pkg);
        }
    }

    public void proxyBCConfig(int type, String key, List<String> value) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for proxyBCConfig type:" + type + " key:" + key + " value:" + value);
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "proxyBCConfig permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            Log.i(TAG, "proxy config:" + type + " ," + key + " ," + value);
            this.mAM.proxyBCConfig(type, key, value);
        }
    }

    public void proxyWakeLockByPidUid(int pid, int uid, boolean proxy) {
        Log.i(TAG, "proxyWakeLockByPidUid, pid: " + pid + ", uid: " + uid + ", proxy: " + proxy);
        if (1000 == Binder.getCallingUid() && this.mSystemReady) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mPms.proxyWakeLockByPidUid(pid, uid, proxy);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            Log.w(TAG, "proxyWakeLockByPidUid, system not ready!");
        }
    }

    public void forceReleaseWakeLockByPidUid(int pid, int uid) {
        Log.i(TAG, "forceReleaseWakeLockByPidUid, pid: " + pid + ", uid: " + uid);
        if (1000 == Binder.getCallingUid() && this.mSystemReady) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mPms.forceReleaseWakeLockByPidUid(pid, uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            Log.w(TAG, "forceReleaseWakeLockByPidUid, system not ready!");
        }
    }

    public void forceRestoreWakeLockByPidUid(int pid, int uid) {
        Log.i(TAG, "forceRestoreWakeLockByPidUid, pid: " + pid + ", uid: " + uid);
        if (1000 == Binder.getCallingUid() && this.mSystemReady) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mPms.forceRestoreWakeLockByPidUid(pid, uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            Log.w(TAG, "forceRestoreWakeLockByPidUid, system not ready!");
        }
    }

    public boolean getWakeLockByUid(int uid, int wakeflag) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for getWakeLockByUid ");
            return false;
        } else if (1000 == Binder.getCallingUid()) {
            return this.mPms.getWakeLockByUid(uid, wakeflag);
        } else {
            Log.e(TAG, "getWakeLockByUid permission not allowed. uid = " + Binder.getCallingUid());
            return false;
        }
    }

    public void setLcdRatio(int ratio, boolean autoAdjust) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for setLcdRatio");
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "setLcdRatio permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            this.mPms.setLcdRatio(ratio, autoAdjust);
        }
    }

    public boolean proxyApp(String pkg, int uid, boolean proxy) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for proxyApp");
            return false;
        } else if (1000 == Binder.getCallingUid()) {
            return this.mLMS.proxyGps(pkg, uid, proxy);
        } else {
            Log.e(TAG, "proxyApp permission not allowed. uid = " + Binder.getCallingUid());
            return false;
        }
    }

    public void configBrightnessRange(int ratioMin, int ratioMax, int autoLimit) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for configBrightnessRange");
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "configBrightnessRange permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            this.mPms.configBrightnessRange(ratioMin, ratioMax, autoLimit);
        }
    }

    public boolean closeSocketsForUid(int uid) {
        boolean ret = false;
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for close socket");
            return false;
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "close socket permission not allowed. uid = " + Binder.getCallingUid());
            return false;
        } else {
            IBinder b = ServiceManager.getService("network_management");
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            if (b != null) {
                try {
                    _data.writeInterfaceToken(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                    _data.writeInt(uid);
                    b.transact(CODE_CLOSE_SOCKETS_FOR_UID, _data, _reply, 0);
                    _reply.readException();
                    ret = true;
                } catch (RemoteException localRemoteException) {
                    Log.e(TAG, "close socket error", localRemoteException);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
            _reply.recycle();
            _data.recycle();
            return ret;
        }
    }

    public void killProc(int pid) {
        if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "killProc permission not allowed. uid = " + Binder.getCallingUid());
            return;
        }
        Log.i(TAG, "killProc pid=" + pid);
        Process.killProcessQuiet(pid);
    }
}
