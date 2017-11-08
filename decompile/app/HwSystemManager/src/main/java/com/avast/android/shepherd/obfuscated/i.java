package com.avast.android.shepherd.obfuscated;

import java.nio.charset.Charset;

/* compiled from: Unknown */
public class i {
    public static byte[] a(byte[] bArr, long j) {
        try {
            return e.a(e.a(j.a(j), bArr), Charset.defaultCharset().encode("").array(), bArr.length);
        } catch (Exception e) {
            throw new f(e);
        } catch (Exception e2) {
            throw new f(e2);
        } catch (Exception e22) {
            throw new f(e22);
        }
    }
}
