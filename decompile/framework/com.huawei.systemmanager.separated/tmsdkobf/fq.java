package tmsdkobf;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class fq {
    private ByteBuffer mx;
    protected String my = "GBK";

    public static class a {
        public byte mz;
        public int tag;
    }

    public fq(byte[] bArr) {
        this.mx = ByteBuffer.wrap(bArr);
    }

    public fq(byte[] bArr, int i) {
        this.mx = ByteBuffer.wrap(bArr);
        this.mx.position(i);
    }

    public void d(byte[] bArr) {
        this.mx = ByteBuffer.wrap(bArr);
    }

    public static int a(a aVar, ByteBuffer byteBuffer) {
        byte b = byteBuffer.get();
        aVar.mz = (byte) ((byte) (b & 15));
        aVar.tag = (b & 240) >> 4;
        if (aVar.tag != 15) {
            return 1;
        }
        aVar.tag = byteBuffer.get() & 255;
        return 2;
    }

    public void a(a aVar) {
        a(aVar, this.mx);
    }

    private int b(a aVar) {
        return a(aVar, this.mx.duplicate());
    }

    private void skip(int i) {
        this.mx.position(this.mx.position() + i);
    }

    public boolean af(int i) {
        try {
            a aVar = new a();
            while (true) {
                int b = b(aVar);
                if (i > aVar.tag && aVar.mz != fs.STRUCT_END) {
                    skip(b);
                    a(aVar.mz);
                }
            }
            if (i != aVar.tag) {
                return false;
            }
            return true;
        } catch (fn e) {
            return false;
        } catch (BufferUnderflowException e2) {
            return false;
        }
    }

    public void r() {
        a aVar = new a();
        while (this.mx.remaining() != 0) {
            a(aVar);
            a(aVar.mz);
            if (aVar.mz == fs.STRUCT_END) {
                return;
            }
        }
    }

    private void s() {
        a aVar = new a();
        a(aVar);
        a(aVar.mz);
    }

    private void a(byte b) {
        int i = 0;
        int a;
        switch (b) {
            case (byte) 0:
                skip(1);
                return;
            case (byte) 1:
                skip(2);
                return;
            case (byte) 2:
                skip(4);
                return;
            case (byte) 3:
                skip(8);
                return;
            case (byte) 4:
                skip(4);
                return;
            case (byte) 5:
                skip(8);
                return;
            case (byte) 6:
                i = this.mx.get();
                if (i < 0) {
                    i += 256;
                }
                skip(i);
                return;
            case (byte) 7:
                skip(this.mx.getInt());
                return;
            case (byte) 8:
                a = a(0, 0, true);
                while (i < a * 2) {
                    s();
                    i++;
                }
                return;
            case (byte) 9:
                a = a(0, 0, true);
                while (i < a) {
                    s();
                    i++;
                }
                return;
            case (byte) 10:
                r();
                return;
            case (byte) 11:
            case (byte) 12:
                return;
            case (byte) 13:
                a aVar = new a();
                a(aVar);
                if (aVar.mz == (byte) 0) {
                    skip(a(0, 0, true));
                    return;
                }
                throw new fn("skipField with invalid type, type value: " + b + ", " + aVar.mz);
            default:
                throw new fn("invalid type.");
        }
    }

    public boolean a(boolean z, int i, boolean z2) {
        if (a((byte) 0, i, z2) == (byte) 0) {
            return false;
        }
        return true;
    }

    public byte a(byte b, int i, boolean z) {
        if (af(i)) {
            a aVar = new a();
            a(aVar);
            switch (aVar.mz) {
                case (byte) 0:
                    return this.mx.get();
                case (byte) 11:
                    return b;
                case (byte) 12:
                    return (byte) 0;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (!z) {
            return b;
        } else {
            throw new fn("require field not exist.");
        }
    }

    public short a(short s, int i, boolean z) {
        if (af(i)) {
            a aVar = new a();
            a(aVar);
            switch (aVar.mz) {
                case (byte) 0:
                    return (short) this.mx.get();
                case (byte) 1:
                    return this.mx.getShort();
                case (byte) 11:
                    return s;
                case (byte) 12:
                    return (short) 0;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (!z) {
            return s;
        } else {
            throw new fn("require field not exist.");
        }
    }

    public int a(int i, int i2, boolean z) {
        if (af(i2)) {
            a aVar = new a();
            a(aVar);
            switch (aVar.mz) {
                case (byte) 0:
                    return this.mx.get();
                case (byte) 1:
                    return this.mx.getShort();
                case (byte) 2:
                    return this.mx.getInt();
                case (byte) 11:
                    return i;
                case (byte) 12:
                    return 0;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (!z) {
            return i;
        } else {
            throw new fn("require field not exist.");
        }
    }

    public long a(long j, int i, boolean z) {
        if (af(i)) {
            a aVar = new a();
            a(aVar);
            switch (aVar.mz) {
                case (byte) 0:
                    return (long) this.mx.get();
                case (byte) 1:
                    return (long) this.mx.getShort();
                case (byte) 2:
                    return (long) this.mx.getInt();
                case (byte) 3:
                    return this.mx.getLong();
                case (byte) 12:
                    return 0;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (!z) {
            return j;
        } else {
            throw new fn("require field not exist.");
        }
    }

    public float a(float f, int i, boolean z) {
        if (af(i)) {
            a aVar = new a();
            a(aVar);
            switch (aVar.mz) {
                case (byte) 4:
                    return this.mx.getFloat();
                case (byte) 11:
                    return f;
                case (byte) 12:
                    return 0.0f;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (!z) {
            return f;
        } else {
            throw new fn("require field not exist.");
        }
    }

    public double a(double d, int i, boolean z) {
        if (af(i)) {
            a aVar = new a();
            a(aVar);
            switch (aVar.mz) {
                case (byte) 4:
                    return (double) this.mx.getFloat();
                case (byte) 5:
                    return this.mx.getDouble();
                case (byte) 11:
                    return d;
                case (byte) 12:
                    return 0.0d;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (!z) {
            return d;
        } else {
            throw new fn("require field not exist.");
        }
    }

    public String a(int i, boolean z) {
        if (af(i)) {
            a aVar = new a();
            a(aVar);
            int i2;
            byte[] bArr;
            switch (aVar.mz) {
                case (byte) 6:
                    i2 = this.mx.get();
                    if (i2 < 0) {
                        i2 += 256;
                    }
                    bArr = new byte[i2];
                    this.mx.get(bArr);
                    try {
                        return new String(bArr, this.my);
                    } catch (UnsupportedEncodingException e) {
                        return new String(bArr);
                    }
                case (byte) 7:
                    i2 = this.mx.getInt();
                    if (i2 <= fs.JCE_MAX_STRING_LENGTH && i2 >= 0) {
                        bArr = new byte[i2];
                        this.mx.get(bArr);
                        try {
                            return new String(bArr, this.my);
                        } catch (UnsupportedEncodingException e2) {
                            return new String(bArr);
                        }
                    }
                    throw new fn("String too long: " + i2);
                case (byte) 11:
                    return null;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (!z) {
            return null;
        } else {
            throw new fn("require field not exist.");
        }
    }

    public <K, V> HashMap<K, V> a(Map<K, V> map, int i, boolean z) {
        return (HashMap) a(new HashMap(), map, i, z);
    }

    private <K, V> Map<K, V> a(Map<K, V> map, Map<K, V> map2, int i, boolean z) {
        if (map2 == null || map2.isEmpty()) {
            return new HashMap();
        }
        Entry entry = (Entry) map2.entrySet().iterator().next();
        Object key = entry.getKey();
        Object value = entry.getValue();
        if (af(i)) {
            a aVar = new a();
            a(aVar);
            switch (aVar.mz) {
                case (byte) 8:
                    int a = a(0, 0, true);
                    if (a >= 0) {
                        for (int i2 = 0; i2 < a; i2++) {
                            map.put(b(key, 0, true), b(value, 1, true));
                        }
                        break;
                    }
                    throw new fn("size invalid: " + a);
                case (byte) 11:
                    break;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (z) {
            throw new fn("require field not exist.");
        }
        return map;
    }

    public boolean[] a(boolean[] zArr, int i, boolean z) {
        boolean[] zArr2 = null;
        if (af(i)) {
            a aVar = new a();
            a(aVar);
            switch (aVar.mz) {
                case (byte) 9:
                    int a = a(0, 0, true);
                    if (a >= 0) {
                        zArr2 = new boolean[a];
                        for (int i2 = 0; i2 < a; i2++) {
                            zArr2[i2] = a(zArr2[0], 0, true);
                        }
                        break;
                    }
                    throw new fn("size invalid: " + a);
                case (byte) 11:
                    break;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (z) {
            throw new fn("require field not exist.");
        }
        return zArr2;
    }

    public byte[] a(byte[] bArr, int i, boolean z) {
        if (af(i)) {
            a aVar = new a();
            a(aVar);
            byte[] bArr2;
            switch (aVar.mz) {
                case (byte) 9:
                    int a = a(0, 0, true);
                    if (a >= 0) {
                        bArr2 = new byte[a];
                        for (int i2 = 0; i2 < a; i2++) {
                            bArr2[i2] = (byte) a(bArr2[0], 0, true);
                        }
                        return bArr2;
                    }
                    throw new fn("size invalid: " + a);
                case (byte) 11:
                    return null;
                case (byte) 13:
                    a aVar2 = new a();
                    a(aVar2);
                    if (aVar2.mz == (byte) 0) {
                        int a2 = a(0, 0, true);
                        if (a2 >= 0) {
                            bArr2 = new byte[a2];
                            this.mx.get(bArr2);
                            return bArr2;
                        }
                        throw new fn("invalid size, tag: " + i + ", type: " + aVar.mz + ", " + aVar2.mz + ", size: " + a2);
                    }
                    throw new fn("type mismatch, tag: " + i + ", type: " + aVar.mz + ", " + aVar2.mz);
                default:
                    throw new fn("type mismatch.");
            }
        } else if (!z) {
            return null;
        } else {
            throw new fn("require field not exist.");
        }
    }

    public short[] a(short[] sArr, int i, boolean z) {
        short[] sArr2 = null;
        if (af(i)) {
            a aVar = new a();
            a(aVar);
            switch (aVar.mz) {
                case (byte) 9:
                    int a = a(0, 0, true);
                    if (a >= 0) {
                        sArr2 = new short[a];
                        for (int i2 = 0; i2 < a; i2++) {
                            sArr2[i2] = (short) a(sArr2[0], 0, true);
                        }
                        break;
                    }
                    throw new fn("size invalid: " + a);
                case (byte) 11:
                    break;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (z) {
            throw new fn("require field not exist.");
        }
        return sArr2;
    }

    public int[] a(int[] iArr, int i, boolean z) {
        int[] iArr2 = null;
        if (af(i)) {
            a aVar = new a();
            a(aVar);
            switch (aVar.mz) {
                case (byte) 9:
                    int a = a(0, 0, true);
                    if (a >= 0) {
                        iArr2 = new int[a];
                        for (int i2 = 0; i2 < a; i2++) {
                            iArr2[i2] = a(iArr2[0], 0, true);
                        }
                        break;
                    }
                    throw new fn("size invalid: " + a);
                case (byte) 11:
                    break;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (z) {
            throw new fn("require field not exist.");
        }
        return iArr2;
    }

    public long[] a(long[] jArr, int i, boolean z) {
        long[] jArr2 = null;
        if (af(i)) {
            a aVar = new a();
            a(aVar);
            switch (aVar.mz) {
                case (byte) 9:
                    int a = a(0, 0, true);
                    if (a >= 0) {
                        jArr2 = new long[a];
                        for (int i2 = 0; i2 < a; i2++) {
                            jArr2[i2] = a(jArr2[0], 0, true);
                        }
                        break;
                    }
                    throw new fn("size invalid: " + a);
                case (byte) 11:
                    break;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (z) {
            throw new fn("require field not exist.");
        }
        return jArr2;
    }

    public float[] a(float[] fArr, int i, boolean z) {
        float[] fArr2 = null;
        if (af(i)) {
            a aVar = new a();
            a(aVar);
            switch (aVar.mz) {
                case (byte) 9:
                    int a = a(0, 0, true);
                    if (a >= 0) {
                        fArr2 = new float[a];
                        for (int i2 = 0; i2 < a; i2++) {
                            fArr2[i2] = a(fArr2[0], 0, true);
                        }
                        break;
                    }
                    throw new fn("size invalid: " + a);
                case (byte) 11:
                    break;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (z) {
            throw new fn("require field not exist.");
        }
        return fArr2;
    }

    public double[] a(double[] dArr, int i, boolean z) {
        double[] dArr2 = null;
        if (af(i)) {
            a aVar = new a();
            a(aVar);
            switch (aVar.mz) {
                case (byte) 9:
                    int a = a(0, 0, true);
                    if (a >= 0) {
                        dArr2 = new double[a];
                        for (int i2 = 0; i2 < a; i2++) {
                            dArr2[i2] = a(dArr2[0], 0, true);
                        }
                        break;
                    }
                    throw new fn("size invalid: " + a);
                case (byte) 11:
                    break;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (z) {
            throw new fn("require field not exist.");
        }
        return dArr2;
    }

    public <T> T[] a(T[] tArr, int i, boolean z) {
        if (tArr != null && tArr.length != 0) {
            return a(tArr[0], i, z);
        }
        throw new fn("unable to get type of key and value.");
    }

    public <T> List<T> a(List<T> list, int i, boolean z) {
        int i2 = 0;
        if (list == null || list.isEmpty()) {
            return new ArrayList();
        }
        Object[] a = a(list.get(0), i, z);
        if (a == null) {
            return null;
        }
        List arrayList = new ArrayList();
        while (i2 < a.length) {
            arrayList.add(a[i2]);
            i2++;
        }
        return arrayList;
    }

    private <T> T[] a(T t, int i, boolean z) {
        if (af(i)) {
            a aVar = new a();
            a(aVar);
            switch (aVar.mz) {
                case (byte) 9:
                    int a = a(0, 0, true);
                    if (a >= 0) {
                        Object[] objArr = (Object[]) Array.newInstance(t.getClass(), a);
                        for (int i2 = 0; i2 < a; i2++) {
                            objArr[i2] = b(t, 0, true);
                        }
                        return objArr;
                    }
                    throw new fn("size invalid: " + a);
                case (byte) 11:
                    break;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (z) {
            throw new fn("require field not exist.");
        }
        return null;
    }

    public fs a(fs fsVar, int i, boolean z) {
        fs fsVar2 = null;
        if (af(i)) {
            try {
                fsVar2 = (fs) fsVar.getClass().newInstance();
                a aVar = new a();
                a(aVar);
                if (aVar.mz == (byte) 10) {
                    fsVar2.readFrom(this);
                    r();
                } else {
                    throw new fn("type mismatch.");
                }
            } catch (Exception e) {
                throw new fn(e.getMessage());
            }
        } else if (z) {
            throw new fn("require field not exist.");
        }
        return fsVar2;
    }

    public <T> Object b(T t, int i, boolean z) {
        if (t instanceof Byte) {
            return Byte.valueOf(a((byte) 0, i, z));
        }
        if (t instanceof Boolean) {
            return Boolean.valueOf(a(false, i, z));
        }
        if (t instanceof Short) {
            return Short.valueOf(a((short) 0, i, z));
        }
        if (t instanceof Integer) {
            return Integer.valueOf(a(0, i, z));
        }
        if (t instanceof Long) {
            return Long.valueOf(a(0, i, z));
        }
        if (t instanceof Float) {
            return Float.valueOf(a(0.0f, i, z));
        }
        if (t instanceof Double) {
            return Double.valueOf(a(0.0d, i, z));
        }
        if (t instanceof String) {
            return a(i, z);
        }
        if (t instanceof Map) {
            return a((Map) t, i, z);
        }
        if (t instanceof List) {
            return a((List) t, i, z);
        }
        if (t instanceof fs) {
            return a((fs) t, i, z);
        }
        if (!t.getClass().isArray()) {
            throw new fn("read object error: unsupport type.");
        } else if ((t instanceof byte[]) || (t instanceof Byte[])) {
            return a(null, i, z);
        } else {
            if (t instanceof boolean[]) {
                return a(null, i, z);
            }
            if (t instanceof short[]) {
                return a(null, i, z);
            }
            if (t instanceof int[]) {
                return a(null, i, z);
            }
            if (t instanceof long[]) {
                return a(null, i, z);
            }
            if (t instanceof float[]) {
                return a(null, i, z);
            }
            if (t instanceof double[]) {
                return a(null, i, z);
            }
            return a((Object[]) t, i, z);
        }
    }

    public int ae(String str) {
        this.my = str;
        return 0;
    }
}
