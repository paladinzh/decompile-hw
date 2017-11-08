package com.android.server.location;

import android.content.Context;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.location.gnsschrlog.CSegEVENT_GEOLOCATION_DATA_COLLECT_EVENT;
import com.android.server.location.gnsschrlog.CSubCurrentCell;
import com.android.server.location.gnsschrlog.CSubNeighborCell;
import com.android.server.location.gnsschrlog.CSubWifiApInfo;
import com.android.server.location.gnsschrlog.ChrLogBaseModel;
import com.android.server.location.gnsschrlog.GnssChrCommonInfo;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import com.android.server.location.gnsschrlog.GnssLogManager;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import huawei.android.debug.HwDBGSwitchController;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import libcore.io.IoUtils;

public class GeolocationCollectManager {
    private static final int CELL_COLLECT_INTERVAL_DEFAULT = 10000;
    private static final int CELL_COLLECT_MAX_NUM_DEFAULT = 1000;
    private static final int DATA_COLLECT_TYPE_ALL = 0;
    private static final int DATA_COLLECT_TYPE_CELL = 2;
    private static final int DATA_COLLECT_TYPE_NONE = -1;
    private static final int DATA_COLLECT_TYPE_WIFI = 1;
    private static final boolean DEBUG = HwDBGSwitchController.getDBGSwitch();
    private static final String GEOLOCATION_CONFIG_FILE = "vendor/etc/geoloc.conf";
    private static final int GEOLOCATION_DATA_COLLECT_EVENT = 75;
    private static final String GPS_PROVIDER = "gps";
    private static final int INVAILD_CELLID = -1;
    private static final int LOCATION_ACCURACY_RANGE_DEFAULT = 20;
    private static final int LOCATION_COLLECT_INTERVAL_DEFAULT = 1000;
    private static final int LOCATION_DISTANCE_INTERVAL_DEFAULT = 5;
    private static final int LOCATION_TYPE_ALL_PROVIDER = 0;
    private static final int LOCATION_TYPE_GPS_PROVIDER = 1;
    private static final int LOCATION_TYPE_NETWORK_PROVIDER = 2;
    private static final Long LOGS_RESET_TIME_INTERVAL = Long.valueOf(86400000);
    private static final int MAX_CELL_INFO_NUM = 8;
    private static final int MAX_NEIGHBOR_CELL_INFO_NUM = 16;
    private static final int NETWORK_CLASS_2_G = 1;
    private static final int NETWORK_CLASS_3_G = 2;
    private static final int NETWORK_CLASS_4_G = 3;
    private static final String NETWORK_PROVIDER = "network";
    public static final Comparator<ScanResult> SCAN_RESULT_RSSI_COMPARATOR = new Comparator<ScanResult>() {
        public int compare(ScanResult r1, ScanResult r2) {
            return r2.level - r1.level;
        }
    };
    private static final String TAG = "HwGnssLog_GeolocationCollectManager";
    private static final int TRIGGER_NOW = 1;
    private static final int WIFI_AP_MAX_NUM_DEFAULT = 100;
    private static final int WIFI_COLLECT_MAX_NUM_DEFAULT = 500;
    private static final int mCONFIG_WIFI_SCANRESULT_AP_TIME = 10000;
    private int mCONFIG_CELL_COLLECTION_INTERVAL = 5000;
    private int mCONFIG_CELL_COLLECTION_NUM = 1000;
    private int mCONFIG_COLLECTION_TYPE = 0;
    private int mCONFIG_LOCATION_ACC = 20;
    private int mCONFIG_LOCATION_COLLECT_INTERVAL = 1000;
    private int mCONFIG_LOCATION_DISTANCE_INTERVAL = 0;
    private int mCONFIG_LOCATION_NLP_ACC = 20;
    private int mCONFIG_LOCATION_PROVIDER = 0;
    private int mCONFIG_WIFI_COLLECT_AP_NUM = 100;
    private int mCONFIG_WIFI_COLLECT_AP_NUM_THRESHOLD = 5;
    private int mCONFIG_WIFI_COLLECT_NUM = 500;
    private int mCONFIG_WIFI_SCANRESULT_VALID_INTERVAL = 5000;
    private int mCellCollectCount = 0;
    private boolean mCellCollectEnable = false;
    protected GnssChrCommonInfo mChrComInfo = new GnssChrCommonInfo();
    private Context mContext;
    CSegEVENT_GEOLOCATION_DATA_COLLECT_EVENT mGeoLocationDataCollectEvt;
    private GeoWifInfoCollectManager mGeoWifiCollectManager;
    private double mLastLatitude = 0.0d;
    private double mLastLongitude = 0.0d;
    private long mLastResetTime = 0;
    private long mLastUploadTime = 0;
    private Properties mProperties;
    private String mValidLocationProvider;
    private int mWifiCollectCount = 0;
    private boolean mWifiCollectEnable = false;

