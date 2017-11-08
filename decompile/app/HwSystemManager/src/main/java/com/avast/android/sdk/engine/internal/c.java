package com.avast.android.sdk.engine.internal;

import com.avast.android.sdk.engine.obfuscated.al;
import com.avast.android.sdk.engine.obfuscated.ao;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/* compiled from: Unknown */
public class c {
    public final String a;

    /* compiled from: Unknown */
    private enum a {
        PAYLOAD_PREFIX((short) 0);
        
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

    /* compiled from: Unknown */
    public enum b {
        ADDONS,
        MALWARE
    }

    private c(String str) {
        this.a = str;
    }

    public static Integer a() {
        return Integer.valueOf(Integer.parseInt("dp-1".substring("dp-1".indexOf("-") + 1)));
    }

    public static List<c> a(byte[] bArr) {
        List<c> linkedList = new LinkedList();
        if (bArr == null) {
            return linkedList;
        }
        ao.a(al.a(bArr));
        int i = 0;
        while (i < bArr.length) {
            int intValue = ((Integer) al.a(bArr, null, Integer.TYPE, i)).intValue() + 4;
            Object obj = new byte[intValue];
            System.arraycopy(bArr, i, obj, 0, intValue);
            intValue += i;
            c b = b(obj);
            if (b != null) {
                linkedList.add(b);
            }
            i = intValue;
        }
        return linkedList;
    }

    public static c b(byte[] bArr) {
        c cVar = null;
        try {
            if (((Integer) al.a(bArr, null, Integer.TYPE, 0)).intValue() + 4 == bArr.length) {
                int i = 4;
                while (i < bArr.length) {
                    int intValue = ((Integer) al.a(bArr, null, Integer.TYPE, i)).intValue();
                    i += 4;
                    if (bArr[(i + intValue) - 1] == (byte) -1) {
                        c cVar2;
                        a a = a.a(((Short) al.a(bArr, null, Short.TYPE, i)).shortValue());
                        if (a != null) {
                            switch (d.a[a.ordinal()]) {
                                case 1:
                                    cVar2 = new c(new String(bArr, i + 2, (intValue - 2) - 1));
                                    continue;
                                default:
                                    break;
                            }
                        }
                        cVar2 = cVar;
                        i += intValue;
                        cVar = cVar2;
                    } else {
                        throw new IllegalArgumentException("Invalid payload length");
                    }
                }
                return cVar;
            }
            throw new IllegalArgumentException("Invalid structure length");
        } catch (Throwable e) {
            ao.d("Exception parsing detection prefix", e);
        }
    }
}
