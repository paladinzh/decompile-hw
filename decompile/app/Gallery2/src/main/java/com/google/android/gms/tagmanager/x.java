package com.google.android.gms.tagmanager;

import android.util.Log;

/* compiled from: Unknown */
class x implements bi {
    private int rc = 5;

    x() {
    }

    public void c(String str, Throwable th) {
        if (this.rc <= 6) {
            Log.e("GoogleTagManager", str, th);
        }
    }

    public void t(String str) {
        if (this.rc <= 6) {
            Log.e("GoogleTagManager", str);
        }
    }

    public void u(String str) {
        if (this.rc <= 4) {
            Log.i("GoogleTagManager", str);
        }
    }

    public void v(String str) {
        if (this.rc <= 2) {
            Log.v("GoogleTagManager", str);
        }
    }

    public void w(String str) {
        if (this.rc <= 5) {
            Log.w("GoogleTagManager", str);
        }
    }
}
