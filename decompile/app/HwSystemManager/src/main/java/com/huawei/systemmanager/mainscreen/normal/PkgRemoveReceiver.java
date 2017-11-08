package com.huawei.systemmanager.mainscreen.normal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;

public abstract class PkgRemoveReceiver {
    private static final String TAG = "PkgRemoveReceiver";
    private final Context mContext = GlobalContext.getContext();
    private BroadcastReceiver mOtherReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Utility.checkBroadcast(context, intent)) {
                String action = intent.getAction();
                HwLog.i(PkgRemoveReceiver.TAG, "recevie action:" + action);
                if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action) || "android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED".equals(action)) {
                    PkgRemoveReceiver.this.doBluetoothStateChange();
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action) || "android.net.wifi.STATE_CHANGE".equals(action)) {
                    PkgRemoveReceiver.this.doWifiStateChange();
                } else if (AntiVirusTools.ACTION_FOUND_VIRUSSCAN_APP.equals(action)) {
                    PkgRemoveReceiver.this.doFoundVirusscanApp(intent);
                }
            }
        }
    };
    private BroadcastReceiver mPkgReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Utility.checkBroadcast(context, intent)) {
                String action = intent.getAction();
                boolean replacing = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
                String pkgName = intent.getData().getSchemeSpecificPart();
                if ("android.intent.action.PACKAGE_REMOVED".equals(action) && !replacing) {
                    PkgRemoveReceiver.this.doPkgRemove(pkgName);
                }
            }
        }
    };
    private boolean mRegister = false;

    protected abstract void doPkgRemove(String str);

    public void registeReceiver() {
        if (!this.mRegister) {
            this.mRegister = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addDataScheme("package");
            this.mContext.registerReceiver(this.mPkgReceiver, filter);
            IntentFilter otherFilter = new IntentFilter();
            otherFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
            otherFilter.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
            otherFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            otherFilter.addAction("android.net.wifi.STATE_CHANGE");
            otherFilter.addAction(AntiVirusTools.ACTION_FOUND_VIRUSSCAN_APP);
            this.mContext.registerReceiver(this.mOtherReceiver, otherFilter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        }
    }

    public void unRegisteReceiver() {
        if (this.mRegister) {
            this.mRegister = false;
            this.mContext.unregisterReceiver(this.mPkgReceiver);
            this.mContext.unregisterReceiver(this.mOtherReceiver);
        }
    }

    protected void doBluetoothStateChange() {
    }

    protected void doWifiStateChange() {
    }

    protected void doFoundVirusscanApp(Intent intent) {
    }
}
