package com.fyusion.sdk.share;

import com.fyusion.sdk.common.ext.Key;
import com.fyusion.sdk.common.ext.internal.Settings;
import com.fyusion.sdk.common.ext.internal.b;
import com.fyusion.sdk.common.ext.internal.b.a;

/* compiled from: Unknown */
public class FyuseShareParameters extends b {
    public static final Key<Boolean> SHARE_RESOLUTION_FULL = new Key("com.fyusion.sdk.key.share.resolution.full");

    /* compiled from: Unknown */
    public static class Builder extends a {
        public FyuseShareParameters build() {
            return new FyuseShareParameters(this.a);
        }
    }

    private FyuseShareParameters(Settings settings) {
        super(settings);
    }
}
