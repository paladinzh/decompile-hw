package com.avast.android.sdk.engine;

import com.avast.android.sdk.engine.obfuscated.al;
import com.avast.android.sdk.engine.obfuscated.ao;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* compiled from: Unknown */
public class PrivacyScanResult {
    private int a = -1;

    /* compiled from: Unknown */
    private enum a {
        PAYLOAD_PRIVACY_RANK((short) 0);
        
        private static final Map<Short, a> b = null;
        private final short c;

        static {
            b = new HashMap();
            Iterator it = EnumSet.allOf(a.class).iterator();
            while (it.hasNext()) {
                a aVar = (a) it.next();
                b.put(Short.valueOf(aVar.a()), aVar);
            }
        }

        private a(short s) {
            this.c = (short) s;
        }

        public static a a(short s) {
            return (a) b.get(Short.valueOf(s));
        }

        public final short a() {
            return this.c;
        }
    }

    static PrivacyScanResult a(byte[] bArr) {
        PrivacyScanResult privacyScanResult = new PrivacyScanResult();
        if (bArr == null) {
            return privacyScanResult;
        }
        try {
            if (((Integer) al.a(bArr, null, Integer.TYPE, 0)).intValue() + 4 == bArr.length) {
                int i = 4;
                while (i < bArr.length) {
                    int intValue = ((Integer) al.a(bArr, null, Integer.TYPE, i)).intValue();
                    i += 4;
                    if (bArr[(i + intValue) - 1] == (byte) -1) {
                        a a = a.a(((Short) al.a(bArr, null, Short.TYPE, i)).shortValue());
                        i += 2;
                        if (a != null) {
                            switch (e.a[a.ordinal()]) {
                                case 1:
                                    Integer num = (Integer) al.a(bArr, null, Integer.TYPE, i);
                                    if (num == null) {
                                        break;
                                    }
                                    privacyScanResult.a = num.intValue();
                                    break;
                                default:
                                    break;
                            }
                        }
                        i += intValue;
                    } else {
                        throw new IllegalArgumentException("Invalid payload length");
                    }
                }
                return privacyScanResult;
            }
            throw new IllegalArgumentException("Invalid structure length");
        } catch (Throwable e) {
            ao.d("Exception parsing VPS privacy information result", e);
        }
    }

    public int getRank() {
        return this.a;
    }
}
