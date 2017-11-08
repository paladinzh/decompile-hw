package com.android.server.location.gnsschrlog;

public class CSubHiGeo_WiFi_Pos_Count extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogLong lCache = new LogLong();
    public LogLong lFusion = new LogLong();
    public LogLong lOffline = new LogLong();
    public LogLong lOnline = new LogLong();

    public CSubHiGeo_WiFi_Pos_Count() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("lOnline", Integer.valueOf(8));
        this.fieldMap.put("lOnline", this.lOnline);
        this.lengthMap.put("lOffline", Integer.valueOf(8));
        this.fieldMap.put("lOffline", this.lOffline);
        this.lengthMap.put("lFusion", Integer.valueOf(8));
        this.fieldMap.put("lFusion", this.lFusion);
        this.lengthMap.put("lCache", Integer.valueOf(8));
        this.fieldMap.put("lCache", this.lCache);
        this.enSubEventId.setValue("HiGeo_WiFi_Pos_Count");
    }
}
