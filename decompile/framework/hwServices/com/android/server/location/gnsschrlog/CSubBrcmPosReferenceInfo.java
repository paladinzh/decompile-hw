package com.android.server.location.gnsschrlog;

public class CSubBrcmPosReferenceInfo extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogLong lTcxo_Offset = new LogLong();
    public LogString strAgc_BDS = new LogString(5);
    public LogString strAgc_GLO = new LogString(5);
    public LogString strAgc_GPS = new LogString(5);
    public LogByte ucAidingStatus = new LogByte();
    public LogByte ucPosSource = new LogByte();
    public LogByte ucTimeSource = new LogByte();

    public CSubBrcmPosReferenceInfo() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("strAgc_GPS", Integer.valueOf(5));
        this.fieldMap.put("strAgc_GPS", this.strAgc_GPS);
        this.lengthMap.put("strAgc_GLO", Integer.valueOf(5));
        this.fieldMap.put("strAgc_GLO", this.strAgc_GLO);
        this.lengthMap.put("strAgc_BDS", Integer.valueOf(5));
        this.fieldMap.put("strAgc_BDS", this.strAgc_BDS);
        this.lengthMap.put("lTcxo_Offset", Integer.valueOf(8));
        this.fieldMap.put("lTcxo_Offset", this.lTcxo_Offset);
        this.lengthMap.put("ucPosSource", Integer.valueOf(1));
        this.fieldMap.put("ucPosSource", this.ucPosSource);
        this.lengthMap.put("ucTimeSource", Integer.valueOf(1));
        this.fieldMap.put("ucTimeSource", this.ucTimeSource);
        this.lengthMap.put("ucAidingStatus", Integer.valueOf(1));
        this.fieldMap.put("ucAidingStatus", this.ucAidingStatus);
        this.enSubEventId.setValue("BrcmPosReferenceInfo");
    }
}
