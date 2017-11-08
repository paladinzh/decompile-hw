package tmsdkobf;

/* compiled from: Unknown */
public final class dn extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public int cJ = dd.hL.value();
    public int iC = 0;

    static {
        boolean z = false;
        if (!dn.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public dn() {
        n(this.iC);
        o(this.cJ);
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
        dn dnVar = (dn) obj;
        if (ft.equals(this.iC, dnVar.iC) && ft.equals(this.cJ, dnVar.cJ)) {
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

    public void n(int i) {
        this.iC = i;
    }

    public void o(int i) {
        this.cJ = i;
    }

    public void readFrom(fq fqVar) {
        n(fqVar.a(this.iC, 0, true));
        o(fqVar.a(this.cJ, 1, false));
    }

    public void writeTo(fr frVar) {
        frVar.write(this.iC, 0);
        frVar.write(this.cJ, 1);
    }
}
