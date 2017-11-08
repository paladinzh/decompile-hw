package com.android.server.location.gnsschrlog;

public class CSubHiGeo_MM_Status extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogString strAMAP_version = new LogString(32);
    public LogString strFLP_version = new LogString(32);
    public LogString strsend_binder = new LogString(8);
    public LogString strsend_get_location_source = new LogString(8);

    public CSubHiGeo_MM_Status() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("strsend_binder", Integer.valueOf(8));
        this.fieldMap.put("strsend_binder", this.strsend_binder);
        this.lengthMap.put("strsend_get_location_source", Integer.valueOf(8));
        this.fieldMap.put("strsend_get_location_source", this.strsend_get_location_source);
        this.lengthMap.put("strAMAP_version", Integer.valueOf(32));
        this.fieldMap.put("strAMAP_version", this.strAMAP_version);
        this.lengthMap.put("strFLP_version", Integer.valueOf(32));
        this.fieldMap.put("strFLP_version", this.strFLP_version);
        this.enSubEventId.setValue("HiGeo_MM_Status");
    }
}
