package com.android.server.location.gnsschrlog;

public class CSegEVENT_HIGEO_STATISTIC_EVENT extends ChrLogBaseEventModel {
    public CSubHiGeo_PDR_STEP_LENGTH cHiGeo_PDR_STEP_LENGTH = null;
    public CSubHiGeo_RM_DOWNLOAD_STATUS cHiGeo_RM_DOWNLOAD_STATUS = null;
    public ENCEventId enEventId = new ENCEventId();
    public LogLong lStartTime = new LogLong();
    public LogString strLBSversion = new LogString(32);
    public LogString strProviderMode = new LogString(8);
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucCardIndex = new LogByte();
    public LogByte ucErrorCode = new LogByte();
    public LogByte ucPosMode = new LogByte();
    public LogShort usLen = new LogShort();

    public void setCSubHiGeo_RM_DOWNLOAD_STATUS(CSubHiGeo_RM_DOWNLOAD_STATUS pHiGeo_RM_DOWNLOAD_STATUS) {
        if (pHiGeo_RM_DOWNLOAD_STATUS != null) {
            this.cHiGeo_RM_DOWNLOAD_STATUS = pHiGeo_RM_DOWNLOAD_STATUS;
            this.lengthMap.put("cHiGeo_RM_DOWNLOAD_STATUS", Integer.valueOf(this.cHiGeo_RM_DOWNLOAD_STATUS.getTotalBytes()));
            this.fieldMap.put("cHiGeo_RM_DOWNLOAD_STATUS", this.cHiGeo_RM_DOWNLOAD_STATUS);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_PDR_STEP_LENGTH(CSubHiGeo_PDR_STEP_LENGTH pHiGeo_PDR_STEP_LENGTH) {
        if (pHiGeo_PDR_STEP_LENGTH != null) {
            this.cHiGeo_PDR_STEP_LENGTH = pHiGeo_PDR_STEP_LENGTH;
            this.lengthMap.put("cHiGeo_PDR_STEP_LENGTH", Integer.valueOf(this.cHiGeo_PDR_STEP_LENGTH.getTotalBytes()));
            this.fieldMap.put("cHiGeo_PDR_STEP_LENGTH", this.cHiGeo_PDR_STEP_LENGTH);
            this.usLen.setValue(getTotalLen());
        }
    }

    public CSegEVENT_HIGEO_STATISTIC_EVENT() {
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("strProviderMode", Integer.valueOf(8));
        this.fieldMap.put("strProviderMode", this.strProviderMode);
        this.lengthMap.put("lStartTime", Integer.valueOf(8));
        this.fieldMap.put("lStartTime", this.lStartTime);
        this.lengthMap.put("ucPosMode", Integer.valueOf(1));
        this.fieldMap.put("ucPosMode", this.ucPosMode);
        this.lengthMap.put("strLBSversion", Integer.valueOf(32));
        this.fieldMap.put("strLBSversion", this.strLBSversion);
        this.lengthMap.put("ucErrorCode", Integer.valueOf(1));
        this.fieldMap.put("ucErrorCode", this.ucErrorCode);
        this.lengthMap.put("cHiGeo_RM_DOWNLOAD_STATUS", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_RM_DOWNLOAD_STATUS", this.cHiGeo_RM_DOWNLOAD_STATUS);
        this.lengthMap.put("cHiGeo_PDR_STEP_LENGTH", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_PDR_STEP_LENGTH", this.cHiGeo_PDR_STEP_LENGTH);
        this.enEventId.setValue("HIGEO_STATISTIC_EVENT");
        this.usLen.setValue(getTotalLen());
    }
}
