package com.android.server.location.gnsschrlog;

public class CSubLos_pos_param extends CSubSv_Status {
    public LogInt iAccuracy = new LogInt();
    public LogInt iSpeed = new LogInt();
    public LogLong lTime = new LogLong();

    public CSubLos_pos_param() {
        this.lengthMap.put("lTime", Integer.valueOf(8));
        this.fieldMap.put("lTime", this.lTime);
        this.lengthMap.put("iSpeed", Integer.valueOf(4));
        this.fieldMap.put("iSpeed", this.iSpeed);
        this.lengthMap.put("iAccuracy", Integer.valueOf(4));
        this.fieldMap.put("iAccuracy", this.iAccuracy);
        this.enSubEventId.setValue("Los_pos_param");
    }
}
