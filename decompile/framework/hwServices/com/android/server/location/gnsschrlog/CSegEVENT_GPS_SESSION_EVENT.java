package com.android.server.location.gnsschrlog;

import java.util.ArrayList;
import java.util.List;

public class CSegEVENT_GPS_SESSION_EVENT extends ChrLogBaseEventModel {
    public List<CSubApk_Name> cApk_NameList = new ArrayList(8);
    public CSubBrcmPosReferenceInfo cBrcmPosReferenceInfo = null;
    public CSubFixPos_status cFixPos_status = null;
    public CSubHiGeo_AR_Count cHiGeo_AR_Count = null;
    public List<CSubHiGeo_AR_Status> cHiGeo_AR_StatusList = new ArrayList(8);
    public List<CSubHiGeo_GPS_Pos_Status> cHiGeo_GPS_Pos_StatusList = new ArrayList(8);
    public List<CSubHiGeo_GWKF_Pos_Status> cHiGeo_GWKF_Pos_StatusList = new ArrayList(8);
    public CSubHiGeo_MM_Status cHiGeo_MM_Status = null;
    public List<CSubHiGeo_Mode> cHiGeo_ModeList = new ArrayList(8);
    public CSubHiGeo_Scan_Status cHiGeo_Scan_Status = null;
    public List<CSubHiGeo_Vdr_Pos_Status> cHiGeo_Vdr_Pos_StatusList = new ArrayList(8);
    public List<CSubHiGeo_WiFi_Initial_APcount> cHiGeo_WiFi_Initial_APcountList = new ArrayList(8);
    public CSubHiGeo_WiFi_Pos_Count cHiGeo_WiFi_Pos_Count = null;
    public List<CSubHiGeo_WiFi_Pos_Status> cHiGeo_WiFi_Pos_StatusList = new ArrayList(8);
    public List<CSubHiGeo_WiFi_Stationary_Status> cHiGeo_WiFi_Stationary_StatusList = new ArrayList(8);
    public List<CSubLosPos_Status> cLosPos_StatusList = new ArrayList(8);
    public List<CSubResumePos_Status> cResumePos_StatusList = new ArrayList(8);
    public List<CSubVdrDisableTime> cVdrDisableTimeList = new ArrayList(8);
    public List<CSubVdrEnableTime> cVdrEnableTimeList = new ArrayList(8);
    public ENCAppUsedParm enAppUsedParm = new ENCAppUsedParm();
    public ENCEventId enEventId = new ENCEventId();
    public ENCNetworkStatus enNetworkStatus = new ENCNetworkStatus();
    public LogInt iAvgPositionAcc = new LogInt();
    public LogInt iCell_Lac = new LogInt();
    public LogInt iCell_Mcc = new LogInt();
    public LogInt iCell_Mnc = new LogInt();
    public LogInt iFixAccuracy = new LogInt();
    public LogInt iFixSpeed = new LogInt();
    public LogInt iLostPosCnt = new LogInt();
    public LogInt iTTFF = new LogInt();
    public LogLong lCatchSvTime = new LogLong();
    public LogLong lFirstCatchSvTime = new LogLong();
    public LogLong lHiGeo_C_Reroute = new LogLong();
    public LogLong lHiGeo_GPS_Lost_Count = new LogLong();
    public LogLong lHiGeo_GWKF_Start_Count = new LogLong();
    public LogLong lHiGeo_PDR_DR_Count = new LogLong();
    public LogLong lHiGeo_VDR_Start_Count = new LogLong();
    public LogLong lHiGeo_WiFi_Start_Count = new LogLong();
    public LogLong lStartTime = new LogLong();
    public LogLong lStopTime = new LogLong();
    public LogString strDataCall_Switch = new LogString(6);
    public LogString strHiGeo_LBSversion = new LogString(32);
    public LogString strIsGpsdResart = new LogString(6);
    public LogString strIsIssueSession = new LogString(6);
    public LogString strLocSetStatus = new LogString(20);
    public LogString strNetWorkAvailable = new LogString(6);
    public LogString strProviderMode = new LogString(8);
    public LogString strRef_Clk = new LogString(6);
    public LogString strWifi_Switch = new LogString(6);
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucAvgCN0When40KMPH = new LogByte();
    public LogByte ucCardIndex = new LogByte();
    public LogByte ucInjectAiding = new LogByte();
    public LogByte ucPosMode = new LogByte();
    public LogShort usCellN_ID = new LogShort();
    public LogShort usGpsdReStartCnt = new LogShort();
    public LogShort usLen = new LogShort();

