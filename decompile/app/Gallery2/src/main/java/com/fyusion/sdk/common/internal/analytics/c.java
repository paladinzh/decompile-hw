package com.fyusion.sdk.common.internal.analytics;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.fyusion.sdk.common.DLog;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* compiled from: Unknown */
public class c {
    private e a;
    private ExecutorService b;
    private Context c;
    private String d;
    private Future<?> e;
    private boolean f = false;

    private void a() {
        if (this.c == null) {
            throw new IllegalStateException("context has not been set");
        } else if (this.a == null) {
            throw new IllegalStateException("fyulytics store has not been set");
        } else if (this.d == null) {
            throw new IllegalStateException("server URL is not valid");
        }
    }

    private void a(int i) {
        if (!this.a.e()) {
            if (this.e == null || this.e.isDone()) {
                b();
                this.e = this.b.submit(new b(this.d, this.a, i));
            }
        }
    }

    private void b() {
        if (this.b == null) {
            this.b = Executors.newSingleThreadExecutor();
        }
    }

    private static boolean b(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isAvailable() && activeNetworkInfo.isConnected() && activeNetworkInfo.getType() == 1;
    }

    private void c() {
        a(Integer.MAX_VALUE);
    }

    void a(Context context) {
        this.c = context;
    }

    void a(e eVar) {
        this.a = eVar;
    }

    void a(String str) {
        this.d = str;
    }

    void a(boolean z) {
        this.f = z;
        if (this.f) {
            DLog.i(Fyulytics.TAG, "Send on wifi only");
        }
    }

    void b(String str) {
        a();
        boolean a = this.a.a(str);
        if (!this.f) {
            c();
        } else if (b(this.c)) {
            c();
        } else if (!a && this.a.d()) {
            a(100);
        }
    }
}
