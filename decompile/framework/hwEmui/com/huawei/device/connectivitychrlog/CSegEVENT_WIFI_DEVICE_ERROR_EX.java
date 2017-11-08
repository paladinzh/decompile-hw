package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_DEVICE_ERROR_EX extends ChrLogBaseEventModel {
    public LogByteArray aucExt_info = new LogByteArray(8192);
    public ENCEventId enEventId = new ENCEventId();
    public ENCWifiDeviceErrorReason enWifiDeviceErrorReason = new ENCWifiDeviceErrorReason();
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucCardIndex = new LogByte();
    public LogShort usLen = new LogShort();

    public CSegEVENT_WIFI_DEVICE_ERROR_EX() {
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
        this.lengthMap.put("aucExt_info", Integer.valueOf(8192));
        this.fieldMap.put("aucExt_info", this.aucExt_info);
        this.enEventId.setValue("WIFI_DEVICE_ERROR_EX");
        this.usLen.setValue(getTotalLen());
    }
}
