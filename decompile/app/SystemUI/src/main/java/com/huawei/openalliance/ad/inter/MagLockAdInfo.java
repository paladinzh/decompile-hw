package com.huawei.openalliance.ad.inter;

import java.util.List;

/* compiled from: Unknown */
public class MagLockAdInfo {
    private List<String> invalidContentIds;
    private List<MagLockAd> multiAds;
    private int retCode;

    public List<String> getInvalidContentIds() {
        return this.invalidContentIds;
    }

    public List<MagLockAd> getMultiAds() {
        return this.multiAds;
    }

    public int getRetCode() {
        return this.retCode;
    }

    public void setInvalidContentIds(List<String> list) {
        this.invalidContentIds = list;
    }

    public void setMultiAds(List<MagLockAd> list) {
        this.multiAds = list;
    }

    public void setRetCode(int i) {
        this.retCode = i;
    }

    public String toString() {
        return "MagLockAdInfo [multiAds=" + this.multiAds + ", invalidContentIds=" + this.invalidContentIds + ", retCode=" + this.retCode + "]";
    }
}
