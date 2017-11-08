package com.android.server.location.gnsschrlog;

public class CSubHiGeo_PDR_STEP_LENGTH extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt iARState = new LogInt();
    public LogLong lGPS_distance = new LogLong();
    public LogLong lPDR_step_count = new LogLong();

    public CSubHiGeo_PDR_STEP_LENGTH() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("iARState", Integer.valueOf(4));
        this.fieldMap.put("iARState", this.iARState);
        this.lengthMap.put("lGPS_distance", Integer.valueOf(8));
        this.fieldMap.put("lGPS_distance", this.lGPS_distance);
        this.lengthMap.put("lPDR_step_count", Integer.valueOf(8));
        this.fieldMap.put("lPDR_step_count", this.lPDR_step_count);
        this.enSubEventId.setValue("HiGeo_PDR_STEP_LENGTH");
    }
}
