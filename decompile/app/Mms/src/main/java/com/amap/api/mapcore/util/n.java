package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Bundle;
import com.amap.api.mapcore.util.ac.a;
import com.amap.api.maps.AMap;
import java.io.IOException;

/* compiled from: OfflineMapDownloadTask */
public class n extends dp implements a {
    private ac a;
    private ae b;
    private ag c;
    private Context e;
    private Bundle f;
    private AMap g;
    private boolean h;

    public n(ag agVar, Context context) {
        this.f = new Bundle();
        this.h = false;
        this.c = agVar;
        this.e = context;
    }

    public n(ag agVar, Context context, AMap aMap) {
        this(agVar, context);
        this.g = aMap;
    }

    public void a() {
        if (this.c.x()) {
            this.c.a(ah.a.file_io_exception);
            return;
        }
        try {
            g();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void b() {
        this.h = true;
        if (this.a == null) {
            e();
        } else {
            this.a.c();
        }
        if (this.b != null) {
            this.b.a();
        }
    }

    private String f() {
        return bj.b(this.e);
    }

    private void g() throws IOException {
        this.a = new ac(new ad(this.c.getUrl(), f(), this.c.y(), 1, this.c.z()), this.c.getUrl(), this.e, this.c);
        this.a.a((a) this);
        this.b = new ae(this.c, this.c);
        if (!this.h) {
            this.a.a();
        }
    }

    public void c() {
        this.g = null;
        if (this.f != null) {
            this.f.clear();
            this.f = null;
        }
    }

    public void d() {
        if (this.b != null) {
            this.b.b();
        }
    }
}
