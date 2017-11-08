package tmsdkobf;

/* compiled from: Unknown */
public final class dq extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public int iJ = 0;
    public int iK = 0;
    public int iL = 0;

    static {
        boolean z = false;
        if (!dq.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public dq() {
        s(this.iJ);
        t(this.iK);
        u(this.iL);
    }

    public dq(int i, int i2, int i3) {
        s(i);
        t(i2);
        u(i3);
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
        dq dqVar = (dq) obj;
        if (ft.equals(this.iJ, dqVar.iJ) && ft.equals(this.iK, dqVar.iK) && ft.equals(this.iL, dqVar.iL)) {
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
        s(fqVar.a(this.iJ, 1, true));
        t(fqVar.a(this.iK, 2, true));
        u(fqVar.a(this.iL, 3, true));
    }

    public void s(int i) {
        this.iJ = i;
    }

    public void t(int i) {
        this.iK = i;
    }

    public void u(int i) {
        this.iL = i;
    }

    public void writeTo(fr frVar) {
        frVar.write(this.iJ, 1);
        frVar.write(this.iK, 2);
        frVar.write(this.iL, 3);
    }
}
