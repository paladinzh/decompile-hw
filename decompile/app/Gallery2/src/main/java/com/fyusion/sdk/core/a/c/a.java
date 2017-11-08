package com.fyusion.sdk.core.a.c;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;
import com.fyusion.sdk.common.HardwareAbstractionLayer;
import com.fyusion.sdk.core.a.b;
import com.fyusion.sdk.core.util.GLHelper;
import com.fyusion.sdk.core.util.d;
import com.fyusion.sdk.core.util.pool.ByteBufferPool;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* compiled from: Unknown */
public class a extends com.fyusion.sdk.core.a.a {
    private int[] c;
    private int d;
    private int e;
    private int f;
    private boolean g;

    public a(int i, int i2) {
        super(((i % 8 <= 0 ? 0 : 8 - (i % 8)) + i) / 4, i2, new b(i, i2, ((i % 8 <= 0 ? 0 : 8 - (i % 8)) + i) / 4, i2));
        this.c = new int[1];
        this.g = true;
        this.f = i;
        this.d = 0;
        this.e = (((this.a * i2) * 3) / 2) * 4;
        if (HardwareAbstractionLayer.supportedGLESVersion() < 3) {
            this.g = false;
            return;
        }
        GLES20.glGenBuffers(this.c.length, this.c, 0);
        for (int glBindBuffer : this.c) {
            GLES20.glBindBuffer(35051, glBindBuffer);
            GLES20.glBufferData(35051, this.e, null, 35041);
        }
        GLES20.glBindBuffer(35051, 0);
    }

    private static ByteBuffer a(ByteBuffer byteBuffer) {
        ByteBuffer byteBuffer2 = (ByteBuffer) ByteBufferPool.INSTANCE.mustAcquire(byteBuffer.capacity());
        byteBuffer2.rewind();
        byteBuffer2.put(byteBuffer);
        return byteBuffer2;
    }

    private b e() {
        ByteBuffer byteBuffer = (ByteBuffer) ByteBufferPool.INSTANCE.mustAcquire(this.e);
        GLES20.glFinish();
        GLES20.glReadPixels(0, 0, this.a, (this.b * 3) / 2, 6408, 5121, byteBuffer);
        return new b(byteBuffer, 1, this.f, this.b, this.a * 4);
    }

    private b f() {
        GLES30.glReadBuffer(1029);
        GLES30.glBindBuffer(35051, this.c[this.d]);
        d.a();
        GLHelper.readBufferToPBO(this.a, (this.b * 3) / 2, 6408);
        ByteBuffer order = ((ByteBuffer) GLES30.glMapBufferRange(35051, 0, this.e, 1)).order(ByteOrder.nativeOrder());
        if (order.limit() < this.e) {
            Log.e("YUVCodecOutputSurface", "Buffer too small");
        }
        b bVar = new b(a(order), 1, this.f, this.b, this.a * 4);
        GLES30.glUnmapBuffer(35051);
        GLES20.glBindBuffer(35051, 0);
        return bVar;
    }

    public b d() {
        return !this.g ? e() : f();
    }
}
