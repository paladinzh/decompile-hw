package com.huawei.device.connectivitychrlog;

import com.huawei.connectivitylog.ConnectivityLogManager;

public class CSegEVENT_WIFI_CONNECT_AUTH_FAILED_EX extends ChrLogBaseEventModel {
    public LogByteArray aucExt_info = new LogByteArray(8192);
    public ENCEventId enEventId = new ENCEventId();
    public ENCWifiConnectAuthFailedReason enWifiConnectAuthFailedReason = new ENCWifiConnectAuthFailedReason();
    public ENCucHwStatus enucHwStatus = new ENCucHwStatus();
    public LogInt iAP_RSSI = new LogInt();
    public LogString strAP_MAC = new LogString(17);
    public LogString strAP_SSID = new LogString(32);
    public LogString strAP_auth_alg = new LogString(20);
    public LogString strAP_eap = new LogString(25);
    public LogString strAP_group = new LogString(25);
    public LogString strAP_key_mgmt = new LogString(10);
    public LogString strAP_pairwise = new LogString(20);
    public LogString strAP_proto = new LogString(10);
    public LogString strSTA_MAC = new LogString(17);
    public LogString strapVendorInfo = new LogString(ConnectivityLogManager.WIFI_PORTAL_SAMPLES_COLLECTE);
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucCardIndex = new LogByte();
    public LogShort usAP_channel = new LogShort();
    public LogShort usLen = new LogShort();
    public LogShort usSubErrorCode = new LogShort();

    public CSegEVENT_WIFI_CONNECT_AUTH_FAILED_EX() {
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("enWifiConnectAuthFailedReason", Integer.valueOf(1));
        this.fieldMap.put("enWifiConnectAuthFailedReason", this.enWifiConnectAuthFailedReason);
        this.lengthMap.put("usSubErrorCode", Integer.valueOf(2));
        this.fieldMap.put("usSubErrorCode", this.usSubErrorCode);
        this.lengthMap.put("strSTA_MAC", Integer.valueOf(17));
        this.fieldMap.put("strSTA_MAC", this.strSTA_MAC);
        this.lengthMap.put("strAP_MAC", Integer.valueOf(17));
        this.fieldMap.put("strAP_MAC", this.strAP_MAC);
        this.lengthMap.put("strAP_SSID", Integer.valueOf(32));
        this.fieldMap.put("strAP_SSID", this.strAP_SSID);
        this.lengthMap.put("strAP_proto", Integer.valueOf(10));
        this.fieldMap.put("strAP_proto", this.strAP_proto);
        this.lengthMap.put("strAP_key_mgmt", Integer.valueOf(10));
        this.fieldMap.put("strAP_key_mgmt", this.strAP_key_mgmt);
        this.lengthMap.put("strAP_auth_alg", Integer.valueOf(20));
        this.fieldMap.put("strAP_auth_alg", this.strAP_auth_alg);
        this.lengthMap.put("strAP_pairwise", Integer.valueOf(20));
        this.fieldMap.put("strAP_pairwise", this.strAP_pairwise);
        this.lengthMap.put("strAP_group", Integer.valueOf(25));
        this.fieldMap.put("strAP_group", this.strAP_group);
        this.lengthMap.put("strAP_eap", Integer.valueOf(25));
        this.fieldMap.put("strAP_eap", this.strAP_eap);
        this.lengthMap.put("usAP_channel", Integer.valueOf(2));
        this.fieldMap.put("usAP_channel", this.usAP_channel);
        this.lengthMap.put("iAP_RSSI", Integer.valueOf(4));
        this.fieldMap.put("iAP_RSSI", this.iAP_RSSI);
        this.lengthMap.put("enucHwStatus", Integer.valueOf(1));
        this.fieldMap.put("enucHwStatus", this.enucHwStatus);
        this.lengthMap.put("strapVendorInfo", Integer.valueOf(ConnectivityLogManager.WIFI_PORTAL_SAMPLES_COLLECTE));
        this.fieldMap.put("strapVendorInfo", this.strapVendorInfo);
        this.lengthMap.put("aucExt_info", Integer.valueOf(8192));
        this.fieldMap.put("aucExt_info", this.aucExt_info);
        this.enEventId.setValue("WIFI_CONNECT_AUTH_FAILED_EX");
        this.usLen.setValue(getTotalLen());
    }
}
