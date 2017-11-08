package com.huawei.systemmanager.comm.grule.rules.xml;

import com.huawei.systemmanager.customize.CustomizeManager;

public class AppLockNotMonitorListRule extends XmlRuleParseAssetBase {
    private static final String APPLOCK_WHITE_LIST_MATCHING_KEY = "applock_white_list_match";
    private static final String APPLOCK_WHITE_LIST_XML_ASSET = "applock/hsm_applock_white_apps.xml";
    private static final String APPLOCK_WHITE_LIST_XML_CUST = CustomizeManager.composeCustFileName("xml/hsm/applock/hsm_applock_white_apps.xml");

    String getDiskCustFilePath() {
        return APPLOCK_WHITE_LIST_XML_CUST;
    }

    String getAssetFile() {
        return APPLOCK_WHITE_LIST_XML_ASSET;
    }

    String getMatchingKey() {
        return APPLOCK_WHITE_LIST_MATCHING_KEY;
    }
}
