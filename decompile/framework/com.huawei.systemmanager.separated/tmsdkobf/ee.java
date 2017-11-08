package tmsdkobf;

/* compiled from: Unknown */
public final class ee extends fs implements Cloneable {
    static dg fF;
    static final /* synthetic */ boolean fJ;
    public int fE = 0;
    public dg fy = null;

    static {
        boolean z = false;
        if (!ee.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public ee() {
        a(this.fy);
        c(this.fE);
    }

    public ee(dg dgVar, int i) {
        a(dgVar);
        c(i);
    }

    public void a(dg dgVar) {
        this.fy = dgVar;
    }

    public void c(int i) {
        this.fE = i;
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
        ee eeVar = (ee) obj;
        if (ft.equals(this.fy, eeVar.fy) && ft.equals(this.fE, eeVar.fE)) {
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
        if (fF == null) {
            fF = new dg();
        }
        a((dg) fqVar.a(fF, 0, true));
        c(fqVar.a(this.fE, 1, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.fy, 0);
        frVar.write(this.fE, 1);
    }
}
