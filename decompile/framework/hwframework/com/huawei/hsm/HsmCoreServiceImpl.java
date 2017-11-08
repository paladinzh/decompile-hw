package com.huawei.hsm;

import android.content.Context;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import com.huawei.hsm.IHsmCoreService.Stub;
import com.huawei.hsm.transacthandler.AbsTransactHandler;
import com.huawei.hsm.transacthandler.MediaProcessHandler;
import com.huawei.lcagent.client.MetricConstant;

public final class HsmCoreServiceImpl extends Stub {
    private static final String TAG = "HsmCoreServiceImpl";
    private static SparseArray<AbsTransactHandler> sHandlerMap = new SparseArray();

    static {
        MediaProcessHandler mediaHandler = new MediaProcessHandler();
        sHandlerMap.put(102, mediaHandler);
        sHandlerMap.put(103, mediaHandler);
        sHandlerMap.put(MetricConstant.CAMERA_METRIC_ID_EX, mediaHandler);
    }

    public HsmCoreServiceImpl(Context context) {
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        Log.e(TAG, "onTransact in code is: " + code);
        switch (code) {
            case 102:
            case 103:
            case MetricConstant.CAMERA_METRIC_ID_EX /*104*/:
                AbsTransactHandler itf = getHandler(code);
                if (itf != null) {
                    itf.handleTransactCode(code, data, reply);
                    return true;
                }
                Log.w(TAG, "onTransact can't get valid handler by code: " + code);
                return super.onTransact(code, data, reply, flags);
            default:
                Log.d(TAG, "onTransact default call super!");
                return super.onTransact(code, data, reply, flags);
        }
    }

    private AbsTransactHandler getHandler(int code) {
        return (AbsTransactHandler) sHandlerMap.get(code);
    }
}
