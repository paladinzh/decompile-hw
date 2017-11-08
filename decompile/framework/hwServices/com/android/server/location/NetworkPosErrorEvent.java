package com.android.server.location;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.provider.Settings.Secure;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.location.gnsschrlog.CSegEVENT_GPS_POS_ERROR_EVENT;
import com.android.server.location.gnsschrlog.CSubApk_Name;
import com.android.server.location.gnsschrlog.CSubNetwork_Pos_Timeout;
import com.android.server.location.gnsschrlog.ChrLogBaseModel;
import com.android.server.location.gnsschrlog.GnssChrCommonInfo;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import huawei.android.debug.HwDBGSwitchController;
import java.util.ArrayList;
import java.util.Date;

public class NetworkPosErrorEvent {
    private static final int ALMANAC_MASK = 1;
    private static final int DAILY_REPORT = 2;
    private static final boolean DEBUG = HwDBGSwitchController.getDBGSwitch();
    private static final int EPHEMERIS_MASK = 0;
    private static final int GPS_POS_ERROR_EVENT = 72;
    public static final String LOCATION_MODE_BATTERY_SAVING = "BATTERY_SAVING";
    public static final String LOCATION_MODE_HIGH_ACCURACY = "HIGH_ACCURACY";
    public static final String LOCATION_MODE_OFF = "LOCATION_OFF";
    public static final String LOCATION_MODE_SENSORS_ONLY = "DEVICE_ONLY";
    public static final int NETWORK_POSITION_TIMEOUT = 22;
    private static final String TAG = "HwGnssLog_PosErrEvent";
    private static final int TRIGGER_NOW = 1;
    public static final int TYPE_WIFI = 100;
    private static final int USED_FOR_FIX_MASK = 2;
    private static final boolean VERBOSE = HwDBGSwitchController.getDBGSwitch();
    private ArrayList mApkList = new ArrayList();
    protected GnssChrCommonInfo mChrComInfo = new GnssChrCommonInfo();
    public Context mContext;
    CSegEVENT_GPS_POS_ERROR_EVENT mNetworkPosErrEvt;

    static class GpsApkName extends CSubApk_Name {
        public GpsApkName(String name, String version) {
            if (name != null) {
                this.strApkName.setValue(name);
                this.strApkVersion.setValue(version);
            }
        }
    }

    static class NetworkPosTimeoutParam extends CSubNetwork_Pos_Timeout {
        public NetworkPosTimeoutParam(int suberrorcode, boolean IsDataAvailable) {
            this.ucSubErrorCode.setValue(suberrorcode);
            this.strIsDataAvailable.setValue(Boolean.toString(IsDataAvailable));
        }
    }

    NetworkPosErrorEvent(Context context) {
        this.mContext = context;
        this.mNetworkPosErrEvt = new CSegEVENT_GPS_POS_ERROR_EVENT();
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

    public void setNetworkAvailable(boolean isNetAvailable) {
        this.mNetworkPosErrEvt.strNetWorkAvailable.setValue(Boolean.toString(isNetAvailable));
    }

    private void setScreenState() {
        this.mNetworkPosErrEvt.strScreenState.setValue(Boolean.toString(((PowerManager) this.mContext.getSystemService("power")).isScreenOn()));
    }

    private void setMobileInfo() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        CellLocation location = tm.getCellLocation();
        this.mNetworkPosErrEvt.strDataCall_Switch.setValue(Boolean.toString(tm.getDataEnabled()));
        String operator = tm.getNetworkOperator();
        if (operator != null && operator.length() > 3) {
            int mcc = Integer.parseInt(operator.substring(0, 3));
            int mnc = Integer.parseInt(operator.substring(3));
            if (DEBUG) {
                Log.d(TAG, "mcc : " + mcc + "mnc : " + mnc);
            }
            this.mNetworkPosErrEvt.iCell_Mcc.setValue(mcc);
            this.mNetworkPosErrEvt.iCell_Mnc.setValue(mnc);
            if (location instanceof GsmCellLocation) {
                GsmCellLocation gsmLocation = (GsmCellLocation) tm.getCellLocation();
                if (gsmLocation != null) {
                    int lac = gsmLocation.getLac();
                    int cellid = gsmLocation.getCid();
                    this.mNetworkPosErrEvt.iCell_Lac.setValue(lac);
                    this.mNetworkPosErrEvt.iCell_Cid.setValue(cellid);
                    if (DEBUG) {
                        Log.d(TAG, "lac : " + lac + "cellid" + cellid);
                    }
                }
            } else if (location instanceof CdmaCellLocation) {
                CdmaCellLocation cdmaLocation = (CdmaCellLocation) tm.getCellLocation();
                if (cdmaLocation != null) {
                    int cdmaLac = cdmaLocation.getNetworkId();
                    int cid = cdmaLocation.getBaseStationId();
                    int cdmaMnc = cdmaLocation.getSystemId();
                    this.mNetworkPosErrEvt.iCell_Baseid.setValue(cid);
                    this.mNetworkPosErrEvt.usCell_SID.setValue(cdmaMnc);
                    this.mNetworkPosErrEvt.usCellN_ID.setValue(cdmaLac);
                    if (DEBUG) {
                        Log.d(TAG, "cid : " + cid + "cdmaMnc : " + cdmaMnc + "cdmaLac : " + cdmaLac);
                    }
                }
            } else {
                Log.d(TAG, "another cell location!do nothing.");
            }
        }
    }

