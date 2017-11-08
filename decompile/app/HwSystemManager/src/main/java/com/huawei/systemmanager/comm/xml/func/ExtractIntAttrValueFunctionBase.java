package com.huawei.systemmanager.comm.xml.func;

public class ExtractIntAttrValueFunctionBase extends ExtractAttrValueFunction<Integer> {
    public ExtractIntAttrValueFunctionBase(String attr) {
        super(attr);
    }

    protected Integer transformFromString(String value) {
        return Integer.valueOf(Integer.parseInt(value));
    }
}
