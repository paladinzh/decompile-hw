package com.android.server.location.gnsschrlog;

import java.util.ArrayList;
import java.util.List;

public class CSegEVENT_GPS_POS_ERROR_EVENT extends ChrLogBaseEventModel {
    public List<CSubApk_Name> cApk_NameList = new ArrayList(8);
    public CSubBrcm_Assert_Info cBrcm_Assert_Info = null;
    public CSubData_Delivery_Delay cData_Delivery_Delay = null;
    public CSubFirst_Fix_Time_Out cFirst_Fix_Time_Out = null;
    public CSubHiGeo_AR_Status cHiGeo_AR_Status = null;
    public CSubHiGeo_Mode cHiGeo_Mode = null;
    public CSubHiGeo_WiFi_Pos_Status cHiGeo_WiFi_Pos_Status = null;
    public CSubHiGeo_Wifi_Pos_Error cHiGeo_Wifi_Pos_Error = null;
    public CSubHiGeo_Wifi_Scan_Error cHiGeo_Wifi_Scan_Error = null;
    public CSubLos_pos_param cLos_pos_param = null;
    public CSubNetwork_Pos_Timeout cNetwork_Pos_Timeout = null;
    public CSubNtp_Data_Param cNtp_Data_Param = null;
    public CSubPdr_Pos_Error cPdr_Pos_Error = null;
    public ENCEventId enEventId = new ENCEventId();
    public ENCNetworkStatus enNetworkStatus = new ENCNetworkStatus();
    public ENCScreen_Orientation enScreen_Orientation = new ENCScreen_Orientation();
    public LogInt iCell_Baseid = new LogInt();
    public LogInt iCell_Cid = new LogInt();
    public LogInt iCell_Lac = new LogInt();
    public LogInt iCell_Mcc = new LogInt();
    public LogInt iCell_Mnc = new LogInt();
    public LogInt iWifi_Service_Init_Fail_Type = new LogInt();
    public LogLong lStartTime = new LogLong();
    public LogString strConnect_WifiPos_Service_Error = new LogString(32);
    public LogString strDataCall_Switch = new LogString(6);
    public LogString strHiGeo_LBSversion = new LogString(32);
    public LogString strLocSetStatus = new LogString(20);
    public LogString strNetWorkAvailable = new LogString(6);
    public LogString strProviderMode = new LogString(8);
    public LogString strScreenState = new LogString(6);
    public LogString strWifi_Bssid = new LogString(17);
    public LogString strWifi_Ssid = new LogString(32);
    public LogString strWifi_Switch = new LogString(6);
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucBT_Switch = new LogByte();
    public LogByte ucCardIndex = new LogByte();
    public LogByte ucErrorCode = new LogByte();
    public LogByte ucNFC_Switch = new LogByte();
    public LogByte ucPosMode = new LogByte();
    public LogByte ucUSB_State = new LogByte();
    public LogShort usCellN_ID = new LogShort();
    public LogShort usCell_SID = new LogShort();
    public LogShort usLen = new LogShort();

