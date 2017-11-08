package com.huawei.systemmanager.hsmstat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.huawei.systemmanager.SystemManagerApplication;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.IHsmStatService.Stub;
import com.huawei.systemmanager.hsmstat.base.ActivityStatEntry;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.StatEntry;
import com.huawei.systemmanager.service.MainService;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class HsmStatProxy implements IHsmStat {
    private static final long BIND_SERVICE_DELAY_TIME = 0;
    private static final long DELAY_STAT_CACHE_DATA = 2000;
    private static final int MAX_CACHE_DATA_NUM = 20;
    private static final int MSG_BIND_SERVICE = 2;
    private boolean mBindedFirstTime;
    private IHsmStatService mBinder;
    private ArrayList<StatEntry> mCacheData = new ArrayList(20);
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            HwLog.i(HsmStatConst.TAG, "service disconnect");
            HsmStatProxy.this.mBinder = null;
            HsmStatProxy.this.mHandler.sendEmptyMessage(2);
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            HwLog.i(HsmStatConst.TAG, "service connect");
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    HsmStatProxy.this.bindService();
                    return;
                default:
                    return;
            }
        }
    };
    private Runnable mSendCacheDataDelay = new Runnable() {
        public void run() {
            if (HsmStatProxy.this.ensureBinder()) {
                HsmStatProxy.this.statCachedData();
            }
        }
    };

    HsmStatProxy() {
        this.mHandler.sendEmptyMessageDelayed(2, 0);
    }

    public void activityStat(int action, String activityName, String params) {
        if (ensureBinder()) {
            statCachedData();
            statActivityActionInner(action, activityName, params);
            return;
        }
        pushCachData(new ActivityStatEntry(action, activityName, params));
    }

    public boolean eStat(String key, String value) {
        if (ensureBinder()) {
            statCachedData();
            return statEInner(key, value);
        }
        pushCachData(new StatEntry(key, value));
        return false;
    }

    public boolean rStat() {
        if (!ensureBinder()) {
            return false;
        }
        try {
            return this.mBinder.rStat();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public boolean isEnable() {
        if (!ensureBinder()) {
            return false;
        }
        try {
            return this.mBinder.isEnable();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public boolean setEnable(boolean enable) {
        if (!ensureBinder()) {
            return false;
        }
        try {
            return this.mBinder.setEnable(enable);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }

    private boolean statActivityActionInner(int action, String activityName, String params) {
        try {
            this.mBinder.activityStat(action, activityName, params);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            HwLog.e(HsmStatConst.TAG, "statActivityActionInner failed! action=" + action + ",activityName=" + activityName + ",params=" + params);
            return false;
        } catch (Exception e2) {
            e2.printStackTrace();
            HwLog.e(HsmStatConst.TAG, "statActivityActionInner failed! action=" + action + ",activityName=" + activityName + ",params=" + params);
            return false;
        }
    }

    private boolean statEInner(String key, String value) {
        boolean res = false;
        long identity = Binder.clearCallingIdentity();
        try {
            res = this.mBinder.eStat(key, value);
        } catch (RemoteException e) {
            HwLog.e(HsmStatConst.TAG, "statEInner failed! ,key=" + key + ",value=" + value);
            e.printStackTrace();
        } catch (Exception e2) {
            HwLog.e(HsmStatConst.TAG, "statEInner failed! ,key=" + key + ",value=" + value);
            e2.printStackTrace();
        }
        Binder.restoreCallingIdentity(identity);
        return res;
    }

    private void pushCachData(StatEntry entry) {
        if (this.mCacheData.size() >= 20) {
            HwLog.e(HsmStatConst.TAG, "the cache data number is too large, drop it!");
            this.mCacheData.clear();
        }
        this.mCacheData.add(entry);
        this.mHandler.removeCallbacks(this.mSendCacheDataDelay);
        this.mHandler.postDelayed(this.mSendCacheDataDelay, DELAY_STAT_CACHE_DATA);
    }

    private void statCachedData() {
        if (!this.mCacheData.isEmpty()) {
            for (StatEntry statEntry : this.mCacheData) {
                if (statEntry instanceof ActivityStatEntry) {
                    ActivityStatEntry acDcEntry = (ActivityStatEntry) statEntry;
                    statActivityActionInner(acDcEntry.acAction, acDcEntry.key, acDcEntry.value);
                } else {
                    statEInner(statEntry.key, statEntry.value);
                }
            }
            this.mCacheData.clear();
        }
    }

    private boolean ensureBinder() {
        if (!this.mBindedFirstTime) {
            return false;
        }
        if (this.mBinder != null) {
            return true;
        }
        IBinder binder = ServiceManager.getService(HsmStatBinder.NAME);
        if (binder == null) {
            HwLog.e(HsmStatConst.TAG, "get service failed!,binder = null");
            SystemManagerApplication.startMainService();
            return false;
        }
        this.mBinder = Stub.asInterface(binder);
        if (this.mBinder != null) {
            return true;
        }
        HwLog.e(HsmStatConst.TAG, "dc binder constructed failed");
        return false;
    }

    private boolean bindService() {
        this.mBindedFirstTime = true;
        Context ctx = GlobalContext.getContext();
        Intent intent = new Intent(ctx, MainService.class);
        intent.putExtra(HsmStatConst.KEY_PROCESS_ID, Process.myPid());
        intent.putExtra("binder_name", HsmStatBinder.NAME);
        if (ctx.bindService(intent, this.mConnection, 1)) {
            return true;
        }
        HwLog.i(HsmStatConst.TAG, "bind service failed!!");
        return false;
    }
}
