package com.avast.android.sdk.engine.obfuscated;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/* compiled from: Unknown */
public class a extends OutputStream {
    protected final int a;
    protected final List<ByteBuffer> b = new LinkedList();
    protected ByteBuffer c;
    protected int d = 0;

    public a(int i) {
        this.a = i;
        this.c = ByteBuffer.allocate(Math.max(1024, i));
    }

    protected void a() {
        this.c.flip();
        this.b.add(this.c);
        this.c = ByteBuffer.allocate(Math.max(1024, this.a));
    }

    public synchronized ByteBuffer b() {
        this.c.flip();
        if (this.b.isEmpty()) {
            return this.c;
        }
        ByteBuffer allocate = ByteBuffer.allocate(this.d);
        this.b.add(this.c);
        for (ByteBuffer put : this.b) {
            allocate.put(put);
        }
        allocate.flip();
        return allocate;
    }

    public synchronized void write(int i) throws IOException {
        if (this.c.remaining() == 0) {
            a();
        }
        this.c.put((byte) i);
    }

    public synchronized void write(byte[] bArr, int i, int i2) throws IOException {
        while (i2 > 0) {
            if (this.c.remaining() == 0) {
                a();
            }
            int min = Math.min(this.c.remaining(), i2);
            this.c.put(bArr, i, min);
            i += min;
            i2 -= min;
            this.d = min + this.d;
        }
    }
}
