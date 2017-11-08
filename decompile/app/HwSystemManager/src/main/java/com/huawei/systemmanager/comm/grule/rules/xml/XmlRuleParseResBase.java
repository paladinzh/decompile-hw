package com.huawei.systemmanager.comm.grule.rules.xml;

import android.content.Context;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import java.util.List;

abstract class XmlRuleParseResBase extends XmlRuleBase {
    abstract int getXmlResId();

    XmlRuleParseResBase() {
    }

    List<String> getPackageList(Context context, String tagName, String attrName) {
        return XmlParsers.xmlAttrValueListAfterMerged(context, getDiskCustFilePath(), getXmlResId(), XmlParsers.getTagAttrMatchPredicate(tagName, attrName), XmlParsers.getRowToAttrValueFunc(attrName));
    }
}
