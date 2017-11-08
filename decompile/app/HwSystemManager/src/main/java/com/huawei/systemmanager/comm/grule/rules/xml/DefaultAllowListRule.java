package com.huawei.systemmanager.comm.grule.rules.xml;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.customize.CustomizeManager;

public class DefaultAllowListRule extends XmlRuleParseResBase {
    private static final String FILE_WHITE_PACKAGENAME = CustomizeManager.composeCustFileName("xml/default_white_package_name.xml");
    private static final String WHITE_MATCHING_KEY = "white_match";

    String getDiskCustFilePath() {
        return FILE_WHITE_PACKAGENAME;
    }

    int getXmlResId() {
        return R.xml.default_white_package_name;
    }

    String getMatchingKey() {
        return WHITE_MATCHING_KEY;
    }
}
