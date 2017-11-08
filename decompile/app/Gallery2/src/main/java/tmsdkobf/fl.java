package tmsdkobf;

import java.util.HashMap;
import java.util.Map;

public final class fl extends fs {
    static final /* synthetic */ boolean fJ;
    static byte[] mt = null;
    static Map<String, String> mu = null;
    public short mj = (short) 0;
    public byte mk = (byte) 0;
    public int ml = 0;
    public int mm = 0;
    public String mn = null;
    public String mo = null;
    public byte[] mp;
    public int mq = 0;
    public Map<String, String> mr;
    public Map<String, String> ms;

    static {
        boolean z = false;
        if (!fl.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public boolean equals(Object o) {
        fl t = (fl) o;
        return ft.equals(1, t.mj) && ft.equals(1, t.mk) && ft.equals(1, t.ml) && ft.equals(1, t.mm) && ft.equals(Integer.valueOf(1), t.mn) && ft.equals(Integer.valueOf(1), t.mo) && ft.equals(Integer.valueOf(1), t.mp) && ft.equals(1, t.mq) && ft.equals(Integer.valueOf(1), t.mr) && ft.equals(Integer.valueOf(1), t.ms);
    }

    public Object clone() {
        Object o = null;
        try {
            o = super.clone();
        } catch (CloneNotSupportedException e) {
            if (!fJ) {
                throw new AssertionError();
            }
        }
        return o;
    }

    public void writeTo(fr _os) {
        _os.a(this.mj, 1);
        _os.b(this.mk, 2);
        _os.write(this.ml, 3);
        _os.write(this.mm, 4);
        _os.a(this.mn, 5);
        _os.a(this.mo, 6);
        _os.a(this.mp, 7);
        _os.write(this.mq, 8);
        _os.a(this.mr, 9);
        _os.a(this.ms, 10);
    }

    public void readFrom(fq _is) {
        try {
            this.mj = (short) _is.a(this.mj, 1, true);
            this.mk = (byte) _is.a(this.mk, 2, true);
            this.ml = _is.a(this.ml, 3, true);
            this.mm = _is.a(this.mm, 4, true);
            this.mn = _is.a(5, true);
            this.mo = _is.a(6, true);
            if (mt == null) {
                mt = new byte[1];
            }
            this.mp = _is.a(mt, 7, true);
            this.mq = _is.a(this.mq, 8, true);
            if (mu == null) {
                mu = new HashMap();
                mu.put("", "");
            }
            this.mr = (Map) _is.b(mu, 9, true);
            if (mu == null) {
                mu = new HashMap();
                mu.put("", "");
            }
            this.ms = (Map) _is.b(mu, 10, true);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("RequestPacket decode error " + fk.c(this.mp));
            throw new RuntimeException(e);
        }
    }
}
