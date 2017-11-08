package com.android.server.location.gnsschrlog;

public class CSubWifiApInfo extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt iAP_RSSI = new LogInt();
    public LogLong lAP_ScanTime = new LogLong();
    public LogString strAP_Bssid = new LogString(17);
    public LogString strAP_SSID = new LogString(32);

    public CSubWifiApInfo() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("strAP_Bssid", Integer.valueOf(17));
        this.fieldMap.put("strAP_Bssid", this.strAP_Bssid);
        this.lengthMap.put("strAP_SSID", Integer.valueOf(32));
        this.fieldMap.put("strAP_SSID", this.strAP_SSID);
        this.lengthMap.put("iAP_RSSI", Integer.valueOf(4));
        this.fieldMap.put("iAP_RSSI", this.iAP_RSSI);
        this.lengthMap.put("lAP_ScanTime", Integer.valueOf(8));
        this.fieldMap.put("lAP_ScanTime", this.lAP_ScanTime);
        this.enSubEventId.setValue("WifiApInfo");
    }
}
