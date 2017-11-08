package com.huawei.systemmanager.comm.xml.filter;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.huawei.systemmanager.comm.xml.base.SimpleXmlRow;

public abstract class ExtAttrValueMatchBase implements Predicate<SimpleXmlRow> {
    private String mAttrName;

    public abstract boolean valueMatchRule(String str);

    public ExtAttrValueMatchBase(String attrName) {
        this.mAttrName = attrName;
    }

    public boolean apply(SimpleXmlRow row) {
        if (row == null) {
            return false;
        }
        return valueChecker(row.getAttrValue(this.mAttrName));
    }

    private boolean valueChecker(String attrValue) {
        if (Strings.isNullOrEmpty(attrValue)) {
            return false;
        }
        return valueMatchRule(attrValue);
    }
}
