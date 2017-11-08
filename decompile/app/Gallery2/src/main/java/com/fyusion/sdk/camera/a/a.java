package com.fyusion.sdk.camera.a;

import android.graphics.Bitmap;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.ext.FyuseState;
import com.fyusion.sdk.common.ext.b;
import com.fyusion.sdk.common.ext.e;
import com.fyusion.sdk.common.ext.g;
import com.fyusion.sdk.common.ext.l;
import com.fyusion.sdk.common.ext.m;
import fyusion.vislib.OnlineImageStabilizerWrapper;
import java.io.File;
import java.io.IOException;
import java.util.List;

/* compiled from: Unknown */
public class a {
    private b a = g.a();
    private l b;
    private List<m> c;
    private OnlineImageStabilizerWrapper d;
    private e e;

    public a(File file) {
        this.b = new l(this.a, file);
    }

    public a a(e eVar) {
        this.e = eVar;
        return this;
    }

    public a a(OnlineImageStabilizerWrapper onlineImageStabilizerWrapper) {
        this.d = onlineImageStabilizerWrapper;
        return this;
    }

    public a a(List<m> list) {
        this.c = list;
        return this;
    }

    public String a() {
        return this.b.a();
    }

    public void a(Bitmap bitmap) throws IOException {
        this.b.a(bitmap, this.e);
    }

    public File b() {
        return this.b.c();
    }

    public File c() {
        return this.b.b();
    }

    public File d() {
        DLog.i("CameraDataManager", "recording to: " + this.b.c().getName());
        this.b.a(FyuseState.RECORDING);
        return this.b.l();
    }

    public void e() {
        String path = this.b.c().getPath();
        this.a.a(path, this.c);
        this.a.a(path, this.d);
        this.a.a(path, this.e);
    }

    public void f() {
        this.b.a(FyuseState.RAW);
        try {
            this.b.a(false);
        } catch (Throwable e) {
            DLog.e("CameraDataManager", "Unable to create fyuse file", e);
        }
    }

    public void g() {
        com.fyusion.sdk.common.util.a.a(this.b.c());
    }
}
