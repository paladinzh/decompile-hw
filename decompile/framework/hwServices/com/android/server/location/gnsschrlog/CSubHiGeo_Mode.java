package com.android.server.location.gnsschrlog;

public class CSubHiGeo_Mode extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogLong lSwitchDistance = new LogLong();
    public LogLong lTimeStamp = new LogLong();
    public LogByte ucSwitchCause = new LogByte();
    public LogByte ucmode = new LogByte();

    public CSubHiGeo_Mode() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("lTimeStamp", Integer.valueOf(8));
        this.fieldMap.put("lTimeStamp", this.lTimeStamp);
        this.lengthMap.put("ucmode", Integer.valueOf(1));
        this.fieldMap.put("ucmode", this.ucmode);
        this.lengthMap.put("ucSwitchCause", Integer.valueOf(1));
        this.fieldMap.put("ucSwitchCause", this.ucSwitchCause);
        this.lengthMap.put("lSwitchDistance", Integer.valueOf(8));
        this.fieldMap.put("lSwitchDistance", this.lSwitchDistance);
        this.enSubEventId.setValue("HiGeo_Mode");
    }
}
