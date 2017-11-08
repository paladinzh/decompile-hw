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
import com.android.server.location.gnsschrlog.CSubBrcm_Assert_Info;
import com.android.server.location.gnsschrlog.CSubData_Delivery_Delay;
import com.android.server.location.gnsschrlog.CSubFirst_Fix_Time_Out;
import com.android.server.location.gnsschrlog.CSubHiGeo_Wifi_Pos_Error;
import com.android.server.location.gnsschrlog.CSubHiGeo_Wifi_Scan_Error;
import com.android.server.location.gnsschrlog.CSubLos_pos_param;
import com.android.server.location.gnsschrlog.CSubNtp_Data_Param;
import com.android.server.location.gnsschrlog.CSubPdr_Pos_Error;
import com.android.server.location.gnsschrlog.ChrLogBaseModel;
import com.android.server.location.gnsschrlog.GnssChrCommonInfo;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import huawei.android.debug.HwDBGSwitchController;
import java.util.ArrayList;
import java.util.Date;

public class GpsPosErrorEvent {
    public static final int AGPS_CONN_FAILED = 18;
    public static final int AGPS_TIMEOUT = 14;
    private static final int ALMANAC_MASK = 1;
    public static final int CONNECT_WIFIPOS_SERIVCE_ERROR = 34;
    private static final int DAILY_REPORT = 2;
    public static final int DATA_DELIVERY_DELAY = 17;
    private static final boolean DEBUG = HwDBGSwitchController.getDBGSwitch();
    private static final int EPHEMERIS_MASK = 0;
    private static final int EXTEND_CONNECT_WIFIPOS_SERIVCE_ERROR = 24;
    private static final int EXTEND_PDR_POS_ERROR = 25;
    private static final int EXTEND_WIFI_POS_ERROR = 21;
    private static final int EXTEND_WIFI_SCAN_ERROR = 22;
    private static final int EXTEND_WIFI_SERVICE_INIT_FAIL = 23;
    private static final int EXTEND_WRITE_ERROR_INFO = 26;
    public static final int GPSD_NOT_RECOVERY_FAILED = 21;
    public static final int GPS_ADD_BATCHING_FAILED = 10;
    public static final int GPS_ADD_GEOFENCE_FAILED = 9;
    public static final int GPS_BRCM_ASSERT = 30;
    public static final int GPS_CLOSE_GPS_SWITCH_FAILED = 8;
    public static final int GPS_DAILY_CNT_REPORT_FAILD = 25;
    public static final int GPS_INIT_FAILED = 24;
    public static final int GPS_IN_DOOR_FAILED = 20;
    public static final int GPS_LOCAL_DATA_ERR = 29;
    public static final int GPS_LOST_POSITION_FAILED = 11;
    public static final int GPS_LOST_POSITION_UNSURE_FAILED = 23;
    public static final int GPS_LOW_SIGNAL_FAILED = 19;
    public static final int GPS_NTP_DLOAD_FAILED = 4;
    public static final int GPS_NTP_WRONG = 26;
    public static final int GPS_OPEN_GPS_SWITCH_FAILED = 7;
    public static final int GPS_PERMISSION_DENIED = 6;
    private static final int GPS_POS_ERROR_EVENT = 72;
    public static final int GPS_POS_START_FAILED = 1;
    public static final int GPS_POS_STOP_FAILED = 2;
    public static final int GPS_SET_POS_MODE_FAILED = 5;
    public static final int GPS_SUPL_DATA_ERR = 28;
    public static final int GPS_WAKE_LOCK_NOT_RELEASE_FAILED = 12;
    public static final int GPS_XTRA_DATA_ERR = 27;
    public static final int GPS_XTRA_DLOAD_FAILED = 3;
    public static final int HOTSTART_TIMEOUT = 15;
    public static final int LOCATIONPROVIDER_BIND_FAIL = 31;
    public static final String LOCATION_MODE_BATTERY_SAVING = "BATTERY_SAVING";
    public static final String LOCATION_MODE_HIGH_ACCURACY = "HIGH_ACCURACY";
    public static final String LOCATION_MODE_OFF = "LOCATION_OFF";
    public static final String LOCATION_MODE_SENSORS_ONLY = "DEVICE_ONLY";
    public static final int NAVIGATION_ABORT = 16;
    public static final int NETWORK_POSITION_TIMEOUT = 22;
    public static final int PDR_POS_ERROR = 35;
    public static final int STANDALONE_TIMEOUT = 13;
    private static final String TAG = "HwGnssLog_PosErrEvent";
    private static final int TRIGGER_NOW = 1;
    public static final int TYPE_WIFI = 100;
    public static final int UNKNOWN_ISSUE = 0;
    private static final int USED_FOR_FIX_MASK = 2;
    private static final boolean VERBOSE = HwDBGSwitchController.getDBGSwitch();
    private static final String VERTICAL_ESC_SEPARATE = "\\|";
    public static final int WIFI_BG_SEARCH_OFF = 36;
    public static final int WIFI_POS_ERROR = 32;
    public static final int WIFI_SCAN_ERROR = 33;
    public static final int WIFI_SERVICE_INIT_FAIL = 37;
    private ArrayList mApkList = new ArrayList();
    protected GnssChrCommonInfo mChrComInfo = new GnssChrCommonInfo();
    public Context mContext;
    CSegEVENT_GPS_POS_ERROR_EVENT mGpsPosErrEvt;
    private GpsSessionEvent mGpsSessionEvent;