    private void setWifiInfo() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(GnssConnectivityLogManager.SUBSYS_WIFI);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            this.mNetworkPosErrEvt.strWifi_Switch.setValue(Boolean.toString(wifiManager.isWifiEnabled()));
            if (wifiInfo != null) {
                if (DEBUG) {
                    Log.d(TAG, "WIFI info : " + wifiInfo.toString() + "SSID : " + wifiInfo.getSSID() + "BSSID : " + wifiInfo.getBSSID());
                }
                this.mNetworkPosErrEvt.strWifi_Bssid.setValue(wifiInfo.getBSSID());
                this.mNetworkPosErrEvt.strWifi_Ssid.setValue(wifiInfo.getSSID());
            }
        }
    }

    private boolean getAllowedProviders(String provider) {
        return TextUtils.delimitedStringContains(Secure.getString(this.mContext.getContentResolver(), "location_providers_allowed"), ',', provider);
    }

    private String setGpsSettingStatus() {
        boolean gpsEnabled = getAllowedProviders("gps");
        boolean networkEnabled = getAllowedProviders("network");
        if (gpsEnabled && networkEnabled) {
            return "HIGH_ACCURACY";
        }
        if (gpsEnabled) {
            return "DEVICE_ONLY";
        }
        if (networkEnabled) {
            return "BATTERY_SAVING";
        }
        return "LOCATION_OFF";
    }

    private void setPosErrCommParam() {
        Date date = new Date();
        setWifiInfo();
        setScreenState();
        setMobileInfo();
        this.mNetworkPosErrEvt.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
        this.mNetworkPosErrEvt.tmTimeStamp.setValue(date);
        this.mNetworkPosErrEvt.strLocSetStatus.setValue(setGpsSettingStatus());
    }

    public void setNetworkPosTimeOUTInfo(int subErrorcode, boolean isNetworkAvaiable) {
        this.mNetworkPosErrEvt.setCSubNetwork_Pos_Timeout(new NetworkPosTimeoutParam(subErrorcode, isNetworkAvaiable));
    }

    public void setNetworkInfo(int networkstate) {
        this.mNetworkPosErrEvt.enNetworkStatus.setValue(networkStatusToString(networkstate));
    }

    public void setGpsApkName(String name, String version) {
        if (!this.mApkList.contains(name)) {
            this.mApkList.add(name);
            this.mNetworkPosErrEvt.setCSubApk_NameList(new GpsApkName(name, version));
        }
    }

    public void setStartTime(long time) {
        this.mNetworkPosErrEvt.lStartTime.setValue(time);
    }

    public void setProvider(String providermode) {
        this.mNetworkPosErrEvt.strProviderMode.setValue(providermode);
    }

    public void writeNetworkPosErrInfo() {
        Date date = new Date();
        this.mApkList.clear();
        setPosErrCommParam();
        this.mNetworkPosErrEvt.ucErrorCode.setValue(22);
        ChrLogBaseModel cChrLogBaseModel = this.mNetworkPosErrEvt;
        Log.d(TAG, "fillPosErrInfo: 72  ,ErrorCode:22");
        GnssConnectivityLogManager.getInstance().reportAbnormalEventEx(cChrLogBaseModel, 14, 1, GPS_POS_ERROR_EVENT, date, 1);
    }
}
