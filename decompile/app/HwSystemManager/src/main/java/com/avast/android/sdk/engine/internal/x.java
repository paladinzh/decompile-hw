package com.avast.android.sdk.engine.internal;

import com.avast.android.sdk.internal.d.a;

/* compiled from: Unknown */
class x extends a {
    final /* synthetic */ String a;

    x(String str) {
        this.a = str;
    }

    public boolean a(String str, String str2) {
        return str != null && str.equals(this.a) && str2.endsWith(".so");
    }
}
