package com.huawei.systemmanager.comm.grule.rules.xml;

import com.huawei.systemmanager.customize.CustomizeManager;

public class NotificationMonitorListRule extends XmlRuleParseAssetBase {
    private static final String MATCHING_KEY = "notification_black_list_match";
    private static final String XML_ASSET = "notification/hsm_notification_black_apps.xml";
    private static final String XML_DISK_CUST = CustomizeManager.composeCustFileName("xml/hsm/notification/hsm_notification_black_apps.xml");

    String getDiskCustFilePath() {
        return XML_DISK_CUST;
    }

    String getAssetFile() {
        return XML_ASSET;
    }

    String getMatchingKey() {
        return MATCHING_KEY;
    }
}
