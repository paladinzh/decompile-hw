package com.android.server.location.gnsschrlog;

import java.util.ArrayList;
import java.util.List;

public class CSegEVENT_GEOLOCATION_DATA_COLLECT_EVENT extends ChrLogBaseEventModel {
    public List<CSubCurrentCell> cCurrentCellList = new ArrayList(8);
    public List<CSubNeighborCell> cNeighborCellList = new ArrayList(8);
    public List<CSubWifiApInfo> cWifiApInfoList = new ArrayList(8);
    public ENCEventId enEventId = new ENCEventId();
    public LogInt iAccuracy = new LogInt();
    public LogInt iBearing = new LogInt();
    public LogLong lBootTime = new LogLong();
    public LogLong llocation_time = new LogLong();
    public LogLong lwifi_scaned_time = new LogLong();
    public LogString strLatitude = new LogString(12);
    public LogString strLongitude = new LogString(12);
    public LogString strSpeed = new LogString(12);
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucCardIndex = new LogByte();
    public LogByte ucErrorCode = new LogByte();
    public LogByte ucType = new LogByte();
    public LogShort usLen = new LogShort();

    public void setCSubCurrentCellList(CSubCurrentCell pCurrentCell) {
        if (pCurrentCell != null) {
            this.cCurrentCellList.add(pCurrentCell);
            this.lengthMap.put("cCurrentCellList", Integer.valueOf((((ChrLogBaseModel) this.cCurrentCellList.get(0)).getTotalBytes() * this.cCurrentCellList.size()) + 2));
            this.fieldMap.put("cCurrentCellList", this.cCurrentCellList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubNeighborCellList(CSubNeighborCell pNeighborCell) {
        if (pNeighborCell != null) {
            this.cNeighborCellList.add(pNeighborCell);
            this.lengthMap.put("cNeighborCellList", Integer.valueOf((((ChrLogBaseModel) this.cNeighborCellList.get(0)).getTotalBytes() * this.cNeighborCellList.size()) + 2));
            this.fieldMap.put("cNeighborCellList", this.cNeighborCellList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubWifiApInfoList(CSubWifiApInfo pWifiApInfo) {
        if (pWifiApInfo != null) {
            this.cWifiApInfoList.add(pWifiApInfo);
            this.lengthMap.put("cWifiApInfoList", Integer.valueOf((((ChrLogBaseModel) this.cWifiApInfoList.get(0)).getTotalBytes() * this.cWifiApInfoList.size()) + 2));
            this.fieldMap.put("cWifiApInfoList", this.cWifiApInfoList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public CSegEVENT_GEOLOCATION_DATA_COLLECT_EVENT() {
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("ucErrorCode", Integer.valueOf(1));
        this.fieldMap.put("ucErrorCode", this.ucErrorCode);
        this.lengthMap.put("ucType", Integer.valueOf(1));
        this.fieldMap.put("ucType", this.ucType);
        this.lengthMap.put("strLatitude", Integer.valueOf(12));
        this.fieldMap.put("strLatitude", this.strLatitude);
        this.lengthMap.put("strLongitude", Integer.valueOf(12));
        this.fieldMap.put("strLongitude", this.strLongitude);
        this.lengthMap.put("iAccuracy", Integer.valueOf(4));
        this.fieldMap.put("iAccuracy", this.iAccuracy);
        this.lengthMap.put("iBearing", Integer.valueOf(4));
        this.fieldMap.put("iBearing", this.iBearing);
        this.lengthMap.put("llocation_time", Integer.valueOf(8));
        this.fieldMap.put("llocation_time", this.llocation_time);
        this.lengthMap.put("strSpeed", Integer.valueOf(12));
        this.fieldMap.put("strSpeed", this.strSpeed);
        this.lengthMap.put("cCurrentCellList", Integer.valueOf(2));
        this.fieldMap.put("cCurrentCellList", this.cCurrentCellList);
        this.lengthMap.put("cNeighborCellList", Integer.valueOf(2));
        this.fieldMap.put("cNeighborCellList", this.cNeighborCellList);
        this.lengthMap.put("lwifi_scaned_time", Integer.valueOf(8));
        this.fieldMap.put("lwifi_scaned_time", this.lwifi_scaned_time);
        this.lengthMap.put("cWifiApInfoList", Integer.valueOf(2));
        this.fieldMap.put("cWifiApInfoList", this.cWifiApInfoList);
        this.lengthMap.put("lBootTime", Integer.valueOf(8));
        this.fieldMap.put("lBootTime", this.lBootTime);
        this.enEventId.setValue("GEOLOCATION_DATA_COLLECT_EVENT");
        this.usLen.setValue(getTotalLen());
    }
}
