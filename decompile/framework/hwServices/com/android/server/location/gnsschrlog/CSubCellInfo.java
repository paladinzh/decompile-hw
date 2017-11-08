package com.android.server.location.gnsschrlog;

public class CSubCellInfo extends ChrLogBaseEventModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt iCell_ID = new LogInt();
    public LogInt iCell_Lac = new LogInt();
    public LogInt iCell_Mcc = new LogInt();
    public LogInt iCell_Mnc = new LogInt();
    public LogInt iChannel_Number = new LogInt();
    public LogInt iPhysical_Identity = new LogInt();
    public LogInt iRAT = new LogInt();
    public LogInt iSignal_Strength = new LogInt();

    public CSubCellInfo() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("iCell_Mcc", Integer.valueOf(4));
        this.fieldMap.put("iCell_Mcc", this.iCell_Mcc);
        this.lengthMap.put("iCell_Mnc", Integer.valueOf(4));
        this.fieldMap.put("iCell_Mnc", this.iCell_Mnc);
        this.lengthMap.put("iCell_Lac", Integer.valueOf(4));
        this.fieldMap.put("iCell_Lac", this.iCell_Lac);
        this.lengthMap.put("iCell_ID", Integer.valueOf(4));
        this.fieldMap.put("iCell_ID", this.iCell_ID);
        this.lengthMap.put("iSignal_Strength", Integer.valueOf(4));
        this.fieldMap.put("iSignal_Strength", this.iSignal_Strength);
        this.lengthMap.put("iRAT", Integer.valueOf(4));
        this.fieldMap.put("iRAT", this.iRAT);
        this.lengthMap.put("iChannel_Number", Integer.valueOf(4));
        this.fieldMap.put("iChannel_Number", this.iChannel_Number);
        this.lengthMap.put("iPhysical_Identity", Integer.valueOf(4));
        this.fieldMap.put("iPhysical_Identity", this.iPhysical_Identity);
        this.enSubEventId.setValue("CellInfo");
    }
}
