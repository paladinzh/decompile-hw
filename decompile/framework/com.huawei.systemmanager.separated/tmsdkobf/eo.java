package tmsdkobf;

/* compiled from: Unknown */
public final class eo extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public String C = "";
    public int D = 0;
    public int kX = 0;
    public int kY = 0;
    public String title = "";
    public int type = 0;

    static {
        boolean z = false;
        if (!eo.class.desiredAssertionStatus()) {
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

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        eo eoVar = (eo) obj;
        if (ft.equals(this.title, eoVar.title) && ft.equals(this.C, eoVar.C) && ft.equals(this.type, eoVar.type) && ft.equals(this.D, eoVar.D) && ft.equals(this.kX, eoVar.kX) && ft.equals(this.kY, eoVar.kY)) {
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
        this.title = fqVar.a(0, true);
        this.C = fqVar.a(1, true);
        this.type = fqVar.a(this.type, 2, true);
        this.D = fqVar.a(this.D, 3, true);
        this.kX = fqVar.a(this.kX, 4, false);
        this.kY = fqVar.a(this.kY, 5, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.title, 0);
        frVar.a(this.C, 1);
        frVar.write(this.type, 2);
        frVar.write(this.D, 3);
        frVar.write(this.kX, 4);
        frVar.write(this.kY, 5);
    }
}
