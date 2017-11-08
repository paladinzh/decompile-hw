package com.fyusion.sdk.common.ext.filter.a;

/* compiled from: Unknown */
public enum m {
    SHARPEN(0),
    TONECURVE(7),
    BRIGHTNESS(10),
    CONTRAST(20),
    SATURATION(30),
    SKINTONESATURATION(30),
    EXPOSURE(40),
    HIGHLIGHTSHADOW(50),
    VIGNETTE(60),
    BLUR(80),
    CLAMP(500);
    
    private final int l;

    private m(int i) {
        this.l = i;
    }

    public int a() {
        return this.l;
    }
}
