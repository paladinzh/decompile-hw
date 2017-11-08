package com.huawei.systemmanager.secpatch.common;

public class SecPatchSearchItem {
    public String mDesc;
    public String mSid;

    public SecPatchSearchItem(String sid, String digest) {
        this.mSid = sid;
        this.mDesc = digest;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{ ");
        buf.append("mSid is [").append(this.mSid).append("] ");
        buf.append("mDigest is [").append(this.mDesc).append("] ");
        buf.append("} ");
        return buf.toString();
    }
}