    public GeolocationCollectManager(Context context) {
        this.mContext = context;
        this.mProperties = new Properties();
        loadGeolocationCfg();
        this.mGeoWifiCollectManager = new GeoWifInfoCollectManager(this.mContext);
    }

    private void loadGeolocationCfg() {
        this.mValidLocationProvider = AppHibernateCst.INVALID_PKG;
        if (loadPropertiesFromFile(GEOLOCATION_CONFIG_FILE, this.mProperties)) {
            Log.d(TAG, "Geolocation properties reloaded, size = " + this.mProperties.size());
            this.mCONFIG_COLLECTION_TYPE = Integer.parseInt(this.mProperties.getProperty("GEO_LOCATION_COLLECT_TYPE", Long.toString(0)));
            this.mCONFIG_LOCATION_ACC = Integer.parseInt(this.mProperties.getProperty("LOCATION_MAX_ACCURACY", Long.toString(20)));
            this.mCONFIG_LOCATION_NLP_ACC = Integer.parseInt(this.mProperties.getProperty("LOCATION_NLP_MAX_ACCURACY", Long.toString(20)));
            this.mCONFIG_LOCATION_PROVIDER = Integer.parseInt(this.mProperties.getProperty("LOCATION_PROVIDER_TYPE", Long.toString(0)));
            this.mCONFIG_LOCATION_COLLECT_INTERVAL = Integer.parseInt(this.mProperties.getProperty("LOCATION_COLLECT_INTERVAL", Long.toString(1000)));
            this.mCONFIG_WIFI_COLLECT_NUM = Integer.parseInt(this.mProperties.getProperty("WIFI_COLLECT_MAX_NUM", Long.toString(500)));
            this.mCONFIG_WIFI_COLLECT_AP_NUM = Integer.parseInt(this.mProperties.getProperty("WIFI_AP_COLLCT_MAX_NUM", Long.toString(100)));
            this.mCONFIG_CELL_COLLECTION_NUM = Integer.parseInt(this.mProperties.getProperty("CELL_COLLECT_MAX_NUM", Long.toString(1000)));
            this.mCONFIG_CELL_COLLECTION_INTERVAL = Integer.parseInt(this.mProperties.getProperty("CELL_COLLECT_INTERVAL", Long.toString(MemoryConstant.MIN_INTERVAL_OP_TIMEOUT)));
            this.mCONFIG_LOCATION_DISTANCE_INTERVAL = Integer.parseInt(this.mProperties.getProperty("GEO_LOCATION_DISTANCE_INTERVAL", Long.toString(5)));
            this.mLastResetTime = System.currentTimeMillis();
        } else {
            Log.d(TAG, "load geo location cfg file failed, using default value!geoloc config file path = vendor/etc/geoloc.conf");
        }
        if (this.mCONFIG_LOCATION_PROVIDER == 1) {
            this.mValidLocationProvider = GPS_PROVIDER;
        } else if (this.mCONFIG_LOCATION_PROVIDER == 2) {
            this.mValidLocationProvider = NETWORK_PROVIDER;
        } else {
            this.mValidLocationProvider = "networkgps";
        }
    }

