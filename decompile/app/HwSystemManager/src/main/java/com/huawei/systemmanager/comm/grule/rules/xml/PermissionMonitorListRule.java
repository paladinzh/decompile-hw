package com.huawei.systemmanager.comm.grule.rules.xml;

import com.huawei.systemmanager.customize.CustomizeManager;

public class PermissionMonitorListRule extends XmlRuleParseAssetBase {
    private static final String MATCHING_KEY = "permission_black_list_match";
    private static final String XML_ASSET = "permission/hsm_permission_black_apps.xml";
    public static final String XML_DISK_CUST = CustomizeManager.composeCustFileName("xml/hsm/permission/hsm_permission_black_apps.xml");

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
