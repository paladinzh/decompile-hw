package com.huawei.systemmanager.optimize.process;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public abstract class AbsProtectAppControl {
    protected static final int MSG_CHECK_PACKAGE_FULL = 8;
    protected static final int MSG_CLOSE = 3;
    protected static final int MSG_INSTALL = 6;
    protected static final int MSG_REFRESH_DATA = 5;
    protected static final int MSG_SET_NO_PROTECT = 2;
    protected static final int MSG_SET_PROTECT = 1;
    protected static final int MSG_UNINSTALL = 7;
    static final List<IDataChangedListener> sListeners = Lists.newArrayList();
    protected final MyContentObserver mContentObserver;
    protected final Context mContext;
    protected BarHandler mHandler;
    protected final HandlerThread mHandlerThread = new HandlerThread("handler_thread");

    private class BarHandler extends Handler {
        public BarHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HwLog.i(AbsProtectAppControl.getTAG(), "handleMessage:" + msg.what);
            switch (msg.what) {
                case 1:
                    AbsProtectAppControl.this.protectAppToDB(msg.obj);
                    return;
                case 2:
                    AbsProtectAppControl.this.notProtectFromDB(msg.obj);
                    return;
                case 5:
                    AbsProtectAppControl.this.loadData();
                    AbsProtectAppControl.this.notifyListenerDataRefresh();
                    return;
                case 6:
                    AbsProtectAppControl.this.installAppInner(msg.obj);
                    return;
                case 7:
                    AbsProtectAppControl.this.uninstallAppInner((String) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    private class MyContentObserver extends ContentObserver {
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            AbsProtectAppControl.this.sendMessage(5, null);
        }
    }

    protected abstract Object getObjectLock();

    public abstract void installAppInner(String str);

    public abstract void loadData();

    protected abstract void notProtectFromDB(ArrayList<String> arrayList);

    protected abstract void protectAppToDB(ArrayList<String> arrayList);

    public abstract boolean setNoProtect(List<String> list);

    public abstract boolean setProtect(List<String> list);

    protected abstract void uninstallAppInner(String str);

    protected static String getTAG() {
        return "AbsProtectAppControl";
    }

    protected AbsProtectAppControl(Context context) {
        this.mContext = context.getApplicationContext();
        this.mContentObserver = new MyContentObserver(null);
    }

    public static void registerListener(IDataChangedListener callback) {
        synchronized (sListeners) {
            sListeners.add(callback);
        }
        HwLog.i(getTAG(), "register listener:" + callback);
    }

    public static void unregisterListener(IDataChangedListener callback) {
        synchronized (sListeners) {
            sListeners.remove(callback);
        }
        HwLog.i(getTAG(), "unregister listener:" + callback);
    }

    protected static List<IDataChangedListener> getListeners() {
        List newArrayList;
        synchronized (sListeners) {
            newArrayList = Lists.newArrayList(sListeners);
        }
        return newArrayList;
    }

    public boolean setProtect(String pkgName) {
        return setProtect(Lists.newArrayList(pkgName));
    }

    public boolean setNoProtect(String pkgName) {
        return setNoProtect(Lists.newArrayList(pkgName));
    }

    protected void sendMessage(int what, Object obj) {
        synchronized (getObjectLock()) {
            if (this.mHandler == null) {
                Looper looper = this.mHandlerThread.getLooper();
                if (looper == null) {
                    HwLog.e(getTAG(), "hanlderthread looper is null!!!!!");
                    return;
                }
                this.mHandler = new BarHandler(looper);
            }
            this.mHandler.obtainMessage(what, obj).sendToTarget();
        }
    }

    public void installApp(String apkName) {
        sendMessage(6, apkName);
    }

    public void uninstallApp(String pkgName) {
        sendMessage(7, pkgName);
    }

    protected void removeAdcardApp(String[] packages) {
        for (String pkg : packages) {
            HwLog.i(getTAG(), "removeAdcardApp, " + pkg);
            for (IDataChangedListener listener : getListeners()) {
                listener.onPackageRemoved(pkg);
            }
        }
    }

    protected void notifyListenerDataRefresh() {
        for (IDataChangedListener l : getListeners()) {
            l.onProtectedAppRefresh();
        }
    }
}