    public void setCSubApk_NameList(CSubApk_Name pApk_Name) {
        if (pApk_Name != null) {
            this.cApk_NameList.add(pApk_Name);
            this.lengthMap.put("cApk_NameList", Integer.valueOf((((ChrLogBaseModel) this.cApk_NameList.get(0)).getTotalBytes() * this.cApk_NameList.size()) + 2));
            this.fieldMap.put("cApk_NameList", this.cApk_NameList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_AR_Status(CSubHiGeo_AR_Status pHiGeo_AR_Status) {
        if (pHiGeo_AR_Status != null) {
            this.cHiGeo_AR_Status = pHiGeo_AR_Status;
            this.lengthMap.put("cHiGeo_AR_Status", Integer.valueOf(this.cHiGeo_AR_Status.getTotalBytes()));
            this.fieldMap.put("cHiGeo_AR_Status", this.cHiGeo_AR_Status);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_Mode(CSubHiGeo_Mode pHiGeo_Mode) {
        if (pHiGeo_Mode != null) {
            this.cHiGeo_Mode = pHiGeo_Mode;
            this.lengthMap.put("cHiGeo_Mode", Integer.valueOf(this.cHiGeo_Mode.getTotalBytes()));
            this.fieldMap.put("cHiGeo_Mode", this.cHiGeo_Mode);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_WiFi_Pos_Status(CSubHiGeo_WiFi_Pos_Status pHiGeo_WiFi_Pos_Status) {
        if (pHiGeo_WiFi_Pos_Status != null) {
            this.cHiGeo_WiFi_Pos_Status = pHiGeo_WiFi_Pos_Status;
            this.lengthMap.put("cHiGeo_WiFi_Pos_Status", Integer.valueOf(this.cHiGeo_WiFi_Pos_Status.getTotalBytes()));
            this.fieldMap.put("cHiGeo_WiFi_Pos_Status", this.cHiGeo_WiFi_Pos_Status);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubNtp_Data_Param(CSubNtp_Data_Param pNtp_Data_Param) {
        if (pNtp_Data_Param != null) {
            this.cNtp_Data_Param = pNtp_Data_Param;
            this.lengthMap.put("cNtp_Data_Param", Integer.valueOf(this.cNtp_Data_Param.getTotalBytes()));
            this.fieldMap.put("cNtp_Data_Param", this.cNtp_Data_Param);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubLos_pos_param(CSubLos_pos_param pLos_pos_param) {
        if (pLos_pos_param != null) {
            this.cLos_pos_param = pLos_pos_param;
            this.lengthMap.put("cLos_pos_param", Integer.valueOf(this.cLos_pos_param.getTotalBytes()));
            this.fieldMap.put("cLos_pos_param", this.cLos_pos_param);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubFirst_Fix_Time_Out(CSubFirst_Fix_Time_Out pFirst_Fix_Time_Out) {
        if (pFirst_Fix_Time_Out != null) {
            this.cFirst_Fix_Time_Out = pFirst_Fix_Time_Out;
            this.lengthMap.put("cFirst_Fix_Time_Out", Integer.valueOf(this.cFirst_Fix_Time_Out.getTotalBytes()));
            this.fieldMap.put("cFirst_Fix_Time_Out", this.cFirst_Fix_Time_Out);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubData_Delivery_Delay(CSubData_Delivery_Delay pData_Delivery_Delay) {
        if (pData_Delivery_Delay != null) {
            this.cData_Delivery_Delay = pData_Delivery_Delay;
            this.lengthMap.put("cData_Delivery_Delay", Integer.valueOf(this.cData_Delivery_Delay.getTotalBytes()));
            this.fieldMap.put("cData_Delivery_Delay", this.cData_Delivery_Delay);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubNetwork_Pos_Timeout(CSubNetwork_Pos_Timeout pNetwork_Pos_Timeout) {
        if (pNetwork_Pos_Timeout != null) {
            this.cNetwork_Pos_Timeout = pNetwork_Pos_Timeout;
            this.lengthMap.put("cNetwork_Pos_Timeout", Integer.valueOf(this.cNetwork_Pos_Timeout.getTotalBytes()));
            this.fieldMap.put("cNetwork_Pos_Timeout", this.cNetwork_Pos_Timeout);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubBrcm_Assert_Info(CSubBrcm_Assert_Info pBrcm_Assert_Info) {
        if (pBrcm_Assert_Info != null) {
            this.cBrcm_Assert_Info = pBrcm_Assert_Info;
            this.lengthMap.put("cBrcm_Assert_Info", Integer.valueOf(this.cBrcm_Assert_Info.getTotalBytes()));
            this.fieldMap.put("cBrcm_Assert_Info", this.cBrcm_Assert_Info);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_Wifi_Pos_Error(CSubHiGeo_Wifi_Pos_Error pHiGeo_Wifi_Pos_Error) {
        if (pHiGeo_Wifi_Pos_Error != null) {
            this.cHiGeo_Wifi_Pos_Error = pHiGeo_Wifi_Pos_Error;
            this.lengthMap.put("cHiGeo_Wifi_Pos_Error", Integer.valueOf(this.cHiGeo_Wifi_Pos_Error.getTotalBytes()));
            this.fieldMap.put("cHiGeo_Wifi_Pos_Error", this.cHiGeo_Wifi_Pos_Error);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubHiGeo_Wifi_Scan_Error(CSubHiGeo_Wifi_Scan_Error pHiGeo_Wifi_Scan_Error) {
        if (pHiGeo_Wifi_Scan_Error != null) {
            this.cHiGeo_Wifi_Scan_Error = pHiGeo_Wifi_Scan_Error;
            this.lengthMap.put("cHiGeo_Wifi_Scan_Error", Integer.valueOf(this.cHiGeo_Wifi_Scan_Error.getTotalBytes()));
            this.fieldMap.put("cHiGeo_Wifi_Scan_Error", this.cHiGeo_Wifi_Scan_Error);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubPdr_Pos_Error(CSubPdr_Pos_Error pPdr_Pos_Error) {
        if (pPdr_Pos_Error != null) {
            this.cPdr_Pos_Error = pPdr_Pos_Error;
            this.lengthMap.put("cPdr_Pos_Error", Integer.valueOf(this.cPdr_Pos_Error.getTotalBytes()));
            this.fieldMap.put("cPdr_Pos_Error", this.cPdr_Pos_Error);
            this.usLen.setValue(getTotalLen());
        }
    }

    public CSegEVENT_GPS_POS_ERROR_EVENT() {
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
        this.lengthMap.put("cApk_NameList", Integer.valueOf(2));
        this.fieldMap.put("cApk_NameList", this.cApk_NameList);
        this.lengthMap.put("strProviderMode", Integer.valueOf(8));
        this.fieldMap.put("strProviderMode", this.strProviderMode);
        this.lengthMap.put("lStartTime", Integer.valueOf(8));
        this.fieldMap.put("lStartTime", this.lStartTime);
        this.lengthMap.put("ucPosMode", Integer.valueOf(1));
        this.fieldMap.put("ucPosMode", this.ucPosMode);
        this.lengthMap.put("strLocSetStatus", Integer.valueOf(20));
        this.fieldMap.put("strLocSetStatus", this.strLocSetStatus);
        this.lengthMap.put("strScreenState", Integer.valueOf(6));
        this.fieldMap.put("strScreenState", this.strScreenState);
        this.lengthMap.put("enScreen_Orientation", Integer.valueOf(1));
        this.fieldMap.put("enScreen_Orientation", this.enScreen_Orientation);
        this.lengthMap.put("strDataCall_Switch", Integer.valueOf(6));
        this.fieldMap.put("strDataCall_Switch", this.strDataCall_Switch);
        this.lengthMap.put("iCell_Mcc", Integer.valueOf(4));
        this.fieldMap.put("iCell_Mcc", this.iCell_Mcc);
        this.lengthMap.put("iCell_Mnc", Integer.valueOf(4));
        this.fieldMap.put("iCell_Mnc", this.iCell_Mnc);
        this.lengthMap.put("iCell_Lac", Integer.valueOf(4));
        this.fieldMap.put("iCell_Lac", this.iCell_Lac);
        this.lengthMap.put("iCell_Cid", Integer.valueOf(4));
        this.fieldMap.put("iCell_Cid", this.iCell_Cid);
        this.lengthMap.put("iCell_Baseid", Integer.valueOf(4));
        this.fieldMap.put("iCell_Baseid", this.iCell_Baseid);
        this.lengthMap.put("usCell_SID", Integer.valueOf(2));
        this.fieldMap.put("usCell_SID", this.usCell_SID);
        this.lengthMap.put("usCellN_ID", Integer.valueOf(2));
        this.fieldMap.put("usCellN_ID", this.usCellN_ID);
        this.lengthMap.put("enNetworkStatus", Integer.valueOf(1));
        this.fieldMap.put("enNetworkStatus", this.enNetworkStatus);
        this.lengthMap.put("strNetWorkAvailable", Integer.valueOf(6));
        this.fieldMap.put("strNetWorkAvailable", this.strNetWorkAvailable);
        this.lengthMap.put("ucBT_Switch", Integer.valueOf(1));
        this.fieldMap.put("ucBT_Switch", this.ucBT_Switch);
        this.lengthMap.put("ucNFC_Switch", Integer.valueOf(1));
        this.fieldMap.put("ucNFC_Switch", this.ucNFC_Switch);
        this.lengthMap.put("ucUSB_State", Integer.valueOf(1));
        this.fieldMap.put("ucUSB_State", this.ucUSB_State);
        this.lengthMap.put("strWifi_Switch", Integer.valueOf(6));
        this.fieldMap.put("strWifi_Switch", this.strWifi_Switch);
        this.lengthMap.put("strWifi_Bssid", Integer.valueOf(17));
        this.fieldMap.put("strWifi_Bssid", this.strWifi_Bssid);
        this.lengthMap.put("strWifi_Ssid", Integer.valueOf(32));
        this.fieldMap.put("strWifi_Ssid", this.strWifi_Ssid);
        this.lengthMap.put("strHiGeo_LBSversion", Integer.valueOf(32));
        this.fieldMap.put("strHiGeo_LBSversion", this.strHiGeo_LBSversion);
        this.lengthMap.put("cHiGeo_AR_Status", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_AR_Status", this.cHiGeo_AR_Status);
        this.lengthMap.put("cHiGeo_Mode", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_Mode", this.cHiGeo_Mode);
        this.lengthMap.put("cHiGeo_WiFi_Pos_Status", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_WiFi_Pos_Status", this.cHiGeo_WiFi_Pos_Status);
        this.lengthMap.put("iWifi_Service_Init_Fail_Type", Integer.valueOf(4));
        this.fieldMap.put("iWifi_Service_Init_Fail_Type", this.iWifi_Service_Init_Fail_Type);
        this.lengthMap.put("strConnect_WifiPos_Service_Error", Integer.valueOf(32));
        this.fieldMap.put("strConnect_WifiPos_Service_Error", this.strConnect_WifiPos_Service_Error);
        this.lengthMap.put("cNtp_Data_Param", Integer.valueOf(2));
        this.fieldMap.put("cNtp_Data_Param", this.cNtp_Data_Param);
        this.lengthMap.put("cLos_pos_param", Integer.valueOf(2));
        this.fieldMap.put("cLos_pos_param", this.cLos_pos_param);
        this.lengthMap.put("cFirst_Fix_Time_Out", Integer.valueOf(2));
        this.fieldMap.put("cFirst_Fix_Time_Out", this.cFirst_Fix_Time_Out);
        this.lengthMap.put("cData_Delivery_Delay", Integer.valueOf(2));
        this.fieldMap.put("cData_Delivery_Delay", this.cData_Delivery_Delay);
        this.lengthMap.put("cNetwork_Pos_Timeout", Integer.valueOf(2));
        this.fieldMap.put("cNetwork_Pos_Timeout", this.cNetwork_Pos_Timeout);
        this.lengthMap.put("cBrcm_Assert_Info", Integer.valueOf(2));
        this.fieldMap.put("cBrcm_Assert_Info", this.cBrcm_Assert_Info);
        this.lengthMap.put("cHiGeo_Wifi_Pos_Error", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_Wifi_Pos_Error", this.cHiGeo_Wifi_Pos_Error);
        this.lengthMap.put("cHiGeo_Wifi_Scan_Error", Integer.valueOf(2));
        this.fieldMap.put("cHiGeo_Wifi_Scan_Error", this.cHiGeo_Wifi_Scan_Error);
        this.lengthMap.put("cPdr_Pos_Error", Integer.valueOf(2));
        this.fieldMap.put("cPdr_Pos_Error", this.cPdr_Pos_Error);
        this.enEventId.setValue("GPS_POS_ERROR_EVENT");
        this.usLen.setValue(getTotalLen());
    }
}