    static class DataDeliveryDelayInfo extends CSubData_Delivery_Delay {
        public DataDeliveryDelayInfo(int difftime) {
            this.iDelayTime.setValue(difftime);
        }
    }

    static class FirstFixTimeoutStatus extends CSubFirst_Fix_Time_Out {
        public FirstFixTimeoutStatus(int svCount, int usedSvCount, String svInfo, int injectParam) {
            this.lTime.setValue(System.currentTimeMillis());
            this.iSvCount.setValue(svCount);
            this.iUsedSvCount.setValue(usedSvCount);
            this.strSvInfo.setValue(svInfo);
            this.ucInjectAiding.setValue(injectParam);
        }
    }

    static class GpsApkName extends CSubApk_Name {
        public GpsApkName(String name, String version) {
            if (name != null) {
                this.strApkName.setValue(name);
                this.strApkVersion.setValue(version);
            }
        }
    }

    static class LostPosParam extends CSubLos_pos_param {
        public LostPosParam(long time, int accuracy, int speed, int svCount, int usedSvCount, String svInfo) {
            this.lTime.setValue(time);
            this.iSpeed.setValue(speed);
            this.iAccuracy.setValue(accuracy);
            this.iSvCount.setValue(svCount);
            this.iUsedSvCount.setValue(usedSvCount);
            this.strSvInfo.setValue(svInfo);
        }
    }

    static class NtpDataParam extends CSubNtp_Data_Param {
        public NtpDataParam(long ntptime, long realtime, String ntpIpAddr) {
            this.lReal_Time.setValue(realtime);
            this.lNtp_Time.setValue(ntptime);
            this.strNtp_IpAddr.setValue(ntpIpAddr);
        }
    }

    static class PdrPosError extends CSubPdr_Pos_Error {
        public PdrPosError(long startTime, long recoverTime) {
            this.lStart_Time.setValue(startTime);
            this.lRecover_Time.setValue(recoverTime);
        }
    }

    static class WifiPosError extends CSubHiGeo_Wifi_Pos_Error {
        public WifiPosError(long startTime, long recoverTime, String cause) {
            this.lStart_Time.setValue(startTime);
            this.lRecover_Time.setValue(recoverTime);
            this.strCause.setValue(cause);
        }
    }

    static class WifiScanError extends CSubHiGeo_Wifi_Scan_Error {
        public WifiScanError(long stuckTime, String wlanStatus) {
            this.lStuck_Time.setValue(stuckTime);
            this.strWlan_status.setValue(wlanStatus);
        }
    }

    GpsPosErrorEvent(Context context) {
        this.mContext = context;
        this.mGpsPosErrEvt = new CSegEVENT_GPS_POS_ERROR_EVENT();
    }

    public void createNewGpsPosErrorEvent() {
        this.mApkList.clear();
        this.mGpsPosErrEvt = new CSegEVENT_GPS_POS_ERROR_EVENT();
    }

    public void setGpsSessionEvent(GpsSessionEvent event) {
        this.mGpsSessionEvent = event;
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

    private void setScreenState() {
        this.mGpsPosErrEvt.strScreenState.setValue(Boolean.toString(((PowerManager) this.mContext.getSystemService("power")).isScreenOn()));
    }

    private void setWifiInfo() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(GnssConnectivityLogManager.SUBSYS_WIFI);
        if (wifiManager != null) {
            boolean wifiState = wifiManager.isWifiEnabled();
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            this.mGpsPosErrEvt.strWifi_Switch.setValue(Boolean.toString(wifiState));
            if (wifiInfo != null) {
                if (DEBUG) {
                    Log.d(TAG, "SSID = " + wifiInfo.getSSID() + ",,,,,,,,BSSID = " + wifiInfo.getBSSID());
                }
                this.mGpsPosErrEvt.strWifi_Bssid.setValue(wifiInfo.getBSSID());
                this.mGpsPosErrEvt.strWifi_Ssid.setValue(wifiInfo.getSSID());
            }
        }
    }

