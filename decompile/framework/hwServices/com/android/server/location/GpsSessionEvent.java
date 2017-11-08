package com.android.server.location;

import android.content.Context;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.location.gnsschrlog.CSegEVENT_GPS_SESSION_EVENT;
import com.android.server.location.gnsschrlog.CSubApk_Name;
import com.android.server.location.gnsschrlog.CSubBrcmPosReferenceInfo;
import com.android.server.location.gnsschrlog.CSubFixPos_status;
import com.android.server.location.gnsschrlog.CSubHiGeo_AR_Count;
import com.android.server.location.gnsschrlog.CSubHiGeo_AR_Status;
import com.android.server.location.gnsschrlog.CSubHiGeo_GPS_Pos_Status;
import com.android.server.location.gnsschrlog.CSubHiGeo_GWKF_Pos_Status;
import com.android.server.location.gnsschrlog.CSubHiGeo_MM_Status;
import com.android.server.location.gnsschrlog.CSubHiGeo_Mode;
import com.android.server.location.gnsschrlog.CSubHiGeo_Scan_Status;
import com.android.server.location.gnsschrlog.CSubHiGeo_Vdr_Pos_Status;
import com.android.server.location.gnsschrlog.CSubHiGeo_WiFi_Initial_APcount;
import com.android.server.location.gnsschrlog.CSubHiGeo_WiFi_Pos_Count;
import com.android.server.location.gnsschrlog.CSubHiGeo_WiFi_Pos_Status;
import com.android.server.location.gnsschrlog.CSubHiGeo_WiFi_Stationary_Status;
import com.android.server.location.gnsschrlog.CSubLosPos_Status;
import com.android.server.location.gnsschrlog.CSubResumePos_Status;
import com.android.server.location.gnsschrlog.ChrLogBaseModel;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import huawei.android.debug.HwDBGSwitchController;
import java.util.ArrayList;
import java.util.Date;

public class GpsSessionEvent {
    private static final int ALMANAC_MASK = 1;
    private static final int DAILY_REPORT = 2;
    private static final boolean DEBUG = HwDBGSwitchController.getDBGSwitch();
    private static final int EPHEMERIS_MASK = 0;
    private static final int EXTEND_AR_COUNT = 16;
    private static final int EXTEND_AR_STATUS = 3;
    private static final int EXTEND_CREROUTE = 2;
    private static final int EXTEND_GPS_LOST_COUNT = 12;
    private static final int EXTEND_GPS_POS_STATUS = 6;
    private static final int EXTEND_GWKF_POS_STATUS = 8;
    private static final int EXTEND_GWKF_START_COUNT = 14;
    private static final int EXTEND_HIGEO_MODE = 5;
    private static final int EXTEND_LBSVERSION = 1;
    private static final int EXTEND_MM_STATUS = 18;
    private static final int EXTEND_PDR_DR_COUNT = 17;
    private static final int EXTEND_SCAN_STATUS = 20;
    private static final int EXTEND_VDR_POS_STATUS = 11;
    private static final int EXTEND_VDR_START_COUNT = 15;
    private static final int EXTEND_WIFI_INIT_APCOUNT = 19;
    private static final int EXTEND_WIFI_POS_COUNT = 9;
    private static final int EXTEND_WIFI_POS_STATUS = 7;
    private static final int EXTEND_WIFI_START_COUNT = 13;
    private static final int EXTEND_WIFI_STATION_STATUS = 4;
    private static final int GPS_SESSION_EVENT = 73;
    private static final int GpsType = 14;
    public static final String LOCATION_MODE_BATTERY_SAVING = "BATTERY_SAVING";
    public static final String LOCATION_MODE_HIGH_ACCURACY = "HIGH_ACCURACY";
    public static final String LOCATION_MODE_OFF = "LOCATION_OFF";
    public static final String LOCATION_MODE_SENSORS_ONLY = "DEVICE_ONLY";
    private static final int SET_ISLOCATION_VALID = 10;
    private static final String TAG = "HwGpsLog_SessionEvent";
    private static final int TRIGGER_NOW = 1;
    public static final int TYPE_WIFI = 100;
    private static final int USED_FOR_FIX_MASK = 2;
    private static final boolean VERBOSE = HwDBGSwitchController.getDBGSwitch();
    private static final String VERTICAL_ESC_SEPARATE = "\\|";
    private ARStatus mARStatus;
    private ArrayList mApkList = new ArrayList();
    CSubBrcmPosReferenceInfo mBrcmReferenceInfo;
    public Context mContext;
    CSegEVENT_GPS_SESSION_EVENT mGpsSessionEvt;
    private HigeoMode mHiGeoMode;
    HwGnssDftGnssSessionParam mHwGnssDftGnssSessionParam;
    private String mLBSVersion;
    private WifiPosStatus mWifiPosStatus;

