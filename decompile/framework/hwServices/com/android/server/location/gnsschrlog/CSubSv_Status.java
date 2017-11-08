package com.android.server.location.gnsschrlog;

public class CSubSv_Status extends ChrLogBaseEventModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt iSvCount = new LogInt();
    public LogInt iUsedSvCount = new LogInt();
    public LogString strSvInfo = new LogString(256);

    public CSubSv_Status() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("iSvCount", Integer.valueOf(4));
        this.fieldMap.put("iSvCount", this.iSvCount);
        this.lengthMap.put("iUsedSvCount", Integer.valueOf(4));
        this.fieldMap.put("iUsedSvCount", this.iUsedSvCount);
        this.lengthMap.put("strSvInfo", Integer.valueOf(256));
        this.fieldMap.put("strSvInfo", this.strSvInfo);
        this.enSubEventId.setValue("Sv_Status");
    }
}
