package com.fyusion.sdk.common.ext.filter.a;

import android.opengl.GLES20;
import android.util.Log;
import com.fyusion.sdk.common.q;
import java.util.ArrayList;
import java.util.Iterator;

/* compiled from: Unknown */
public class o {
    public int a;
    public int b;
    public int c;
    public int d;
    boolean e = false;
    boolean f = false;

    public o(int i, int i2) {
        this.c = i;
        this.d = i2;
        int[] iArr = new int[1];
        GLES20.glGenFramebuffers(1, iArr, 0);
        int i3 = iArr[0];
        GLES20.glBindFramebuffer(36160, i3);
        GLES20.glGenTextures(1, iArr, 0);
        int i4 = iArr[0];
        GLES20.glBindTexture(3553, i4);
        GLES20.glTexImage2D(3553, 0, 6407, i, i2, 0, 6407, 5121, null);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glFramebufferTexture2D(36160, 36064, 3553, i4, 0);
        q.b();
        this.a = i3;
        this.b = i4;
        GLES20.glBindFramebuffer(36160, 0);
        if (this.f) {
            Log.v("FrameBufferObject", "FrameBufferObject(int width, int height), fboId: " + this.a);
            Log.v("FrameBufferObject", "FrameBufferObject(int width, int height), textureId: " + this.b);
        }
    }

    public o(int i, int i2, int i3) {
        this.c = i;
        this.d = i2;
        int[] iArr = new int[1];
        GLES20.glGenFramebuffers(1, iArr, 0);
        int i4 = iArr[0];
        GLES20.glBindFramebuffer(36160, i4);
        GLES20.glBindTexture(3553, i3);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glFramebufferTexture2D(36160, 36064, 3553, i3, 0);
        q.b();
        this.a = i4;
        this.b = i3;
        GLES20.glBindFramebuffer(36160, 0);
        if (this.f) {
            Log.v("FrameBufferObject", "FrameBufferObject(int width, int height, int fbo_textureId), fboId: " + this.a);
            Log.v("FrameBufferObject", "FrameBufferObject(int width, int height, int fbo_textureId), textureId: " + this.b);
        }
    }

    public o(o oVar) {
        this.a = oVar.a;
        this.b = oVar.b;
        this.c = oVar.c;
        this.d = oVar.d;
    }

    public void a() {
        GLES20.glDeleteFramebuffers(1, new int[]{this.a}, 0);
        if (this.f) {
            Log.v("FrameBufferObject", "releaseFBResourceNotTexture, fboId_: " + this.a);
        }
    }

    public void a(o oVar) {
        this.a = oVar.a;
        this.b = oVar.b;
        this.c = oVar.c;
        this.d = oVar.d;
    }

    public void a(ArrayList<Integer> arrayList, ArrayList<Integer> arrayList2) {
        Boolean valueOf;
        Boolean valueOf2 = Boolean.valueOf(true);
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            if (((Integer) it.next()).intValue() == this.b) {
                valueOf = Boolean.valueOf(false);
                break;
            }
        }
        valueOf = valueOf2;
        if (valueOf.booleanValue()) {
            GLES20.glDeleteTextures(1, new int[]{this.b}, 0);
        }
        valueOf2 = Boolean.valueOf(true);
        it = arrayList2.iterator();
        while (it.hasNext()) {
            if (((Integer) it.next()).intValue() == this.a) {
                valueOf = Boolean.valueOf(false);
                break;
            }
        }
        valueOf = valueOf2;
        if (valueOf.booleanValue()) {
            GLES20.glDeleteFramebuffers(1, new int[]{this.a}, 0);
        }
    }

    public void b() {
        int[] iArr = new int[]{this.a};
        GLES20.glDeleteTextures(1, new int[]{this.b}, 0);
        GLES20.glDeleteFramebuffers(1, iArr, 0);
        if (this.f) {
            Log.v("FrameBufferObject", "releaseFBOResource, fboId_: " + this.a);
            Log.v("FrameBufferObject", "releaseFBOResource, fbo_textureId_: " + this.b);
        }
    }
}
