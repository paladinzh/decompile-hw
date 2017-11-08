package com.android.server.location.gnsschrlog;

public class CSegEVENT_CHR_GNSS_HAL_EVENT_INJECT extends ChrLogBaseEventModel {
    public ENCEventId enEventId = new ENCEventId();
    public ENCGpsInjectError enGpsInjectError = new ENCGpsInjectError();
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucCardIndex = new LogByte();
    public LogShort usLen = new LogShort();

    public CSegEVENT_CHR_GNSS_HAL_EVENT_INJECT() {
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("enGpsInjectError", Integer.valueOf(1));
        this.fieldMap.put("enGpsInjectError", this.enGpsInjectError);
        this.enEventId.setValue("CHR_GNSS_HAL_EVENT_INJECT");
        this.usLen.setValue(getTotalLen());
    }
}
