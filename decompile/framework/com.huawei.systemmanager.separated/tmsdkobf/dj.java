package tmsdkobf;

/* compiled from: Unknown */
public final class dj extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public String is = "";
    public String it = "";

    static {
        boolean z = false;
        if (!dj.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public dj() {
        u(this.is);
        v(this.it);
    }

    public dj(String str, String str2) {
        u(str);
        v(str2);
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

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        dj djVar = (dj) obj;
        if (ft.equals(this.is, djVar.is) && ft.equals(this.it, djVar.it)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void readFrom(fq fqVar) {
        u(fqVar.a(0, true));
        v(fqVar.a(1, false));
    }

    public void u(String str) {
        this.is = str;
    }

    public void v(String str) {
        this.it = str;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.is, 0);
        if (this.it != null) {
            frVar.a(this.it, 1);
        }
    }
}
