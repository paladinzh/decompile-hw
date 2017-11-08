package com.huawei.systemmanager.comm.xml.filter;

import com.google.common.base.Predicate;
import com.huawei.systemmanager.comm.xml.base.SimpleXmlRow;

public class TagNameMatch implements Predicate<SimpleXmlRow> {
    private String mTag;

    public TagNameMatch(String tag) {
        this.mTag = tag;
    }

    public boolean apply(SimpleXmlRow row) {
        if (row == null || !row.getTag().equals(this.mTag)) {
            return false;
        }
        return true;
    }
}
