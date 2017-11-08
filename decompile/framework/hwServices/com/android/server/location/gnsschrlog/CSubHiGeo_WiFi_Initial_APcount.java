package com.android.server.location.gnsschrlog;

public class CSubHiGeo_WiFi_Initial_APcount extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogLong lTimeStamp = new LogLong();
    public LogString strAPcount = new LogString(12);

    public CSubHiGeo_WiFi_Initial_APcount() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("lTimeStamp", Integer.valueOf(8));
        this.fieldMap.put("lTimeStamp", this.lTimeStamp);
        this.lengthMap.put("strAPcount", Integer.valueOf(12));
        this.fieldMap.put("strAPcount", this.strAPcount);
        this.enSubEventId.setValue("HiGeo_WiFi_Initial_APcount");
    }
}
