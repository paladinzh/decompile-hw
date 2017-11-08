package com.huawei.device.connectivitychrlog;

import com.huawei.connectivitylog.ConnectivityLogManager;
import java.util.ArrayList;
import java.util.List;

public class CSegEVENT_WIFI_ACCESS_WEB_SLOWLY extends ChrLogBaseEventModel {
    private CSubApRoaming cApRoaming = null;
    private CSubBTStatus cBTStatus = null;
    private CSubCPUInfo cCPUInfo = null;
    private CSubCellID cCellID = null;
    private CSubDNS cDNS = null;
    private CSubMemInfo cMemInfo = null;
    private CSubNET_CFG cNET_CFG = null;
    private CSubPacketCount cPacketCount = null;
    private List<CSubRSSIGROUP_EVENT_EX> cRSSIGROUP_EVENT_EXList = new ArrayList(4);
    private CSubTCP_STATIST cTCP_STATIST = null;
    private CSubTRAFFIC_GROUND cTRAFFIC_GROUND = null;
    private CSubWL_COUNTERS cWL_COUNTERS = null;
    public ENCEventId enEventId = new ENCEventId();
    public ENCWIFI_REPEATER_STATUS enWIFI_REPEATER_STATUS = new ENCWIFI_REPEATER_STATUS();
    public ENCWifiAccessWebSlowlyReason enWifiAccessWebSlowlyReason = new ENCWifiAccessWebSlowlyReason();
    public ENCucHwStatus enucHwStatus = new ENCucHwStatus();
    public LogInt iAP_RSSI = new LogInt();
    public LogInt iRepeterFreq = new LogInt();
    public LogInt iTime_NetNormalTime = new LogInt();
    public LogInt iTime_NetSlowlyTime = new LogInt();
    public LogInt idetect_RTT_arp = new LogInt();
    public LogInt idetect_RTT_baidu = new LogInt();
    public LogInt ilost_beacon_amount = new LogInt();
    public LogInt imonitor_interval = new LogInt();
    public LogInt irx_beacon_from_assoc_ap = new LogInt();
    public LogInt irx_byte_amount = new LogInt();
    public LogInt irx_frame_amount = new LogInt();
    public LogInt isdio_info_ksoclrreq = new LogInt();
    public LogInt isdio_info_ksoclrretry = new LogInt();
    public LogInt isdio_info_ksosetreq = new LogInt();
    public LogInt isdio_info_ksosetretry = new LogInt();
    public LogInt isdio_info_readb = new LogInt();
    public LogInt isdio_info_readbreq = new LogInt();
    public LogInt isdio_info_readw = new LogInt();
    public LogInt isdio_info_readwreq = new LogInt();
    public LogInt isdio_info_writeb = new LogInt();
    public LogInt isdio_info_writebreq = new LogInt();
    public LogInt isdio_info_writew = new LogInt();
    public LogInt isdio_info_writewreq = new LogInt();
    public LogInt itx_byte_amount = new LogInt();
    public LogInt itx_data_frame_err_amount = new LogInt();
    public LogInt itx_frame_amount = new LogInt();
    public LogInt itx_retrans_amount = new LogInt();
    public LogString strAP_MAC = new LogString(17);
    public LogString strAP_SSID = new LogString(32);
    public LogString strAP_auth_alg = new LogString(20);
    public LogString strAP_eap = new LogString(25);
    public LogString strAP_group = new LogString(25);
    public LogString strAP_key_mgmt = new LogString(32);
    public LogString strAP_pairwise = new LogString(20);
    public LogString strAP_proto = new LogString(10);
    public LogString strCountryCode = new LogString(8);
    public LogString strDNS_ADDRESS = new LogString(31);
    public LogString strIP_LEASETIME = new LogString(10);
    public LogString strIP_public = new LogString(48);
    public LogString strInfo = new LogString(512);
    public LogString strRoutes = new LogString(15);
    public LogString strSTA_MAC = new LogString(17);
    public LogString strUIDInfo = new LogString(512);
    public LogString strWIFI_GATE = new LogString(15);
    public LogString strWIFI_IP = new LogString(15);
    public LogString strapVendorInfo = new LogString(ConnectivityLogManager.WIFI_PORTAL_SAMPLES_COLLECTE);
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucCardIndex = new LogByte();
    public LogByte ucFailReason = new LogByte();
    public LogByte ucGWCount = new LogByte();
    public LogByte ucIsMobleAP = new LogByte();
    public LogByte ucIsOnScreen = new LogByte();
    public LogByte ucPortalStatus = new LogByte();
    public LogByte ucPublicEss = new LogByte();
    public LogByte ucScanAlwaysAvailble = new LogByte();
    public LogByte ucWIFIAlwaysNotifation = new LogByte();
    public LogByte ucWIFISleepPolicy = new LogByte();
    public LogByte ucWifiProStatus = new LogByte();
    public LogByte ucWifiToPDP = new LogByte();
    public LogByte ucap_distance = new LogByte();
    public LogByte ucdisturbing_degree = new LogByte();
    public LogByte ucisPortal = new LogByte();
    public LogByte uctraffic_aftersuspend = new LogByte();
    public LogShort usAP_channel = new LogShort();
    public LogShort usAP_link_speed = new LogShort();
    public LogShort usLen = new LogShort();
    public LogShort usSubErrorCode = new LogShort();

