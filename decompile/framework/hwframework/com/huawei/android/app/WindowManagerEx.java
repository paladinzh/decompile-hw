package com.huawei.android.app;

import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;

public class WindowManagerEx {
    private static final int IS_INPUT_METHOD_VISIBLE_TOKEN = 1004;
    private static final String LOG_TAG = "WindowManagerEx";
    private static final Singleton<IWindowManager> gDefault = new Singleton<IWindowManager>() {
        protected IWindowManager create() {
            return Stub.asInterface(ServiceManager.getService("window"));
        }
    };

    public static boolean isInputMethodVisible() throws RemoteException {
        int ret = 0;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.view.IWindowManager");
            getDefault().asBinder().transact(IS_INPUT_METHOD_VISIBLE_TOKEN, data, reply, 0);
            reply.readException();
            ret = reply.readInt();
            Log.e(LOG_TAG, "ret: " + ret);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "isInputMethodVisible", e);
        }
        if (ret > 0) {
            return true;
        }
        return false;
    }

    private static IWindowManager getDefault() {
        return (IWindowManager) gDefault.get();
    }
}
