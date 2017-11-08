package com.android.server.location.gnsschrlog;

public class CSubResumePos_Status extends CSubSv_Status {
    public LogInt iResumePosAccuracy = new LogInt();
    public LogInt iResumePosSpeed = new LogInt();
    public LogLong lResumePosTime = new LogLong();

    public CSubResumePos_Status() {
        this.lengthMap.put("lResumePosTime", Integer.valueOf(8));
        this.fieldMap.put("lResumePosTime", this.lResumePosTime);
        this.lengthMap.put("iResumePosSpeed", Integer.valueOf(4));
        this.fieldMap.put("iResumePosSpeed", this.iResumePosSpeed);
        this.lengthMap.put("iResumePosAccuracy", Integer.valueOf(4));
        this.fieldMap.put("iResumePosAccuracy", this.iResumePosAccuracy);
        this.enSubEventId.setValue("ResumePos_Status");
    }
}
