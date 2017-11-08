package com.fyusion.sdk.core.a.b;

import android.opengl.GLES20;
import com.fyusion.sdk.core.a.d;
import java.util.concurrent.atomic.AtomicInteger;
import org.mtnwrw.pdqimg.PDQImage;
import org.mtnwrw.pdqimg.PDQImage.Plane;

/* compiled from: Unknown */
public class c implements d<PDQImage> {
    private static AtomicInteger a = new AtomicInteger(0);
    private boolean b = false;
    private PDQImage c;

    public c(PDQImage pDQImage) {
        if (pDQImage != null) {
            this.c = pDQImage;
            a.addAndGet(1);
            return;
        }
        throw new IllegalArgumentException("Image is null");
    }

    private boolean a(int i) {
        return i == this.c.getBitDepth();
    }

    public /* synthetic */ Object a() {
        return h();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void a(int i, int i2) {
        if (!f()) {
            Plane[] planes = this.c.getPlanes();
            int width = this.c.getWidth();
            int height = this.c.getHeight();
            if (planes[0] != null) {
                if (!(planes[1] == null || planes[2] == null)) {
                    planes[0].getBuffer().rewind();
                    GLES20.glBindTexture(3553, i);
                    GLES20.glTexImage2D(3553, 0, 6409, width, height, 0, 6409, 5121, planes[0].getBuffer());
                    GLES20.glBindTexture(3553, i2);
                    GLES20.glTexImage2D(3553, 0, 6410, this.c.getWidth() / 2, this.c.getHeight() / 2, 0, 6410, 5121, this.c.getFormat() != 17 ? planes[1].getBuffer() : planes[2].getBuffer());
                }
            }
        }
    }

    public synchronized void a(int i, int i2, int i3, int i4, int i5) {
        if (!f()) {
            GLES20.glUniform1i(i, 2);
            GLES20.glUniform1i(i2, 1);
            GLES20.glUniform1i(i3, 0);
            GLES20.glUniform1i(i4, b());
            GLES20.glUniform1i(i5, c());
        }
    }

    public boolean a(int i, int i2, int i3) {
        boolean z = false;
        if (this.b || !a(i3)) {
            return false;
        }
        if (this.c.getWidth() == i && this.c.getHeight() == i2) {
            z = true;
        }
        return z;
    }

    public boolean a(Class<?> cls) {
        return cls == PDQImage.class;
    }

    public synchronized int b() {
        if (this.b) {
            return 0;
        }
        return this.c.getWidth();
    }

    public synchronized int c() {
        if (this.b) {
            return 0;
        }
        return this.c.getHeight();
    }

    public synchronized int d() {
        if (this.b) {
            return 0;
        }
        return this.c.getSize();
    }

    public synchronized void e() {
        if (!this.b) {
            this.b = true;
            this.c.close();
        }
    }

    public synchronized boolean f() {
        return this.b;
    }

    protected void finalize() throws Throwable {
        a.addAndGet(-1);
        super.finalize();
    }

    public synchronized boolean g() {
        boolean z = false;
        synchronized (this) {
            if (!this.b) {
                z = true;
            }
        }
        return z;
    }

    public synchronized PDQImage h() {
        if (this.b) {
            return null;
        }
        return this.c;
    }
}
