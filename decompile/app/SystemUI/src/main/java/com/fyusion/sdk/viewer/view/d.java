package com.fyusion.sdk.viewer.view;

import android.graphics.Bitmap;
import android.util.Log;
import com.fyusion.sdk.common.c;
import com.fyusion.sdk.common.c.b;
import com.fyusion.sdk.common.e;

/* compiled from: Unknown */
public class d {
    private static boolean j = false;
    com.fyusion.sdk.core.a.d a;
    com.fyusion.sdk.core.a.d b;
    Bitmap c;
    float d;
    float e;
    b f;
    b g;
    int h;
    int i;

    d() {
        this.a = null;
        this.b = null;
        this.c = null;
        this.d = 0.0f;
        this.e = 0.0f;
        this.f = c.b();
        this.g = c.b();
        this.h = -1;
        this.i = -1;
    }

    public d(com.fyusion.sdk.core.a.d dVar, int i) {
        this.a = dVar;
        this.b = null;
        this.c = null;
        this.d = 1.0f;
        this.e = 0.0f;
        this.f = c.b();
        this.g = c.b();
        this.h = i;
        this.i = -1;
    }

    public com.fyusion.sdk.core.a.d a() {
        return this.a;
    }

    public void a(i iVar, String str) {
        if (this.a == null && this.b == null && j) {
            Log.e("uploadTo", "No image set");
        }
        iVar.a(1.0f - this.d);
        if (this.a != null) {
            if (this.b == null) {
                iVar.a(this.a, this.h, str);
                iVar.c(this.f);
                return;
            }
            iVar.a(this.a, this.h, this.b, this.i, str);
            iVar.a(this.f, this.g);
        }
    }

    public com.fyusion.sdk.core.a.d b() {
        return this.b;
    }

    public e c() {
        return new e((double) this.a.b(), (double) this.a.c());
    }
}
