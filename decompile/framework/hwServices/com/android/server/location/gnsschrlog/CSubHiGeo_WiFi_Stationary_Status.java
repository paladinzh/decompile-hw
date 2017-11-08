package com.android.server.location.gnsschrlog;

public class CSubHiGeo_WiFi_Stationary_Status extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogLong lTimeStamp = new LogLong();
    public LogByte ucactivity = new LogByte();

    public CSubHiGeo_WiFi_Stationary_Status() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("lTimeStamp", Integer.valueOf(8));
        this.fieldMap.put("lTimeStamp", this.lTimeStamp);
        this.lengthMap.put("ucactivity", Integer.valueOf(1));
        this.fieldMap.put("ucactivity", this.ucactivity);
        this.enSubEventId.setValue("HiGeo_WiFi_Stationary_Status");
    }
}
