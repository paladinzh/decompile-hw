package com.avast.android.sdk.engine.obfuscated;

import com.google.protobuf.ByteString;

/* compiled from: Unknown */
public class o {
    private final ByteString a;
    private final ByteString b;
    private final long c;

    public o(ByteString byteString, ByteString byteString2, long j) {
        this.a = byteString;
        this.b = byteString2;
        this.c = j;
    }

    public ByteString a() {
        return this.a;
    }

    public ByteString b() {
        return this.b;
    }

    public long c() {
        return this.c;
    }

    public boolean d() {
        return !((this.c > System.currentTimeMillis() ? 1 : (this.c == System.currentTimeMillis() ? 0 : -1)) >= 0);
    }
}
