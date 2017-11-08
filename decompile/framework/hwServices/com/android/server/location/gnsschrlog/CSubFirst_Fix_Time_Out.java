package com.android.server.location.gnsschrlog;

public class CSubFirst_Fix_Time_Out extends CSubSv_Status {
    public LogLong lTime = new LogLong();
    public LogByte ucInjectAiding = new LogByte();

    public CSubFirst_Fix_Time_Out() {
        this.lengthMap.put("lTime", Integer.valueOf(8));
        this.fieldMap.put("lTime", this.lTime);
        this.lengthMap.put("ucInjectAiding", Integer.valueOf(1));
        this.fieldMap.put("ucInjectAiding", this.ucInjectAiding);
        this.enSubEventId.setValue("First_Fix_Time_Out");
    }
}
