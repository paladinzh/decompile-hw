package com.android.server.location.gnsschrlog;

public class CSubHiGeo_RM_DOWNLOAD_STATUS extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt iAction = new LogInt();
    public LogInt iAfterSize = new LogInt();
    public LogInt iBeforeSize = new LogInt();
    public LogLong lDownloadTime = new LogLong();
    public LogString strCityName = new LogString(32);
    public LogString strFailureCause = new LogString(32);
    public LogString strSuccessful = new LogString(8);

    public CSubHiGeo_RM_DOWNLOAD_STATUS() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("strCityName", Integer.valueOf(32));
        this.fieldMap.put("strCityName", this.strCityName);
        this.lengthMap.put("iAction", Integer.valueOf(4));
        this.fieldMap.put("iAction", this.iAction);
        this.lengthMap.put("iBeforeSize", Integer.valueOf(4));
        this.fieldMap.put("iBeforeSize", this.iBeforeSize);
        this.lengthMap.put("iAfterSize", Integer.valueOf(4));
        this.fieldMap.put("iAfterSize", this.iAfterSize);
        this.lengthMap.put("strSuccessful", Integer.valueOf(8));
        this.fieldMap.put("strSuccessful", this.strSuccessful);
        this.lengthMap.put("strFailureCause", Integer.valueOf(32));
        this.fieldMap.put("strFailureCause", this.strFailureCause);
        this.lengthMap.put("lDownloadTime", Integer.valueOf(8));
        this.fieldMap.put("lDownloadTime", this.lDownloadTime);
        this.enSubEventId.setValue("HiGeo_RM_DOWNLOAD_STATUS");
    }
}
