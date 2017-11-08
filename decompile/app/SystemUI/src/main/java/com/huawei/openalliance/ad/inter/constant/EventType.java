package com.huawei.openalliance.ad.inter.constant;

/* compiled from: Unknown */
public enum EventType {
    IMPRESSION("imp"),
    CLICK("click"),
    SWIPEUP("swipeup"),
    REMOVE("remove"),
    SHARE("share"),
    FAVORITE("favorite"),
    CLOSE("userclose"),
    SHOWEND("showstop");
    
    private final String event;

    private EventType(String str) {
        this.event = str;
    }

    public String value() {
        return this.event;
    }
}
