package com.fyusion.sdk.core.util.pool;

import java.nio.ByteBuffer;

/* compiled from: Unknown */
public class ByteBufferPool extends a<ByteBuffer> {
    public static final ByteBufferPool INSTANCE = new ByteBufferPool(10);

    public ByteBufferPool(int i) {
        super(i);
    }

    protected /* synthetic */ Object a(int i) {
        return b(i);
    }

    protected boolean a(ByteBuffer byteBuffer, int i) {
        return byteBuffer.capacity() >= i;
    }

    protected ByteBuffer b(int i) {
        return ByteBuffer.allocateDirect(i);
    }
}
