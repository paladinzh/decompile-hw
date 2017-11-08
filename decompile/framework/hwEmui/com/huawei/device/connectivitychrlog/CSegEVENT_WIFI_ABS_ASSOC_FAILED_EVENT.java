package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_ABS_ASSOC_FAILED_EVENT extends ChrLogBaseEventModel {
    public ENCEventId enEventId = new ENCEventId();
    public ENCWifiConnectAssocFailedReason enWifiConnectAssocFailedReason = new ENCWifiConnectAssocFailedReason();
    public LogString strAP_MAC = new LogString(17);
    public LogString strAP_SSID = new LogString(32);
    public LogString strSTA_MAC = new LogString(17);
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucAP_auth_type = new LogByte();
    public LogByte ucCardIndex = new LogByte();
    public LogByte ucSwitchType = new LogByte();
    public LogShort usAP_RSSI = new LogShort();
    public LogShort usAP_channel = new LogShort();
    public LogShort usLen = new LogShort();
    public LogShort usSubErrorCode = new LogShort();

    public CSegEVENT_WIFI_ABS_ASSOC_FAILED_EVENT() {
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("enWifiConnectAssocFailedReason", Integer.valueOf(1));
        this.fieldMap.put("enWifiConnectAssocFailedReason", this.enWifiConnectAssocFailedReason);
        this.lengthMap.put("usSubErrorCode", Integer.valueOf(2));
        this.fieldMap.put("usSubErrorCode", this.usSubErrorCode);
        this.lengthMap.put("strSTA_MAC", Integer.valueOf(17));
        this.fieldMap.put("strSTA_MAC", this.strSTA_MAC);
        this.lengthMap.put("strAP_MAC", Integer.valueOf(17));
        this.fieldMap.put("strAP_MAC", this.strAP_MAC);
        this.lengthMap.put("strAP_SSID", Integer.valueOf(32));
        this.fieldMap.put("strAP_SSID", this.strAP_SSID);
        this.lengthMap.put("usAP_channel", Integer.valueOf(2));
        this.fieldMap.put("usAP_channel", this.usAP_channel);
        this.lengthMap.put("usAP_RSSI", Integer.valueOf(2));
        this.fieldMap.put("usAP_RSSI", this.usAP_RSSI);
        this.lengthMap.put("ucAP_auth_type", Integer.valueOf(1));
        this.fieldMap.put("ucAP_auth_type", this.ucAP_auth_type);
        this.lengthMap.put("ucSwitchType", Integer.valueOf(1));
        this.fieldMap.put("ucSwitchType", this.ucSwitchType);
        this.enEventId.setValue("WIFI_ABS_ASSOC_FAILED_EVENT");
        this.usLen.setValue(getTotalLen());
    }
}
