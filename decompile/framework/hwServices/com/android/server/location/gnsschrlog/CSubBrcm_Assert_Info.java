package com.android.server.location.gnsschrlog;

public class CSubBrcm_Assert_Info extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogString strAssertInfo = new LogString(200);

    public CSubBrcm_Assert_Info() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("strAssertInfo", Integer.valueOf(200));
        this.fieldMap.put("strAssertInfo", this.strAssertInfo);
        this.enSubEventId.setValue("Brcm_Assert_Info");
    }
}
