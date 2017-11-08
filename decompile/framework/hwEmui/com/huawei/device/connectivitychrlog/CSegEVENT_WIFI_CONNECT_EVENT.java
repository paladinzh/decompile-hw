package com.huawei.device.connectivitychrlog;

import com.huawei.connectivitylog.ConnectivityLogManager;
import java.util.ArrayList;
import java.util.List;

public class CSegEVENT_WIFI_CONNECT_EVENT extends ChrLogBaseEventModel {
    public List<CSubAssoc_Chr_Event> cAssoc_Chr_EventList = new ArrayList(5);
    private List<CSubAuth_Chr_Event> cAuth_Chr_EventList = new ArrayList(5);
    private List<CSubDHCP_Chr_Event> cDHCP_Chr_EventList = new ArrayList(5);
    private List<CSubRSSIGROUP_EVENT> cRSSIGROUP_EVENTList = new ArrayList(4);
    public ENCEventId enEventId = new ENCEventId();
    public ENCTriggerReason enTriggerReason = new ENCTriggerReason();
    public ENCconnect_type enconnect_type = new ENCconnect_type();
    public ENCucHwStatus enucHwStatus = new ENCucHwStatus();
    public LogInt iAP_RSSI = new LogInt();
    public LogInt iconnect_success_time = new LogInt();
    public LogLong lTimeStamp1 = new LogLong();
    public LogLong lTimeStamp2 = new LogLong();
    public LogLong lTimeStamp3 = new LogLong();
    public LogString strAP_MAC = new LogString(17);
    public LogString strAP_SSID = new LogString(32);
    public LogString strAP_auth_alg = new LogString(20);
    public LogString strAP_eap = new LogString(25);
    public LogString strAP_group = new LogString(25);
    public LogString strAP_key_mgmt = new LogString(32);
    public LogString strAP_pairwise = new LogString(20);
    public LogString strAP_proto = new LogString(10);
    public LogString strDNS_ADDRESS = new LogString(31);
    public LogString strIP_LEASETIME = new LogString(10);
    public LogString strProxySettingInfo = new LogString(64);
    public LogString strRoutes = new LogString(15);
    public LogString strSTA_MAC = new LogString(17);
    public LogString strThreadNameConnectAP = new LogString(50);
    public LogString strThreadNameDisableAP = new LogString(50);
    public LogString strWIFI_GATE = new LogString(15);
    public LogString strWIFI_IP = new LogString(15);
    public LogString strapVendorInfo = new LogString(ConnectivityLogManager.WIFI_PORTAL_SAMPLES_COLLECTE);
    public LogDate tmTimeStamp = new LogDate(6);
    public LogByte ucBTConnState = new LogByte();
    public LogByte ucBTState = new LogByte();
    public LogByte ucCardIndex = new LogByte();
    public LogByte ucIsMobleAP = new LogByte();
    public LogByte ucIsOnScreen = new LogByte();
    public LogByte ucProxySettings = new LogByte();
    public LogByte ucPublicEss = new LogByte();
    public LogByte ucScanAlwaysAvailble = new LogByte();
    public LogByte ucWIFIAlwaysNotifation = new LogByte();
    public LogByte ucWIFISleepPolicy = new LogByte();
    public LogByte ucWifiProStatus = new LogByte();
    public LogByte ucWifiToPDP = new LogByte();
    public LogShort usAP_channel = new LogShort();
    public LogShort usAP_link_speed = new LogShort();
    public LogShort usLen = new LogShort();

