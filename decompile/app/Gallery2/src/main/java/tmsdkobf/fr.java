package tmsdkobf;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class fr {
    private ByteBuffer mx;
    protected String my;

    public fr(int capacity) {
        this.my = "GBK";
        this.mx = ByteBuffer.allocate(capacity);
    }

    public fr() {
        this(128);
    }

    public ByteBuffer t() {
        return this.mx;
    }

    public byte[] toByteArray() {
        byte[] newBytes = new byte[this.mx.position()];
        System.arraycopy(this.mx.array(), 0, newBytes, 0, this.mx.position());
        return newBytes;
    }

    public void ag(int len) {
        if (this.mx.remaining() < len) {
            ByteBuffer bs2 = ByteBuffer.allocate((this.mx.capacity() + len) * 2);
            bs2.put(this.mx.array(), 0, this.mx.position());
            this.mx = bs2;
        }
    }

    public void a(byte type, int tag) {
        if (tag < 15) {
            this.mx.put((byte) ((tag << 4) | type));
        } else if (tag >= 256) {
            throw new fp("tag is too large: " + tag);
        } else {
            this.mx.put((byte) (type | 240));
            this.mx.put((byte) tag);
        }
    }

    public void a(boolean b, int tag) {
        int i = 0;
        if (b) {
            i = 1;
        }
        b((byte) i, tag);
    }

    public void b(byte b, int tag) {
        ag(3);
        if (b != (byte) 0) {
            a((byte) 0, tag);
            this.mx.put(b);
            return;
        }
        a((byte) fs.ZERO_TAG, tag);
    }

    public void a(short n, int tag) {
        ag(4);
        if (n >= (short) -128 && n <= (short) 127) {
            b((byte) n, tag);
            return;
        }
        a((byte) 1, tag);
        this.mx.putShort(n);
    }

    public void write(int n, int tag) {
        ag(6);
        if (n >= -32768 && n <= 32767) {
            a((short) n, tag);
            return;
        }
        a((byte) 2, tag);
        this.mx.putInt(n);
    }

    public void b(long n, int tag) {
        Object obj = 1;
        ag(10);
        if ((n < -2147483648L ? 1 : null) == null) {
            if (n <= 2147483647L) {
                obj = null;
            }
            if (obj == null) {
                write((int) n, tag);
                return;
            }
        }
        a((byte) 3, tag);
        this.mx.putLong(n);
    }

    public void a(float n, int tag) {
        ag(6);
        a((byte) 4, tag);
        this.mx.putFloat(n);
    }

    public void a(double n, int tag) {
        ag(10);
        a((byte) 5, tag);
        this.mx.putDouble(n);
    }

    public void a(String s, int tag) {
        byte[] by;
        try {
            by = s.getBytes(this.my);
        } catch (UnsupportedEncodingException e) {
            by = s.getBytes();
        }
        ag(by.length + 10);
        if (by.length <= 255) {
            a((byte) 6, tag);
            this.mx.put((byte) by.length);
            this.mx.put(by);
            return;
        }
        a((byte) 7, tag);
        this.mx.putInt(by.length);
        this.mx.put(by);
    }

    public <K, V> void a(Map<K, V> m, int tag) {
        ag(8);
        a((byte) 8, tag);
        write(m != null ? m.size() : 0, 0);
        if (m != null) {
            for (Entry<K, V> en : m.entrySet()) {
                a(en.getKey(), 0);
                a(en.getValue(), 1);
            }
        }
    }

    public void a(boolean[] l, int tag) {
        ag(8);
        a((byte) 9, tag);
        write(l.length, 0);
        for (boolean e : l) {
            a(e, 0);
        }
    }

    public void a(byte[] l, int tag) {
        ag(l.length + 8);
        a((byte) fs.SIMPLE_LIST, tag);
        a((byte) 0, 0);
        write(l.length, 0);
        this.mx.put(l);
    }

    public void a(short[] l, int tag) {
        ag(8);
        a((byte) 9, tag);
        write(l.length, 0);
        for (short e : l) {
            a(e, 0);
        }
    }

    public void a(int[] l, int tag) {
        ag(8);
        a((byte) 9, tag);
        write(l.length, 0);
        for (int e : l) {
            write(e, 0);
        }
    }

    public void a(long[] l, int tag) {
        ag(8);
        a((byte) 9, tag);
        write(l.length, 0);
        for (long e : l) {
            b(e, 0);
        }
    }

    public void a(float[] l, int tag) {
        ag(8);
        a((byte) 9, tag);
        write(l.length, 0);
        for (float e : l) {
            a(e, 0);
        }
    }

    public void a(double[] l, int tag) {
        ag(8);
        a((byte) 9, tag);
        write(l.length, 0);
        for (double e : l) {
            a(e, 0);
        }
    }

    private void a(Object[] l, int tag) {
        ag(8);
        a((byte) 9, tag);
        write(l.length, 0);
        for (Object e : l) {
            a(e, 0);
        }
    }

    public <T> void a(Collection<T> l, int tag) {
        ag(8);
        a((byte) 9, tag);
        write(l != null ? l.size() : 0, 0);
        if (l != null) {
            for (T e : l) {
                a((Object) e, 0);
            }
        }
    }

    public void a(fs o, int tag) {
        ag(2);
        a((byte) 10, tag);
        o.writeTo(this);
        ag(2);
        a((byte) fs.STRUCT_END, 0);
    }

    public void a(Object o, int tag) {
        if (o instanceof Byte) {
            b(((Byte) o).byteValue(), tag);
        } else if (o instanceof Boolean) {
            a(((Boolean) o).booleanValue(), tag);
        } else if (o instanceof Short) {
            a(((Short) o).shortValue(), tag);
        } else if (o instanceof Integer) {
            write(((Integer) o).intValue(), tag);
        } else if (o instanceof Long) {
            b(((Long) o).longValue(), tag);
        } else if (o instanceof Float) {
            a(((Float) o).floatValue(), tag);
        } else if (o instanceof Double) {
            a(((Double) o).doubleValue(), tag);
        } else if (o instanceof String) {
            a((String) o, tag);
        } else if (o instanceof Map) {
            a((Map) o, tag);
        } else if (o instanceof List) {
            a((List) o, tag);
        } else if (o instanceof fs) {
            a((fs) o, tag);
        } else if (o instanceof byte[]) {
            a((byte[]) o, tag);
        } else if (o instanceof boolean[]) {
            a((boolean[]) o, tag);
        } else if (o instanceof short[]) {
            a((short[]) o, tag);
        } else if (o instanceof int[]) {
            a((int[]) o, tag);
        } else if (o instanceof long[]) {
            a((long[]) o, tag);
        } else if (o instanceof float[]) {
            a((float[]) o, tag);
        } else if (o instanceof double[]) {
            a((double[]) o, tag);
        } else if (o.getClass().isArray()) {
            a((Object[]) o, tag);
        } else if (o instanceof Collection) {
            a((Collection) o, tag);
        } else {
            throw new fp("write object error: unsupport type. " + o.getClass());
        }
    }

    public int ae(String se) {
        this.my = se;
        return 0;
    }
}
