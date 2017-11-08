package com.avast.android.sdk.engine.internal;

import android.content.Context;
import com.avast.android.sdk.engine.internal.q.c;
import com.avast.android.sdk.engine.internal.vps.a.b;
import com.avast.android.sdk.engine.obfuscated.al;
import com.avast.android.sdk.engine.obfuscated.ao;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public class a {
    public static Integer a(Context context) {
        l.a();
        Map hashMap = new HashMap();
        hashMap.put(Short.valueOf(b.CONTEXT_CONTEXT_ID.a()), context);
        Integer a = a((byte[]) q.a(context, c.ACQUIRE_VPS_CONTEXT, hashMap));
        if (a == null || a.intValue() < 0) {
            l.b();
        }
        ao.a("EngineInterface: acquired VPS context ID = " + a);
        return a;
    }

    private static Integer a(byte[] bArr) {
        try {
            int i = 4;
            if (((Integer) al.a(bArr, null, Integer.TYPE, 0)).intValue() + 4 == bArr.length) {
                while (true) {
                    int i2 = i;
                    if (i2 >= bArr.length) {
                        return Integer.valueOf(-1);
                    }
                    int intValue = ((Integer) al.a(bArr, null, Integer.TYPE, i2)).intValue();
                    i2 += 4;
                    if (bArr[(i2 + intValue) - 1] == (byte) -1) {
                        b a = b.a(((Short) al.a(bArr, null, Short.TYPE, i2)).shortValue());
                        if (a != null) {
                            switch (b.a[a.ordinal()]) {
                                case 1:
                                    return (Integer) al.a(bArr, null, Integer.TYPE, i2 + 2);
                                default:
                                    break;
                            }
                        }
                        i = i2 + intValue;
                    } else {
                        throw new IllegalArgumentException("parseContextId Invalid payload length");
                    }
                }
            }
            throw new IllegalArgumentException("Invalid structure length");
        } catch (Throwable e) {
            ao.b("Exception parsing context ID", e);
        }
    }

    public static void a(Context context, int i) {
        if (i >= 0) {
            Map hashMap = new HashMap();
            hashMap.put(Short.valueOf(b.CONTEXT_ID_INTEGER_ID.a()), Integer.valueOf(i));
            hashMap.put(Short.valueOf(b.CONTEXT_CONTEXT_ID.a()), context);
            q.a(context, c.RELEASE_VPS_CONTEXT, hashMap);
            l.b();
            ao.a("EngineInterface: released VPS context ID = " + i);
        }
    }
}
