package tmsdkobf;

/* compiled from: Unknown */
public final class ef extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public String cL = "";
    public String iq = "";
    public int js = 0;
    public int jt = 0;
    public String ju = "";
    public String name = "";
    public String path = "";

    static {
        boolean z = false;
        if (!ef.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
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
        fo foVar = new fo(stringBuilder, i);
        foVar.a(this.cL, "pkgname");
        foVar.a(this.iq, "cert");
        foVar.a(this.js, "softsize");
        foVar.a(this.path, "path");
        foVar.a(this.name, "name");
        foVar.a(this.jt, "isOfficial");
        foVar.a(this.ju, "expanda");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        ef efVar = (ef) obj;
        if (ft.equals(this.cL, efVar.cL) && ft.equals(this.iq, efVar.iq) && ft.equals(this.js, efVar.js) && ft.equals(this.path, efVar.path) && ft.equals(this.name, efVar.name) && ft.equals(this.jt, efVar.jt) && ft.equals(this.ju, efVar.ju)) {
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
        this.cL = fqVar.a(0, true);
        this.iq = fqVar.a(1, true);
        this.js = fqVar.a(this.js, 3, false);
        this.path = fqVar.a(4, false);
        this.name = fqVar.a(5, false);
        this.jt = fqVar.a(this.jt, 6, false);
        this.ju = fqVar.a(7, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.cL, 0);
        frVar.a(this.iq, 1);
        frVar.write(this.js, 3);
        if (this.path != null) {
            frVar.a(this.path, 4);
        }
        if (this.name != null) {
            frVar.a(this.name, 5);
        }
        frVar.write(this.jt, 6);
        if (this.ju != null) {
            frVar.a(this.ju, 7);
        }
    }
}
