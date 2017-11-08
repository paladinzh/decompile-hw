package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_CLOSE_FAILED extends ChrLogBaseEventModel {
    public ENCCLOSE_FAILED enCLOSE_FAILED = new ENCCLOSE_FAILED();
    public ENCEventId enEventId = new ENCEventId();
    public ENCucHwStatus enucHwStatus = new ENCucHwStatus();
    public LogString strRemark = new LogString(100);
    public LogString strSTA_MAC = new LogString(17);
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucBTConnState = new LogByte();
    public LogByte ucBTState = new LogByte();
    public LogByte ucCardIndex = new LogByte();
    public LogByte ucIsMobleAP = new LogByte();
    public LogByte ucIsOnScreen = new LogByte();
    public LogByte ucScanAlwaysAvailble = new LogByte();
    public LogByte ucWIFIAlwaysNotifation = new LogByte();
    public LogByte ucWIFISleepPolicy = new LogByte();
    public LogByte ucWifiProStatus = new LogByte();
    public LogByte ucWifiToPDP = new LogByte();
    public LogShort usLen = new LogShort();
    public LogShort usSubErrorCode = new LogShort();

    public CSegEVENT_WIFI_CLOSE_FAILED() {
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("enCLOSE_FAILED", Integer.valueOf(1));
        this.fieldMap.put("enCLOSE_FAILED", this.enCLOSE_FAILED);
        this.lengthMap.put("usSubErrorCode", Integer.valueOf(2));
        this.fieldMap.put("usSubErrorCode", this.usSubErrorCode);
        this.lengthMap.put("strSTA_MAC", Integer.valueOf(17));
        this.fieldMap.put("strSTA_MAC", this.strSTA_MAC);
        this.lengthMap.put("enucHwStatus", Integer.valueOf(1));
        this.fieldMap.put("enucHwStatus", this.enucHwStatus);
        this.lengthMap.put("ucBTState", Integer.valueOf(1));
        this.fieldMap.put("ucBTState", this.ucBTState);
        this.lengthMap.put("ucBTConnState", Integer.valueOf(1));
        this.fieldMap.put("ucBTConnState", this.ucBTConnState);
        this.lengthMap.put("strRemark", Integer.valueOf(100));
        this.fieldMap.put("strRemark", this.strRemark);
        this.lengthMap.put("ucScanAlwaysAvailble", Integer.valueOf(1));
        this.fieldMap.put("ucScanAlwaysAvailble", this.ucScanAlwaysAvailble);
        this.lengthMap.put("ucWIFIAlwaysNotifation", Integer.valueOf(1));
        this.fieldMap.put("ucWIFIAlwaysNotifation", this.ucWIFIAlwaysNotifation);
        this.lengthMap.put("ucWIFISleepPolicy", Integer.valueOf(1));
        this.fieldMap.put("ucWIFISleepPolicy", this.ucWIFISleepPolicy);
        this.lengthMap.put("ucWifiProStatus", Integer.valueOf(1));
        this.fieldMap.put("ucWifiProStatus", this.ucWifiProStatus);
        this.lengthMap.put("ucWifiToPDP", Integer.valueOf(1));
        this.fieldMap.put("ucWifiToPDP", this.ucWifiToPDP);
        this.lengthMap.put("ucIsMobleAP", Integer.valueOf(1));
        this.fieldMap.put("ucIsMobleAP", this.ucIsMobleAP);
        this.lengthMap.put("ucIsOnScreen", Integer.valueOf(1));
        this.fieldMap.put("ucIsOnScreen", this.ucIsOnScreen);
        this.enEventId.setValue("WIFI_CLOSE_FAILED");
        this.usLen.setValue(getTotalLen());
    }
}
