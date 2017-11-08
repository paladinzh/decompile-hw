package com.huawei.systemmanager.comm.grule.rules.xml;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.customize.CustomizeManager;

public class DefaultMonitorListRule extends XmlRuleParseResBase {
    private static final String BLACK_MATCHING_KEY = "black_match";
    private static final String FILE_BLACK_PACKAGENAME = CustomizeManager.composeCustFileName("xml/default_black_package_name.xml");

    String getDiskCustFilePath() {
        return FILE_BLACK_PACKAGENAME;
    }

    int getXmlResId() {
        return R.xml.default_black_package_name;
    }

    String getMatchingKey() {
        return BLACK_MATCHING_KEY;
    }
}
