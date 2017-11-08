package com.avast.android.shepherd.obfuscated;

import java.io.IOException;

/* compiled from: Unknown */
public class u extends IOException {
    public u(String str, Throwable th) {
        super(str);
        if (th != null) {
            initCause(th);
        }
    }

    public u(Throwable th) {
        this("Cannot get key from registration server", null);
    }
}
