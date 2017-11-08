package tmsdkobf;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class fo {
    private StringBuilder mv;
    private int mw = 0;

    private void ad(String fieldName) {
        for (int i = 0; i < this.mw; i++) {
            this.mv.append('\t');
        }
        if (fieldName != null) {
            this.mv.append(fieldName).append(": ");
        }
    }

    public fo(StringBuilder sb, int level) {
        this.mv = sb;
        this.mw = level;
    }

    public fo a(boolean b, String fieldName) {
        char c;
        ad(fieldName);
        StringBuilder stringBuilder = this.mv;
        if (b) {
            c = 'T';
        } else {
            c = 'F';
        }
        stringBuilder.append(c).append('\n');
        return this;
    }

    public fo a(byte n, String fieldName) {
        ad(fieldName);
        this.mv.append(n).append('\n');
        return this;
    }

    public fo a(char n, String fieldName) {
        ad(fieldName);
        this.mv.append(n).append('\n');
        return this;
    }

    public fo a(short n, String fieldName) {
        ad(fieldName);
        this.mv.append(n).append('\n');
        return this;
    }

    public fo a(int n, String fieldName) {
        ad(fieldName);
        this.mv.append(n).append('\n');
        return this;
    }

    public fo a(long n, String fieldName) {
        ad(fieldName);
        this.mv.append(n).append('\n');
        return this;
    }

    public fo a(float n, String fieldName) {
        ad(fieldName);
        this.mv.append(n).append('\n');
        return this;
    }

    public fo a(double n, String fieldName) {
        ad(fieldName);
        this.mv.append(n).append('\n');
        return this;
    }

    public fo a(String s, String fieldName) {
        ad(fieldName);
        if (s != null) {
            this.mv.append(s).append('\n');
        } else {
            this.mv.append("null").append('\n');
        }
        return this;
    }

    public fo a(byte[] v, String fieldName) {
        ad(fieldName);
        if (v == null) {
            this.mv.append("null").append('\n');
            return this;
        } else if (v.length != 0) {
            this.mv.append(v.length).append(", [").append('\n');
            fo jd = new fo(this.mv, this.mw + 1);
            for (byte o : v) {
                jd.a(o, null);
            }
            a(']', null);
            return this;
        } else {
            this.mv.append(v.length).append(", []").append('\n');
            return this;
        }
    }

    public fo a(short[] v, String fieldName) {
        ad(fieldName);
        if (v == null) {
            this.mv.append("null").append('\n');
            return this;
        } else if (v.length != 0) {
            this.mv.append(v.length).append(", [").append('\n');
            fo jd = new fo(this.mv, this.mw + 1);
            for (short o : v) {
                jd.a(o, null);
            }
            a(']', null);
            return this;
        } else {
            this.mv.append(v.length).append(", []").append('\n');
            return this;
        }
    }

    public fo a(int[] v, String fieldName) {
        ad(fieldName);
        if (v == null) {
            this.mv.append("null").append('\n');
            return this;
        } else if (v.length != 0) {
            this.mv.append(v.length).append(", [").append('\n');
            fo jd = new fo(this.mv, this.mw + 1);
            for (int o : v) {
                jd.a(o, null);
            }
            a(']', null);
            return this;
        } else {
            this.mv.append(v.length).append(", []").append('\n');
            return this;
        }
    }

    public fo a(long[] v, String fieldName) {
        ad(fieldName);
        if (v == null) {
            this.mv.append("null").append('\n');
            return this;
        } else if (v.length != 0) {
            this.mv.append(v.length).append(", [").append('\n');
            fo jd = new fo(this.mv, this.mw + 1);
            for (long o : v) {
                jd.a(o, null);
            }
            a(']', null);
            return this;
        } else {
            this.mv.append(v.length).append(", []").append('\n');
            return this;
        }
    }

    public fo a(float[] v, String fieldName) {
        ad(fieldName);
        if (v == null) {
            this.mv.append("null").append('\n');
            return this;
        } else if (v.length != 0) {
            this.mv.append(v.length).append(", [").append('\n');
            fo jd = new fo(this.mv, this.mw + 1);
            for (float o : v) {
                jd.a(o, null);
            }
            a(']', null);
            return this;
        } else {
            this.mv.append(v.length).append(", []").append('\n');
            return this;
        }
    }

    public fo a(double[] v, String fieldName) {
        ad(fieldName);
        if (v == null) {
            this.mv.append("null").append('\n');
            return this;
        } else if (v.length != 0) {
            this.mv.append(v.length).append(", [").append('\n');
            fo jd = new fo(this.mv, this.mw + 1);
            for (double o : v) {
                jd.a(o, null);
            }
            a(']', null);
            return this;
        } else {
            this.mv.append(v.length).append(", []").append('\n');
            return this;
        }
    }

    public <K, V> fo a(Map<K, V> m, String fieldName) {
        ad(fieldName);
        if (m == null) {
            this.mv.append("null").append('\n');
            return this;
        } else if (m.isEmpty()) {
            this.mv.append(m.size()).append(", {}").append('\n');
            return this;
        } else {
            this.mv.append(m.size()).append(", {").append('\n');
            fo jd1 = new fo(this.mv, this.mw + 1);
            fo jd = new fo(this.mv, this.mw + 2);
            for (Entry en : m.entrySet()) {
                jd1.a('(', null);
                jd.a(en.getKey(), null);
                jd.a(en.getValue(), null);
                jd1.a(')', null);
            }
            a('}', null);
            return this;
        }
    }

    public <T> fo a(T[] v, String fieldName) {
        ad(fieldName);
        if (v == null) {
            this.mv.append("null").append('\n');
            return this;
        } else if (v.length != 0) {
            this.mv.append(v.length).append(", [").append('\n');
            fo jd = new fo(this.mv, this.mw + 1);
            for (Object o : v) {
                jd.a(o, null);
            }
            a(']', null);
            return this;
        } else {
            this.mv.append(v.length).append(", []").append('\n');
            return this;
        }
    }

    public <T> fo a(Collection<T> v, String fieldName) {
        if (v != null) {
            return a(v.toArray(), fieldName);
        }
        ad(fieldName);
        this.mv.append("null").append('\t');
        return this;
    }

    public <T> fo a(T o, String fieldName) {
        if (o == null) {
            this.mv.append("null").append('\n');
        } else if (o instanceof Byte) {
            a(((Byte) o).byteValue(), fieldName);
        } else if (o instanceof Boolean) {
            a(((Boolean) o).booleanValue(), fieldName);
        } else if (o instanceof Short) {
            a(((Short) o).shortValue(), fieldName);
        } else if (o instanceof Integer) {
            a(((Integer) o).intValue(), fieldName);
        } else if (o instanceof Long) {
            a(((Long) o).longValue(), fieldName);
        } else if (o instanceof Float) {
            a(((Float) o).floatValue(), fieldName);
        } else if (o instanceof Double) {
            a(((Double) o).doubleValue(), fieldName);
        } else if (o instanceof String) {
            a((String) o, fieldName);
        } else if (o instanceof Map) {
            a((Map) o, fieldName);
        } else if (o instanceof List) {
            a((List) o, fieldName);
        } else if (o instanceof fs) {
            a((fs) o, fieldName);
        } else if (o instanceof byte[]) {
            a((byte[]) o, fieldName);
        } else if (o instanceof boolean[]) {
            a((boolean[]) o, fieldName);
        } else if (o instanceof short[]) {
            a((short[]) o, fieldName);
        } else if (o instanceof int[]) {
            a((int[]) o, fieldName);
        } else if (o instanceof long[]) {
            a((long[]) o, fieldName);
        } else if (o instanceof float[]) {
            a((float[]) o, fieldName);
        } else if (o instanceof double[]) {
            a((double[]) o, fieldName);
        } else if (o.getClass().isArray()) {
            a((Object[]) o, fieldName);
        } else {
            throw new fp("write object error: unsupport type.");
        }
        return this;
    }

    public fo a(fs v, String fieldName) {
        a('{', fieldName);
        if (v != null) {
            v.display(this.mv, this.mw + 1);
        } else {
            this.mv.append('\t').append("null");
        }
        a('}', null);
        return this;
    }
}
