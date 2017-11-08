package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_CLOSE_FAILED_EX extends ChrLogBaseEventModel {
    public LogByteArray aucExt_info = new LogByteArray(8192);
    public ENCCLOSE_FAILED enCLOSE_FAILED = new ENCCLOSE_FAILED();
    public ENCEventId enEventId = new ENCEventId();
    public ENCucHwStatus enucHwStatus = new ENCucHwStatus();
    public LogString strRemark = new LogString(100);
    public LogString strSTA_MAC = new LogString(17);
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucCardIndex = new LogByte();
    public LogShort usLen = new LogShort();
    public LogShort usSubErrorCode = new LogShort();

    public CSegEVENT_WIFI_CLOSE_FAILED_EX() {
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
        this.lengthMap.put("strRemark", Integer.valueOf(100));
        this.fieldMap.put("strRemark", this.strRemark);
        this.lengthMap.put("aucExt_info", Integer.valueOf(8192));
        this.fieldMap.put("aucExt_info", this.aucExt_info);
        this.enEventId.setValue("WIFI_CLOSE_FAILED_EX");
        this.usLen.setValue(getTotalLen());
    }
}
