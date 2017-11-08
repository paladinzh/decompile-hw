package com.huawei.permissionmanager.model;

public class TypeValuePair {
    public int mType;
    public int mValue;

    public TypeValuePair(int type) {
        this.mType = type;
    }

    public void setValue(int value) {
        this.mValue = value;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == null || !(o instanceof TypeValuePair)) {
            return false;
        }
        if (this.mType == ((TypeValuePair) o).mType) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return 0;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(", type:");
        buf.append(this.mType);
        buf.append(", value:");
        buf.append(this.mValue);
        return buf.toString();
    }
}
