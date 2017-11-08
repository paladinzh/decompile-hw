package com.fyusion.sdk.viewer.internal.b;

import android.support.annotation.Nullable;
import java.io.IOException;

/* compiled from: Unknown */
public final class d extends IOException {
    private final int a;

    public d(int i) {
        this("Http request failed with status code: " + i, i);
    }

    public d(String str) {
        this(str, -1);
    }

    public d(String str, int i) {
        this(str, i, null);
    }

    public d(String str, int i, @Nullable Throwable th) {
        super(str, th);
        this.a = i;
    }

    public int a() {
        return this.a;
    }
}
