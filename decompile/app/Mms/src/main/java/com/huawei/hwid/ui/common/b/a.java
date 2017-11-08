package com.huawei.hwid.ui.common.b;

import java.util.ArrayList;
import java.util.List;

/* compiled from: ForgetPwdNotifier */
public class a {
    private static a a = null;
    private List b = new ArrayList();

    public static synchronized a a() {
        a aVar;
        synchronized (a.class) {
            if (a == null) {
                a = new a();
            }
            aVar = a;
        }
        return aVar;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void a(b bVar) {
        synchronized (this.b) {
            if (bVar == null) {
            } else if (!this.b.contains(bVar)) {
                this.b.add(bVar);
            }
        }
    }

    public void b(b bVar) {
        synchronized (this.b) {
            if (this.b.contains(bVar)) {
                this.b.remove(bVar);
            }
        }
    }

    public void b() {
        synchronized (this.b) {
            for (b a : this.b) {
                a.a();
            }
        }
    }
}
