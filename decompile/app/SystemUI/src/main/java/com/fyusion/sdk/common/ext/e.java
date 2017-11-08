package com.fyusion.sdk.common.ext;

import android.opengl.GLES20;
import com.fyusion.sdk.core.util.b;
import com.fyusion.sdk.core.util.c;

/* compiled from: Unknown */
public class e {
    private int a = -1;
    private int b = -1;
    private int c = -1;
    private boolean d = false;

    /* compiled from: Unknown */
    public static class a {
        public int a = -1;
        public int b = -1;
        public boolean c = false;
    }

    private void b(a aVar) {
        int[] iArr = new int[]{-1};
        GLES20.glGenTextures(1, iArr, 0);
        this.c = iArr[0];
        GLES20.glBindTexture(3553, this.c);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexImage2D(3553, 0, 6407, aVar.a, aVar.b, 0, 6407, 5121, null);
        GLES20.glBindTexture(3553, 0);
    }

    private void c(a aVar) {
        if (aVar.c) {
            int[] iArr = new int[]{-1};
            GLES20.glGenRenderbuffers(1, iArr, 0);
            this.b = iArr[0];
            GLES20.glBindRenderbuffer(36161, this.b);
            GLES20.glRenderbufferStorage(36161, 33189, aVar.a, aVar.b);
            GLES20.glBindRenderbuffer(36161, 0);
        }
    }

    private void d(a aVar) {
        int[] iArr = new int[]{-1};
        GLES20.glGenFramebuffers(1, iArr, 0);
        this.a = iArr[0];
        GLES20.glBindFramebuffer(36160, this.a);
        if (this.c > 0) {
            GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.c, 0);
            if (!aVar.c) {
                GLES20.glDisable(2929);
            } else if (this.b > 0) {
                GLES20.glFramebufferRenderbuffer(36160, 36096, 36161, this.b);
                GLES20.glEnable(2929);
            } else {
                throw new RuntimeException("Please initialize the depth render buffer first");
            }
            c.b();
            GLES20.glBindFramebuffer(36160, 0);
            return;
        }
        throw new RuntimeException("Please initialize the texture first");
    }

    private void f() {
        GLES20.glDeleteTextures(1, new int[]{this.c}, 0);
        this.c = -1;
    }

    private void g() {
        GLES20.glDeleteRenderbuffers(1, new int[]{this.b}, 0);
        this.b = -1;
    }

    private void h() {
        GLES20.glDeleteFramebuffers(1, new int[]{this.a}, 0);
        this.a = -1;
    }

    public void a() {
        if (b()) {
            f();
            g();
            h();
            this.d = false;
        }
    }

    public void a(a aVar) {
        b(aVar);
        c(aVar);
        d(aVar);
        this.d = true;
    }

    public boolean b() {
        return this.d;
    }

    public void c() {
        b.a(b());
        GLES20.glBindFramebuffer(36160, this.a);
    }

    public void d() {
        GLES20.glBindFramebuffer(36160, 0);
    }

    public int e() {
        return this.c;
    }
}