    static class ARStatus extends CSubHiGeo_AR_Status {
        public ARStatus(long timeStamp, int activity, int event) {
            this.lTimeStamp.setValue(timeStamp);
            this.ucactivity.setValue(activity);
            this.ucevent.setValue(event);
        }
    }

    static class CMMStatus extends CSubHiGeo_MM_Status {
        public CMMStatus(String sendBinder, String locationSource, String AMAPVersion, String flpVersion) {
            this.strsend_binder.setValue(sendBinder);
            this.strsend_get_location_source.setValue(locationSource);
            this.strAMAP_version.setValue(AMAPVersion);
            this.strFLP_version.setValue(flpVersion);
        }
    }

    enum FixMode {
        COLD_START,
        WARM_START,
        HOT_START
    }

    static class FixPos_Status extends CSubFixPos_status {
        public FixPos_Status(long time, int svCount, int usedSvCount, String svInfo) {
            this.lFixTime.setValue(time);
            this.iSvCount.setValue(svCount);
            this.iUsedSvCount.setValue(usedSvCount);
            this.strSvInfo.setValue(svInfo);
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

    static class GpsPosStatus extends CSubHiGeo_GPS_Pos_Status {
        public GpsPosStatus(long timeStamp, int status, String gpsAcc) {
            this.lTimeStamp.setValue(timeStamp);
            this.ucStatus.setValue(status);
            this.strGpsAcc.setValue(gpsAcc);
        }
    }

    static class GwkfPosStatus extends CSubHiGeo_GWKF_Pos_Status {
        public GwkfPosStatus(long timeStamp, int status, String gpsAcc) {
            this.lTimeStamp.setValue(timeStamp);
            this.ucstatus.setValue(status);
            this.strGWKFacc.setValue(gpsAcc);
        }
    }

    static class HigeoMode extends CSubHiGeo_Mode {
        public HigeoMode(long timeStamp, int mode, int switchCause, long switchDistance) {
            this.lTimeStamp.setValue(timeStamp);
            this.ucmode.setValue(mode);
            this.ucSwitchCause.setValue(switchCause);
            this.lSwitchDistance.setValue(switchDistance);
        }
    }

    static class LostPos_Status extends CSubLosPos_Status {
        public LostPos_Status(long time, int accuracy, int speed, int svCount, int usedSvCount, String svInfo) {
            this.lLosPosTime.setValue(time);
            this.iLosPosSpeed.setValue(speed);
            this.iLosPosAccuracy.setValue(accuracy);
            this.iSvCount.setValue(svCount);
            this.iUsedSvCount.setValue(usedSvCount);
            this.strSvInfo.setValue(svInfo);
        }
    }

    static class ResumePos_Status extends CSubResumePos_Status {
        public ResumePos_Status(long time, int accuracy, int speed, int svCount, int usedSvCount, String svInfo) {
            this.strSvInfo.setValue(svInfo);
            this.iSvCount.setValue(svCount);
            this.lResumePosTime.setValue(time);
            this.iResumePosSpeed.setValue(speed);
            this.iUsedSvCount.setValue(usedSvCount);
            this.iResumePosAccuracy.setValue(accuracy);
        }
    }

    static class ScanStatus extends CSubHiGeo_Scan_Status {
        public ScanStatus(long lessThan5, long to10s, long over10s) {
            this.lLessThanFiveSec.setValue(lessThan5);
            this.lFiveSecToTenSec.setValue(to10s);
            this.lOverTenSec.setValue(over10s);
        }
    }

    static class VdrPosStatus extends CSubHiGeo_Vdr_Pos_Status {
        public VdrPosStatus(long timeStamp, int status, String vdrAcc) {
            this.lTimeStamp.setValue(timeStamp);
            this.ucstatus.setValue(status);
            this.strVdrAcc.setValue(vdrAcc);
        }
    }

    static class WifiInitialAPCount extends CSubHiGeo_WiFi_Initial_APcount {
        public WifiInitialAPCount(long timeStamp, String apCount) {
            this.lTimeStamp.setValue(timeStamp);
            this.strAPcount.setValue(apCount);
        }
    }

    static class WifiPosCount extends CSubHiGeo_WiFi_Pos_Count {
        public WifiPosCount(long online, long offline, long fusion, long cache) {
            this.lOnline.setValue(online);
            this.lOffline.setValue(offline);
            this.lFusion.setValue(fusion);
            this.lCache.setValue(cache);
        }
    }

    static class WifiPosStatus extends CSubHiGeo_WiFi_Pos_Status {
        public WifiPosStatus(long timeStamp, int status, String gpsAcc) {
            this.lTimeStamp.setValue(timeStamp);
            this.ucstatus.setValue(status);
            this.strGpsAcc.setValue(gpsAcc);
        }
    }

    static class WifiStationaryStatus extends CSubHiGeo_WiFi_Stationary_Status {
        public WifiStationaryStatus(long timeStamp, int activity) {
            this.lTimeStamp.setValue(timeStamp);
            this.ucactivity.setValue(activity);
        }
    }

    static class arCount extends CSubHiGeo_AR_Count {
        public arCount(long vehicleCount, long bicycleCount, long walkingCount, long runningCount, long stillCount, long stopVehicleCount) {
            this.lin_vehicle.setValue(vehicleCount);
            this.lon_bicycle.setValue(bicycleCount);
            this.lwalking.setValue(walkingCount);
            this.lrunning.setValue(runningCount);
            this.lstill.setValue(stillCount);
            this.lstop_vehicle.setValue(stopVehicleCount);
        }
    }

    GpsSessionEvent(Context context) {
        this.mContext = context;
        this.mGpsSessionEvt = new CSegEVENT_GPS_SESSION_EVENT();
        this.mHwGnssDftGnssSessionParam = new HwGnssDftGnssSessionParam();
        this.mBrcmReferenceInfo = new CSubBrcmPosReferenceInfo();
        Log.d(TAG, "GpsSessionEvent , mGpsSessionEvt is :" + this.mGpsSessionEvt);
    }

    public void createNewGpsSessionEvent() {
        this.mGpsSessionEvt = new CSegEVENT_GPS_SESSION_EVENT();
        this.mBrcmReferenceInfo = new CSubBrcmPosReferenceInfo();
    }

    private static String networkStatusToString(int networkStatus) {
        String res = "UNKNOW";
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
        this.mGpsSessionEvt.strNetWorkAvailable.setValue(Boolean.toString(isNetAvailable));
    }

    private void setWifiInfo() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(GnssConnectivityLogManager.SUBSYS_WIFI);
        if (wifiManager != null) {
            this.mGpsSessionEvt.strWifi_Switch.setValue(Boolean.toString(wifiManager.isWifiEnabled()));
        }
    }

