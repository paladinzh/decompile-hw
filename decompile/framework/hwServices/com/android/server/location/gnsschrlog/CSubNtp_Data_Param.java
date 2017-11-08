package com.android.server.location.gnsschrlog;

public class CSubNtp_Data_Param extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogLong lNtp_Time = new LogLong();
    public LogLong lReal_Time = new LogLong();
    public LogString strNtp_IpAddr = new LogString(100);

    public CSubNtp_Data_Param() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("lReal_Time", Integer.valueOf(8));
        this.fieldMap.put("lReal_Time", this.lReal_Time);
        this.lengthMap.put("lNtp_Time", Integer.valueOf(8));
        this.fieldMap.put("lNtp_Time", this.lNtp_Time);
        this.lengthMap.put("strNtp_IpAddr", Integer.valueOf(100));
        this.fieldMap.put("strNtp_IpAddr", this.strNtp_IpAddr);
        this.enSubEventId.setValue("Ntp_Data_Param");
    }
}
