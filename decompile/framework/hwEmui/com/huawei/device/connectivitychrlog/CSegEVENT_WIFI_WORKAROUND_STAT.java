package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_WORKAROUND_STAT extends ChrLogBaseEventModel {
    public ENCEventId enEventId = new ENCEventId();
    public LogString strAP_MAC = new LogString(17);
    public LogString strAP_SSID = new LogString(32);
    public LogString strRemark = new LogString(32);
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucCardIndex = new LogByte();
    public LogShort usLen = new LogShort();
    public LogShort usSubErrorCode = new LogShort();
    public LogShort usWork_Ret = new LogShort();
    public LogShort usWork_Status = new LogShort();

    public CSegEVENT_WIFI_WORKAROUND_STAT() {
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("usSubErrorCode", Integer.valueOf(2));
        this.fieldMap.put("usSubErrorCode", this.usSubErrorCode);
        this.lengthMap.put("strAP_MAC", Integer.valueOf(17));
        this.fieldMap.put("strAP_MAC", this.strAP_MAC);
        this.lengthMap.put("strAP_SSID", Integer.valueOf(32));
        this.fieldMap.put("strAP_SSID", this.strAP_SSID);
        this.lengthMap.put("usWork_Ret", Integer.valueOf(2));
        this.fieldMap.put("usWork_Ret", this.usWork_Ret);
        this.lengthMap.put("usWork_Status", Integer.valueOf(2));
        this.fieldMap.put("usWork_Status", this.usWork_Status);
        this.lengthMap.put("strRemark", Integer.valueOf(32));
        this.fieldMap.put("strRemark", this.strRemark);
        this.enEventId.setValue("WIFI_WORKAROUND_STAT");
        this.usLen.setValue(getTotalLen());
    }
}
