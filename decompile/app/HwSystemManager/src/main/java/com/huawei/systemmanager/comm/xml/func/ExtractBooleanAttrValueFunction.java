package com.huawei.systemmanager.comm.xml.func;

public class ExtractBooleanAttrValueFunction extends ExtractAttrValueFunction<Boolean> {
    public ExtractBooleanAttrValueFunction(String attr) {
        super(attr);
    }

    protected Boolean transformFromString(String value) {
        return Boolean.valueOf(Boolean.parseBoolean(value));
    }
}
