package com.huawei.systemmanager.comm.xml.func;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.huawei.systemmanager.comm.xml.base.SimpleXmlRow;

public abstract class ExtractAttrValueFunction<T> implements Function<SimpleXmlRow, T> {
    private String mAttr;

    protected abstract T transformFromString(String str);

    public ExtractAttrValueFunction(String attr) {
        this.mAttr = attr;
    }

    public T apply(SimpleXmlRow row) {
        if (row == null) {
            return null;
        }
        return checkAndReturn(row.getAttrValue(this.mAttr));
    }

    private T checkAndReturn(String value) {
        if (!Strings.isNullOrEmpty(value)) {
            return transformFromString(value);
        }
        throw new IllegalArgumentException("input value should not be empty");
    }
}