    public void setCSubBTStatus(CSubBTStatus pBTStatus) {
        if (pBTStatus != null) {
            this.cBTStatus = pBTStatus;
            this.lengthMap.put("cBTStatus", Integer.valueOf(this.cBTStatus.getTotalBytes()));
            this.fieldMap.put("cBTStatus", this.cBTStatus);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubCellID(CSubCellID pCellID) {
        if (pCellID != null) {
            this.cCellID = pCellID;
            this.lengthMap.put("cCellID", Integer.valueOf(this.cCellID.getTotalBytes()));
            this.fieldMap.put("cCellID", this.cCellID);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubNET_CFG(CSubNET_CFG pNET_CFG) {
        if (pNET_CFG != null) {
            this.cNET_CFG = pNET_CFG;
            this.lengthMap.put("cNET_CFG", Integer.valueOf(this.cNET_CFG.getTotalBytes()));
            this.fieldMap.put("cNET_CFG", this.cNET_CFG);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubCPUInfo(CSubCPUInfo pCPUInfo) {
        if (pCPUInfo != null) {
            this.cCPUInfo = pCPUInfo;
            this.lengthMap.put("cCPUInfo", Integer.valueOf(this.cCPUInfo.getTotalBytes()));
            this.fieldMap.put("cCPUInfo", this.cCPUInfo);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubMemInfo(CSubMemInfo pMemInfo) {
        if (pMemInfo != null) {
            this.cMemInfo = pMemInfo;
            this.lengthMap.put("cMemInfo", Integer.valueOf(this.cMemInfo.getTotalBytes()));
            this.fieldMap.put("cMemInfo", this.cMemInfo);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubTRAFFIC_GROUND(CSubTRAFFIC_GROUND pTRAFFIC_GROUND) {
        if (pTRAFFIC_GROUND != null) {
            this.cTRAFFIC_GROUND = pTRAFFIC_GROUND;
            this.lengthMap.put("cTRAFFIC_GROUND", Integer.valueOf(this.cTRAFFIC_GROUND.getTotalBytes()));
            this.fieldMap.put("cTRAFFIC_GROUND", this.cTRAFFIC_GROUND);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubDNS(CSubDNS pDNS) {
        if (pDNS != null) {
            this.cDNS = pDNS;
            this.lengthMap.put("cDNS", Integer.valueOf(this.cDNS.getTotalBytes()));
            this.fieldMap.put("cDNS", this.cDNS);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubPacketCount(CSubPacketCount pPacketCount) {
        if (pPacketCount != null) {
            this.cPacketCount = pPacketCount;
            this.lengthMap.put("cPacketCount", Integer.valueOf(this.cPacketCount.getTotalBytes()));
            this.fieldMap.put("cPacketCount", this.cPacketCount);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubRSSIGROUP_EVENT_EXList(CSubRSSIGROUP_EVENT_EX pRSSIGROUP_EVENT_EX) {
        if (pRSSIGROUP_EVENT_EX != null) {
            this.cRSSIGROUP_EVENT_EXList.add(pRSSIGROUP_EVENT_EX);
            this.lengthMap.put("cRSSIGROUP_EVENT_EXList", Integer.valueOf((((ChrLogBaseModel) this.cRSSIGROUP_EVENT_EXList.get(0)).getTotalBytes() * this.cRSSIGROUP_EVENT_EXList.size()) + 2));
            this.fieldMap.put("cRSSIGROUP_EVENT_EXList", this.cRSSIGROUP_EVENT_EXList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubWL_COUNTERS(CSubWL_COUNTERS pWL_COUNTERS) {
        if (pWL_COUNTERS != null) {
            this.cWL_COUNTERS = pWL_COUNTERS;
            this.lengthMap.put("cWL_COUNTERS", Integer.valueOf(this.cWL_COUNTERS.getTotalBytes()));
            this.fieldMap.put("cWL_COUNTERS", this.cWL_COUNTERS);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubTCP_STATIST(CSubTCP_STATIST pTCP_STATIST) {
        if (pTCP_STATIST != null) {
            this.cTCP_STATIST = pTCP_STATIST;
            this.lengthMap.put("cTCP_STATIST", Integer.valueOf(this.cTCP_STATIST.getTotalBytes()));
            this.fieldMap.put("cTCP_STATIST", this.cTCP_STATIST);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubApRoaming(CSubApRoaming pApRoaming) {
        if (pApRoaming != null) {
            this.cApRoaming = pApRoaming;
            this.lengthMap.put("cApRoaming", Integer.valueOf(this.cApRoaming.getTotalBytes()));
            this.fieldMap.put("cApRoaming", this.cApRoaming);
            this.usLen.setValue(getTotalLen());
        }
    }

    public CSegEVENT_WIFI_ACCESS_WEB_SLOWLY() {
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("enWifiAccessWebSlowlyReason", Integer.valueOf(1));
        this.fieldMap.put("enWifiAccessWebSlowlyReason", this.enWifiAccessWebSlowlyReason);
        this.lengthMap.put("usSubErrorCode", Integer.valueOf(2));
        this.fieldMap.put("usSubErrorCode", this.usSubErrorCode);
        this.lengthMap.put("strSTA_MAC", Integer.valueOf(17));
        this.fieldMap.put("strSTA_MAC", this.strSTA_MAC);
        this.lengthMap.put("strAP_MAC", Integer.valueOf(17));
        this.fieldMap.put("strAP_MAC", this.strAP_MAC);
        this.lengthMap.put("strAP_SSID", Integer.valueOf(32));
        this.fieldMap.put("strAP_SSID", this.strAP_SSID);
        this.lengthMap.put("strAP_proto", Integer.valueOf(10));
        this.fieldMap.put("strAP_proto", this.strAP_proto);
        this.lengthMap.put("strAP_key_mgmt", Integer.valueOf(32));
        this.fieldMap.put("strAP_key_mgmt", this.strAP_key_mgmt);
        this.lengthMap.put("strAP_auth_alg", Integer.valueOf(20));
        this.fieldMap.put("strAP_auth_alg", this.strAP_auth_alg);
        this.lengthMap.put("strAP_pairwise", Integer.valueOf(20));
        this.fieldMap.put("strAP_pairwise", this.strAP_pairwise);
        this.lengthMap.put("strAP_group", Integer.valueOf(25));
        this.fieldMap.put("strAP_group", this.strAP_group);
        this.lengthMap.put("strAP_eap", Integer.valueOf(25));
        this.fieldMap.put("strAP_eap", this.strAP_eap);
        this.lengthMap.put("usAP_link_speed", Integer.valueOf(2));
        this.fieldMap.put("usAP_link_speed", this.usAP_link_speed);
        this.lengthMap.put("usAP_channel", Integer.valueOf(2));
        this.fieldMap.put("usAP_channel", this.usAP_channel);
        this.lengthMap.put("iAP_RSSI", Integer.valueOf(4));
        this.fieldMap.put("iAP_RSSI", this.iAP_RSSI);
        this.lengthMap.put("strWIFI_IP", Integer.valueOf(15));
        this.fieldMap.put("strWIFI_IP", this.strWIFI_IP);
        this.lengthMap.put("strIP_LEASETIME", Integer.valueOf(10));
        this.fieldMap.put("strIP_LEASETIME", this.strIP_LEASETIME);
        this.lengthMap.put("strWIFI_GATE", Integer.valueOf(15));
        this.fieldMap.put("strWIFI_GATE", this.strWIFI_GATE);
        this.lengthMap.put("strDNS_ADDRESS", Integer.valueOf(31));
        this.fieldMap.put("strDNS_ADDRESS", this.strDNS_ADDRESS);
        this.lengthMap.put("strRoutes", Integer.valueOf(15));
        this.fieldMap.put("strRoutes", this.strRoutes);
        this.lengthMap.put("enucHwStatus", Integer.valueOf(1));
        this.fieldMap.put("enucHwStatus", this.enucHwStatus);
        this.lengthMap.put("ucPublicEss", Integer.valueOf(1));
        this.fieldMap.put("ucPublicEss", this.ucPublicEss);
        this.lengthMap.put("strapVendorInfo", Integer.valueOf(ConnectivityLogManager.WIFI_PORTAL_SAMPLES_COLLECTE));
        this.fieldMap.put("strapVendorInfo", this.strapVendorInfo);
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
        this.lengthMap.put("strInfo", Integer.valueOf(512));
        this.fieldMap.put("strInfo", this.strInfo);
        this.lengthMap.put("strUIDInfo", Integer.valueOf(512));
        this.fieldMap.put("strUIDInfo", this.strUIDInfo);
        this.lengthMap.put("ucFailReason", Integer.valueOf(1));
        this.fieldMap.put("ucFailReason", this.ucFailReason);
        this.lengthMap.put("cBTStatus", Integer.valueOf(2));
        this.fieldMap.put("cBTStatus", this.cBTStatus);
        this.lengthMap.put("cCellID", Integer.valueOf(2));
        this.fieldMap.put("cCellID", this.cCellID);
        this.lengthMap.put("cNET_CFG", Integer.valueOf(2));
        this.fieldMap.put("cNET_CFG", this.cNET_CFG);
        this.lengthMap.put("cCPUInfo", Integer.valueOf(2));
        this.fieldMap.put("cCPUInfo", this.cCPUInfo);
        this.lengthMap.put("cMemInfo", Integer.valueOf(2));
        this.fieldMap.put("cMemInfo", this.cMemInfo);
        this.lengthMap.put("cTRAFFIC_GROUND", Integer.valueOf(2));
        this.fieldMap.put("cTRAFFIC_GROUND", this.cTRAFFIC_GROUND);
        this.lengthMap.put("cDNS", Integer.valueOf(2));
        this.fieldMap.put("cDNS", this.cDNS);
        this.lengthMap.put("iTime_NetSlowlyTime", Integer.valueOf(4));
        this.fieldMap.put("iTime_NetSlowlyTime", this.iTime_NetSlowlyTime);
        this.lengthMap.put("iTime_NetNormalTime", Integer.valueOf(4));
        this.fieldMap.put("iTime_NetNormalTime", this.iTime_NetNormalTime);
        this.lengthMap.put("ucisPortal", Integer.valueOf(1));
        this.fieldMap.put("ucisPortal", this.ucisPortal);
        this.lengthMap.put("strCountryCode", Integer.valueOf(8));
        this.fieldMap.put("strCountryCode", this.strCountryCode);
        this.lengthMap.put("cPacketCount", Integer.valueOf(2));
        this.fieldMap.put("cPacketCount", this.cPacketCount);
        this.lengthMap.put("cRSSIGROUP_EVENT_EXList", Integer.valueOf(2));
        this.fieldMap.put("cRSSIGROUP_EVENT_EXList", this.cRSSIGROUP_EVENT_EXList);
        this.lengthMap.put("cWL_COUNTERS", Integer.valueOf(2));
        this.fieldMap.put("cWL_COUNTERS", this.cWL_COUNTERS);
        this.lengthMap.put("cTCP_STATIST", Integer.valueOf(2));
        this.fieldMap.put("cTCP_STATIST", this.cTCP_STATIST);
        this.lengthMap.put("ucGWCount", Integer.valueOf(1));
        this.fieldMap.put("ucGWCount", this.ucGWCount);
        this.lengthMap.put("strIP_public", Integer.valueOf(48));
        this.fieldMap.put("strIP_public", this.strIP_public);
        this.lengthMap.put("isdio_info_readbreq", Integer.valueOf(4));
        this.fieldMap.put("isdio_info_readbreq", this.isdio_info_readbreq);
        this.lengthMap.put("isdio_info_readb", Integer.valueOf(4));
        this.fieldMap.put("isdio_info_readb", this.isdio_info_readb);
        this.lengthMap.put("isdio_info_writebreq", Integer.valueOf(4));
        this.fieldMap.put("isdio_info_writebreq", this.isdio_info_writebreq);
        this.lengthMap.put("isdio_info_writeb", Integer.valueOf(4));
        this.fieldMap.put("isdio_info_writeb", this.isdio_info_writeb);
        this.lengthMap.put("isdio_info_readwreq", Integer.valueOf(4));
        this.fieldMap.put("isdio_info_readwreq", this.isdio_info_readwreq);
        this.lengthMap.put("isdio_info_readw", Integer.valueOf(4));
        this.fieldMap.put("isdio_info_readw", this.isdio_info_readw);
        this.lengthMap.put("isdio_info_writewreq", Integer.valueOf(4));
        this.fieldMap.put("isdio_info_writewreq", this.isdio_info_writewreq);
        this.lengthMap.put("isdio_info_writew", Integer.valueOf(4));
        this.fieldMap.put("isdio_info_writew", this.isdio_info_writew);
        this.lengthMap.put("isdio_info_ksosetreq", Integer.valueOf(4));
        this.fieldMap.put("isdio_info_ksosetreq", this.isdio_info_ksosetreq);
        this.lengthMap.put("isdio_info_ksosetretry", Integer.valueOf(4));
        this.fieldMap.put("isdio_info_ksosetretry", this.isdio_info_ksosetretry);
        this.lengthMap.put("isdio_info_ksoclrreq", Integer.valueOf(4));
        this.fieldMap.put("isdio_info_ksoclrreq", this.isdio_info_ksoclrreq);
        this.lengthMap.put("isdio_info_ksoclrretry", Integer.valueOf(4));
        this.fieldMap.put("isdio_info_ksoclrretry", this.isdio_info_ksoclrretry);
        this.lengthMap.put("idetect_RTT_arp", Integer.valueOf(4));
        this.fieldMap.put("idetect_RTT_arp", this.idetect_RTT_arp);
        this.lengthMap.put("idetect_RTT_baidu", Integer.valueOf(4));
        this.fieldMap.put("idetect_RTT_baidu", this.idetect_RTT_baidu);
        this.lengthMap.put("uctraffic_aftersuspend", Integer.valueOf(1));
        this.fieldMap.put("uctraffic_aftersuspend", this.uctraffic_aftersuspend);
        this.lengthMap.put("ucPortalStatus", Integer.valueOf(1));
        this.fieldMap.put("ucPortalStatus", this.ucPortalStatus);
        this.lengthMap.put("cApRoaming", Integer.valueOf(2));
        this.fieldMap.put("cApRoaming", this.cApRoaming);
        this.lengthMap.put("imonitor_interval", Integer.valueOf(4));
        this.fieldMap.put("imonitor_interval", this.imonitor_interval);
        this.lengthMap.put("itx_frame_amount", Integer.valueOf(4));
        this.fieldMap.put("itx_frame_amount", this.itx_frame_amount);
        this.lengthMap.put("itx_byte_amount", Integer.valueOf(4));
        this.fieldMap.put("itx_byte_amount", this.itx_byte_amount);
        this.lengthMap.put("itx_data_frame_err_amount", Integer.valueOf(4));
        this.fieldMap.put("itx_data_frame_err_amount", this.itx_data_frame_err_amount);
        this.lengthMap.put("itx_retrans_amount", Integer.valueOf(4));
        this.fieldMap.put("itx_retrans_amount", this.itx_retrans_amount);
        this.lengthMap.put("irx_frame_amount", Integer.valueOf(4));
        this.fieldMap.put("irx_frame_amount", this.irx_frame_amount);
        this.lengthMap.put("irx_byte_amount", Integer.valueOf(4));
        this.fieldMap.put("irx_byte_amount", this.irx_byte_amount);
        this.lengthMap.put("irx_beacon_from_assoc_ap", Integer.valueOf(4));
        this.fieldMap.put("irx_beacon_from_assoc_ap", this.irx_beacon_from_assoc_ap);
        this.lengthMap.put("ucap_distance", Integer.valueOf(1));
        this.fieldMap.put("ucap_distance", this.ucap_distance);
        this.lengthMap.put("ucdisturbing_degree", Integer.valueOf(1));
        this.fieldMap.put("ucdisturbing_degree", this.ucdisturbing_degree);
        this.lengthMap.put("ilost_beacon_amount", Integer.valueOf(4));
        this.fieldMap.put("ilost_beacon_amount", this.ilost_beacon_amount);
        this.lengthMap.put("enWIFI_REPEATER_STATUS", Integer.valueOf(1));
        this.fieldMap.put("enWIFI_REPEATER_STATUS", this.enWIFI_REPEATER_STATUS);
        this.lengthMap.put("iRepeterFreq", Integer.valueOf(4));
        this.fieldMap.put("iRepeterFreq", this.iRepeterFreq);
        this.enEventId.setValue("WIFI_ACCESS_WEB_SLOWLY");
        this.usLen.setValue(getTotalLen());
    }
}
