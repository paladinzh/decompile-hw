package tmsdkobf;

/* compiled from: Unknown */
public final class dp extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public String iG = "";
    public String iH = "";
    public String iI = "";

    static {
        boolean z = false;
        if (!dp.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public dp() {
        x(this.iG);
        y(this.iH);
        z(this.iI);
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
        dp dpVar = (dp) obj;
        if (ft.equals(this.iG, dpVar.iG) && ft.equals(this.iH, dpVar.iH) && ft.equals(this.iI, dpVar.iI)) {
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
        x(fqVar.a(0, true));
        y(fqVar.a(1, false));
        z(fqVar.a(2, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.iG, 0);
        if (this.iH != null) {
            frVar.a(this.iH, 1);
        }
        if (this.iI != null) {
            frVar.a(this.iI, 2);
        }
    }

    public void x(String str) {
        this.iG = str;
    }

    public void y(String str) {
        this.iH = str;
    }

    public void z(String str) {
        this.iI = str;
    }
}
