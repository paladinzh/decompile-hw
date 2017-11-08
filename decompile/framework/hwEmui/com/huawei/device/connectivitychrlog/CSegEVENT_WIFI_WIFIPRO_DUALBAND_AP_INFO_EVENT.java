package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT extends ChrLogBaseEventModel {
    public ENCEventId enEventId = new ENCEventId();
    public LogString strBSSID_2G = new LogString(17);
    public LogString strBSSID_5G = new LogString(17);
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucCardIndex = new LogByte();
    public LogByte ucSingleOrMixed = new LogByte();
    public LogShort usLen = new LogShort();

    public CSegEVENT_WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT() {
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("ucSingleOrMixed", Integer.valueOf(1));
        this.fieldMap.put("ucSingleOrMixed", this.ucSingleOrMixed);
        this.lengthMap.put("strBSSID_2G", Integer.valueOf(17));
        this.fieldMap.put("strBSSID_2G", this.strBSSID_2G);
        this.lengthMap.put("strBSSID_5G", Integer.valueOf(17));
        this.fieldMap.put("strBSSID_5G", this.strBSSID_5G);
        this.enEventId.setValue("WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT");
        this.usLen.setValue(getTotalLen());
    }
}