    private boolean loadPropertiesFromFile(String filename, Properties properties) {
        Throwable th;
        try {
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(new File(filename));
                try {
                    properties.load(stream2);
                    IoUtils.closeQuietly(stream2);
                    return true;
                } catch (Throwable th2) {
                    th = th2;
                    stream = stream2;
                    IoUtils.closeQuietly(stream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                IoUtils.closeQuietly(stream);
                throw th;
            }
        } catch (IOException e) {
            Log.e(TAG, "Could not open GPS configuration file " + filename);
            return false;
        }
    }

    public void setGeoLocationInfo(Location location, long time, String provider) {
        if (!GnssLogManager.getInstance().isOverseaCommercialUser()) {
            if (DEBUG) {
                Log.d(TAG, "====setGeoLocationInfo:location= " + location + ",provider = " + provider);
            }
            if (this.mCONFIG_COLLECTION_TYPE == -1) {
                if (DEBUG) {
                    Log.d(TAG, "geolocation config param set to forbidden collecting location, just retrun !");
                }
            } else if (this.mWifiCollectCount > this.mCONFIG_WIFI_COLLECT_NUM && this.mCellCollectCount > this.mCONFIG_CELL_COLLECTION_NUM) {
                if (DEBUG) {
                    Log.d(TAG, "collect count large than max nums, ignore !");
                }
            } else if (checkLocationAvailable(location, time, provider)) {
                if (this.mCONFIG_COLLECTION_TYPE != 2) {
                    this.mWifiCollectEnable = checkWifiInfoAvailable();
                }
                if (this.mCONFIG_COLLECTION_TYPE != 1) {
                    this.mCellCollectEnable = checkCellInfoAvilabale();
                }
                if (DEBUG) {
                    Log.d(TAG, "====setGeoLocationInfo: mCellCollectEnable = " + this.mCellCollectEnable + ",mWifiCollectEnable = " + this.mWifiCollectEnable);
                }
                if (this.mCellCollectEnable || this.mWifiCollectEnable) {
                    writeGeoLocationCollectInfo(location, time, provider);
                    this.mLastLatitude = location.getLatitude();
                    this.mLastLongitude = location.getLongitude();
                    this.mLastUploadTime = System.currentTimeMillis();
                    long now = System.currentTimeMillis();
                    if (now > this.mLastResetTime + LOGS_RESET_TIME_INTERVAL.longValue()) {
                        this.mCellCollectCount = 0;
                        this.mWifiCollectCount = 0;
                        this.mLastResetTime = now;
                    }
                    return;
                }
                if (DEBUG) {
                    Log.d(TAG, "neither cell or wifi vaild to collect, just retrun !");
                }
            } else {
                if (DEBUG) {
                    Log.d(TAG, "current Location is not vaild, just retrun !");
                }
            }
        }
    }

    private boolean checkLocationAvailable(Location location, long time, String provider) {
        if (this.mValidLocationProvider.contains(provider)) {
            long difftime = System.currentTimeMillis() - this.mLastUploadTime;
            if (difftime < ((long) this.mCONFIG_LOCATION_COLLECT_INTERVAL)) {
                if (DEBUG) {
                    Log.d(TAG, "difftime less than uploading interval, Ignore!config interval is : " + this.mCONFIG_LOCATION_COLLECT_INTERVAL + " , difftime is : " + difftime);
                }
                return false;
            }
            int acc = (int) location.getAccuracy();
            if (provider.equalsIgnoreCase(GPS_PROVIDER)) {
                if (acc > this.mCONFIG_LOCATION_ACC) {
                    if (DEBUG) {
                        Log.d(TAG, "gps accuracy is large than max offset, Ignore!config acc is : " + this.mCONFIG_LOCATION_ACC + " , current accuracy is : " + acc);
                    }
                    return false;
                }
            } else if (acc > this.mCONFIG_LOCATION_NLP_ACC) {
                if (DEBUG) {
                    Log.d(TAG, "nlp accuracy is large than max offset, Ignore!config acc is : " + this.mCONFIG_LOCATION_ACC + " , current accuracy is : " + acc);
                }
                return false;
            }
            double distance = getDistance(this.mLastLatitude, this.mLastLongitude, location.getLatitude(), location.getLongitude());
            if (DEBUG) {
                Log.d(TAG, "distance : " + distance);
            }
            if (distance >= ((double) this.mCONFIG_LOCATION_DISTANCE_INTERVAL)) {
                return true;
            }
            if (DEBUG) {
                Log.d(TAG, "gps distance is less than min offset, Ignore!config distance is : " + this.mCONFIG_LOCATION_DISTANCE_INTERVAL + " , current distance is : " + distance);
            }
            return false;
        }
        if (DEBUG) {
            Log.d(TAG, "location provider is not allowed to upload location, Ignore!config location provider is : " + this.mValidLocationProvider + " , current location provider is : " + provider);
        }
        return false;
    }

    private boolean checkWifiInfoAvailable() {
        if (this.mWifiCollectCount > this.mCONFIG_WIFI_COLLECT_NUM) {
            if (DEBUG) {
                Log.d(TAG, "wifi collect count large than max value, Ignore!config max count is : " + this.mCONFIG_WIFI_COLLECT_NUM + " , current wifi collect count  is : " + this.mWifiCollectCount);
            }
            return false;
        } else if (this.mGeoWifiCollectManager.checkWifiInfoAvaiable()) {
            this.mWifiCollectCount++;
            return true;
        } else {
            if (DEBUG) {
                Log.d(TAG, "wifi collect not vaild, Ignore!");
            }
            return false;
        }
    }

    private boolean checkCellInfoAvilabale() {
        long difftime = System.currentTimeMillis() - this.mLastUploadTime;
        if (difftime < ((long) this.mCONFIG_CELL_COLLECTION_INTERVAL)) {
            if (DEBUG) {
                Log.d(TAG, "cell difftime less than uploading interval, Ignore!config interval is : " + this.mCONFIG_CELL_COLLECTION_INTERVAL + " , difftime is : " + difftime);
            }
            return false;
        } else if (this.mCellCollectCount > this.mCONFIG_CELL_COLLECTION_NUM) {
            if (DEBUG) {
                Log.d(TAG, "cell collect count large than max value, Ignore!config max count is : " + this.mCONFIG_CELL_COLLECTION_NUM + " , current wifi collect count  is : " + this.mCellCollectCount);
            }
            return false;
        } else {
            this.mCellCollectCount++;
            return true;
        }
    }

    public void writeGeoLocationCollectInfo(Location location, long time, String provider) {
        Date date = new Date();
        setGeoLocationCollectInfoParam(location, time, provider);
        ChrLogBaseModel cChrLogBaseModel = this.mGeoLocationDataCollectEvt;
        if ((this.mGeoLocationDataCollectEvt.cCurrentCellList.size() + this.mGeoLocationDataCollectEvt.cNeighborCellList.size()) + this.mGeoLocationDataCollectEvt.cWifiApInfoList.size() > 0) {
            if (DEBUG) {
                Log.d(TAG, "report GEOLOCATION_DATA_COLLECT_EVENT:mCellCollectCount = " + this.mCellCollectCount + "mWifiCollectCount = " + this.mWifiCollectCount);
            }
            GnssConnectivityLogManager.getInstance().reportAbnormalEventEx(cChrLogBaseModel, 14, 1, GEOLOCATION_DATA_COLLECT_EVENT, date, 1);
        }
    }

    private void setGeoLocationCollectInfoParam(Location location, long time, String provider) {
        Date date = new Date();
        this.mGeoLocationDataCollectEvt = new CSegEVENT_GEOLOCATION_DATA_COLLECT_EVENT();
        this.mGeoLocationDataCollectEvt.tmTimeStamp.setValue(date);
        this.mGeoLocationDataCollectEvt.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
        if (provider.equalsIgnoreCase(GPS_PROVIDER)) {
            this.mGeoLocationDataCollectEvt.ucType.setValue(0);
        } else {
            this.mGeoLocationDataCollectEvt.ucType.setValue(1);
        }
        this.mGeoLocationDataCollectEvt.strLatitude.setValue(location.getLatitude() + AppHibernateCst.INVALID_PKG);
        this.mGeoLocationDataCollectEvt.strLongitude.setValue(location.getLongitude() + AppHibernateCst.INVALID_PKG);
        this.mGeoLocationDataCollectEvt.iAccuracy.setValue((int) location.getAccuracy());
        this.mGeoLocationDataCollectEvt.iBearing.setValue((int) location.getBearing());
        this.mGeoLocationDataCollectEvt.llocation_time.setValue(location.getTime());
        this.mGeoLocationDataCollectEvt.strSpeed.setValue(location.getSpeed() + AppHibernateCst.INVALID_PKG);
        this.mGeoLocationDataCollectEvt.lwifi_scaned_time.setValue(this.mGeoWifiCollectManager.getWifiScanDiffTime());
        this.mGeoLocationDataCollectEvt.lBootTime.setValue(SystemClock.elapsedRealtime());
        setCellInfo();
        setWifiApInfo();
    }

    public void setCellInfo() {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        List<CSubCurrentCell> cCurrentCellList = new ArrayList();
        List<CSubNeighborCell> cNeighborCellList = new ArrayList();
        if (telephonyManager != null) {
            List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
            if (cellInfoList != null) {
                for (CellInfo cellInfo : cellInfoList) {
                    CSubCurrentCell SubCurrentCell = new CSubCurrentCell();
                    CSubNeighborCell SubNeighborCell = new CSubNeighborCell();
                    if (cellInfo instanceof CellInfoGsm) {
                        if (cellInfo.isRegistered()) {
                            SubCurrentCell.iCell_Mcc.setValue(((CellInfoGsm) cellInfo).getCellIdentity().getMcc());
                            SubCurrentCell.iCell_Mnc.setValue(((CellInfoGsm) cellInfo).getCellIdentity().getMnc());
                            SubCurrentCell.iCell_Lac.setValue(((CellInfoGsm) cellInfo).getCellIdentity().getLac());
                            SubCurrentCell.iCell_ID.setValue(((CellInfoGsm) cellInfo).getCellIdentity().getCid());
                            SubCurrentCell.iSignal_Strength.setValue(((CellInfoGsm) cellInfo).getCellSignalStrength().getDbm());
                            SubCurrentCell.iChannel_Number.setValue(((CellInfoGsm) cellInfo).getCellIdentity().getArfcn());
                            SubCurrentCell.iPhysical_Identity.setValue(((CellInfoGsm) cellInfo).getCellIdentity().getBsic());
                            SubCurrentCell.iRAT.setValue(1);
                            cCurrentCellList.add(SubCurrentCell);
                        } else {
                            SubNeighborCell.iCell_Mcc.setValue(((CellInfoGsm) cellInfo).getCellIdentity().getMcc());
                            SubNeighborCell.iCell_Mnc.setValue(((CellInfoGsm) cellInfo).getCellIdentity().getMnc());
                            SubNeighborCell.iCell_Lac.setValue(((CellInfoGsm) cellInfo).getCellIdentity().getLac());
                            SubNeighborCell.iCell_ID.setValue(((CellInfoGsm) cellInfo).getCellIdentity().getCid());
                            SubNeighborCell.iSignal_Strength.setValue(((CellInfoGsm) cellInfo).getCellSignalStrength().getDbm());
                            SubNeighborCell.iChannel_Number.setValue(((CellInfoGsm) cellInfo).getCellIdentity().getArfcn());
                            SubNeighborCell.iPhysical_Identity.setValue(((CellInfoGsm) cellInfo).getCellIdentity().getBsic());
                            SubNeighborCell.iRAT.setValue(1);
                            cNeighborCellList.add(SubNeighborCell);
                        }
                    } else if (cellInfo instanceof CellInfoLte) {
                        if (cellInfo.isRegistered()) {
                            SubCurrentCell.iCell_Mcc.setValue(((CellInfoLte) cellInfo).getCellIdentity().getMcc());
                            SubCurrentCell.iCell_Mnc.setValue(((CellInfoLte) cellInfo).getCellIdentity().getMnc());
                            SubCurrentCell.iCell_Lac.setValue(((CellInfoLte) cellInfo).getCellIdentity().getTac());
                            SubCurrentCell.iCell_ID.setValue(((CellInfoLte) cellInfo).getCellIdentity().getCi());
                            SubCurrentCell.iSignal_Strength.setValue(((CellInfoLte) cellInfo).getCellSignalStrength().getDbm());
                            SubCurrentCell.iChannel_Number.setValue(((CellInfoLte) cellInfo).getCellIdentity().getEarfcn());
                            SubCurrentCell.iPhysical_Identity.setValue(((CellInfoLte) cellInfo).getCellIdentity().getPci());
                            SubCurrentCell.iRAT.setValue(3);
                            cCurrentCellList.add(SubCurrentCell);
                        } else {
                            SubNeighborCell.iCell_Mcc.setValue(((CellInfoLte) cellInfo).getCellIdentity().getMcc());
                            SubNeighborCell.iCell_Mnc.setValue(((CellInfoLte) cellInfo).getCellIdentity().getMnc());
                            SubNeighborCell.iCell_Lac.setValue(((CellInfoLte) cellInfo).getCellIdentity().getTac());
                            SubNeighborCell.iCell_ID.setValue(((CellInfoLte) cellInfo).getCellIdentity().getCi());
                            SubNeighborCell.iSignal_Strength.setValue(((CellInfoLte) cellInfo).getCellSignalStrength().getDbm());
                            SubNeighborCell.iChannel_Number.setValue(((CellInfoLte) cellInfo).getCellIdentity().getEarfcn());
                            SubNeighborCell.iPhysical_Identity.setValue(((CellInfoLte) cellInfo).getCellIdentity().getPci());
                            SubNeighborCell.iRAT.setValue(3);
                            cNeighborCellList.add(SubNeighborCell);
                        }
                    } else if (cellInfo instanceof CellInfoWcdma) {
                        if (cellInfo.isRegistered()) {
                            SubCurrentCell.iCell_Mcc.setValue(((CellInfoWcdma) cellInfo).getCellIdentity().getMcc());
                            SubCurrentCell.iCell_Mnc.setValue(((CellInfoWcdma) cellInfo).getCellIdentity().getMnc());
                            SubCurrentCell.iCell_Lac.setValue(((CellInfoWcdma) cellInfo).getCellIdentity().getLac());
                            SubCurrentCell.iCell_ID.setValue(((CellInfoWcdma) cellInfo).getCellIdentity().getCid());
                            SubCurrentCell.iSignal_Strength.setValue(((CellInfoWcdma) cellInfo).getCellSignalStrength().getDbm());
                            SubCurrentCell.iChannel_Number.setValue(((CellInfoWcdma) cellInfo).getCellIdentity().getUarfcn());
                            SubCurrentCell.iPhysical_Identity.setValue(((CellInfoWcdma) cellInfo).getCellIdentity().getPsc());
                            SubCurrentCell.iRAT.setValue(2);
                            cCurrentCellList.add(SubCurrentCell);
                        } else {
                            SubNeighborCell.iCell_Mcc.setValue(((CellInfoWcdma) cellInfo).getCellIdentity().getMcc());
                            SubNeighborCell.iCell_Mnc.setValue(((CellInfoWcdma) cellInfo).getCellIdentity().getMnc());
                            SubNeighborCell.iCell_Lac.setValue(((CellInfoWcdma) cellInfo).getCellIdentity().getLac());
                            SubNeighborCell.iCell_ID.setValue(((CellInfoWcdma) cellInfo).getCellIdentity().getCid());
                            SubNeighborCell.iSignal_Strength.setValue(((CellInfoWcdma) cellInfo).getCellSignalStrength().getDbm());
                            SubNeighborCell.iChannel_Number.setValue(((CellInfoWcdma) cellInfo).getCellIdentity().getUarfcn());
                            SubNeighborCell.iPhysical_Identity.setValue(((CellInfoWcdma) cellInfo).getCellIdentity().getPsc());
                            SubNeighborCell.iRAT.setValue(2);
                            cNeighborCellList.add(SubNeighborCell);
                        }
                    }
                }
            } else {
                return;
            }
        }
        int i = 0;
        while (i < cCurrentCellList.size() && i < 8) {
            this.mGeoLocationDataCollectEvt.setCSubCurrentCellList((CSubCurrentCell) cCurrentCellList.get(i));
            i++;
        }
        i = 0;
        while (i < cNeighborCellList.size() && i < 16) {
            this.mGeoLocationDataCollectEvt.setCSubNeighborCellList((CSubNeighborCell) cNeighborCellList.get(i));
            i++;
        }
    }

    public void setWifiApInfo() {
        List<CSubWifiApInfo> listCSubWifiApInfo = new ArrayList();
        if (this.mWifiCollectEnable) {
            WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(GnssConnectivityLogManager.SUBSYS_WIFI);
            if (wifiManager != null) {
                List<ScanResult> mLists = wifiManager.getScanResults();
                if (wifiManager.getScanResults().size() > this.mCONFIG_WIFI_COLLECT_AP_NUM_THRESHOLD) {
                    List<ScanResult> sortedScanResults = new ArrayList();
                    for (ScanResult result : mLists) {
                        sortedScanResults.add(result);
                    }
                    Collections.sort(sortedScanResults, SCAN_RESULT_RSSI_COMPARATOR);
                    for (ScanResult result2 : sortedScanResults) {
                        if (listCSubWifiApInfo.size() >= 100) {
                            break;
                        }
                        long time = result2.timestamp / 1000;
                        if (this.mGeoLocationDataCollectEvt.lBootTime.getValue() - time <= MemoryConstant.MIN_INTERVAL_OP_TIMEOUT) {
                            CSubWifiApInfo SubWifiApInfo = new CSubWifiApInfo();
                            SubWifiApInfo.strAP_SSID.setValue(result2.SSID);
                            SubWifiApInfo.strAP_Bssid.setValue(result2.BSSID);
                            SubWifiApInfo.iAP_RSSI.setValue(result2.level);
                            SubWifiApInfo.lAP_ScanTime.setValue(time);
                            listCSubWifiApInfo.add(SubWifiApInfo);
                        }
                    }
                } else {
                    return;
                }
            }
            return;
        }
        int i = 0;
        while (listCSubWifiApInfo != null && i < listCSubWifiApInfo.size()) {
            this.mGeoLocationDataCollectEvt.setCSubWifiApInfoList((CSubWifiApInfo) listCSubWifiApInfo.get(i));
            i++;
        }
        this.mGeoWifiCollectManager.resetWifiApListFlashed();
    }

    public static double getDistance(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        float[] results = new float[1];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
        return (double) results[0];
    }
}
