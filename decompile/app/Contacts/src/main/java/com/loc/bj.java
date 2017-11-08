package com.loc;

import java.util.concurrent.Callable;

/* compiled from: DiskLruCache */
class bj implements Callable<Void> {
    final /* synthetic */ bi a;

    bj(bi biVar) {
        this.a = biVar;
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

    public /* synthetic */ Object call() throws Exception {
        return a();
    }
}
