package com.huawei.systemmanager.comm.xml.filter;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.huawei.systemmanager.comm.xml.base.SimpleXmlRow;

public class AttrValueValidMatch implements Predicate<SimpleXmlRow> {
    private String mAttrName;

    public AttrValueValidMatch(String attrName) {
        this.mAttrName = attrName;
    }

    public boolean apply(SimpleXmlRow row) {
        boolean z = false;
        if (row == null) {
            return false;
        }
        if (!Strings.isNullOrEmpty(row.getAttrValue(this.mAttrName))) {
            z = true;
        }
        return z;
    }
}
