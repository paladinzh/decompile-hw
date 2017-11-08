package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_REPEATER_OPEN_OR_CLOSE_FAILED extends ChrLogBaseEventModel {
    public ENCEventId enEventId = new ENCEventId();
    public ENCWIFI_REPEATER_OPEN_OR_CLOSE_FAILED_REASON enWIFI_REPEATER_OPEN_OR_CLOSE_FAILED_REASON = new ENCWIFI_REPEATER_OPEN_OR_CLOSE_FAILED_REASON();
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucCardIndex = new LogByte();
    public LogByte ucOpenOrClose = new LogByte();
    public LogShort usLen = new LogShort();

    public CSegEVENT_WIFI_REPEATER_OPEN_OR_CLOSE_FAILED() {
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("ucOpenOrClose", Integer.valueOf(1));
        this.fieldMap.put("ucOpenOrClose", this.ucOpenOrClose);
        this.lengthMap.put("enWIFI_REPEATER_OPEN_OR_CLOSE_FAILED_REASON", Integer.valueOf(1));
        this.fieldMap.put("enWIFI_REPEATER_OPEN_OR_CLOSE_FAILED_REASON", this.enWIFI_REPEATER_OPEN_OR_CLOSE_FAILED_REASON);
        this.enEventId.setValue("WIFI_REPEATER_OPEN_OR_CLOSE_FAILED");
        this.usLen.setValue(getTotalLen());
    }
}
