package tmsdkobf;

/* compiled from: Unknown */
public final class dv extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public int iT = 0;
    public int iU = 0;

    static {
        boolean z = false;
        if (!dv.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public dv() {
        w(this.iT);
        x(this.iU);
    }

    public dv(int i, int i2) {
        w(i);
        x(i2);
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
        dv dvVar = (dv) obj;
        if (ft.equals(this.iT, dvVar.iT) && ft.equals(this.iU, dvVar.iU)) {
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
        w(fqVar.a(this.iT, 0, true));
        x(fqVar.a(this.iU, 1, true));
    }

    public void w(int i) {
        this.iT = i;
    }

    public void writeTo(fr frVar) {
        frVar.write(this.iT, 0);
        frVar.write(this.iU, 1);
    }

    public void x(int i) {
        this.iU = i;
    }
}
