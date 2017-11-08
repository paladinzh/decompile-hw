package com.huawei.systemmanager.optimize.process.Predicate;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashSet;

public class NotificationPredicate extends FutureTaskPredicate<HashSet<String>, ProcessAppItem> {
    static final int SMCS_NOTIFICATION_GET_NOTI = 1;
    private static final String TAG = "NotificationPredicate";

    public boolean apply(ProcessAppItem item) {
        if (item == null) {
            return false;
        }
        HashSet<String> result = (HashSet) getResult();
        if (result == null) {
            HwLog.e(TAG, "NotificationPredicate result is null!");
            return false;
        }
        boolean filter = result.contains(item.getPackageName());
        if (!filter) {
            return true;
        }
        HwLog.i(TAG, "NotificationPredicate = " + item.getName() + "; contains = " + filter);
        return false;
    }

    protected HashSet<String> doInbackground() throws Exception {
        return getNotificationPkgs();
    }

    private HashSet<String> getNotificationPkgs() {
        RemoteException e;
        HashSet<String> hashSet;
        Exception e2;
        Throwable th;
        Parcel parcel = null;
        Parcel parcel2 = null;
        try {
            IBinder notiservice = ServiceManager.getService("notification");
            if (notiservice == null) {
                return null;
            }
            parcel = Parcel.obtain();
            parcel2 = Parcel.obtain();
            parcel.writeInt(1);
            notiservice.transact(1599293262, parcel, parcel2, 0);
            HashSet<String> notificationPkgs = new HashSet();
            try {
                int size = parcel2.readInt();
                for (int i = 0; i < size; i++) {
                    String pkg = parcel2.readString();
                    if (pkg != null && pkg.length() > 0) {
                        notificationPkgs.add(pkg);
                    }
                }
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel2 != null) {
                    parcel2.recycle();
                }
                return notificationPkgs;
            } catch (RemoteException e3) {
                e = e3;
                hashSet = notificationPkgs;
                HwLog.e(TAG, "SMCSCoOperatorManager.getNotificationPkgs: transact caught remote exception: " + e.toString());
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel2 != null) {
                    parcel2.recycle();
                }
                return null;
            } catch (Exception e4) {
                e2 = e4;
                hashSet = notificationPkgs;
                try {
                    HwLog.e(TAG, "SMCSCoOperatorManager.getNotificationPkgs: catch exception: " + e2.toString());
                    e2.printStackTrace();
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    if (parcel2 != null) {
                        parcel2.recycle();
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    if (parcel2 != null) {
                        parcel2.recycle();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel2 != null) {
                    parcel2.recycle();
                }
                throw th;
            }
        } catch (RemoteException e5) {
            e = e5;
            HwLog.e(TAG, "SMCSCoOperatorManager.getNotificationPkgs: transact caught remote exception: " + e.toString());
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
            return null;
        } catch (Exception e6) {
            e2 = e6;
            HwLog.e(TAG, "SMCSCoOperatorManager.getNotificationPkgs: catch exception: " + e2.toString());
            e2.printStackTrace();
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
            return null;
        }
    }
}
