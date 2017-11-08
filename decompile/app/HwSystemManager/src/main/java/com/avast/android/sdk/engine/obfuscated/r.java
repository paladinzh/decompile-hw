package com.avast.android.sdk.engine.obfuscated;

import java.net.URI;

/* compiled from: Unknown */
class r extends q {
    final /* synthetic */ String a;

    r(String str) {
        this.a = str;
    }

    public URI a(String str, String str2) {
        try {
            return new URI(String.format(this.a, new Object[]{str, str2}));
        } catch (Throwable e) {
            throw new IllegalArgumentException("URI has not proper format", e);
        } catch (Throwable e2) {
            throw new IllegalArgumentException("URI has not proper format - it must contain %d for keyID and %d for seqNum", e2);
        }
    }
}
