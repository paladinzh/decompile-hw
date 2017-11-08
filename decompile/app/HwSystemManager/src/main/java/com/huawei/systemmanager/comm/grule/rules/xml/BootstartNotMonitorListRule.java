package com.huawei.systemmanager.comm.grule.rules.xml;

import com.huawei.systemmanager.customize.CustomizeManager;

public class BootstartNotMonitorListRule extends XmlRuleParseAssetBase {
    private static final String MATCHING_KEY = "bootstart_white_list_match";
    private static final String XML_ASSET = "bootstart/hsm_bootstart_white_apps.xml";
    private static final String XML_DISK_CUST = CustomizeManager.composeCustFileName("xml/hsm/bootstart/hsm_bootstart_white_apps.xml");

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
