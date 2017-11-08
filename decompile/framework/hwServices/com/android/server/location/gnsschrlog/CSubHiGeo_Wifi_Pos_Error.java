package com.android.server.location.gnsschrlog;

public class CSubHiGeo_Wifi_Pos_Error extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogLong lRecover_Time = new LogLong();
    public LogLong lStart_Time = new LogLong();
    public LogString strCause = new LogString(32);

    public CSubHiGeo_Wifi_Pos_Error() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("lStart_Time", Integer.valueOf(8));
        this.fieldMap.put("lStart_Time", this.lStart_Time);
        this.lengthMap.put("lRecover_Time", Integer.valueOf(8));
        this.fieldMap.put("lRecover_Time", this.lRecover_Time);
        this.lengthMap.put("strCause", Integer.valueOf(32));
        this.fieldMap.put("strCause", this.strCause);
        this.enSubEventId.setValue("HiGeo_Wifi_Pos_Error");
    }
}