    public void setCSubApk_NameList(CSubApk_Name pApk_Name) {
        if (pApk_Name != null) {
            this.cApk_NameList.add(pApk_Name);
            this.lengthMap.put("cApk_NameList", Integer.valueOf((((ChrLogBaseModel) this.cApk_NameList.get(0)).getTotalBytes() * this.cApk_NameList.size()) + 2));
            this.fieldMap.put("cApk_NameList", this.cApk_NameList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubFixPos_status(CSubFixPos_status pFixPos_status) {
        if (pFixPos_status != null) {
            this.cFixPos_status = pFixPos_status;
            this.lengthMap.put("cFixPos_status", Integer.valueOf(this.cFixPos_status.getTotalBytes()));
            this.fieldMap.put("cFixPos_status", this.cFixPos_status);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_AR_StatusList(CSubHiGeo_AR_Status pHiGeo_AR_Status) {
        if (pHiGeo_AR_Status != null) {
            if (this.cHiGeo_AR_StatusList.size() >= 100) {
                this.cHiGeo_AR_StatusList.remove(0);
            }
            this.cHiGeo_AR_StatusList.add(pHiGeo_AR_Status);
            this.lengthMap.put("cHiGeo_AR_StatusList", Integer.valueOf((((ChrLogBaseModel) this.cHiGeo_AR_StatusList.get(0)).getTotalBytes() * this.cHiGeo_AR_StatusList.size()) + 2));
            this.fieldMap.put("cHiGeo_AR_StatusList", this.cHiGeo_AR_StatusList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_WiFi_Stationary_StatusList(CSubHiGeo_WiFi_Stationary_Status pHiGeo_WiFi_Stationary_Status) {
        if (pHiGeo_WiFi_Stationary_Status != null) {
            if (this.cHiGeo_WiFi_Stationary_StatusList.size() > 50) {
                this.cHiGeo_WiFi_Stationary_StatusList.remove(0);
            }
            this.cHiGeo_WiFi_Stationary_StatusList.add(pHiGeo_WiFi_Stationary_Status);
            this.lengthMap.put("cHiGeo_WiFi_Stationary_StatusList", Integer.valueOf((((ChrLogBaseModel) this.cHiGeo_WiFi_Stationary_StatusList.get(0)).getTotalBytes() * this.cHiGeo_WiFi_Stationary_StatusList.size()) + 2));
            this.fieldMap.put("cHiGeo_WiFi_Stationary_StatusList", this.cHiGeo_WiFi_Stationary_StatusList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_ModeList(CSubHiGeo_Mode pHiGeo_Mode) {
        if (pHiGeo_Mode != null) {
            this.cHiGeo_ModeList.add(pHiGeo_Mode);
            this.lengthMap.put("cHiGeo_ModeList", Integer.valueOf((((ChrLogBaseModel) this.cHiGeo_ModeList.get(0)).getTotalBytes() * this.cHiGeo_ModeList.size()) + 2));
            this.fieldMap.put("cHiGeo_ModeList", this.cHiGeo_ModeList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_GPS_Pos_StatusList(CSubHiGeo_GPS_Pos_Status pHiGeo_GPS_Pos_Status) {
        if (pHiGeo_GPS_Pos_Status != null) {
            if (this.cHiGeo_GPS_Pos_StatusList.size() >= 100) {
                this.cHiGeo_GPS_Pos_StatusList.remove(0);
            }
            this.cHiGeo_GPS_Pos_StatusList.add(pHiGeo_GPS_Pos_Status);
            this.lengthMap.put("cHiGeo_GPS_Pos_StatusList", Integer.valueOf((((ChrLogBaseModel) this.cHiGeo_GPS_Pos_StatusList.get(0)).getTotalBytes() * this.cHiGeo_GPS_Pos_StatusList.size()) + 2));
            this.fieldMap.put("cHiGeo_GPS_Pos_StatusList", this.cHiGeo_GPS_Pos_StatusList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_WiFi_Pos_StatusList(CSubHiGeo_WiFi_Pos_Status pHiGeo_WiFi_Pos_Status) {
        if (pHiGeo_WiFi_Pos_Status != null) {
            this.cHiGeo_WiFi_Pos_StatusList.add(pHiGeo_WiFi_Pos_Status);
            this.lengthMap.put("cHiGeo_WiFi_Pos_StatusList", Integer.valueOf((((ChrLogBaseModel) this.cHiGeo_WiFi_Pos_StatusList.get(0)).getTotalBytes() * this.cHiGeo_WiFi_Pos_StatusList.size()) + 2));
            this.fieldMap.put("cHiGeo_WiFi_Pos_StatusList", this.cHiGeo_WiFi_Pos_StatusList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_WiFi_Initial_APcountList(CSubHiGeo_WiFi_Initial_APcount pHiGeo_WiFi_Initial_APcount) {
        if (pHiGeo_WiFi_Initial_APcount != null) {
            this.cHiGeo_WiFi_Initial_APcountList.add(pHiGeo_WiFi_Initial_APcount);
            this.lengthMap.put("cHiGeo_WiFi_Initial_APcountList", Integer.valueOf((((ChrLogBaseModel) this.cHiGeo_WiFi_Initial_APcountList.get(0)).getTotalBytes() * this.cHiGeo_WiFi_Initial_APcountList.size()) + 2));
            this.fieldMap.put("cHiGeo_WiFi_Initial_APcountList", this.cHiGeo_WiFi_Initial_APcountList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_Vdr_Pos_StatusList(CSubHiGeo_Vdr_Pos_Status pHiGeo_Vdr_Pos_Status) {
        if (pHiGeo_Vdr_Pos_Status != null) {
            this.cHiGeo_Vdr_Pos_StatusList.add(pHiGeo_Vdr_Pos_Status);
            this.lengthMap.put("cHiGeo_Vdr_Pos_StatusList", Integer.valueOf((((ChrLogBaseModel) this.cHiGeo_Vdr_Pos_StatusList.get(0)).getTotalBytes() * this.cHiGeo_Vdr_Pos_StatusList.size()) + 2));
            this.fieldMap.put("cHiGeo_Vdr_Pos_StatusList", this.cHiGeo_Vdr_Pos_StatusList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_GWKF_Pos_StatusList(CSubHiGeo_GWKF_Pos_Status pHiGeo_GWKF_Pos_Status) {
        if (pHiGeo_GWKF_Pos_Status != null) {
            if (this.cHiGeo_GWKF_Pos_StatusList.size() >= 100) {
                this.cHiGeo_GWKF_Pos_StatusList.remove(0);
            }
            this.cHiGeo_GWKF_Pos_StatusList.add(pHiGeo_GWKF_Pos_Status);
            this.lengthMap.put("cHiGeo_GWKF_Pos_StatusList", Integer.valueOf((((ChrLogBaseModel) this.cHiGeo_GWKF_Pos_StatusList.get(0)).getTotalBytes() * this.cHiGeo_GWKF_Pos_StatusList.size()) + 2));
            this.fieldMap.put("cHiGeo_GWKF_Pos_StatusList", this.cHiGeo_GWKF_Pos_StatusList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubLosPos_StatusList(CSubLosPos_Status pLosPos_Status) {
        if (pLosPos_Status != null) {
            this.cLosPos_StatusList.add(pLosPos_Status);
            this.lengthMap.put("cLosPos_StatusList", Integer.valueOf((((ChrLogBaseModel) this.cLosPos_StatusList.get(0)).getTotalBytes() * this.cLosPos_StatusList.size()) + 2));
            this.fieldMap.put("cLosPos_StatusList", this.cLosPos_StatusList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubVdrEnableTimeList(CSubVdrEnableTime pVdrEnableTime) {
        if (pVdrEnableTime != null) {
            this.cVdrEnableTimeList.add(pVdrEnableTime);
            this.lengthMap.put("cVdrEnableTimeList", Integer.valueOf((((ChrLogBaseModel) this.cVdrEnableTimeList.get(0)).getTotalBytes() * this.cVdrEnableTimeList.size()) + 2));
            this.fieldMap.put("cVdrEnableTimeList", this.cVdrEnableTimeList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubVdrDisableTimeList(CSubVdrDisableTime pVdrDisableTime) {
        if (pVdrDisableTime != null) {
            this.cVdrDisableTimeList.add(pVdrDisableTime);
            this.lengthMap.put("cVdrDisableTimeList", Integer.valueOf((((ChrLogBaseModel) this.cVdrDisableTimeList.get(0)).getTotalBytes() * this.cVdrDisableTimeList.size()) + 2));
            this.fieldMap.put("cVdrDisableTimeList", this.cVdrDisableTimeList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubResumePos_StatusList(CSubResumePos_Status pResumePos_Status) {
        if (pResumePos_Status != null) {
            this.cResumePos_StatusList.add(pResumePos_Status);
            this.lengthMap.put("cResumePos_StatusList", Integer.valueOf((((ChrLogBaseModel) this.cResumePos_StatusList.get(0)).getTotalBytes() * this.cResumePos_StatusList.size()) + 2));
            this.fieldMap.put("cResumePos_StatusList", this.cResumePos_StatusList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_WiFi_Pos_Count(CSubHiGeo_WiFi_Pos_Count pHiGeo_WiFi_Pos_Count) {
        if (pHiGeo_WiFi_Pos_Count != null) {
            this.cHiGeo_WiFi_Pos_Count = pHiGeo_WiFi_Pos_Count;
            this.lengthMap.put("cHiGeo_WiFi_Pos_Count", Integer.valueOf(this.cHiGeo_WiFi_Pos_Count.getTotalBytes()));
            this.fieldMap.put("cHiGeo_WiFi_Pos_Count", this.cHiGeo_WiFi_Pos_Count);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_AR_Count(CSubHiGeo_AR_Count pHiGeo_AR_Count) {
        if (pHiGeo_AR_Count != null) {
            this.cHiGeo_AR_Count = pHiGeo_AR_Count;
            this.lengthMap.put("cHiGeo_AR_Count", Integer.valueOf(this.cHiGeo_AR_Count.getTotalBytes()));
            this.fieldMap.put("cHiGeo_AR_Count", this.cHiGeo_AR_Count);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_MM_Status(CSubHiGeo_MM_Status pHiGeo_MM_Status) {
        if (pHiGeo_MM_Status != null) {
            this.cHiGeo_MM_Status = pHiGeo_MM_Status;
            this.lengthMap.put("cHiGeo_MM_Status", Integer.valueOf(this.cHiGeo_MM_Status.getTotalBytes()));
            this.fieldMap.put("cHiGeo_MM_Status", this.cHiGeo_MM_Status);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_Scan_Status(CSubHiGeo_Scan_Status pHiGeo_Scan_Status) {
        if (pHiGeo_Scan_Status != null) {
            this.cHiGeo_Scan_Status = pHiGeo_Scan_Status;
            this.lengthMap.put("cHiGeo_Scan_Status", Integer.valueOf(this.cHiGeo_Scan_Status.getTotalBytes()));
            this.fieldMap.put("cHiGeo_Scan_Status", this.cHiGeo_Scan_Status);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubBrcmPosReferenceInfo(CSubBrcmPosReferenceInfo pBrcmPosReferenceInfo) {
        if (pBrcmPosReferenceInfo != null) {
            this.cBrcmPosReferenceInfo = pBrcmPosReferenceInfo;
            this.lengthMap.put("cBrcmPosReferenceInfo", Integer.valueOf(this.cBrcmPosReferenceInfo.getTotalBytes()));
            this.fieldMap.put("cBrcmPosReferenceInfo", this.cBrcmPosReferenceInfo);
            this.usLen.setValue(getTotalLen());
        }
    }

    public CSegEVENT_GPS_SESSION_EVENT() {
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("cApk_NameList", Integer.valueOf(2));
        this.fieldMap.put("cApk_NameList", this.cApk_NameList);
        this.lengthMap.put("strHiGeo_LBSversion", Integer.valueOf(32));
        this.fieldMap.put("strHiGeo_LBSversion", this.strHiGeo_LBSversion);
        this.lengthMap.put("strProviderMode", Integer.valueOf(8));
        this.fieldMap.put("strProviderMode", this.strProviderMode);
        this.lengthMap.put("lStartTime", Integer.valueOf(8));
        this.fieldMap.put("lStartTime", this.lStartTime);
        this.lengthMap.put("ucInjectAiding", Integer.valueOf(1));
        this.fieldMap.put("ucInjectAiding", this.ucInjectAiding);
        this.lengthMap.put("ucPosMode", Integer.valueOf(1));
        this.fieldMap.put("ucPosMode", this.ucPosMode);
        this.lengthMap.put("strLocSetStatus", Integer.valueOf(20));
        this.fieldMap.put("strLocSetStatus", this.strLocSetStatus);
        this.lengthMap.put("enNetworkStatus", Integer.valueOf(1));
        this.fieldMap.put("enNetworkStatus", this.enNetworkStatus);
        this.lengthMap.put("strNetWorkAvailable", Integer.valueOf(6));
        this.fieldMap.put("strNetWorkAvailable", this.strNetWorkAvailable);
        this.lengthMap.put("strWifi_Switch", Integer.valueOf(6));
        this.fieldMap.put("strWifi_Switch", this.strWifi_Switch);
        this.lengthMap.put("strDataCall_Switch", Integer.valueOf(6));
        this.fieldMap.put("strDataCall_Switch", this.strDataCall_Switch);
        this.lengthMap.put("iCell_Mcc", Integer.valueOf(4));
        this.fieldMap.put("iCell_Mcc", this.iCell_Mcc);
        this.lengthMap.put("iCell_Mnc", Integer.valueOf(4));
        this.fieldMap.put("iCell_Mnc", this.iCell_Mnc);
        this.lengthMap.put("iCell_Lac", Integer.valueOf(4));
        this.fieldMap.put("iCell_Lac", this.iCell_Lac);
        this.lengthMap.put("usCellN_ID", Integer.valueOf(2));
        this.fieldMap.put("usCellN_ID", this.usCellN_ID);
        this.lengthMap.put("lFirstCatchSvTime", Integer.valueOf(8));
        this.fieldMap.put("lFirstCatchSvTime", this.lFirstCatchSvTime);
        this.lengthMap.put("ucAvgCN0When40KMPH", Integer.valueOf(1));
        this.fieldMap.put("ucAvgCN0When40KMPH", this.ucAvgCN0When40KMPH);
        this.lengthMap.put("lCatchSvTime", Integer.valueOf(8));
        this.fieldMap.put("lCatchSvTime", this.lCatchSvTime);
        this.lengthMap.put("iFixSpeed", Integer.valueOf(4));
        this.fieldMap.put("iFixSpeed", this.iFixSpeed);
        this.lengthMap.put("iFixAccuracy", Integer.valueOf(4));
        this.fieldMap.put("iFixAccuracy", this.iFixAccuracy);
        this.lengthMap.put("iTTFF", Integer.valueOf(4));
        this.fieldMap.put("iTTFF", this.iTTFF);
        this.lengthMap.put("cFixPos_status", Integer.valueOf(2));
        this.fieldMap.put("cFixPos_status", this.cFixPos_status);
        this.lengthMap.put("iAvgPositionAcc", Integer.valueOf(4));
        this.fieldMap.put("iAvgPositionAcc", this.iAvgPositionAcc);
        this.lengthMap.put("enAppUsedParm", Integer.valueOf(1));
        this.fieldMap.put("enAppUsedParm", this.enAppUsedParm);
        this.lengthMap.put("lHiGeo_C_Reroute", Integer.valueOf(8));
        this.fieldMap.put("lHiGeo_C_Reroute", this.lHiGeo_C_Reroute);
        this.lengthMap.put("cHiGeo_AR_StatusList", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_AR_StatusList", this.cHiGeo_AR_StatusList);
        this.lengthMap.put("cHiGeo_WiFi_Stationary_StatusList", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_WiFi_Stationary_StatusList", this.cHiGeo_WiFi_Stationary_StatusList);
        this.lengthMap.put("cHiGeo_ModeList", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_ModeList", this.cHiGeo_ModeList);
        this.lengthMap.put("cHiGeo_GPS_Pos_StatusList", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_GPS_Pos_StatusList", this.cHiGeo_GPS_Pos_StatusList);
        this.lengthMap.put("cHiGeo_WiFi_Pos_StatusList", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_WiFi_Pos_StatusList", this.cHiGeo_WiFi_Pos_StatusList);
        this.lengthMap.put("cHiGeo_WiFi_Initial_APcountList", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_WiFi_Initial_APcountList", this.cHiGeo_WiFi_Initial_APcountList);
        this.lengthMap.put("cHiGeo_Vdr_Pos_StatusList", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_Vdr_Pos_StatusList", this.cHiGeo_Vdr_Pos_StatusList);
        this.lengthMap.put("cHiGeo_GWKF_Pos_StatusList", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_GWKF_Pos_StatusList", this.cHiGeo_GWKF_Pos_StatusList);
        this.lengthMap.put("cLosPos_StatusList", Integer.valueOf(2));
        this.fieldMap.put("cLosPos_StatusList", this.cLosPos_StatusList);
        this.lengthMap.put("cVdrEnableTimeList", Integer.valueOf(2));
        this.fieldMap.put("cVdrEnableTimeList", this.cVdrEnableTimeList);
        this.lengthMap.put("cVdrDisableTimeList", Integer.valueOf(2));
        this.fieldMap.put("cVdrDisableTimeList", this.cVdrDisableTimeList);
        this.lengthMap.put("cResumePos_StatusList", Integer.valueOf(2));
        this.fieldMap.put("cResumePos_StatusList", this.cResumePos_StatusList);
        this.lengthMap.put("strIsGpsdResart", Integer.valueOf(6));
        this.fieldMap.put("strIsGpsdResart", this.strIsGpsdResart);
        this.lengthMap.put("usGpsdReStartCnt", Integer.valueOf(2));
        this.fieldMap.put("usGpsdReStartCnt", this.usGpsdReStartCnt);
        this.lengthMap.put("strIsIssueSession", Integer.valueOf(6));
        this.fieldMap.put("strIsIssueSession", this.strIsIssueSession);
        this.lengthMap.put("strRef_Clk", Integer.valueOf(6));
        this.fieldMap.put("strRef_Clk", this.strRef_Clk);
        this.lengthMap.put("lStopTime", Integer.valueOf(8));
        this.fieldMap.put("lStopTime", this.lStopTime);
        this.lengthMap.put("cHiGeo_WiFi_Pos_Count", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_WiFi_Pos_Count", this.cHiGeo_WiFi_Pos_Count);
        this.lengthMap.put("iLostPosCnt", Integer.valueOf(4));
        this.fieldMap.put("iLostPosCnt", this.iLostPosCnt);
        this.lengthMap.put("lHiGeo_GPS_Lost_Count", Integer.valueOf(8));
        this.fieldMap.put("lHiGeo_GPS_Lost_Count", this.lHiGeo_GPS_Lost_Count);
        this.lengthMap.put("lHiGeo_WiFi_Start_Count", Integer.valueOf(8));
        this.fieldMap.put("lHiGeo_WiFi_Start_Count", this.lHiGeo_WiFi_Start_Count);
        this.lengthMap.put("lHiGeo_VDR_Start_Count", Integer.valueOf(8));
        this.fieldMap.put("lHiGeo_VDR_Start_Count", this.lHiGeo_VDR_Start_Count);
        this.lengthMap.put("lHiGeo_GWKF_Start_Count", Integer.valueOf(8));
        this.fieldMap.put("lHiGeo_GWKF_Start_Count", this.lHiGeo_GWKF_Start_Count);
        this.lengthMap.put("cHiGeo_AR_Count", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_AR_Count", this.cHiGeo_AR_Count);
        this.lengthMap.put("lHiGeo_PDR_DR_Count", Integer.valueOf(8));
        this.fieldMap.put("lHiGeo_PDR_DR_Count", this.lHiGeo_PDR_DR_Count);
        this.lengthMap.put("cHiGeo_MM_Status", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_MM_Status", this.cHiGeo_MM_Status);
        this.lengthMap.put("cHiGeo_Scan_Status", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_Scan_Status", this.cHiGeo_Scan_Status);
        this.lengthMap.put("cBrcmPosReferenceInfo", Integer.valueOf(2));
        this.fieldMap.put("cBrcmPosReferenceInfo", this.cBrcmPosReferenceInfo);
        this.enEventId.setValue("GPS_SESSION_EVENT");
        this.usLen.setValue(getTotalLen());
    }
}
