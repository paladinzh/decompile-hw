package com.avast.android.shepherd.obfuscated;

import java.io.IOException;
import java.io.OutputStream;
import javax.crypto.Mac;

/* compiled from: Unknown */
public class m extends OutputStream {
    private final Mac a;
    private final OutputStream b;

    public m(Mac mac, OutputStream outputStream) {
        this.a = mac;
        this.b = outputStream;
    }

    public byte[] a() {
        return this.a.doFinal();
    }

    public void close() {
        this.b.close();
    }

    protected void finalize() {
        super.finalize();
        try {
            close();
        } catch (IOException e) {
        }
    }

    public void flush() {
        this.b.flush();
    }

    public void write(int i) {
        this.a.update((byte) i);
        this.b.write(i);
    }

    public void write(byte[] bArr) {
        this.a.update(bArr);
        this.b.write(bArr);
    }

    public void write(byte[] bArr, int i, int i2) {
        this.a.update(bArr, i, i2);
        this.b.write(bArr, i, i2);
    }
}
