package com.android.contacts.hap.rcs;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.SparseArray;
import com.android.contacts.util.HwLog;
import com.huawei.rcs.commonInterface.IfMsgplus;
import com.huawei.rcs.commonInterface.IfMsgplus.Stub;
import com.huawei.rcs.commonInterface.IfMsgplusCb;

public class RcseProfile {
    private static Context mContext = null;
    private static boolean mExistingState = false;
    private static final Object mMainHandlerLock = new Object();
    private static SparseArray<IfMsgplusCb> mRcsCallbackList = new SparseArray();
    private static IfMsgplus mRcsService = null;
    private static Handler mainHandler;
    private static ServiceConnection mrcsServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName aClassName, IBinder aService) {
            try {
                RcseProfile.mRcsService = Stub.asInterface(aService);
                for (int i = 0; i < RcseProfile.mRcsCallbackList.size(); i++) {
                    RcseProfile.mRcsService.registerCallback(RcseProfile.mRcsCallbackList.keyAt(i), (IfMsgplusCb) RcseProfile.mRcsCallbackList.valueAt(i));
                }
            } catch (RemoteException e) {
                HwLog.e("RcseProfile", "onServiceConnected faild, RemoteException: " + e);
            }
        }

        public void onServiceDisconnected(ComponentName aClassName) {
            RcseProfile.mRcsService = null;
            if (!RcseProfile.mExistingState) {
                RcseProfile.bindservice();
            }
        }
    };
    private static Runnable runnable = new Runnable() {
        public void run() {
            if (RcseProfile.mRcsService == null) {
                Intent bindAction = new Intent();
                bindAction.setPackage("com.huawei.rcsserviceapplication");
                bindAction.setClassName("com.huawei.rcsserviceapplication", "com.huawei.rcs.service.RcsService");
                bindAction.setAction("com.huawei.msgplus.IfMsgplus");
                bindAction.setType("vnd.android.cursor.item/rcs");
                RcseProfile.mContext.bindService(bindAction, RcseProfile.mrcsServiceConnection, 1);
            }
        }
    };

    private static void bindservice() {
        if (mContext != null && mRcsService == null) {
            synchronized (mMainHandlerLock) {
                if (mainHandler == null) {
                    mainHandler = new Handler(mContext.getMainLooper());
                }
                mainHandler.post(runnable);
            }
        }
    }

    public static void init(Context context) {
        mContext = context;
        synchronized (mMainHandlerLock) {
            if (mainHandler == null) {
                mainHandler = new Handler(mContext.getMainLooper());
            }
        }
        if (mRcsService == null) {
            bindservice();
        }
    }

    public static void deInit() {
        if (mContext != null) {
            mExistingState = true;
            mContext.unbindService(mrcsServiceConnection);
        }
    }

    public static IfMsgplus getRcsService() {
        if (mRcsService == null) {
            bindservice();
        }
        return mRcsService;
    }

    public static void registerRcsCallBack(Integer eventId, IfMsgplusCb rcsCallback) {
        HwLog.i("RcseProfile", "registerRcsCallBack");
        if (mRcsService == null) {
            mRcsCallbackList.put(eventId.intValue(), rcsCallback);
            return;
        }
        try {
            mRcsService.registerCallback(eventId.intValue(), rcsCallback);
            mRcsCallbackList.put(eventId.intValue(), rcsCallback);
        } catch (RemoteException e) {
            HwLog.e("RcseProfile", e.toString());
        }
    }
}
