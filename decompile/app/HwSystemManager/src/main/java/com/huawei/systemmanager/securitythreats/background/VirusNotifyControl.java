package com.huawei.systemmanager.securitythreats.background;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.process.HsmProcessUtil;
import com.huawei.systemmanager.securitythreats.comm.SecurityThreatsUtil;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashSet;
import org.json.JSONException;
import org.json.JSONObject;

class VirusNotifyControl {
    private static final String TAG = "VirusNotifyControl";
    private static VirusNotifyControl sInstance;
    private final Context mAppContext;
    private final JSONObject mInstallVirusPkgs;
    private final HashSet<Integer> mNotifiedPkgs = new HashSet();
    private final HashSet<String> mUninstallVirusPkgs = new HashSet();

    private VirusNotifyControl(Context context) {
        this.mAppContext = context.getApplicationContext();
        this.mInstallVirusPkgs = VirusNotifyPref.getInstallVirus(this.mAppContext);
    }

    public static synchronized VirusNotifyControl getInstance(Context context) {
        VirusNotifyControl virusNotifyControl;
        synchronized (VirusNotifyControl.class) {
            if (sInstance == null) {
                sInstance = new VirusNotifyControl(context);
            }
            virusNotifyControl = sInstance;
        }
        return virusNotifyControl;
    }

    public synchronized boolean isInstallVirusEmpty() {
        boolean z = false;
        synchronized (this) {
            if (this.mInstallVirusPkgs.length() == 0) {
                z = true;
            }
        }
        return z;
    }

    public synchronized boolean addInstallVirus(Context context, String pkg, int level) {
        try {
            this.mInstallVirusPkgs.put(pkg, level);
        } catch (JSONException e) {
            HwLog.e(TAG, "addInstallVirus JSONException", e);
        }
        VirusNotifyPref.setInstallVirus(context, this.mInstallVirusPkgs);
        if (!this.mUninstallVirusPkgs.remove(pkg)) {
            SecurityThreatsUtil.notifyVirusToUI(this.mAppContext, pkg, false, false, level);
        }
        return true;
    }

    public synchronized boolean removeInstallVirus(Context context, String pkg) {
        if (this.mInstallVirusPkgs.has(pkg)) {
            this.mInstallVirusPkgs.remove(pkg);
            VirusNotifyPref.setInstallVirus(context, this.mInstallVirusPkgs);
        }
        return true;
    }

    public synchronized boolean containsInstallVirus(String pkg) {
        return this.mInstallVirusPkgs.has(pkg);
    }

    public synchronized int getInstallVirusLevel(String pkg) {
        try {
        } catch (JSONException e) {
            HwLog.e(TAG, "getInstallVirus JSONException", e);
            return 2;
        }
        return this.mInstallVirusPkgs.getInt(pkg);
    }

    public void notifyVirusWhenStartUp(int pid, int uid) {
        if (isInstallVirusEmpty()) {
            HwLog.d(TAG, "notifyVirusWhenStartUp pkgs is empty");
        } else if (this.mNotifiedPkgs.contains(Integer.valueOf(pid))) {
            HwLog.d(TAG, "notifyVirusWhenStartUp pkgs uid is contained");
        } else {
            String pkg = HsmProcessUtil.getAppInfoByUidAndPid(this.mAppContext, uid, pid);
            if (!TextUtils.isEmpty(pkg) && containsInstallVirus(pkg)) {
                this.mNotifiedPkgs.add(Integer.valueOf(pid));
                int level = getInstallVirusLevel(pkg);
                HwLog.i(TAG, "notifyVirusWhenStartUp pkgs=" + pkg + ", level=" + level);
                SecurityThreatsUtil.notifyVirusToUI(this.mAppContext, pkg, true, false, level);
            }
        }
    }

    public void onVirusDied(int pid, int uid) {
        this.mNotifiedPkgs.remove(Integer.valueOf(pid));
    }

    public synchronized void addUninstallVirus(String pkg) {
        this.mUninstallVirusPkgs.add(pkg);
    }
}
