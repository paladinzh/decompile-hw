package tmsdkobf;

/* compiled from: Unknown */
public final class rc extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    static dq lj;
    public int Lt = 0;
    public String iI = "";
    public String iq = "";
    public dq lf = null;
    public String url = "";

    static {
        boolean z = false;
        if (!rc.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public rc() {
        cD(this.Lt);
        setUrl(this.url);
        a(this.lf);
        z(this.iI);
        s(this.iq);
    }

    public void a(dq dqVar) {
        this.lf = dqVar;
    }

    public void cD(int i) {
        this.Lt = i;
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

    public void display(StringBuilder stringBuilder, int i) {
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        rc rcVar = (rc) obj;
        if (ft.equals(this.Lt, rcVar.Lt) && ft.equals(this.url, rcVar.url) && ft.equals(this.lf, rcVar.lf) && ft.equals(this.iI, rcVar.iI) && ft.equals(this.iq, rcVar.iq)) {
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
        cD(fqVar.a(this.Lt, 0, true));
        setUrl(fqVar.a(1, true));
        if (lj == null) {
            lj = new dq();
        }
        a((dq) fqVar.a(lj, 2, false));
        z(fqVar.a(3, false));
        s(fqVar.a(4, false));
    }

    public void s(String str) {
        this.iq = str;
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public void writeTo(fr frVar) {
        frVar.write(this.Lt, 0);
        frVar.a(this.url, 1);
        if (this.lf != null) {
            frVar.a(this.lf, 2);
        }
        if (this.iI != null) {
            frVar.a(this.iI, 3);
        }
        if (this.iq != null) {
            frVar.a(this.iq, 4);
        }
    }

    public void z(String str) {
        this.iI = str;
    }
}
