package com.android.server.location.gnsschrlog;

public class CSegEVENT_NETWK_POS_TIMEOUT_EVENT extends ChrLogBaseEventModel {
    public ENCEventId enEventId = new ENCEventId();
    public LogString strApkName = new LogString(50);
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucCardIndex = new LogByte();
    public LogByte ucErrorCode = new LogByte();
    public LogByte ucLocSetStatus = new LogByte();
    public LogByte ucNetworkStatus = new LogByte();
    public LogShort usLen = new LogShort();

    public CSegEVENT_NETWK_POS_TIMEOUT_EVENT() {
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("ucErrorCode", Integer.valueOf(1));
        this.fieldMap.put("ucErrorCode", this.ucErrorCode);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucLocSetStatus", Integer.valueOf(1));
        this.fieldMap.put("ucLocSetStatus", this.ucLocSetStatus);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("ucNetworkStatus", Integer.valueOf(1));
        this.fieldMap.put("ucNetworkStatus", this.ucNetworkStatus);
        this.lengthMap.put("strApkName", Integer.valueOf(50));
        this.fieldMap.put("strApkName", this.strApkName);
        this.enEventId.setValue("NETWK_POS_TIMEOUT_EVENT");
        this.usLen.setValue(getTotalLen());
    }
}
