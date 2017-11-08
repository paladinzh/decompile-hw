package com.avast.android.sdk.engine.obfuscated;

import java.nio.charset.Charset;

/* compiled from: Unknown */
public class j {
    public static byte[] a(byte[] bArr, long j) throws g {
        try {
            return f.a(f.a(k.a(j), bArr), Charset.defaultCharset().encode("").array(), bArr.length);
        } catch (Exception e) {
            throw new g(e);
        } catch (Exception e2) {
            throw new g(e2);
        } catch (Exception e22) {
            throw new g(e22);
        }
    }
}
