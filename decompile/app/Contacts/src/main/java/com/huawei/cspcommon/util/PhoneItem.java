package com.huawei.cspcommon.util;

public class PhoneItem {
    public String mName;
    public String mNumber;
    public String mSortKey;
    public int mType;

    public PhoneItem(String aNumber, String aName, int aType, String aSortKey) {
        this.mNumber = aNumber;
        this.mName = aName;
        this.mType = aType;
        this.mSortKey = aSortKey;
    }
}
