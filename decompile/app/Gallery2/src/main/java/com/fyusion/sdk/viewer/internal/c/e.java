package com.fyusion.sdk.viewer.internal.c;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.fyusion.sdk.viewer.internal.c.c.a;

/* compiled from: Unknown */
class e implements c {
    private final Context a;
    private final a b;
    private boolean c;
    private boolean d;
    private final BroadcastReceiver e = new BroadcastReceiver(this) {
        final /* synthetic */ e a;

        {
            this.a = r1;
        }

        public void onReceive(Context context, Intent intent) {
            boolean a = this.a.c;
            this.a.c = this.a.a(context);
            if (a != this.a.c) {
                this.a.b.a(this.a.c);
            }
        }
    };

    public e(Context context, a aVar) {
        this.a = context.getApplicationContext();
        this.b = aVar;
    }

    private void a() {
        if (!this.d) {
            this.c = a(this.a);
            this.a.registerReceiver(this.e, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            this.d = true;
        }
    }

    private boolean a(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void b() {
        if (this.d) {
            this.a.unregisterReceiver(this.e);
            this.d = false;
        }
    }

    public void onDestroy() {
    }

    public void onStart() {
        a();
    }

    public void onStop() {
        b();
    }
}
