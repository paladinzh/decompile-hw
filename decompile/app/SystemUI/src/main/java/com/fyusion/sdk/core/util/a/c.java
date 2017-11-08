package com.fyusion.sdk.core.util.a;

import java.nio.ByteBuffer;

/* compiled from: Unknown */
public class c extends a<ByteBuffer> {
    public static final c a = new c(10);

    public c(int i) {
        super(i);
    }

    protected /* synthetic */ Object a(int i) {
        return d(i);
    }

    protected boolean a(ByteBuffer byteBuffer, int i) {
        return byteBuffer.capacity() >= i;
    }

    protected ByteBuffer d(int i) {
        return ByteBuffer.allocateDirect(i);
    }
}
