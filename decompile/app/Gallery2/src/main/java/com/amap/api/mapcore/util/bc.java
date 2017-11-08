package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Bundle;
import com.amap.api.mapcore.util.br.a;
import com.amap.api.maps.AMap;
import java.io.IOException;

/* compiled from: OfflineMapDownloadTask */
public class bc extends hm implements a {
    private br a;
    private bt b;
    private bv c;
    private Context e;
    private Bundle f;
    private AMap g;
    private boolean h;

    public bc(bv bvVar, Context context) {
        this.f = new Bundle();
        this.h = false;
        this.c = bvVar;
        this.e = context;
    }

    public bc(bv bvVar, Context context, AMap aMap) {
        this(bvVar, context);
        this.g = aMap;
    }

    public void a() {
        if (this.c.y()) {
            this.c.a(bw.a.file_io_exception);
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
        return eh.b(this.e);
    }

    private void g() throws IOException {
        this.a = new br(new bs(this.c.getUrl(), f(), this.c.z(), 1, this.c.A()), this.c.getUrl(), this.e, this.c);
        this.a.a((a) this);
        this.b = new bt(this.c, this.c);
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
