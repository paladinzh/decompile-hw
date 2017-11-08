package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class cr extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    static ArrayList<ei> fO;
    public int fL = 0;
    public String fM = "";
    public ArrayList<ei> fN = null;
    public String id = "";
    public int product = dc.ha.value();

    static {
        boolean z = false;
        if (!cr.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public cr() {
        b(this.id);
        e(this.product);
        f(this.fL);
        c(this.fM);
        a(this.fN);
    }

    public void a(ArrayList<ei> arrayList) {
        this.fN = arrayList;
    }

    public void b(String str) {
        this.id = str;
    }

    public void c(String str) {
        this.fM = str;
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

    public void e(int i) {
        this.product = i;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        cr crVar = (cr) obj;
        if (ft.equals(this.id, crVar.id) && ft.equals(this.product, crVar.product) && ft.equals(this.fL, crVar.fL) && ft.equals(this.fM, crVar.fM) && ft.equals(this.fN, crVar.fN)) {
            z = true;
        }
        return z;
    }

    public void f(int i) {
        this.fL = i;
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
        b(fqVar.a(0, true));
        e(fqVar.a(this.product, 1, false));
        f(fqVar.a(this.fL, 2, false));
        c(fqVar.a(3, false));
        if (fO == null) {
            fO = new ArrayList();
            fO.add(new ei());
        }
        a((ArrayList) fqVar.b(fO, 4, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.id, 0);
        frVar.write(this.product, 1);
        frVar.write(this.fL, 2);
        if (this.fM != null) {
            frVar.a(this.fM, 3);
        }
        if (this.fN != null) {
            frVar.a(this.fN, 4);
        }
    }
}
