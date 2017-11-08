package com.android.server.location;

import android.os.WorkSource;
import android.util.Log;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class GpsFreezeProc {
    private static String TAG = "GpsFreezeProc";
    private static GpsFreezeProc mGpsFreezeProc;
    private ArrayList<GpsFreezeListener> mFreezeListenerList = new ArrayList();
    private HashMap<String, Integer> mFreezeProcesses = new HashMap();

    private GpsFreezeProc() {
    }

    public static GpsFreezeProc getInstance() {
        if (mGpsFreezeProc == null) {
            mGpsFreezeProc = new GpsFreezeProc();
        }
        return mGpsFreezeProc;
    }

    public void addFreezeProcess(String pkg, int uid) {
        synchronized (this.mFreezeProcesses) {
            this.mFreezeProcesses.put(pkg, Integer.valueOf(uid));
        }
        Log.d(TAG, "addFreezeProcess pkg:" + pkg);
        for (GpsFreezeListener freezeListener : this.mFreezeListenerList) {
            freezeListener.onFreezeProChange();
        }
    }

    public void removeFreezeProcess(String pkg, int uid) {
        synchronized (this.mFreezeProcesses) {
            if (uid == 0) {
                if ("".equals(pkg)) {
                    this.mFreezeProcesses.clear();
                }
            }
            this.mFreezeProcesses.remove(pkg);
        }
        Log.d(TAG, "removeFreezeProcess pkg:" + pkg);
        for (GpsFreezeListener freezeListener : this.mFreezeListenerList) {
            freezeListener.onFreezeProChange();
        }
    }

    public boolean isFreeze(String pkgName) {
        boolean containsKey;
        synchronized (this.mFreezeProcesses) {
            containsKey = this.mFreezeProcesses.containsKey(pkgName);
        }
        return containsKey;
    }

    public void registerFreezeListener(GpsFreezeListener freezeListener) {
        this.mFreezeListenerList.add(freezeListener);
    }

    public boolean shouldFreeze(WorkSource workSource) {
        boolean shouldFreeze = true;
        for (int i = 0; i < workSource.size(); i++) {
            if (!getInstance().isFreeze(workSource.getName(i))) {
                shouldFreeze = false;
            }
        }
        if (shouldFreeze) {
            Log.i(TAG, "should freeze gps");
        }
        return shouldFreeze;
    }

    public void dump(PrintWriter pw) {
        pw.println("Location Freeze Proc:");
        synchronized (this.mFreezeProcesses) {
            for (String pkg : this.mFreezeProcesses.keySet()) {
                pw.println("   " + pkg);
            }
        }
    }
}
