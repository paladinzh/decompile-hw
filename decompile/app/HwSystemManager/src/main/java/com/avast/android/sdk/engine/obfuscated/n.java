package com.avast.android.sdk.engine.obfuscated;

import java.io.IOException;
import java.io.OutputStream;
import javax.crypto.Mac;

/* compiled from: Unknown */
public class n extends OutputStream {
    private final Mac a;
    private final OutputStream b;

    public n(Mac mac, OutputStream outputStream) {
        this.a = mac;
        this.b = outputStream;
    }

    public byte[] a() {
        return this.a.doFinal();
    }

    public void close() throws IOException {
        this.b.close();
    }

    protected void finalize() throws Throwable {
        super.finalize();
        try {
            close();
        } catch (IOException e) {
        }
    }

    public void flush() throws IOException {
        this.b.flush();
    }

    public void write(int i) throws IOException {
        this.a.update((byte) i);
        this.b.write(i);
    }

    public void write(byte[] bArr) throws IOException {
        this.a.update(bArr);
        this.b.write(bArr);
    }

    public void write(byte[] bArr, int i, int i2) throws IOException {
        this.a.update(bArr, i, i2);
        this.b.write(bArr, i, i2);
    }
}
