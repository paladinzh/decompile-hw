package com.android.server.security.IFAA;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.core.IHwSecurityPlugin.Creator;
import huawei.android.security.IIFAAPlugin.Stub;
import huawei.android.security.IIFAAPluginCallBack;

public class IFAAPlugin extends Stub implements IHwSecurityPlugin {
    public static final Object BINDLOCK = new Object();
    public static final Creator CREATOR = new Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            if (IFAAPlugin.HW_DEBUG) {
                Slog.d(IFAAPlugin.TAG, "createPlugin");
            }
            return new IFAAPlugin(context);
        }

        public String getPluginPermission() {
            return IFAAPlugin.USE_IFAA_MANAGER;
        }
    };
    private static final int FAILED = 1;
    private static final boolean HW_DEBUG;
    private static final int PROCESS_CMD_TYPE = 1;
    private static final int SUCCESS = 0;
    private static final String TAG = "IFAAPlugin";
    private static final String USE_IFAA_MANAGER = "cn.org.ifaa.permission.USE_IFAA_MANAGER";
    private Context mContext;
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private static class MessageData {
        IBinder mCallBack;
        byte[] mData;

        MessageData(byte[] data, IBinder callBack) {
            this.mData = data;
            this.mCallBack = callBack;
        }
    }

    public native byte[] nativeProcessCmd(Context context, byte[] bArr);

    public native int nativeStart();

    public native void nativeStop();

    static {
        boolean z;
        if (Log.HWINFO) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(TAG, 4);
        } else {
            z = false;
        }
        HW_DEBUG = z;
        try {
            System.loadLibrary("ifaajni_ca");
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "LoadLibrary is error " + e.toString());
        }
    }

    public void processCmd(IIFAAPluginCallBack callBack, byte[] data) {
        if (HW_DEBUG) {
            Slog.d(TAG, "processCmd in IFAAPlugin");
        }
        checkPermission(USE_IFAA_MANAGER);
        if (callBack == null || data == null) {
            Slog.e(TAG, "parameters is error");
            return;
        }
        MessageData messageData = new MessageData(data, callBack.asBinder());
        Message msg = Message.obtain();
        msg.what = 1;
        msg.obj = messageData;
        this.mHandler.sendMessage(msg);
    }

    public IFAAPlugin(Context context) {
        this.mContext = context;
    }

    public IBinder asBinder() {
        return this;
    }

    public void onStart() {
        if (HW_DEBUG) {
            Slog.d(TAG, "is start");
        }
        try {
            if (HW_DEBUG) {
                Slog.d(TAG, "close session");
            }
            nativeStart();
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "start error" + e.toString());
        }
        this.mHandlerThread = new HandlerThread("IFAAPluginThread");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        IFAAPlugin.this.handleProcessCmd(msg);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private void handleProcessCmd(Message msg) {
        byte[] retData;
        if (HW_DEBUG) {
            Slog.d(TAG, "handleProcessCmd in IFAAPlugin ");
        }
        MessageData messageData = msg.obj;
        IBinder callBack = messageData.mCallBack;
        byte[] data = messageData.mData;
        if (HW_DEBUG) {
            Slog.d(TAG, "call JNI  processCmd ");
        }
        try {
            retData = nativeProcessCmd(this.mContext, data);
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, e.toString());
            retData = null;
        }
        IIFAAPluginCallBack mCallBackService = IIFAAPluginCallBack.Stub.asInterface(callBack);
        if (mCallBackService == null) {
            Slog.e(TAG, "callback is invalid!!!");
        } else if (retData != null) {
            try {
                if (HW_DEBUG) {
                    Slog.d(TAG, "call processCmdResult in IIFAAPluginCallBack ");
                }
                mCallBackService.processCmdResult(0, retData);
            } catch (RemoteException e2) {
                Slog.e(TAG, "Error in send processCmdResult to callback.");
            }
        } else {
            if (HW_DEBUG) {
                Slog.d(TAG, "retData is null ");
            }
            mCallBackService.processCmdResult(1, new byte[0]);
        }
    }

    public void onStop() {
        if (this.mHandler != null) {
            this.mHandler = null;
        }
        if (this.mHandlerThread != null) {
            this.mHandlerThread.quitSafely();
            this.mHandlerThread = null;
        }
        try {
            Slog.d(TAG, "close session");
            nativeStop();
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "stop error" + e.toString());
        }
    }

    private void checkPermission(String permission) {
        this.mContext.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }
}
