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

    public fq(byte[] bs) {
        this.mx = ByteBuffer.wrap(bs);
    }

    public fq(byte[] bs, int pos) {
        this.mx = ByteBuffer.wrap(bs);
        this.mx.position(pos);
    }

    public void d(byte[] bs) {
        this.mx = ByteBuffer.wrap(bs);
    }

    public static int a(a hd, ByteBuffer bb) {
        byte b = bb.get();
        hd.mz = (byte) ((byte) (b & 15));
        hd.tag = (b & 240) >> 4;
        if (hd.tag != 15) {
            return 1;
        }
        hd.tag = bb.get() & 255;
        return 2;
    }

    public void a(a hd) {
        a(hd, this.mx);
    }

    private int b(a hd) {
        return a(hd, this.mx.duplicate());
    }

    private void skip(int len) {
        this.mx.position(this.mx.position() + len);
    }

    public boolean af(int tag) {
        try {
            a hd = new a();
            while (true) {
                int len = b(hd);
                if (tag > hd.tag && hd.mz != fs.STRUCT_END) {
                    skip(len);
                    a(hd.mz);
                }
            }
            if (tag != hd.tag) {
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
        a hd = new a();
        while (this.mx.remaining() != 0) {
            a(hd);
            a(hd.mz);
            if (hd.mz == fs.STRUCT_END) {
                return;
            }
        }
    }

    private void s() {
        a hd = new a();
        a(hd);
        a(hd.mz);
    }

    private void a(byte type) {
        int size;
        int i;
        switch (type) {
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
                int len = this.mx.get();
                if (len < 0) {
                    len += 256;
                }
                skip(len);
                return;
            case (byte) 7:
                skip(this.mx.getInt());
                return;
            case (byte) 8:
                size = a(0, 0, true);
                for (i = 0; i < size * 2; i++) {
                    s();
                }
                return;
            case (byte) 9:
                size = a(0, 0, true);
                for (i = 0; i < size; i++) {
                    s();
                }
                return;
            case (byte) 10:
                r();
                return;
            case (byte) 11:
            case (byte) 12:
                return;
            case (byte) 13:
                a hd = new a();
                a(hd);
                if (hd.mz == (byte) 0) {
                    skip(a(0, 0, true));
                    return;
                }
                throw new fn("skipField with invalid type, type value: " + type + ", " + hd.mz);
            default:
                throw new fn("invalid type.");
        }
    }

    public boolean a(boolean b, int tag, boolean isRequire) {
        if (a((byte) 0, tag, isRequire) == (byte) 0) {
            return false;
        }
        return true;
    }

    public byte a(byte c, int tag, boolean isRequire) {
        if (af(tag)) {
            a hd = new a();
            a(hd);
            switch (hd.mz) {
                case (byte) 0:
                    return this.mx.get();
                case (byte) 11:
                    return c;
                case (byte) 12:
                    return (byte) 0;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (!isRequire) {
            return c;
        } else {
            throw new fn("require field not exist.");
        }
    }

    public short a(short n, int tag, boolean isRequire) {
        if (af(tag)) {
            a hd = new a();
            a(hd);
            switch (hd.mz) {
                case (byte) 0:
                    return (short) this.mx.get();
                case (byte) 1:
                    return this.mx.getShort();
                case (byte) 11:
                    return n;
                case (byte) 12:
                    return (short) 0;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (!isRequire) {
            return n;
        } else {
            throw new fn("require field not exist.");
        }
    }

    public int a(int n, int tag, boolean isRequire) {
        if (af(tag)) {
            a hd = new a();
            a(hd);
            switch (hd.mz) {
                case (byte) 0:
                    return this.mx.get();
                case (byte) 1:
                    return this.mx.getShort();
                case (byte) 2:
                    return this.mx.getInt();
                case (byte) 11:
                    return n;
                case (byte) 12:
                    return 0;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (!isRequire) {
            return n;
        } else {
            throw new fn("require field not exist.");
        }
    }

    public long a(long n, int tag, boolean isRequire) {
        if (af(tag)) {
            a hd = new a();
            a(hd);
            switch (hd.mz) {
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
        } else if (!isRequire) {
            return n;
        } else {
            throw new fn("require field not exist.");
        }
    }

    public float a(float n, int tag, boolean isRequire) {
        if (af(tag)) {
            a hd = new a();
            a(hd);
            switch (hd.mz) {
                case (byte) 4:
                    return this.mx.getFloat();
                case (byte) 11:
                    return n;
                case (byte) 12:
                    return 0.0f;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (!isRequire) {
            return n;
        } else {
            throw new fn("require field not exist.");
        }
    }

    public double a(double n, int tag, boolean isRequire) {
        if (af(tag)) {
            a hd = new a();
            a(hd);
            switch (hd.mz) {
                case (byte) 4:
                    return (double) this.mx.getFloat();
                case (byte) 5:
                    return this.mx.getDouble();
                case (byte) 11:
                    return n;
                case (byte) 12:
                    return 0.0d;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (!isRequire) {
            return n;
        } else {
            throw new fn("require field not exist.");
        }
    }

    public String a(int tag, boolean isRequire) {
        if (af(tag)) {
            a hd = new a();
            a(hd);
            int len;
            byte[] ss;
            switch (hd.mz) {
                case (byte) 6:
                    len = this.mx.get();
                    if (len < 0) {
                        len += 256;
                    }
                    ss = new byte[len];
                    this.mx.get(ss);
                    try {
                        return new String(ss, this.my);
                    } catch (UnsupportedEncodingException e) {
                        return new String(ss);
                    }
                case (byte) 7:
                    len = this.mx.getInt();
                    if (len <= fs.JCE_MAX_STRING_LENGTH && len >= 0) {
                        ss = new byte[len];
                        this.mx.get(ss);
                        try {
                            return new String(ss, this.my);
                        } catch (UnsupportedEncodingException e2) {
                            return new String(ss);
                        }
                    }
                    throw new fn("String too long: " + len);
                case (byte) 11:
                    return null;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (!isRequire) {
            return null;
        } else {
            throw new fn("require field not exist.");
        }
    }

    public <K, V> HashMap<K, V> a(Map<K, V> m, int tag, boolean isRequire) {
        return (HashMap) a(new HashMap(), m, tag, isRequire);
    }

    private <K, V> Map<K, V> a(Map<K, V> mr, Map<K, V> m, int tag, boolean isRequire) {
        if (m == null || m.isEmpty()) {
            return new HashMap();
        }
        Entry<K, V> en = (Entry) m.entrySet().iterator().next();
        K mk = en.getKey();
        V mv = en.getValue();
        if (af(tag)) {
            a hd = new a();
            a(hd);
            switch (hd.mz) {
                case (byte) 8:
                    int size = a(0, 0, true);
                    if (size >= 0) {
                        for (int i = 0; i < size; i++) {
                            mr.put(b(mk, 0, true), b(mv, 1, true));
                        }
                        break;
                    }
                    throw new fn("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (isRequire) {
            throw new fn("require field not exist.");
        }
        return mr;
    }

    public boolean[] a(boolean[] l, int tag, boolean isRequire) {
        boolean[] lr = null;
        if (af(tag)) {
            a hd = new a();
            a(hd);
            switch (hd.mz) {
                case (byte) 9:
                    int size = a(0, 0, true);
                    if (size >= 0) {
                        lr = new boolean[size];
                        for (int i = 0; i < size; i++) {
                            lr[i] = a(lr[0], 0, true);
                        }
                        break;
                    }
                    throw new fn("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (isRequire) {
            throw new fn("require field not exist.");
        }
        return lr;
    }

    public byte[] a(byte[] l, int tag, boolean isRequire) {
        if (af(tag)) {
            a hd = new a();
            a(hd);
            int size;
            byte[] lr;
            switch (hd.mz) {
                case (byte) 9:
                    size = a(0, 0, true);
                    if (size >= 0) {
                        lr = new byte[size];
                        for (int i = 0; i < size; i++) {
                            lr[i] = (byte) a(lr[0], 0, true);
                        }
                        return lr;
                    }
                    throw new fn("size invalid: " + size);
                case (byte) 11:
                    return null;
                case (byte) 13:
                    a hh = new a();
                    a(hh);
                    if (hh.mz == (byte) 0) {
                        size = a(0, 0, true);
                        if (size >= 0) {
                            lr = new byte[size];
                            this.mx.get(lr);
                            return lr;
                        }
                        throw new fn("invalid size, tag: " + tag + ", type: " + hd.mz + ", " + hh.mz + ", size: " + size);
                    }
                    throw new fn("type mismatch, tag: " + tag + ", type: " + hd.mz + ", " + hh.mz);
                default:
                    throw new fn("type mismatch.");
            }
        } else if (!isRequire) {
            return null;
        } else {
            throw new fn("require field not exist.");
        }
    }

    public short[] a(short[] l, int tag, boolean isRequire) {
        short[] lr = null;
        if (af(tag)) {
            a hd = new a();
            a(hd);
            switch (hd.mz) {
                case (byte) 9:
                    int size = a(0, 0, true);
                    if (size >= 0) {
                        lr = new short[size];
                        for (int i = 0; i < size; i++) {
                            lr[i] = (short) a(lr[0], 0, true);
                        }
                        break;
                    }
                    throw new fn("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (isRequire) {
            throw new fn("require field not exist.");
        }
        return lr;
    }

    public int[] a(int[] l, int tag, boolean isRequire) {
        int[] lr = null;
        if (af(tag)) {
            a hd = new a();
            a(hd);
            switch (hd.mz) {
                case (byte) 9:
                    int size = a(0, 0, true);
                    if (size >= 0) {
                        lr = new int[size];
                        for (int i = 0; i < size; i++) {
                            lr[i] = a(lr[0], 0, true);
                        }
                        break;
                    }
                    throw new fn("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (isRequire) {
            throw new fn("require field not exist.");
        }
        return lr;
    }

    public long[] a(long[] l, int tag, boolean isRequire) {
        long[] lr = null;
        if (af(tag)) {
            a hd = new a();
            a(hd);
            switch (hd.mz) {
                case (byte) 9:
                    int size = a(0, 0, true);
                    if (size >= 0) {
                        lr = new long[size];
                        for (int i = 0; i < size; i++) {
                            lr[i] = a(lr[0], 0, true);
                        }
                        break;
                    }
                    throw new fn("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (isRequire) {
            throw new fn("require field not exist.");
        }
        return lr;
    }

    public float[] a(float[] l, int tag, boolean isRequire) {
        float[] lr = null;
        if (af(tag)) {
            a hd = new a();
            a(hd);
            switch (hd.mz) {
                case (byte) 9:
                    int size = a(0, 0, true);
                    if (size >= 0) {
                        lr = new float[size];
                        for (int i = 0; i < size; i++) {
                            lr[i] = a(lr[0], 0, true);
                        }
                        break;
                    }
                    throw new fn("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (isRequire) {
            throw new fn("require field not exist.");
        }
        return lr;
    }

    public double[] a(double[] l, int tag, boolean isRequire) {
        double[] lr = null;
        if (af(tag)) {
            a hd = new a();
            a(hd);
            switch (hd.mz) {
                case (byte) 9:
                    int size = a(0, 0, true);
                    if (size >= 0) {
                        lr = new double[size];
                        for (int i = 0; i < size; i++) {
                            lr[i] = a(lr[0], 0, true);
                        }
                        break;
                    }
                    throw new fn("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (isRequire) {
            throw new fn("require field not exist.");
        }
        return lr;
    }

    public <T> T[] a(T[] l, int tag, boolean isRequire) {
        if (l != null && l.length != 0) {
            return a(l[0], tag, isRequire);
        }
        throw new fn("unable to get type of key and value.");
    }

    public <T> List<T> a(List<T> l, int tag, boolean isRequire) {
        if (l == null || l.isEmpty()) {
            return new ArrayList();
        }
        Object[] tt = a(l.get(0), tag, isRequire);
        if (tt == null) {
            return null;
        }
        ArrayList<T> ll = new ArrayList();
        for (Object add : tt) {
            ll.add(add);
        }
        return ll;
    }

    private <T> T[] a(T mt, int tag, boolean isRequire) {
        if (af(tag)) {
            a hd = new a();
            a(hd);
            switch (hd.mz) {
                case (byte) 9:
                    int size = a(0, 0, true);
                    if (size >= 0) {
                        Object[] lr = (Object[]) Array.newInstance(mt.getClass(), size);
                        for (int i = 0; i < size; i++) {
                            lr[i] = b(mt, 0, true);
                        }
                        return lr;
                    }
                    throw new fn("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new fn("type mismatch.");
            }
        } else if (isRequire) {
            throw new fn("require field not exist.");
        }
        return null;
    }

    public fs a(fs o, int tag, boolean isRequire) {
        fs ref = null;
        if (af(tag)) {
            try {
                ref = (fs) o.getClass().newInstance();
                a hd = new a();
                a(hd);
                if (hd.mz == (byte) 10) {
                    ref.readFrom(this);
                    r();
                } else {
                    throw new fn("type mismatch.");
                }
            } catch (Exception e) {
                throw new fn(e.getMessage());
            }
        } else if (isRequire) {
            throw new fn("require field not exist.");
        }
        return ref;
    }

    public <T> Object b(T o, int tag, boolean isRequire) {
        if (o instanceof Byte) {
            return Byte.valueOf(a((byte) 0, tag, isRequire));
        }
        if (o instanceof Boolean) {
            return Boolean.valueOf(a(false, tag, isRequire));
        }
        if (o instanceof Short) {
            return Short.valueOf(a((short) 0, tag, isRequire));
        }
        if (o instanceof Integer) {
            return Integer.valueOf(a(0, tag, isRequire));
        }
        if (o instanceof Long) {
            return Long.valueOf(a(0, tag, isRequire));
        }
        if (o instanceof Float) {
            return Float.valueOf(a(0.0f, tag, isRequire));
        }
        if (o instanceof Double) {
            return Double.valueOf(a(0.0d, tag, isRequire));
        }
        if (o instanceof String) {
            return a(tag, isRequire);
        }
        if (o instanceof Map) {
            return a((Map) o, tag, isRequire);
        }
        if (o instanceof List) {
            return a((List) o, tag, isRequire);
        }
        if (o instanceof fs) {
            return a((fs) o, tag, isRequire);
        }
        if (!o.getClass().isArray()) {
            throw new fn("read object error: unsupport type.");
        } else if ((o instanceof byte[]) || (o instanceof Byte[])) {
            return a(null, tag, isRequire);
        } else {
            if (o instanceof boolean[]) {
                return a(null, tag, isRequire);
            }
            if (o instanceof short[]) {
                return a(null, tag, isRequire);
            }
            if (o instanceof int[]) {
                return a(null, tag, isRequire);
            }
            if (o instanceof long[]) {
                return a(null, tag, isRequire);
            }
            if (o instanceof float[]) {
                return a(null, tag, isRequire);
            }
            if (o instanceof double[]) {
                return a(null, tag, isRequire);
            }
            return a((Object[]) o, tag, isRequire);
        }
    }

    public int ae(String se) {
        this.my = se;
        return 0;
    }
}
