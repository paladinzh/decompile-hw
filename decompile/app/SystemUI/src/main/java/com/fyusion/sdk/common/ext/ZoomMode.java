package com.fyusion.sdk.common.ext;

/* compiled from: Unknown */
public enum ZoomMode {
    NONE(0),
    FULL(1),
    FULL_WITH_NONE_FOR_360(2);
    
    private int value;

    private ZoomMode(int i) {
        this.value = i;
    }

    public int getValue() {
        return this.value;
    }
}
