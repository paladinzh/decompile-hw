package com.huawei.systemmanager.comm.xml.base;

import android.text.TextUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SimpleXmlRow {
    private Map<String, String> mAttr = new HashMap();
    private String mTag;

    public SimpleXmlRow(String tag) {
        this.mTag = tag;
    }

    public String getTag() {
        return this.mTag;
    }

    public void addAttribute(String attrName, String attrValue) {
        this.mAttr.put(attrName, attrValue);
    }

    public boolean containsAttr(String attrName) {
        return this.mAttr.containsKey(attrName);
    }

    public String getAttrValue(String attrName) {
        return (String) this.mAttr.get(attrName);
    }

    public int getAttrInteger(String attrName) {
        return getAttrInteger(attrName, 0);
    }

    public int getAttrInteger(String attrName, int defaultValue) {
        int res = defaultValue;
        String value = (String) this.mAttr.get(attrName);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            res = Integer.parseInt(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public boolean getAttrBoolean(String attrName) {
        return Boolean.valueOf((String) this.mAttr.get(attrName)).booleanValue();
    }

    public Map<String, String> getAttrMap() {
        return Collections.unmodifiableMap(this.mAttr);
    }

    public String toString() {
        return "[Tag:" + this.mTag + "]" + ";Attr[" + this.mAttr.toString() + "]";
    }
}
