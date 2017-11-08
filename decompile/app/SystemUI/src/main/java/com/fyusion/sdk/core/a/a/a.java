package com.fyusion.sdk.core.a.a;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import com.fyusion.sdk.common.q;
import com.fyusion.sdk.core.a.d;

/* compiled from: Unknown */
public class a implements d<Bitmap> {
    private Bitmap a;
    private boolean b = false;

    public a(Bitmap bitmap) {
        if (bitmap != null) {
            this.a = bitmap;
            return;
        }
        throw new IllegalArgumentException("Image is null");
    }

    public /* synthetic */ Object a() {
        return h();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void a(int i, int i2) {
        if (this.a != null) {
            if (!this.a.isRecycled()) {
                GLES20.glBindTexture(3553, i);
                GLUtils.texImage2D(3553, 0, this.a, 0);
                GLES20.glBindTexture(3553, 0);
                q.a();
            }
        }
    }

    public void a(int i, int i2, int i3, int i4, int i5) {
        GLES20.glUniform1i(i, 0);
    }

    public boolean a(int i, int i2, int i3) {
        boolean z = false;
        if (this.b) {
            return false;
        }
        if ((i * i2) * i3 <= d()) {
            z = true;
        }
        return z;
    }

    public boolean a(Class<?> cls) {
        return cls == Bitmap.class;
    }

    public synchronized int b() {
        if (this.b) {
            return 0;
        }
        return this.a.getWidth();
    }

    public synchronized int c() {
        if (this.b) {
            return 0;
        }
        return this.a.getHeight();
    }

    public synchronized int d() {
        if (this.b) {
            return 0;
        }
        return this.a.getAllocationByteCount();
    }

    public synchronized void e() {
        if (!this.b) {
            this.b = true;
            this.a.recycle();
        }
    }

    public synchronized boolean f() {
        return this.b;
    }

    public synchronized boolean g() {
        if (this.b) {
            return false;
        }
        return this.a.isMutable();
    }

    public synchronized Bitmap h() {
        if (this.b) {
            return null;
        }
        return this.a;
    }
}
