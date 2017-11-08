package com.huawei.cspcommon.cache;

public class MmsMatchContact {
    public Long mContactId;
    public String mLookupKey;
    public String mName;
    public String mNumber;
    public String mSortKey;
    public int mType;

    public MmsMatchContact(String name, String number, Long cID) {
        this.mName = name;
        this.mNumber = number;
        this.mContactId = cID;
    }

    public MmsMatchContact(String name, String number, Long cID, String lookupKey) {
        this.mName = name;
        this.mNumber = number;
        this.mContactId = cID;
        this.mLookupKey = lookupKey;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public int getType() {
        return this.mType;
    }
}
