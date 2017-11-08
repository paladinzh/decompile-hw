package com.huawei.mms.util;

import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Singleton;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import com.huawei.cspcommon.MLog;

public class WindowManagerEx {
    private static final Singleton<IWindowManager> gDefault = new Singleton<IWindowManager>() {
        protected IWindowManager create() {
            return Stub.asInterface(ServiceManager.getService("window"));
        }
    };

    public static boolean isTopFullscreen() {
        int ret = 0;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.view.IWindowManager");
            getDefault().asBinder().transact(206, data, reply, 0);
            ret = reply.readInt();
            MLog.d("WindowManagerEx", "ret: " + ret);
        } catch (RemoteException e) {
            MLog.e("WindowManagerEx", "isTopIsFullscreen", (Throwable) e);
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