    private void setMobileInfo() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        CellLocation location = tm.getCellLocation();
        this.mGpsSessionEvt.strDataCall_Switch.setValue(Boolean.toString(tm.getDataEnabled()));
        String operator = tm.getNetworkOperator();
        if (operator != null && operator.length() > 3) {
            int mcc = Integer.parseInt(operator.substring(0, 3));
            int mnc = Integer.parseInt(operator.substring(3));
            if (DEBUG) {
                Log.d(TAG, "mcc : " + mcc + "mnc : " + mnc);
            }
            this.mGpsSessionEvt.iCell_Mcc.setValue(mcc);
            this.mGpsSessionEvt.iCell_Mnc.setValue(mnc);
            if (location instanceof GsmCellLocation) {
                GsmCellLocation gsmLocation = (GsmCellLocation) tm.getCellLocation();
                if (gsmLocation != null) {
                    this.mGpsSessionEvt.iCell_Lac.setValue(gsmLocation.getLac());
                }
            } else if (location instanceof CdmaCellLocation) {
                CdmaCellLocation cdmaLocation = (CdmaCellLocation) tm.getCellLocation();
                if (cdmaLocation != null) {
                    this.mGpsSessionEvt.usCellN_ID.setValue(cdmaLocation.getNetworkId());
                }
            } else {
                Log.d(TAG, "another cell location!do nothing.");
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

    public void setBrcmReferenceInfo() {
        this.mGpsSessionEvt.setCSubBrcmPosReferenceInfo(this.mBrcmReferenceInfo);
    }

    public void setBrcmPosSource(int posSource) {
        this.mBrcmReferenceInfo.ucPosSource.setValue(posSource);
    }

    public void setBrcmTimeSource(int timeSource) {
        this.mBrcmReferenceInfo.ucTimeSource.setValue(timeSource);
    }

    public void setBrcmAidingStatus(int status) {
        this.mBrcmReferenceInfo.ucAidingStatus.setValue(status);
    }

    public void setBrcmTcxoOffset(int offset) {
        this.mBrcmReferenceInfo.lTcxo_Offset.setValue(offset);
    }

    public void setBrcmAgcData(float gps, float glo, float dbs) {
        this.mBrcmReferenceInfo.strAgc_GPS.setValue(String.valueOf(gps));
        this.mBrcmReferenceInfo.strAgc_GLO.setValue(String.valueOf(glo));
        this.mBrcmReferenceInfo.strAgc_BDS.setValue(String.valueOf(dbs));
    }

    public void setBrcmRestartFlag(boolean isGpsRestarted) {
        this.mGpsSessionEvt.strIsGpsdResart.setValue(Boolean.toString(isGpsRestarted));
    }

    public void setLostPosCnt(int cnt) {
        if (DEBUG) {
            Log.d(TAG, "setLostPosCnt : " + cnt);
        }
        this.mGpsSessionEvt.iLostPosCnt.setValue(cnt);
        this.mHwGnssDftGnssSessionParam.lostPosCnt = cnt;
    }

    public void setReStartCnt(int cnt) {
        this.mGpsSessionEvt.usGpsdReStartCnt.setValue(cnt);
    }

    public void setGpsdRestartFlag(boolean isRestart) {
        if (DEBUG) {
            Log.d(TAG, "setGpsdRestartFlag : " + isRestart);
        }
        this.mGpsSessionEvt.strIsGpsdResart.setValue(Boolean.toString(isRestart));
        this.mHwGnssDftGnssSessionParam.isGpsdResart = isRestart;
    }

    public void setStopTime(long time) {
        if (DEBUG) {
            Log.d(TAG, "setStopTime : " + time);
        }
        this.mGpsSessionEvt.lStopTime.setValue(time);
        this.mHwGnssDftGnssSessionParam.stopTime = time;
    }

    public void setCommFlag(boolean issueSession) {
        if (DEBUG) {
            Log.d(TAG, "setCommFlag : " + issueSession);
        }
        this.mGpsSessionEvt.strIsIssueSession.setValue(Boolean.toString(issueSession));
    }

    public void setAppUsedParm() {
    }

    public void setAvgAcc(int avgPositionAcc) {
        if (DEBUG) {
            Log.d(TAG, "setAvgAcc : " + avgPositionAcc);
        }
        this.mGpsSessionEvt.iAvgPositionAcc.setValue(avgPositionAcc);
    }

    public void setFixLocation(Location location) {
        if (DEBUG) {
            Log.d(TAG, "setFixLocation : ACC" + location.getAccuracy() + ", SPEED" + location.getSpeed());
        }
        this.mGpsSessionEvt.iFixAccuracy.setValue((int) location.getAccuracy());
        this.mGpsSessionEvt.iFixSpeed.setValue((int) location.getSpeed());
    }

    public void setTTFF(int ttff) {
        this.mGpsSessionEvt.iTTFF.setValue(ttff);
        this.mHwGnssDftGnssSessionParam.ttff = ttff;
    }

    public void setFirstCatchSvTime(long time) {
        if (DEBUG) {
            Log.d(TAG, "setFirstCatchSvTime : " + time);
        }
        this.mGpsSessionEvt.lFirstCatchSvTime.setValue(time);
    }

    public void setDrivingAvgCn0(int cn0) {
        this.mGpsSessionEvt.ucAvgCN0When40KMPH.setValue(cn0);
    }

    public void setCatchSvTime(long time) {
        if (DEBUG) {
            Log.d(TAG, "setCatchSvTime : " + time);
        }
        this.mGpsSessionEvt.lCatchSvTime.setValue(time);
        this.mHwGnssDftGnssSessionParam.catchSvTime = time;
    }

    public void setNetworkStatus(int networkStatus) {
        this.mGpsSessionEvt.enNetworkStatus.setValue(networkStatusToString(networkStatus));
    }

    public void setProvider(String providermode) {
        if (DEBUG) {
            Log.d(TAG, "setProvider : " + providermode);
        }
        this.mGpsSessionEvt.strProviderMode.setValue(providermode);
    }

    public void setPosMode(int mode) {
        this.mGpsSessionEvt.ucPosMode.setValue(mode);
    }

    public void setStartTime(long time) {
        if (DEBUG) {
            Log.d(TAG, "set start time ,: " + time);
        }
        this.mGpsSessionEvt.lStartTime.setValue(time);
        this.mHwGnssDftGnssSessionParam.startTime = time;
    }

    public void setInjectParam(int injectParam) {
        this.mGpsSessionEvt.ucInjectAiding.setValue(injectParam);
    }

    public void setGpsApkName(String name, String version) {
        if (DEBUG) {
            Log.d(TAG, "setApkName : " + name + " , version is : " + version);
        }
        if (!this.mApkList.contains(name)) {
            this.mApkList.add(name);
            this.mGpsSessionEvt.setCSubApk_NameList(new GpsApkName(name, version));
        }
    }

    public void setFixPos_SvStatus(long time, int svCount, int usedSvCount, String svInfo) {
        if (DEBUG) {
            Log.d(TAG, "setFixPos_SvStatus ,:time  " + time + " svCount:" + svCount + " ,usedSvCount:" + usedSvCount + " ,svInfo:" + svInfo);
        }
        this.mGpsSessionEvt.setCSubFixPos_status(new FixPos_Status(time, svCount, usedSvCount, svInfo));
    }

    public void setLostPos_SvStatus(long time, int accuracy, int speed, int svCount, int usedSvCount, String svInfo) {
        this.mGpsSessionEvt.setCSubLosPos_StatusList(new LostPos_Status(time, accuracy, speed, svCount, usedSvCount, svInfo));
    }

    public void setResumePos_SvStatus(long time, int accuracy, int speed, int svCount, int usedSvCount, String svInfo) {
        this.mGpsSessionEvt.setCSubResumePos_StatusList(new ResumePos_Status(time, accuracy, speed, svCount, usedSvCount, svInfo));
    }

    private void setCommonPara() {
        Date date = new Date();
        setWifiInfo();
        setMobileInfo();
        setBrcmReferenceInfo();
        this.mGpsSessionEvt.tmTimeStamp.setValue(date);
        this.mGpsSessionEvt.strLocSetStatus.setValue(setGpsSettingStatus());
    }

    public void writeGpsSessionInfo() {
        Log.d(TAG, "writeGpsSessionInfo");
        rptGpsSessionToImonitor();
        Date date = new Date();
        ChrLogBaseModel cChrLogBaseModel = this.mGpsSessionEvt;
        setCommonPara();
        this.mApkList.clear();
        if (DEBUG) {
            Log.d(TAG, "writeGpsSessionInfo, id :73");
        }
        GnssConnectivityLogManager.getInstance().reportAbnormalEventEx(cChrLogBaseModel, 14, 1, GPS_SESSION_EVENT, date, 1);
    }

    private void rptGpsSessionToImonitor() {
        new HwGnssDftManager(this.mContext).sendSessionDataToImonitor(GPS_SESSION_EVENT, this.mHwGnssDftGnssSessionParam);
        this.mHwGnssDftGnssSessionParam.resetParam();
    }

    public void writeSubevent(int event, String parameter) {
        switch (event) {
            case 1:
                setLBSVersion(parameter);
                return;
            case 2:
                setCREroute(parameter);
                return;
            case 3:
                setARStatus(parameter);
                return;
            case 4:
                setWifiStationaryStatus(parameter);
                return;
            case 5:
                setHigeoMode(parameter);
                return;
            case 6:
                setGpsPosStatus(parameter);
                return;
            case 7:
                setWifiPosStatus(parameter);
                return;
            case 8:
                setGwkfPosStatus(parameter);
                return;
            case 9:
                setWifiPosCount(parameter);
                return;
            case 10:
                setIsLocationValid(parameter);
                return;
            case 11:
                setVdrPosStatus(parameter);
                return;
            case 12:
                setGpsLostCount(parameter);
                return;
            case 13:
                setWifiStartCount(parameter);
                return;
            case 14:
                setGWKFStartCount(parameter);
                return;
            case 15:
                setVdrStartCount(parameter);
                return;
            case 16:
                setARCount(parameter);
                return;
            case 17:
                setPdrDrCount(parameter);
                return;
            case 18:
                setMMStatus(parameter);
                return;
            case 19:
                setWiFiInitialAPCount(parameter);
                return;
            case 20:
                setScanStatus(parameter);
                return;
            default:
                Log.d(TAG, "other event: " + event);
                return;
        }
    }

    public void setIsLocationValid(String data) {
        Log.d(TAG, "setIsLocationValid:" + data);
    }

    public void setLBSVersion(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setLBSVersion:" + data);
        this.mGpsSessionEvt.strHiGeo_LBSversion.setValue(data);
        this.mLBSVersion = data;
    }

    public String getLBSVersion() {
        Log.d(TAG, "[HiGeoCHRLog] getLBSVersion:" + this.mLBSVersion);
        return this.mLBSVersion;
    }

    public void setCREroute(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setCREroute:" + data);
        if (!TextUtils.isEmpty(data)) {
            try {
                this.mGpsSessionEvt.lHiGeo_C_Reroute.setValue(Long.parseLong(data));
            } catch (NumberFormatException e) {
                Log.e(TAG, "c_reroute data error.");
            } catch (Exception e2) {
                Log.e(TAG, "write c_reroute error.");
            }
        }
    }

    public void setARStatus(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setARStatus:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 3) {
                try {
                    this.mARStatus = new ARStatus(Long.parseLong(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
                    this.mGpsSessionEvt.setCSubHiGeo_AR_StatusList(this.mARStatus);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "ar status data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "write ar status error.");
                }
            }
        }
    }

    public ARStatus getARStatus() {
        Log.d(TAG, "[HiGeoCHRLog] getARStatus");
        return this.mARStatus;
    }

    public void setWifiStationaryStatus(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setWifiStationaryStatus:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 2) {
                try {
                    this.mGpsSessionEvt.setCSubHiGeo_WiFi_Stationary_StatusList(new WifiStationaryStatus(Long.parseLong(values[0]), Integer.parseInt(values[1])));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "wifiStationaryStatus data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "write wifiStationaryStatus error.");
                }
            }
        }
    }

