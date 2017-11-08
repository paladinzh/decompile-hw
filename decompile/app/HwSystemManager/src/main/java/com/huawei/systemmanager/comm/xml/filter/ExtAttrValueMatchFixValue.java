package com.huawei.systemmanager.comm.xml.filter;

public class ExtAttrValueMatchFixValue extends ExtAttrValueMatchBase {
    private String mFixAttrValue;

    public ExtAttrValueMatchFixValue(String attrName, String fixAttrValue) {
        super(attrName);
        this.mFixAttrValue = fixAttrValue;
    }

    public boolean valueMatchRule(String value) {
        return value.equals(this.mFixAttrValue);
    }
}
