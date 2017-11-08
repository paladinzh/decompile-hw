package com.amap.api.services.core;

import java.util.concurrent.Callable;

/* compiled from: DiskLruCache */
class bl implements Callable<Void> {
    final /* synthetic */ bk a;

    bl(bk bkVar) {
        this.a = bkVar;
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
