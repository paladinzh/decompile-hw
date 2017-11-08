package com.huawei.systemmanager.comm.xml.func;

public class ExtractStringAttrValueFunction extends ExtractAttrValueFunction<String> {
    public ExtractStringAttrValueFunction(String attr) {
        super(attr);
    }

    protected String transformFromString(String value) {
        return value;
    }
}
