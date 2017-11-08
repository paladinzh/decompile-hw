package com.huawei.thermal.adapter;

import android.os.Binder;
import android.util.Log;
import com.huawei.thermal.TContext;
import com.huawei.thermal.adapter.IThermalService.Stub;
import com.huawei.thermal.event.ThermalEvent;
import com.huawei.thermal.eventhub.EventListener;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ThermalService extends Stub {
    private static final boolean DEBUG;
    private EventListener mCallback = null;
    private final ArrayList<ThermalEvent> mFreePool = new ArrayList();
    private TContext mTContext = null;

    static {
        boolean z = false;
        if (Log.isLoggable("ThermalService", 2)) {
            z = true;
        }
        DEBUG = z;
    }

    public ThermalService(TContext tcontext, EventListener callback) {
        this.mTContext = tcontext;
        this.mCallback = callback;
    }

    public int onTemperatureChg(int type, int temp) {
        if (1000 == Binder.getCallingUid()) {
            ThermalEvent evt;
            Log.i("ThermalService", "Receive temperature change, type: " + type + ", temp: " + temp + " from thermald");
            if (this.mFreePool.size() <= 0) {
                if (DEBUG) {
                    Log.i("ThermalService", "new ThermalEvent ");
                }
                evt = new ThermalEvent(100, type, temp);
            } else {
                evt = (ThermalEvent) this.mFreePool.remove(0);
                evt.setSensorTemp(temp);
                evt.setSensorType(type);
                evt.resetAs(evt);
            }
            if (DEBUG) {
                Log.v("ThermalService", "new ThermalEvent:" + evt);
            }
            if (this.mCallback != null) {
                this.mCallback.handleEvent(evt);
            }
            if (3 > this.mFreePool.size()) {
                this.mFreePool.add(evt);
            }
            return 0;
        }
        Log.e("ThermalService", "onTemperatureChg permission not allowed. uid = " + Binder.getCallingUid());
        return -1;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mTContext.dump(fd, pw, args);
    }
}
