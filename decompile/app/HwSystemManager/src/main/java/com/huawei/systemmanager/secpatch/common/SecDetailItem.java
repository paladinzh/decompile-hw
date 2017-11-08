package com.huawei.systemmanager.secpatch.common;

import android.content.Intent;

public class SecDetailItem {
    public String mDesc;
    public String mDesc_en;
    private String mFixVersion;
    private String mOcid;
    private String mSid;
    private String mSrc;

    public SecDetailItem(String sid, String ocid, String src, String digest, String digest_en, String originVersion) {
        this.mSid = sid;
        this.mOcid = ocid;
        this.mSrc = src;
        this.mDesc = digest;
        this.mDesc_en = digest_en;
        this.mFixVersion = originVersion;
    }

    public Intent fillIntentForDetailActivty(Intent intent) {
        intent.putExtra(ConstValues.INTENT_DETAIL_SID, this.mSid);
        intent.putExtra(ConstValues.INTENT_DETAIL_OCID, this.mOcid);
        intent.putExtra(ConstValues.INTENT_DETAIL_SRC, this.mSrc);
        intent.putExtra(ConstValues.INTENT_DETAIL_CHN, this.mDesc);
        intent.putExtra(ConstValues.INTENT_DETAIL_ENG, this.mDesc_en);
        intent.putExtra(ConstValues.INTENT_FIX_VERSION, this.mFixVersion);
        return intent;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{ ");
        buf.append("mSid is [").append(this.mSid).append("] ");
        buf.append("mOcid is [").append(this.mOcid).append("] ");
        buf.append("mSrc is [").append(this.mSrc).append("] ");
        buf.append("mDigest is [").append(this.mDesc).append("] ");
        buf.append("mDigest_en is [").append(this.mDesc_en).append("] ");
        buf.append("mFixVersion is [").append(this.mFixVersion).append("] ");
        buf.append("} ");
        return buf.toString();
    }
}
