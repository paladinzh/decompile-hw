package com.avast.android.shepherd.obfuscated;

/* compiled from: Unknown */
public enum ak {
    BLACKHOLE(0),
    WINQUAL(1),
    VIRUSLAB(2),
    STATS(3),
    ANDROIDSTATS(4),
    APPLEIOS(5),
    SHEPHERD(6),
    CLOUDREP(7),
    JUMPSHOT(8),
    ANDROIDBADNEWS(9);
    
    private int k;

    private ak(int i) {
        this.k = i;
    }

    public int a() {
        return this.k;
    }
}
