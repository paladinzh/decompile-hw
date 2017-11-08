package com.android.server.location.gnsschrlog;

public class CSubHiGeo_Vdr_Pos_Status extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogLong lTimeStamp = new LogLong();
    public LogString strVdrAcc = new LogString(12);
    public LogByte ucstatus = new LogByte();

    public CSubHiGeo_Vdr_Pos_Status() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("lTimeStamp", Integer.valueOf(8));
        this.fieldMap.put("lTimeStamp", this.lTimeStamp);
        this.lengthMap.put("ucstatus", Integer.valueOf(1));
        this.fieldMap.put("ucstatus", this.ucstatus);
        this.lengthMap.put("strVdrAcc", Integer.valueOf(12));
        this.fieldMap.put("strVdrAcc", this.strVdrAcc);
        this.enSubEventId.setValue("HiGeo_Vdr_Pos_Status");
    }
}
