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

    public boolean equals(Object obj) {
        fl flVar = (fl) obj;
        return ft.equals(1, flVar.mj) && ft.equals(1, flVar.mk) && ft.equals(1, flVar.ml) && ft.equals(1, flVar.mm) && ft.equals(Integer.valueOf(1), flVar.mn) && ft.equals(Integer.valueOf(1), flVar.mo) && ft.equals(Integer.valueOf(1), flVar.mp) && ft.equals(1, flVar.mq) && ft.equals(Integer.valueOf(1), flVar.mr) && ft.equals(Integer.valueOf(1), flVar.ms);
    }

    public Object clone() {
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException e) {
            if (!fJ) {
                throw new AssertionError();
            }
        }
        return obj;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.mj, 1);
        frVar.b(this.mk, 2);
        frVar.write(this.ml, 3);
        frVar.write(this.mm, 4);
        frVar.a(this.mn, 5);
        frVar.a(this.mo, 6);
        frVar.a(this.mp, 7);
        frVar.write(this.mq, 8);
        frVar.a(this.mr, 9);
        frVar.a(this.ms, 10);
    }

    public void readFrom(fq fqVar) {
        try {
            this.mj = (short) fqVar.a(this.mj, 1, true);
            this.mk = (byte) fqVar.a(this.mk, 2, true);
            this.ml = fqVar.a(this.ml, 3, true);
            this.mm = fqVar.a(this.mm, 4, true);
            this.mn = fqVar.a(5, true);
            this.mo = fqVar.a(6, true);
            if (mt == null) {
                mt = new byte[1];
            }
            this.mp = fqVar.a(mt, 7, true);
            this.mq = fqVar.a(this.mq, 8, true);
            if (mu == null) {
                mu = new HashMap();
                mu.put("", "");
            }
            this.mr = (Map) fqVar.b(mu, 9, true);
            if (mu == null) {
                mu = new HashMap();
                mu.put("", "");
            }
            this.ms = (Map) fqVar.b(mu, 10, true);
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("RequestPacket decode error " + fk.c(this.mp));
            throw new RuntimeException(e);
        }
    }
}
