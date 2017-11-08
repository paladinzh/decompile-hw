package com.android.server.location.gnsschrlog;

public class CSubHiGeo_Wifi_Scan_Error extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogLong lStuck_Time = new LogLong();
    public LogString strWlan_status = new LogString(32);

    public CSubHiGeo_Wifi_Scan_Error() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("lStuck_Time", Integer.valueOf(8));
        this.fieldMap.put("lStuck_Time", this.lStuck_Time);
        this.lengthMap.put("strWlan_status", Integer.valueOf(32));
        this.fieldMap.put("strWlan_status", this.strWlan_status);
        this.enSubEventId.setValue("HiGeo_Wifi_Scan_Error");
    }
}
