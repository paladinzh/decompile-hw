package com.android.server.location;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import huawei.android.debug.HwDBGSwitchController;

public class HwGnssNetWorkStatus {
    private static final boolean DEBUG = HwDBGSwitchController.getDBGSwitch();
    private static final String TAG = "HwGnssLog_networkStatus";
    private static final int TYPE_WIFI = 100;
    private static final boolean VERBOSE = HwDBGSwitchController.getDBGSwitch();
    private int mCdmaBSid;
    private int mCdmaNid;
    private int mCdmaSid;
    private int mCellid;
    private Context mContext;
    private boolean mDataEnable;
    private int mLac;
    private int mMcc;
    private int mMnc;
    private String mNetworkType = AppHibernateCst.INVALID_PKG;
    private String mWifiBssid = AppHibernateCst.INVALID_PKG;
    private String mWifiSsid = AppHibernateCst.INVALID_PKG;

    public HwGnssNetWorkStatus(Context context) {
        this.mContext = context;
    }

    private static String networkStatusToString(int networkStatus) {
        String res = "NETWORK_TYPE_UNKNOWN";
        switch (networkStatus) {
            case 0:
                return "NETWORK_TYPE_UNKNOWN";
            case 1:
                return "NETWORK_TYPE_GPRS";
            case 2:
                return "NETWORK_TYPE_EDGE";
            case 3:
                return "NETWORK_TYPE_UMTS";
            case 4:
                return "NETWORK_TYPE_CDMA";
            case 5:
                return "NETWORK_TYPE_EVDO_0";
            case 6:
                return "NETWORK_TYPE_EVDO_A";
            case 7:
                return "NETWORK_TYPE_1xRTT";
            case 8:
                return "NETWORK_TYPE_HSDPA";
            case 9:
                return "NETWORK_TYPE_HSUPA";
            case 10:
                return "NETWORK_TYPE_HSPA";
            case 11:
                return "NETWORK_TYPE_IDEN";
            case 12:
                return "NETWORK_TYPE_EVDO_B";
            case 13:
                return "NETWORK_TYPE_LTE";
            case 14:
                return "NETWORK_TYPE_EHRPD";
            case 15:
                return "NETWORK_TYPE_HSPAP";
            case 16:
                return "NETWORK_TYPE_GSM";
            case 17:
                return "NETWORK_TYPE_TD_SCDMA";
            case 18:
                return "NETWORK_TYPE_IWLAN";
            case 100:
                return "TYPE_WIFI";
            default:
                return res;
        }
    }

    public void triggerNetworkRelatedStatus() {
        setMobileInfo();
        setWifiInfo();
        setNetworkType();
    }

    private void setNetworkType() {
        int networkType = 0;
        NetworkInfo networkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == 1) {
                networkType = 100;
            } else if (networkInfo.getType() == 0) {
                networkType = networkInfo.getSubtype();
            }
        }
        this.mNetworkType = networkStatusToString(networkType);
    }

    private void setMobileInfo() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        CellLocation location = tm.getCellLocation();
        this.mDataEnable = tm.getDataEnabled();
        String operator = tm.getNetworkOperator();
        if (operator != null && operator.length() > 3) {
            this.mMcc = Integer.parseInt(operator.substring(0, 3));
            this.mMnc = Integer.parseInt(operator.substring(3));
            if (DEBUG) {
                Log.d(TAG, "mcc : " + this.mMcc + "mnc : " + this.mMnc);
            }
            if (location instanceof GsmCellLocation) {
                GsmCellLocation gsmLocation = (GsmCellLocation) location;
                this.mLac = gsmLocation.getLac();
                this.mCellid = gsmLocation.getCid();
            } else if (location instanceof CdmaCellLocation) {
                CdmaCellLocation cdmaLocation = (CdmaCellLocation) location;
                this.mCdmaNid = cdmaLocation.getNetworkId();
                this.mCdmaBSid = cdmaLocation.getBaseStationId();
                this.mCdmaSid = cdmaLocation.getSystemId();
            } else {
                Log.d(TAG, "another cell location!do nothing.");
            }
        }
    }

    private void setWifiInfo() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(GnssConnectivityLogManager.SUBSYS_WIFI);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                this.mWifiBssid = wifiInfo.getBSSID();
                this.mWifiSsid = wifiInfo.getSSID();
            }
        }
    }

    public String getNetworkType() {
        return this.mNetworkType;
    }

    public boolean getDateEnableStatue() {
        return this.mDataEnable;
    }

    public int getMcc() {
        return this.mMcc;
    }

    public int getMnc() {
        return this.mMnc;
    }

    public int getLac() {
        return this.mLac;
    }

    public int getCellid() {
        return this.mCellid;
    }

    public int getCdmaSid() {
        return this.mCdmaSid;
    }

    public int getCdmaNid() {
        return this.mCdmaNid;
    }

    public int getCdmaBSid() {
        return this.mCdmaBSid;
    }

    public String getWifiBssid() {
        return this.mWifiBssid;
    }

    public String getWifiSsid() {
        return this.mWifiSsid;
    }
}
