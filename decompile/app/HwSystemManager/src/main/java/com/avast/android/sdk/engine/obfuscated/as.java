package com.avast.android.sdk.engine.obfuscated;

import com.avast.android.sdk.engine.ProgressObserver;
import java.io.IOException;
import java.io.OutputStream;

/* compiled from: Unknown */
public class as extends OutputStream {
    private OutputStream a;
    private long b;
    private long c = 0;
    private ProgressObserver d;

    public as(OutputStream outputStream, long j, ProgressObserver progressObserver) {
        this.a = outputStream;
        this.b = j;
        this.d = progressObserver;
    }

    public void close() throws IOException {
        this.a.close();
    }

    public void flush() throws IOException {
        this.a.flush();
    }

    public void write(int i) throws IOException {
        this.a.write(i);
        if (this.d != null) {
            ProgressObserver progressObserver = this.d;
            long j = this.c + 1;
            this.c = j;
            progressObserver.onProgressChanged(j, this.b);
        }
    }

    public void write(byte[] bArr) throws IOException {
        this.a.write(bArr);
        if (this.d != null) {
            this.c += (long) bArr.length;
            this.d.onProgressChanged(this.c, this.b);
        }
    }

    public void write(byte[] bArr, int i, int i2) throws IOException {
        this.a.write(bArr, i, i2);
        if (this.d != null) {
            this.c += (long) i2;
            this.d.onProgressChanged(this.c, this.b);
        }
    }
}
