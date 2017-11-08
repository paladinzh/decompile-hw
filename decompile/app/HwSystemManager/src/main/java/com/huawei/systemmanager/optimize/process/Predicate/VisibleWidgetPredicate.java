package com.huawei.systemmanager.optimize.process.Predicate;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashSet;

public class VisibleWidgetPredicate extends FutureTaskPredicate<HashSet<String>, ProcessAppItem> {
    static final int SMCS_APP_WIDGET_SERVICE_GET_VISIBLE = 1;
    private static final String TAG = "VisibleWidgetPredicate";

    public boolean apply(ProcessAppItem item) {
        if (item == null) {
            return false;
        }
        String pkg = item.getPackageName();
        if (!((HashSet) getResult()).contains(pkg)) {
            return true;
        }
        HwLog.i(TAG, "VisibleWidgetPredicate = " + item.getName() + ", pkg=" + pkg);
        return false;
    }

    protected HashSet<String> doInbackground() throws Exception {
        return getVisibleWidgets();
    }

    private HashSet<String> getVisibleWidgets() {
        RemoteException e;
        HashSet<String> hashSet;
        Exception e2;
        Throwable th;
        Parcel parcel = null;
        Parcel parcel2 = null;
        try {
            IBinder appWidgetS = ServiceManager.getService("appwidget");
            if (appWidgetS == null) {
                return null;
            }
            parcel = Parcel.obtain();
            parcel2 = Parcel.obtain();
            parcel.writeInt(1);
            appWidgetS.transact(1599297111, parcel, parcel2, 0);
            int size = parcel2.readInt();
            HashSet<String> visibleWidgetPkgs = new HashSet();
            int i = 0;
            while (i < size) {
                try {
                    String pkg = parcel2.readString();
                    if (pkg != null && pkg.length() > 0) {
                        HwLog.i(TAG, "item.name:: pkg = " + pkg);
                        visibleWidgetPkgs.add(pkg);
                    }
                    i++;
                } catch (RemoteException e3) {
                    e = e3;
                    hashSet = visibleWidgetPkgs;
                } catch (Exception e4) {
                    e2 = e4;
                    hashSet = visibleWidgetPkgs;
                } catch (Throwable th2) {
                    th = th2;
                }
            }
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
            return visibleWidgetPkgs;
        } catch (RemoteException e5) {
            e = e5;
            HwLog.e(TAG, "getVisibleWidgets: transact caught remote exception: " + e.toString());
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
            return null;
        } catch (Exception e6) {
            e2 = e6;
            try {
                HwLog.e(TAG, "getVisibleWidgets: catch exception: " + e2.toString());
                e2.printStackTrace();
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel2 != null) {
                    parcel2.recycle();
                }
                return null;
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
        }
    }
}
