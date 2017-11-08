package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.d$a;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/* compiled from: Unknown */
class di {
    private static final Object XI = null;
    private static Long XJ = new Long(0);
    private static Double XK = new Double(0.0d);
    private static dh XL = dh.v(0);
    private static String XM = new String("");
    private static Boolean XN = new Boolean(false);
    private static List<Object> XO = new ArrayList(0);
    private static Map<Object, Object> XP = new HashMap();
    private static d$a XQ = r(XM);

    private static Boolean bM(String str) {
        return !"true".equalsIgnoreCase(str) ? !"false".equalsIgnoreCase(str) ? XN : Boolean.FALSE : Boolean.TRUE;
    }

    public static String j(d$a d_a) {
        return m(o(d_a));
    }

    public static d$a ku() {
        return XQ;
    }

    public static String m(Object obj) {
        return obj != null ? obj.toString() : XM;
    }

    public static Boolean n(d$a d_a) {
        return q(o(d_a));
    }

    public static Object o(d$a d_a) {
        int i = 0;
        if (d_a == null) {
            return XI;
        }
        d$a[] d_aArr;
        int length;
        switch (d_a.type) {
            case 1:
                return d_a.fY;
            case 2:
                ArrayList arrayList = new ArrayList(d_a.fZ.length);
                d_aArr = d_a.fZ;
                length = d_aArr.length;
                while (i < length) {
                    Object o = o(d_aArr[i]);
                    if (o == XI) {
                        return XI;
                    }
                    arrayList.add(o);
                    i++;
                }
                return arrayList;
            case 3:
                if (d_a.ga.length == d_a.gb.length) {
                    Map hashMap = new HashMap(d_a.gb.length);
                    while (i < d_a.ga.length) {
                        Object o2 = o(d_a.ga[i]);
                        Object o3 = o(d_a.gb[i]);
                        if (o2 == XI || o3 == XI) {
                            return XI;
                        }
                        hashMap.put(o2, o3);
                        i++;
                    }
                    return hashMap;
                }
                bh.t("Converting an invalid value to object: " + d_a.toString());
                return XI;
            case 4:
                bh.t("Trying to convert a macro reference to object");
                return XI;
            case 5:
                bh.t("Trying to convert a function id to object");
                return XI;
            case 6:
                return Long.valueOf(d_a.ge);
            case 7:
                StringBuffer stringBuffer = new StringBuffer();
                d_aArr = d_a.gg;
                length = d_aArr.length;
                while (i < length) {
                    String j = j(d_aArr[i]);
                    if (j == XM) {
                        return XI;
                    }
                    stringBuffer.append(j);
                    i++;
                }
                return stringBuffer.toString();
            case 8:
                return Boolean.valueOf(d_a.gf);
            default:
                bh.t("Failed to convert a value of type: " + d_a.type);
                return XI;
        }
    }

    public static Boolean q(Object obj) {
        return !(obj instanceof Boolean) ? bM(m(obj)) : (Boolean) obj;
    }

    public static d$a r(Object obj) {
        boolean z = false;
        d$a d_a = new d$a();
        if (obj instanceof d$a) {
            return (d$a) obj;
        }
        if (obj instanceof String) {
            d_a.type = 1;
            d_a.fY = (String) obj;
        } else {
            boolean z2;
            List arrayList;
            boolean z3;
            if (obj instanceof List) {
                d_a.type = 2;
                List<Object> list = (List) obj;
                arrayList = new ArrayList(list.size());
                z2 = false;
                for (Object r : list) {
                    d$a r2 = r(r);
                    if (r2 == XQ) {
                        return XQ;
                    }
                    z3 = z2 || r2.gi;
                    arrayList.add(r2);
                    z2 = z3;
                }
                d_a.fZ = (d$a[]) arrayList.toArray(new d$a[0]);
            } else if (obj instanceof Map) {
                d_a.type = 3;
                Set<Entry> entrySet = ((Map) obj).entrySet();
                arrayList = new ArrayList(entrySet.size());
                List arrayList2 = new ArrayList(entrySet.size());
                z2 = false;
                for (Entry entry : entrySet) {
                    d$a r3 = r(entry.getKey());
                    d$a r4 = r(entry.getValue());
                    if (r3 == XQ || r4 == XQ) {
                        return XQ;
                    }
                    z3 = z2 || r3.gi || r4.gi;
                    arrayList.add(r3);
                    arrayList2.add(r4);
                    z2 = z3;
                }
                d_a.ga = (d$a[]) arrayList.toArray(new d$a[0]);
                d_a.gb = (d$a[]) arrayList2.toArray(new d$a[0]);
            } else if (s(obj)) {
                d_a.type = 1;
                d_a.fY = obj.toString();
            } else if (t(obj)) {
                d_a.type = 6;
                d_a.ge = u(obj);
            } else if (obj instanceof Boolean) {
                d_a.type = 8;
                d_a.gf = ((Boolean) obj).booleanValue();
            } else {
                bh.t("Converting to Value from unknown object type: " + (obj != null ? obj.getClass().toString() : "null"));
                return XQ;
            }
            z = z2;
        }
        d_a.gi = z;
        return d_a;
    }

    private static boolean s(Object obj) {
        if (!((obj instanceof Double) || (obj instanceof Float))) {
            if (!(obj instanceof dh)) {
                return false;
            }
            if (!((dh) obj).kj()) {
                return false;
            }
        }
        return true;
    }

    private static boolean t(Object obj) {
        if (!((obj instanceof Byte) || (obj instanceof Short) || (obj instanceof Integer) || (obj instanceof Long))) {
            if (!(obj instanceof dh)) {
                return false;
            }
            if (!((dh) obj).kk()) {
                return false;
            }
        }
        return true;
    }

    private static long u(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        bh.t("getInt64 received non-Number");
        return 0;
    }
}