    public void setCSubAssoc_Chr_EventList(CSubAssoc_Chr_Event pAssoc_Chr_Event) {
        if (pAssoc_Chr_Event != null) {
            this.cAssoc_Chr_EventList.add(pAssoc_Chr_Event);
            this.lengthMap.put("cAssoc_Chr_EventList", Integer.valueOf((((ChrLogBaseModel) this.cAssoc_Chr_EventList.get(0)).getTotalBytes() * this.cAssoc_Chr_EventList.size()) + 2));
            this.fieldMap.put("cAssoc_Chr_EventList", this.cAssoc_Chr_EventList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubAuth_Chr_EventList(CSubAuth_Chr_Event pAuth_Chr_Event) {
        if (pAuth_Chr_Event != null) {
            this.cAuth_Chr_EventList.add(pAuth_Chr_Event);
            this.lengthMap.put("cAuth_Chr_EventList", Integer.valueOf((((ChrLogBaseModel) this.cAuth_Chr_EventList.get(0)).getTotalBytes() * this.cAuth_Chr_EventList.size()) + 2));
            this.fieldMap.put("cAuth_Chr_EventList", this.cAuth_Chr_EventList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubDHCP_Chr_EventList(CSubDHCP_Chr_Event pDHCP_Chr_Event) {
        if (pDHCP_Chr_Event != null) {
            this.cDHCP_Chr_EventList.add(pDHCP_Chr_Event);
            this.lengthMap.put("cDHCP_Chr_EventList", Integer.valueOf((((ChrLogBaseModel) this.cDHCP_Chr_EventList.get(0)).getTotalBytes() * this.cDHCP_Chr_EventList.size()) + 2));
            this.fieldMap.put("cDHCP_Chr_EventList", this.cDHCP_Chr_EventList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubRSSIGROUP_EVENTList(CSubRSSIGROUP_EVENT pRSSIGROUP_EVENT) {
        if (pRSSIGROUP_EVENT != null) {
            this.cRSSIGROUP_EVENTList.add(pRSSIGROUP_EVENT);
            this.lengthMap.put("cRSSIGROUP_EVENTList", Integer.valueOf((((ChrLogBaseModel) this.cRSSIGROUP_EVENTList.get(0)).getTotalBytes() * this.cRSSIGROUP_EVENTList.size()) + 2));
            this.fieldMap.put("cRSSIGROUP_EVENTList", this.cRSSIGROUP_EVENTList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public CSegEVENT_WIFI_CONNECT_EVENT() {
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("enconnect_type", Integer.valueOf(1));
        this.fieldMap.put("enconnect_type", this.enconnect_type);
        this.lengthMap.put("iconnect_success_time", Integer.valueOf(4));
        this.fieldMap.put("iconnect_success_time", this.iconnect_success_time);
        this.lengthMap.put("lTimeStamp1", Integer.valueOf(8));
        this.fieldMap.put("lTimeStamp1", this.lTimeStamp1);
        this.lengthMap.put("lTimeStamp2", Integer.valueOf(8));
        this.fieldMap.put("lTimeStamp2", this.lTimeStamp2);
        this.lengthMap.put("lTimeStamp3", Integer.valueOf(8));
        this.fieldMap.put("lTimeStamp3", this.lTimeStamp3);
        this.lengthMap.put("cAssoc_Chr_EventList", Integer.valueOf(2));
        this.fieldMap.put("cAssoc_Chr_EventList", this.cAssoc_Chr_EventList);
        this.lengthMap.put("cAuth_Chr_EventList", Integer.valueOf(2));
        this.fieldMap.put("cAuth_Chr_EventList", this.cAuth_Chr_EventList);
        this.lengthMap.put("cDHCP_Chr_EventList", Integer.valueOf(2));
        this.fieldMap.put("cDHCP_Chr_EventList", this.cDHCP_Chr_EventList);
        this.lengthMap.put("strThreadNameConnectAP", Integer.valueOf(50));
        this.fieldMap.put("strThreadNameConnectAP", this.strThreadNameConnectAP);
        this.lengthMap.put("strThreadNameDisableAP", Integer.valueOf(50));
        this.fieldMap.put("strThreadNameDisableAP", this.strThreadNameDisableAP);
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
        this.lengthMap.put("ucBTState", Integer.valueOf(1));
        this.fieldMap.put("ucBTState", this.ucBTState);
        this.lengthMap.put("ucBTConnState", Integer.valueOf(1));
        this.fieldMap.put("ucBTConnState", this.ucBTConnState);
        this.lengthMap.put("ucPublicEss", Integer.valueOf(1));
        this.fieldMap.put("ucPublicEss", this.ucPublicEss);
        this.lengthMap.put("cRSSIGROUP_EVENTList", Integer.valueOf(2));
        this.fieldMap.put("cRSSIGROUP_EVENTList", this.cRSSIGROUP_EVENTList);
        this.lengthMap.put("strapVendorInfo", Integer.valueOf(ConnectivityLogManager.WIFI_PORTAL_SAMPLES_COLLECTE));
        this.fieldMap.put("strapVendorInfo", this.strapVendorInfo);
        this.lengthMap.put("ucScanAlwaysAvailble", Integer.valueOf(1));
        this.fieldMap.put("ucScanAlwaysAvailble", this.ucScanAlwaysAvailble);
        this.lengthMap.put("ucWIFIAlwaysNotifation", Integer.valueOf(1));
        this.fieldMap.put("ucWIFIAlwaysNotifation", this.ucWIFIAlwaysNotifation);
        this.lengthMap.put("ucWIFISleepPolicy", Integer.valueOf(1));
        this.fieldMap.put("ucWIFISleepPolicy", this.ucWIFISleepPolicy);
        this.lengthMap.put("ucProxySettings", Integer.valueOf(1));
        this.fieldMap.put("ucProxySettings", this.ucProxySettings);
        this.lengthMap.put("ucWifiProStatus", Integer.valueOf(1));
        this.fieldMap.put("ucWifiProStatus", this.ucWifiProStatus);
        this.lengthMap.put("strProxySettingInfo", Integer.valueOf(64));
        this.fieldMap.put("strProxySettingInfo", this.strProxySettingInfo);
        this.lengthMap.put("ucWifiToPDP", Integer.valueOf(1));
        this.fieldMap.put("ucWifiToPDP", this.ucWifiToPDP);
        this.lengthMap.put("ucIsMobleAP", Integer.valueOf(1));
        this.fieldMap.put("ucIsMobleAP", this.ucIsMobleAP);
        this.lengthMap.put("ucIsOnScreen", Integer.valueOf(1));
        this.fieldMap.put("ucIsOnScreen", this.ucIsOnScreen);
        this.lengthMap.put("enTriggerReason", Integer.valueOf(1));
        this.fieldMap.put("enTriggerReason", this.enTriggerReason);
        this.enEventId.setValue("WIFI_CONNECT_EVENT");
        this.usLen.setValue(getTotalLen());
    }
}