    public void setHigeoMode(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setHigeoMode:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 4) {
                try {
                    this.mHiGeoMode = new HigeoMode(Long.parseLong(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), Long.parseLong(values[3]));
                    this.mGpsSessionEvt.setCSubHiGeo_ModeList(this.mHiGeoMode);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "higeo mode data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "write higeo mode error.");
                }
            }
        }
    }

    public HigeoMode getHigeoMode() {
        Log.d(TAG, "getHigeoMode");
        return this.mHiGeoMode;
    }

    public void setGpsPosStatus(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setGpsPosStatus:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 3) {
                try {
                    this.mGpsSessionEvt.setCSubHiGeo_GPS_Pos_StatusList(new GpsPosStatus(Long.parseLong(values[0]), Integer.parseInt(values[1]), values[2]));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "gps pos status data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "write gps pos status error.");
                }
            }
        }
    }

    public void setGwkfPosStatus(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setGwkfPosStatus:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 3) {
                try {
                    this.mGpsSessionEvt.setCSubHiGeo_GWKF_Pos_StatusList(new GwkfPosStatus(Long.parseLong(values[0]), Integer.parseInt(values[1]), values[2]));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "gwkf pos status data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "write gwkf pos status error.");
                }
            }
        }
    }

    public void setWifiPosCount(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setWifiPosCount:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 4) {
                try {
                    this.mGpsSessionEvt.setCSubHiGeo_WiFi_Pos_Count(new WifiPosCount(Long.parseLong(values[0]), Long.parseLong(values[1]), Long.parseLong(values[2]), Long.parseLong(values[3])));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "wifi pos count data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "write wifi pos count error.");
                }
            }
        }
    }

    public void setWifiPosStatus(String data) {
        Log.d(TAG, "[HiGeoCHRLog] writeWifiPosStatus:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 3) {
                try {
                    this.mWifiPosStatus = new WifiPosStatus(Long.parseLong(values[0]), Integer.parseInt(values[1]), values[2]);
                    this.mGpsSessionEvt.setCSubHiGeo_WiFi_Pos_StatusList(this.mWifiPosStatus);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "wifi pos status data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "write wifi pos status error.");
                }
            }
        }
    }

    public WifiPosStatus getWifiPosStatus() {
        return this.mWifiPosStatus;
    }

    public void setWiFiInitialAPCount(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setWiFi_Initial_APcount:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 2) {
                try {
                    this.mGpsSessionEvt.setCSubHiGeo_WiFi_Initial_APcountList(new WifiInitialAPCount(Long.parseLong(values[0]), values[1]));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "wifi init ap count data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "wifi init ap count data error.");
                }
            }
        }
    }

    public void setVdrPosStatus(String data) {
        Log.d(TAG, "[HiGeoCHRLog] writeVdrPosStatus:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 3) {
                try {
                    this.mGpsSessionEvt.setCSubHiGeo_Vdr_Pos_StatusList(new VdrPosStatus(Long.parseLong(values[0]), Integer.parseInt(values[1]), values[2]));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "vdr pos status data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "vdr pos status data error.");
                }
            }
        }
    }

    public void setGpsLostCount(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setGpsLostCount:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 1) {
                try {
                    this.mGpsSessionEvt.lHiGeo_GPS_Lost_Count.setValue(Long.parseLong(values[0]));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Gps lost count data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "ps lost count data error.");
                }
            }
        }
    }

    public void setWifiStartCount(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setWifiStartCount:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 1) {
                try {
                    this.mGpsSessionEvt.lHiGeo_WiFi_Start_Count.setValue(Long.parseLong(values[0]));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Wifi start count data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "Wifi start count data error.");
                }
            }
        }
    }

    public void setGWKFStartCount(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setGWKFStartCount:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 1) {
                try {
                    this.mGpsSessionEvt.lHiGeo_GWKF_Start_Count.setValue(Long.parseLong(values[0]));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "GWKF start count data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "GWKF start count data error.");
                }
            }
        }
    }

    public void setVdrStartCount(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setVdrStartCount:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 1) {
                try {
                    this.mGpsSessionEvt.lHiGeo_VDR_Start_Count.setValue(Long.parseLong(values[0]));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "vdr start count data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "vdr start count data error.");
                }
            }
        }
    }

    public void setARCount(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setARCount:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 6) {
                try {
                    this.mGpsSessionEvt.setCSubHiGeo_AR_Count(new arCount(Long.parseLong(values[0]), Long.parseLong(values[1]), Long.parseLong(values[2]), Long.parseLong(values[3]), Long.parseLong(values[4]), Long.parseLong(values[5])));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "ar count data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "ar count data error.");
                }
            }
        }
    }

    public void setPdrDrCount(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setPdrDrCount:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 1) {
                try {
                    this.mGpsSessionEvt.lHiGeo_PDR_DR_Count.setValue(Long.parseLong(values[0]));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "PDR count data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "PDR count data error.");
                }
            }
        }
    }

    public void setMMStatus(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setMMStatus:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 4) {
                try {
                    this.mGpsSessionEvt.setCSubHiGeo_MM_Status(new CMMStatus(values[0], values[1], values[2], values[3]));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "mm status data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "mm status data error.");
                }
            }
        }
    }

    public void setScanStatus(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setScanStatus:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 3) {
                try {
                    this.mGpsSessionEvt.setCSubHiGeo_Scan_Status(new ScanStatus(Long.parseLong(values[0]), Long.parseLong(values[1]), Long.parseLong(values[2])));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "scan status data error.");
                } catch (Exception e2) {
                    Log.e(TAG, "scan status data error.");
                }
            }
        }
    }
}
