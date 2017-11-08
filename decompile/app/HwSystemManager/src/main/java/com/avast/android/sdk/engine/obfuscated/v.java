package com.avast.android.sdk.engine.obfuscated;

import java.io.IOException;

/* compiled from: Unknown */
public class v extends IOException {
    public v(String str, Throwable th) {
        super(str);
        if (th != null) {
            initCause(th);
        }
    }

    public v(Throwable th) {
        this("Cannot get key from registration server", null);
    }
}
