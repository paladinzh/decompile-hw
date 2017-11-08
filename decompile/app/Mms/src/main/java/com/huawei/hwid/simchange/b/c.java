package com.huawei.hwid.simchange.b;

import android.content.Context;

/* compiled from: SimChangeUtil */
final class c implements Runnable {
    final /* synthetic */ Context a;
    final /* synthetic */ String b;
    final /* synthetic */ String c;

    c(Context context, String str, String str2) {
        this.a = context;
        this.b = str;
        this.c = str2;
    }

    public void run() {
        b.c(this.a, this.b, this.c);
    }
}
