package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.as.i.b;
import com.avast.android.shepherd.obfuscated.as.m;
import java.io.InputStream;
import java.nio.ByteBuffer;

/* compiled from: Unknown */
public class aq implements ar {
    private final byte[] a;

    public aq(byte[] bArr) {
        this.a = new byte[bArr.length];
        System.arraycopy(bArr, 0, this.a, 0, bArr.length);
    }

    public InputStream a(Long l, Long l2, m mVar) {
        if (l == null) {
            l = Long.valueOf(0);
        }
        if (l2 != null) {
            if ((l.longValue() + l2.longValue() <= ((long) this.a.length) ? 1 : null) == null) {
            }
            return new a(ByteBuffer.wrap(this.a, l.intValue(), l2.intValue()));
        }
        l2 = Long.valueOf(((long) this.a.length) - l.longValue());
        return new a(ByteBuffer.wrap(this.a, l.intValue(), l2.intValue()));
    }

    public void a(b bVar) {
    }
}
