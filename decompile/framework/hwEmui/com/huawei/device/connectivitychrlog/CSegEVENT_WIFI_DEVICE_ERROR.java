package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_DEVICE_ERROR extends ChrLogBaseEventModel {
    public ENCEventId enEventId = new ENCEventId();
    public ENCWifiDeviceErrorReason enWifiDeviceErrorReason = new ENCWifiDeviceErrorReason();
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucCardIndex = new LogByte();
    public LogShort usLen = new LogShort();

    public CSegEVENT_WIFI_DEVICE_ERROR() {
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("enWifiDeviceErrorReason", Integer.valueOf(1));
        this.fieldMap.put("enWifiDeviceErrorReason", this.enWifiDeviceErrorReason);
        this.enEventId.setValue("WIFI_DEVICE_ERROR");
        this.usLen.setValue(getTotalLen());
    }
}
