package com.huawei.openalliance.ad.inter;

import java.util.List;

/* compiled from: Unknown */
public class MagLockAd {
    private List<MagLockAdContent> adList;
    private int retCode;
    private String slotId;

    public List<MagLockAdContent> getAdList() {
        return this.adList;
    }

    public int getRetCode() {
        return this.retCode;
    }

    public String getSlotId() {
        return this.slotId;
    }

    public void setAdList(List<MagLockAdContent> list) {
        this.adList = list;
    }

    public void setRetCode(int i) {
        this.retCode = i;
    }

    public void setSlotId(String str) {
        this.slotId = str;
    }

    public String toString() {
        return "MagLockAd [slotId=" + this.slotId + ", adList=" + this.adList + ", retCode=" + this.retCode + "]";
    }
}
