package tmsdkobf;

/* compiled from: Unknown */
public final class dw extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public int iC = 0;

    static {
        boolean z = false;
        if (!dw.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public dw() {
        n(this.iC);
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
        if (obj == null) {
            return false;
        }
        return ft.equals(this.iC, ((dw) obj).iC);
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

    public void readFrom(fq fqVar) {
        n(fqVar.a(this.iC, 0, true));
    }

    public void writeTo(fr frVar) {
        frVar.write(this.iC, 0);
    }
}
