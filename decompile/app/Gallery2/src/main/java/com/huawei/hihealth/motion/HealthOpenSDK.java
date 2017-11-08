package com.huawei.hihealth.motion;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.health.IDaemonRemoteManager;
import com.huawei.health.IResultCallback;
import com.huawei.health.IStepDataReport;
import com.huawei.health.ITrackDataReport;
import com.huawei.health.ITrackSportManager;
import com.huawei.health.ITrackSportManager.Stub;
import java.util.ArrayList;
import java.util.List;

public class HealthOpenSDK extends HealthOpenSDKCommon {
    private static final String TAG = HealthOpenSDK.class.getSimpleName();
    private IExecuteResult mCallback = null;
    private String mClientName = null;
    private MyConn mConn = new MyConn();
    private Context mContext = null;
    private IDaemonRemoteManager mDaemonRemoteManager = null;
    private WorkerHandler mHandler = null;
    private ITrackSportManager mITrackSportManager = null;
    private List<StepLocalToRemoteProxy> mStepReportList = new ArrayList();
    private List<TrackLocalToRemoteProxy> mTrackReportList = new ArrayList();
    private HandlerThread mWorkingThread = null;
    ServiceConnection myTrackConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(HealthOpenSDK.TAG, "name : " + name + " service " + service);
            HealthOpenSDK.this.mITrackSportManager = Stub.asInterface(service);
            if (HealthOpenSDK.this.mITrackSportManager != null && HealthOpenSDK.this.mCallback != null) {
                HealthOpenSDK.this.mCallback.onSuccess(null);
                Log.d(HealthOpenSDK.TAG, "Bind Success " + System.currentTimeMillis());
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(HealthOpenSDK.TAG, "onServiceDisconnected " + name);
            HealthOpenSDK.this.mCallback.onServiceException(null);
            HealthOpenSDK.this.mITrackSportManager = null;
        }
    };

    private class ExecuteResultLocalToRemote extends IResultCallback.Stub {
        private IExecuteResult cb;

        public ExecuteResultLocalToRemote(IExecuteResult cb) {
            this.cb = cb;
        }

        public void onSuccess(Bundle bundle) throws RemoteException {
            Log.d(HealthOpenSDK.TAG, "onSuccess " + bundle);
            if (this.cb != null) {
                this.cb.onSuccess(bundle);
            }
        }

        public void onFailed(Bundle bundle) throws RemoteException {
            if (this.cb != null) {
                this.cb.onFailed(bundle);
            }
        }

        public void onServiceException(Bundle bundle) throws RemoteException {
            if (this.cb != null) {
                this.cb.onServiceException(bundle);
            }
        }
    }

    private class MyConn implements ServiceConnection {
        private MyConn() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(HealthOpenSDK.TAG, "name : " + name + " service " + service);
            HealthOpenSDK.this.mDaemonRemoteManager = IDaemonRemoteManager.Stub.asInterface(service);
            if (HealthOpenSDK.this.mDaemonRemoteManager != null && HealthOpenSDK.this.mCallback != null) {
                HealthOpenSDK.this.mCallback.onSuccess(null);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(HealthOpenSDK.TAG, "onServiceDisconnected " + name);
            HealthOpenSDK.this.mDaemonRemoteManager = null;
        }
    }

    private class StepLocalToRemoteProxy extends IStepDataReport.Stub {
        private IExecuteResult executeCb;
        private ICommonReport reportCb;

        public void reportExecuteResult(boolean success) {
            if (this.executeCb != null) {
                if (success) {
                    this.executeCb.onSuccess(null);
                } else {
                    this.executeCb.onFailed(null);
                }
            }
        }

        public void report(Bundle bundle) throws RemoteException {
            Log.d(HealthOpenSDK.TAG, "report " + bundle);
            if (this.reportCb != null) {
                this.reportCb.report(bundle);
            }
        }
    }

    private class TrackLocalToRemoteProxy extends ITrackDataReport.Stub {
        private IExecuteResult executeCb;
        private ICommonReport reportCb;

        public void reportExecuteResult(boolean success) {
            if (this.executeCb != null) {
                if (success) {
                    this.executeCb.onSuccess(null);
                } else {
                    this.executeCb.onFailed(null);
                }
            }
        }

        public void report(Bundle sportInfo) throws RemoteException {
            Log.d(HealthOpenSDK.TAG, "report " + sportInfo);
            if (this.reportCb != null) {
                this.reportCb.report(sportInfo);
            }
        }
    }

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg != null) {
                boolean bindRet;
                switch (msg.what) {
                    case 100:
                        IExecuteResult cb = msg.obj;
                        bindRet = HealthOpenSDK.this.bindStepService();
                        if (cb == null) {
                            return;
                        }
                        if (bindRet) {
                            cb.onSuccess(null);
                            return;
                        } else {
                            cb.onFailed(null);
                            return;
                        }
                    case 101:
                        bindRet = true;
                        StepLocalToRemoteProxy tmp = msg.obj;
                        if (HealthOpenSDK.this.mDaemonRemoteManager == null) {
                            bindRet = HealthOpenSDK.this.bindStepService();
                        }
                        if (bindRet) {
                            HealthOpenSDK.this.tryToRegister(10, tmp);
                            return;
                        } else if (tmp != null) {
                            tmp.reportExecuteResult(false);
                            return;
                        } else {
                            return;
                        }
                    case 102:
                        bindRet = true;
                        TrackLocalToRemoteProxy tmp2 = msg.obj;
                        if (HealthOpenSDK.this.mITrackSportManager == null) {
                            bindRet = HealthOpenSDK.this.bindTrackService();
                        }
                        if (bindRet) {
                            HealthOpenSDK.this.tryToRegisterTrack(100, tmp2);
                            return;
                        } else if (tmp2 != null) {
                            tmp2.reportExecuteResult(false);
                            return;
                        } else {
                            return;
                        }
                    default:
                        return;
                }
            }
        }
    }

    public int initSDK(Context context, IExecuteResult cb, String name) {
        Log.d(TAG, "initSDK : " + name);
        if (context == null || cb == null) {
            return -2;
        }
        if (this.mWorkingThread != null) {
            return -3;
        }
        this.mContext = context;
        this.mCallback = cb;
        this.mWorkingThread = new HandlerThread("health_sdk");
        this.mWorkingThread.start();
        this.mHandler = new WorkerHandler(this.mWorkingThread.getLooper());
        Message msg = this.mHandler.obtainMessage(100);
        msg.obj = cb;
        this.mHandler.sendMessage(msg);
        return 0;
    }

    public void destorySDK() {
        Log.d(TAG, "destorySDK " + this.mClientName);
        if (this.mHandler != null) {
            this.mHandler.removeMessages(101);
            this.mHandler.removeMessages(102);
            this.mHandler = null;
        }
        if (this.mWorkingThread != null) {
            this.mWorkingThread.quit();
            this.mWorkingThread = null;
        }
    }

    private boolean registerTrackCallbackInter(TrackLocalToRemoteProxy report) {
        Log.d(TAG, "registerTrackingReportInter:" + report + " at:" + System.currentTimeMillis());
        if (this.mITrackSportManager == null || report == null) {
            return false;
        }
        try {
            this.mITrackSportManager.registerDataCallback(report);
            this.mTrackReportList.add(report);
            return true;
        } catch (RemoteException e) {
            Log.w(TAG, e.getMessage());
            return false;
        }
    }

    private boolean registerStepCallbackInter(StepLocalToRemoteProxy report) {
        if (this.mDaemonRemoteManager == null || report == null) {
            return false;
        }
        try {
            this.mDaemonRemoteManager.registerStepReportCallback(report);
            this.mStepReportList.add(report);
            return true;
        } catch (RemoteException e) {
            Log.w(TAG, e.getMessage());
            return false;
        }
    }

    public boolean getTodaySportData(IExecuteResult callback) {
        boolean ret = false;
        if (this.mDaemonRemoteManager != null) {
            try {
                this.mDaemonRemoteManager.getTodaySportData(new ExecuteResultLocalToRemote(callback));
                ret = true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "getTodaySportData " + ret);
        return ret;
    }

    private boolean bindStepService() {
        Intent intent = new Intent();
        intent.setPackage("com.huawei.health");
        intent.setClassName("com.huawei.health", "com.huawei.health.manager.DaemonService");
        boolean ret = this.mContext.bindService(intent, this.mConn, 1);
        Log.d(TAG, " ret " + ret);
        return ret;
    }

    private boolean bindTrackService() {
        Intent intent = new Intent();
        intent.setAction("com.huawei.healthcloud.plugintrack.trackSdk.TrackService");
        intent.setPackage("com.huawei.health");
        boolean ret = this.mContext.bindService(intent, this.myTrackConnection, 0);
        Log.d(TAG, "Bind Track Service at " + System.currentTimeMillis());
        return ret;
    }

    private void tryToRegister(int tryTimes, StepLocalToRemoteProxy report) {
        for (int i = 0; i < tryTimes; i++) {
            if (registerStepCallbackInter(report)) {
                if (report != null) {
                    report.reportExecuteResult(true);
                }
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.w(TAG, e.getMessage());
            }
        }
        if (report != null) {
            report.reportExecuteResult(false);
        }
    }

    private void tryToRegisterTrack(int tryTimes, TrackLocalToRemoteProxy report) {
        for (int i = 0; i < tryTimes; i++) {
            if (registerTrackCallbackInter(report)) {
                if (report != null) {
                    report.reportExecuteResult(true);
                }
                return;
            }
            try {
                Log.d(TAG, "Try register sleep:" + System.currentTimeMillis());
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Log.w(TAG, e.getMessage());
            }
        }
        if (report != null) {
            report.reportExecuteResult(false);
        }
    }
}