    private void setMobileInfo() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        CellLocation location = tm.getCellLocation();
        this.mGpsPosErrEvt.strDataCall_Switch.setValue(Boolean.toString(tm.getDataEnabled()));
        String operator = tm.getNetworkOperator();
        if (operator != null && operator.length() > 3) {
            int mcc = Integer.parseInt(operator.substring(0, 3));
            int mnc = Integer.parseInt(operator.substring(3));
            if (DEBUG) {
                Log.d(TAG, "mcc : " + mcc + "mnc : " + mnc);
            }
            this.mGpsPosErrEvt.iCell_Mcc.setValue(mcc);
            this.mGpsPosErrEvt.iCell_Mnc.setValue(mnc);
            if (location instanceof GsmCellLocation) {
                GsmCellLocation gsmLocation = (GsmCellLocation) tm.getCellLocation();
                if (gsmLocation != null) {
                    int lac = gsmLocation.getLac();
                    int cellid = gsmLocation.getCid();
                    this.mGpsPosErrEvt.iCell_Lac.setValue(lac);
                    this.mGpsPosErrEvt.iCell_Cid.setValue(cellid);
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
                    this.mGpsPosErrEvt.iCell_Baseid.setValue(cid);
                    this.mGpsPosErrEvt.usCell_SID.setValue(cdmaMnc);
                    this.mGpsPosErrEvt.usCellN_ID.setValue(cdmaLac);
                    if (DEBUG) {
                        Log.d(TAG, "cid : " + cid + "cdmaMnc : " + cdmaMnc + "cdmaLac : " + cdmaLac);
                    }
                }
            } else {
                Log.d(TAG, "another cell location!do nothing.");
            }
        }
    }

    public void setNetworkAvailable(boolean isNetAvailable) {
        this.mGpsPosErrEvt.strNetWorkAvailable.setValue(Boolean.toString(isNetAvailable));
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
        setScreenState();
        setWifiInfo();
        setMobileInfo();
        HwGnssCommParam hwGnssCommParam = new HwGnssCommParam(this.mContext);
        this.mGpsPosErrEvt.enScreen_Orientation.setValue(hwGnssCommParam.getScreenOrientation());
        this.mGpsPosErrEvt.ucBT_Switch.setValue(hwGnssCommParam.getBtSwitchState());
        this.mGpsPosErrEvt.ucNFC_Switch.setValue(hwGnssCommParam.getNfcSwitchState());
        this.mGpsPosErrEvt.ucUSB_State.setValue(hwGnssCommParam.getUsbConnectState());
        this.mGpsPosErrEvt.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
        this.mGpsPosErrEvt.tmTimeStamp.setValue(date);
        this.mGpsPosErrEvt.strLocSetStatus.setValue(setGpsSettingStatus());
        if (this.mGpsSessionEvent != null) {
            this.mGpsPosErrEvt.strHiGeo_LBSversion.setValue(this.mGpsSessionEvent.getLBSVersion());
            this.mGpsPosErrEvt.setCSubHiGeo_AR_Status(this.mGpsSessionEvent.getARStatus());
            this.mGpsPosErrEvt.setCSubHiGeo_Mode(this.mGpsSessionEvent.getHigeoMode());
            this.mGpsPosErrEvt.setCSubHiGeo_WiFi_Pos_Status(this.mGpsSessionEvent.getWifiPosStatus());
            return;
        }
        Log.d(TAG, "mGpsSessionEvent is null.");
    }

    public void setFirstFixTimeOutStatus(int svCount, int usedSvCount, String svInfo, int injectParam) {
        this.mGpsPosErrEvt.setCSubFirst_Fix_Time_Out(new FirstFixTimeoutStatus(svCount, usedSvCount, svInfo, injectParam));
    }

    public void setDataDeliveryDelay(int difftime) {
        this.mGpsPosErrEvt.setCSubData_Delivery_Delay(new DataDeliveryDelayInfo(difftime));
    }

    public void setLostPos_SvStatus(long time, int accuracy, int speed, int svCount, int usedSvCount, String svInfo) {
        this.mGpsPosErrEvt.setCSubLos_pos_param(new LostPosParam(time, accuracy, speed, svCount, usedSvCount, svInfo));
    }

    public void setBrcmAssertInfo(String assertInfo) {
        CSubBrcm_Assert_Info brcminfo = new CSubBrcm_Assert_Info();
        brcminfo.strAssertInfo.setValue(assertInfo);
        this.mGpsPosErrEvt.setCSubBrcm_Assert_Info(brcminfo);
    }

    public void setNetworkInfo(int networkstate) {
        this.mGpsPosErrEvt.enNetworkStatus.setValue(networkStatusToString(networkstate));
    }

    public void setNtpErrTime(long ntptime, long realtime, String ntpIpAddr) {
        this.mGpsPosErrEvt.setCSubNtp_Data_Param(new NtpDataParam(ntptime, realtime, ntpIpAddr));
    }

    public void setGpsApkName(String name, String version) {
        if (!this.mApkList.contains(name)) {
            this.mApkList.add(name);
            this.mGpsPosErrEvt.setCSubApk_NameList(new GpsApkName(name, version));
        }
    }

    public void setStartTime(long time) {
        this.mGpsPosErrEvt.lStartTime.setValue(time);
    }

    public void setProvider(String providermode) {
        this.mGpsPosErrEvt.strProviderMode.setValue(providermode);
    }

    public void setPosMode(int mode) {
        this.mGpsPosErrEvt.ucPosMode.setValue(mode);
    }

    public void writePosErrInfo(int errorcode) {
        Date date = new Date();
        setPosErrCommParam();
        this.mGpsPosErrEvt.ucErrorCode.setValue(errorcode);
        ChrLogBaseModel cChrLogBaseModel = this.mGpsPosErrEvt;
        Log.d(TAG, "writePosErrInfo: 72 ,ErrorCode:" + errorcode);
        GnssConnectivityLogManager.getInstance().reportAbnormalEventEx(cChrLogBaseModel, 14, 1, GPS_POS_ERROR_EVENT, date, 1, errorcode);
    }

    public void writeSubevent(int event, String parameter) {
        Log.d(TAG, "writeSubevent: " + event + " ,parameter:" + parameter);
        switch (event) {
            case 21:
                setWifiPosError(parameter);
                return;
            case 22:
                setWifiScanError(parameter);
                return;
            case 23:
                setWifiServiceInitFail(parameter);
                return;
            case 24:
                setConnectWifiServiceError(parameter);
                return;
            case 25:
                setPdrPosError(parameter);
                return;
            case 26:
                writePosErrInfo(Integer.parseInt(parameter));
                Log.d(TAG, "writePosErr code:" + Integer.parseInt(parameter));
                return;
            default:
                Log.d(TAG, "other event: " + event);
                return;
        }
    }

    public void setWifiPosError(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setWifiPosError:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 3) {
                try {
                    this.mGpsPosErrEvt.setCSubHiGeo_Wifi_Pos_Error(new WifiPosError(Long.parseLong(values[0]), Long.parseLong(values[1]), values[2]));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "wifi pos error.");
                } catch (Exception e2) {
                    Log.e(TAG, "wifi pos error.");
                }
                writePosErrInfo(32);
            }
        }
    }

    public void setWifiScanError(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setWifiScanError:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 2) {
                try {
                    this.mGpsPosErrEvt.setCSubHiGeo_Wifi_Scan_Error(new WifiScanError(Long.parseLong(values[0]), values[1]));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "wifi scan error.");
                } catch (Exception e2) {
                    Log.e(TAG, "wifi scan error.");
                }
                writePosErrInfo(33);
            }
        }
    }

    public void setWifiServiceInitFail(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setWifiServiceInitFail:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 1) {
                try {
                    this.mGpsPosErrEvt.iWifi_Service_Init_Fail_Type.setValue(Integer.parseInt(values[0]));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Wifi service init error.");
                } catch (Exception e2) {
                    Log.e(TAG, "Wifi service init error.");
                }
                writePosErrInfo(37);
            }
        }
    }

    public void setConnectWifiServiceError(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setConnectWifiServiceError:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 1) {
                try {
                    this.mGpsPosErrEvt.strConnect_WifiPos_Service_Error.setValue(values[0]);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Connect wifi service error.");
                } catch (Exception e2) {
                    Log.e(TAG, "Connect wifi service error.");
                }
                writePosErrInfo(34);
            }
        }
    }

    public void setPdrPosError(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setPdrPosError:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 2) {
                try {
                    this.mGpsPosErrEvt.setCSubPdr_Pos_Error(new PdrPosError(Long.parseLong(values[0]), Long.parseLong(values[1])));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "pdr position error.");
                } catch (Exception e2) {
                    Log.e(TAG, "pdr position error.");
                }
                writePosErrInfo(35);
            }
        }
    }
}
