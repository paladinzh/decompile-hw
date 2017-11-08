package com.huawei.openalliance.ad.utils.db.bean;

/* compiled from: Unknown */
public class ThirdPartyEventRecord extends a {
    private int adType_;
    private long lockTime_ = 0;
    private long time_;
    private String url_;

    public ThirdPartyEventRecord(int i, String str) {
        this.adType_ = i;
        this.url_ = str;
        this.time_ = System.currentTimeMillis();
    }
}
