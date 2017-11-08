package com.android.server.location.gnsschrlog;

public class ENCErrorCode extends Cenum {
    public ENCErrorCode() {
        this.map.put("UNKNOWN_ISSUE", Integer.valueOf(0));
        this.map.put("GPS_POS_START_FAILED", Integer.valueOf(1));
        this.map.put("GPS_POS_STOP_FAILED", Integer.valueOf(2));
        this.map.put("GPS_XTRA_DLOAD_FAILED", Integer.valueOf(3));
        this.map.put("GPS_NTP_DLOAD_FAILED", Integer.valueOf(4));
        this.map.put("GPS_SET_POS_MODE_FAILED", Integer.valueOf(5));
        this.map.put("GPS_PERMISSION_DENIED", Integer.valueOf(6));
        this.map.put("GPS_OPEN_GPS_SWITCH_FAILED", Integer.valueOf(7));
        this.map.put("GPS_CLOSE_GPS_SWITCH_FAILED", Integer.valueOf(8));
        this.map.put("GPS_ADD_GEOFENCE_FAILED", Integer.valueOf(9));
        this.map.put("GPS_ADD_BATCHING_FAILED", Integer.valueOf(10));
        this.map.put("GPS_LOST_POSITION_FAILED", Integer.valueOf(11));
        this.map.put("GPS_WAKE_LOCK_NOT_RELEASE_FAILED", Integer.valueOf(12));
        this.map.put("STANDALONE_TIMEOUT", Integer.valueOf(13));
        this.map.put("AGPS_TIMEOUT", Integer.valueOf(14));
        this.map.put("HOTSTART_TIMEOUT", Integer.valueOf(15));
        this.map.put("NAVIGATION_ABORT", Integer.valueOf(16));
        this.map.put("DATA_DELIVERY_DELAY", Integer.valueOf(17));
        this.map.put("AGPS_CONN_FAILED", Integer.valueOf(18));
        this.map.put("GPS_LOW_SIGNAL_FAILED", Integer.valueOf(19));
        this.map.put("GPS_IN_DOOR_FAILED", Integer.valueOf(20));
        this.map.put("GPSD_NOT_RECOVERY_FAILED", Integer.valueOf(21));
        this.map.put("NETWORK_POSITION_TIMEOUT", Integer.valueOf(22));
        this.map.put("GPS_LOST_POSITION_UNSURE_FAILED", Integer.valueOf(23));
        this.map.put("GPS_INIT_FAILED", Integer.valueOf(24));
        this.map.put("GPS_DAILY_CNT_REPORT_FAILD", Integer.valueOf(25));
        this.map.put("GPS_NTP_WRONG", Integer.valueOf(26));
        this.map.put("GPS_XTRA_DATA_ERR", Integer.valueOf(27));
        this.map.put("GPS_SUPL_DATA_ERR", Integer.valueOf(28));
        this.map.put("GPS_LOCAL_DATA_ERR", Integer.valueOf(29));
        this.map.put("GPS_BRCM_ASSERT", Integer.valueOf(30));
        this.map.put("LOCATIONPROVIDER_BIND_FAIL", Integer.valueOf(31));
        this.map.put("WIFI_POS_ERROR", Integer.valueOf(32));
        this.map.put("WIFI_SCAN_ERROR", Integer.valueOf(33));
        this.map.put("CONNECT_WIFIPOS_SERIVCE_ERROR", Integer.valueOf(34));
        this.map.put("PDR_POS_ERROR", Integer.valueOf(35));
        this.map.put("WIFI_BG_SEARCH_OFF", Integer.valueOf(36));
        this.map.put("WIFI_SERVICE_INIT_FAIL", Integer.valueOf(37));
        this.map.put("HIGEO_RM_DOWNLOAD_STATUS", Integer.valueOf(38));
        this.map.put("HIGEO_PDR_STEP_LENGTH", Integer.valueOf(39));
        setLength(1);
    }
}
