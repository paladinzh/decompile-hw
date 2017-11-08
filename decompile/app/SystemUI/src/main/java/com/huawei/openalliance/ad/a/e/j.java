package com.huawei.openalliance.ad.a.e;

import android.content.Context;
import com.huawei.openalliance.ad.a.a.b;
import com.huawei.openalliance.ad.a.d.e;

/* compiled from: Unknown */
class j implements Runnable {
    final /* synthetic */ b a;
    final /* synthetic */ Context b;
    final /* synthetic */ boolean c;
    final /* synthetic */ h d;

    j(h hVar, b bVar, Context context, boolean z) {
        this.d = hVar;
        this.a = bVar;
        this.b = context;
        this.c = z;
    }

    public void run() {
        if (e.a(this.a)) {
            this.d.a(this.b, this.a, this.c);
        }
        e.a(this.b, this.a.getInvalidcontentid__());
    }
}
