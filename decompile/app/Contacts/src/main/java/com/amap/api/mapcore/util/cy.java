package com.amap.api.mapcore.util;

import java.util.concurrent.Callable;

/* compiled from: DiskLruCache */
class cy implements Callable<Void> {
    final /* synthetic */ cx a;

    cy(cx cxVar) {
        this.a = cxVar;
    }

    public /* synthetic */ Object call() throws Exception {
        return a();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Void a() throws Exception {
        synchronized (this.a) {
            if (this.a.k != null) {
                this.a.j();
                if (this.a.h()) {
                    this.a.g();
                    this.a.m = 0;
                }
            } else {
                return null;
            }
        }
    }
}
