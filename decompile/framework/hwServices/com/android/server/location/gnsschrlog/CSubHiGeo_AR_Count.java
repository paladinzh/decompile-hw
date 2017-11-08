package com.android.server.location.gnsschrlog;

public class CSubHiGeo_AR_Count extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogLong lin_vehicle = new LogLong();
    public LogLong lon_bicycle = new LogLong();
    public LogLong lrunning = new LogLong();
    public LogLong lstill = new LogLong();
    public LogLong lstop_vehicle = new LogLong();
    public LogLong lwalking = new LogLong();

    public CSubHiGeo_AR_Count() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("lin_vehicle", Integer.valueOf(8));
        this.fieldMap.put("lin_vehicle", this.lin_vehicle);
        this.lengthMap.put("lon_bicycle", Integer.valueOf(8));
        this.fieldMap.put("lon_bicycle", this.lon_bicycle);
        this.lengthMap.put("lwalking", Integer.valueOf(8));
        this.fieldMap.put("lwalking", this.lwalking);
        this.lengthMap.put("lrunning", Integer.valueOf(8));
        this.fieldMap.put("lrunning", this.lrunning);
        this.lengthMap.put("lstill", Integer.valueOf(8));
        this.fieldMap.put("lstill", this.lstill);
        this.lengthMap.put("lstop_vehicle", Integer.valueOf(8));
        this.fieldMap.put("lstop_vehicle", this.lstop_vehicle);
        this.enSubEventId.setValue("HiGeo_AR_Count");
    }
}
