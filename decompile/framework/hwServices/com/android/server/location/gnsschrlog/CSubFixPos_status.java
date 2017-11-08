package com.android.server.location.gnsschrlog;

public class CSubFixPos_status extends CSubSv_Status {
    public LogLong lFixTime = new LogLong();

    public CSubFixPos_status() {
        this.lengthMap.put("lFixTime", Integer.valueOf(8));
        this.fieldMap.put("lFixTime", this.lFixTime);
        this.enSubEventId.setValue("FixPos_status");
    }
}
