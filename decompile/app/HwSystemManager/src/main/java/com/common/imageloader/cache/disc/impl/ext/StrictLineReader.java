package com.common.imageloader.cache.disc.impl.ext;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

class StrictLineReader implements Closeable {
    private static final byte CR = (byte) 13;
    private static final byte LF = (byte) 10;
    private byte[] buf;
    private final Charset charset;
    private int end;
    private final InputStream in;
    private int pos;

    public StrictLineReader(InputStream in, Charset charset) {
        this(in, 8192, charset);
    }

    public StrictLineReader(InputStream in, int capacity, Charset charset) {
        if (in == null || charset == null) {
            throw new NullPointerException();
        } else if (capacity < 0) {
            throw new IllegalArgumentException("capacity <= 0");
        } else if (charset.equals(Util.US_ASCII)) {
            this.in = in;
            this.charset = charset;
            this.buf = new byte[capacity];
        } else {
            throw new IllegalArgumentException("Unsupported encoding");
        }
    }

    public void close() throws IOException {
        synchronized (this.in) {
            if (this.buf != null) {
                this.buf = null;
                this.in.close();
            }
        }
    }

    public java.lang.String readLine() throws java.io.IOException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:37)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:61)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r10 = this;
        r9 = 10;
        r5 = r10.in;
        monitor-enter(r5);
        r4 = r10.buf;	 Catch:{ all -> 0x0012 }
        if (r4 != 0) goto L_0x0015;	 Catch:{ all -> 0x0012 }
    L_0x0009:
        r4 = new java.io.IOException;	 Catch:{ all -> 0x0012 }
        r6 = "LineReader is closed";	 Catch:{ all -> 0x0012 }
        r4.<init>(r6);	 Catch:{ all -> 0x0012 }
        throw r4;	 Catch:{ all -> 0x0012 }
    L_0x0012:
        r4 = move-exception;
        monitor-exit(r5);
        throw r4;
    L_0x0015:
        r4 = r10.pos;	 Catch:{ all -> 0x0012 }
        r6 = r10.end;	 Catch:{ all -> 0x0012 }
        if (r4 < r6) goto L_0x001e;	 Catch:{ all -> 0x0012 }
    L_0x001b:
        r10.fillBuf();	 Catch:{ all -> 0x0012 }
    L_0x001e:
        r0 = r10.pos;	 Catch:{ all -> 0x0012 }
    L_0x0020:
        r4 = r10.end;	 Catch:{ all -> 0x0012 }
        if (r0 == r4) goto L_0x0058;	 Catch:{ all -> 0x0012 }
    L_0x0024:
        r4 = r10.buf;	 Catch:{ all -> 0x0012 }
        r4 = r4[r0];	 Catch:{ all -> 0x0012 }
        if (r4 != r9) goto L_0x0055;	 Catch:{ all -> 0x0012 }
    L_0x002a:
        r4 = r10.pos;	 Catch:{ all -> 0x0012 }
        if (r0 == r4) goto L_0x0053;	 Catch:{ all -> 0x0012 }
    L_0x002e:
        r4 = r10.buf;	 Catch:{ all -> 0x0012 }
        r6 = r0 + -1;	 Catch:{ all -> 0x0012 }
        r4 = r4[r6];	 Catch:{ all -> 0x0012 }
        r6 = 13;	 Catch:{ all -> 0x0012 }
        if (r4 != r6) goto L_0x0053;	 Catch:{ all -> 0x0012 }
    L_0x0038:
        r1 = r0 + -1;	 Catch:{ all -> 0x0012 }
    L_0x003a:
        r3 = new java.lang.String;	 Catch:{ all -> 0x0012 }
        r4 = r10.buf;	 Catch:{ all -> 0x0012 }
        r6 = r10.pos;	 Catch:{ all -> 0x0012 }
        r7 = r10.pos;	 Catch:{ all -> 0x0012 }
        r7 = r1 - r7;	 Catch:{ all -> 0x0012 }
        r8 = r10.charset;	 Catch:{ all -> 0x0012 }
        r8 = r8.name();	 Catch:{ all -> 0x0012 }
        r3.<init>(r4, r6, r7, r8);	 Catch:{ all -> 0x0012 }
        r4 = r0 + 1;	 Catch:{ all -> 0x0012 }
        r10.pos = r4;	 Catch:{ all -> 0x0012 }
        monitor-exit(r5);
        return r3;
    L_0x0053:
        r1 = r0;
        goto L_0x003a;
    L_0x0055:
        r0 = r0 + 1;
        goto L_0x0020;
    L_0x0058:
        r2 = new com.common.imageloader.cache.disc.impl.ext.StrictLineReader$1;	 Catch:{ all -> 0x0012 }
        r4 = r10.end;	 Catch:{ all -> 0x0012 }
        r6 = r10.pos;	 Catch:{ all -> 0x0012 }
        r4 = r4 - r6;	 Catch:{ all -> 0x0012 }
        r4 = r4 + 80;	 Catch:{ all -> 0x0012 }
        r2.<init>(r4);	 Catch:{ all -> 0x0012 }
    L_0x0064:
        r4 = r10.buf;	 Catch:{ all -> 0x00a3 }
        r6 = r10.pos;	 Catch:{ all -> 0x00a3 }
        r7 = r10.end;	 Catch:{ all -> 0x00a3 }
        r8 = r10.pos;	 Catch:{ all -> 0x00a3 }
        r7 = r7 - r8;	 Catch:{ all -> 0x00a3 }
        r2.write(r4, r6, r7);	 Catch:{ all -> 0x00a3 }
        r4 = -1;	 Catch:{ all -> 0x00a3 }
        r10.end = r4;	 Catch:{ all -> 0x00a3 }
        r10.fillBuf();	 Catch:{ all -> 0x00a3 }
        r0 = r10.pos;	 Catch:{ all -> 0x00a3 }
    L_0x0078:
        r4 = r10.end;	 Catch:{ all -> 0x00a3 }
        if (r0 == r4) goto L_0x0064;	 Catch:{ all -> 0x00a3 }
    L_0x007c:
        r4 = r10.buf;	 Catch:{ all -> 0x00a3 }
        r4 = r4[r0];	 Catch:{ all -> 0x00a3 }
        if (r4 != r9) goto L_0x00a0;	 Catch:{ all -> 0x00a3 }
    L_0x0082:
        r4 = r10.pos;	 Catch:{ all -> 0x00a3 }
        if (r0 == r4) goto L_0x0091;	 Catch:{ all -> 0x00a3 }
    L_0x0086:
        r4 = r10.buf;	 Catch:{ all -> 0x00a3 }
        r6 = r10.pos;	 Catch:{ all -> 0x00a3 }
        r7 = r10.pos;	 Catch:{ all -> 0x00a3 }
        r7 = r0 - r7;	 Catch:{ all -> 0x00a3 }
        r2.write(r4, r6, r7);	 Catch:{ all -> 0x00a3 }
    L_0x0091:
        r4 = r0 + 1;	 Catch:{ all -> 0x00a3 }
        r10.pos = r4;	 Catch:{ all -> 0x00a3 }
        r4 = r2.toString();	 Catch:{ all -> 0x00a3 }
        if (r2 == 0) goto L_0x009e;
    L_0x009b:
        r2.close();	 Catch:{ all -> 0x0012 }
    L_0x009e:
        monitor-exit(r5);
        return r4;
    L_0x00a0:
        r0 = r0 + 1;
        goto L_0x0078;
    L_0x00a3:
        r4 = move-exception;
        if (r2 == 0) goto L_0x00a9;
    L_0x00a6:
        r2.close();	 Catch:{ all -> 0x0012 }
    L_0x00a9:
        throw r4;	 Catch:{ all -> 0x0012 }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.common.imageloader.cache.disc.impl.ext.StrictLineReader.readLine():java.lang.String");
    }

    private void fillBuf() throws IOException {
        int result = this.in.read(this.buf, 0, this.buf.length);
        if (result == -1) {
            throw new EOFException();
        }
        this.pos = 0;
        this.end = result;
    }
}
