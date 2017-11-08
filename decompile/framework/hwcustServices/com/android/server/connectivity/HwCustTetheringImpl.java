package com.android.server.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.connectivity.Tethering.TetherInterfaceSM;
import java.util.HashMap;

public class HwCustTetheringImpl extends HwCustTethering {
    private static final String ACTION_DUAL_SIM_IMSI_CHANGE = "android.intent.action.ACTION_DUAL_SIM_IMSI_CHANGE";
    private static boolean DISABLE_AP_FOR_IMSI_SWITH = SystemProperties.getBoolean("ro.config.dualimsi.disableap", false);
    protected static final boolean HWDBG;
    private static final String TAG = "HwCustTetheringImpl";
    private HashMap<String, TetherInterfaceSM> mIfaces = null;
    private Object mPublicSync = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && HwCustTetheringImpl.ACTION_DUAL_SIM_IMSI_CHANGE.equals(intent.getAction())) {
                synchronized (HwCustTetheringImpl.this.mPublicSync) {
                    for (Object iface : HwCustTetheringImpl.this.mIfaces.keySet()) {
                        TetherInterfaceSM sm = (TetherInterfaceSM) HwCustTetheringImpl.this.mIfaces.get(iface);
                        if (sm != null && sm.isTethered()) {
                            if (HwCustTetheringImpl.HWDBG) {
                                Log.d(HwCustTetheringImpl.TAG, "disable wifi tethering for imsi switching");
                            }
                            if (HwCustTetheringImpl.this.isUsb((String) iface)) {
                                HwCustTetheringImpl.this.mTethering.setUsbTethering(false);
                            } else if (HwCustTetheringImpl.this.mTethering.isWifi((String) iface) && HwCustTetheringImpl.this.mWifiManager != null) {
                                HwCustTetheringImpl.this.mWifiManager.setWifiApEnabled(null, false);
                            }
                        }
                    }
                }
            }
        }
    };
    private Tethering mTethering = null;
    private WifiManager mWifiManager = null;

    static {
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false : true;
        HWDBG = isLoggable;
    }

    public HwCustTetheringImpl(Context context) {
        super(context);
    }

    public void registerBroadcast(Object publicSync, Tethering tethering, HashMap<String, TetherInterfaceSM> ifaces) {
        if (DISABLE_AP_FOR_IMSI_SWITH && this.mContext != null && tethering != null && ifaces != null) {
            this.mPublicSync = publicSync;
            this.mTethering = tethering;
            this.mIfaces = ifaces;
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_DUAL_SIM_IMSI_CHANGE);
            this.mContext.registerReceiver(this.mReceiver, filter);
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
    }

    private boolean isUsb(String iface) {
        String[] tetherableUsbRegexs = this.mTethering.getTetherableUsbRegexs();
        if (tetherableUsbRegexs == null) {
            return false;
        }
        synchronized (this.mPublicSync) {
            for (String regex : tetherableUsbRegexs) {
                if (iface.matches(regex)) {
                    return true;
                }
            }
            return false;
        }
    }
}
