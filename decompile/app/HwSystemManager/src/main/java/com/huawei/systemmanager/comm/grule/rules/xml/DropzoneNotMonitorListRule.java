package com.huawei.systemmanager.comm.grule.rules.xml;

import com.huawei.systemmanager.customize.CustomizeManager;

public class DropzoneNotMonitorListRule extends XmlRuleParseAssetBase {
    private static final String MATCHING_KEY = "dropzone_white_list_match";
    private static final String XML_ASSET = "dropzone/hsm_dropzone_white_apps.xml";
    private static final String XML_DISK_CUST = CustomizeManager.composeCustFileName("xml/hsm/dropzone/hsm_dropzone_white_apps.xml");

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
