package com.huawei.android.app;

import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import java.util.ArrayList;
import java.util.List;

public class PackageManagerEx {
    private static final String IPACKAGE_MANAGER_DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final String TAG = "PackageManagerEx";
    private static final int TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED = 1008;
    private static final int TRANSACTION_CODE_DELTE_GMS_FROM_UNINSTALLED_DELAPP = 1009;
    private static final int TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST = 1007;
    private static final Singleton<IPackageManager> gDefault = new Singleton<IPackageManager>() {
        protected IPackageManager create() {
            return Stub.asInterface(ServiceManager.getService("package"));
        }
    };

    public static List<String> getPreinstalledApkList() {
        List<String> list = new ArrayList();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST, data, reply, 0);
            reply.readException();
            reply.readStringList(list);
        } catch (Exception e) {
            Log.e(TAG, "failed to getPreinstalledApkList");
        } finally {
            reply.recycle();
            data.recycle();
        }
        return list;
    }

    private static IPackageManager getDefault() {
        return (IPackageManager) gDefault.get();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean checkGmsCoreUninstalled() {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED, data, reply, 0);
            reply.readException();
            res = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to checkGmsCoreUninstalled");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
        return res;
    }

    public static void deleteGmsCoreFromUninstalledDelapp() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_DELTE_GMS_FROM_UNINSTALLED_DELAPP, data, reply, 0);
            reply.readException();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to deleteGmsCoreFromUninstalledDelapp");
        } finally {
            reply.recycle();
            data.recycle();
        }
    }
}
