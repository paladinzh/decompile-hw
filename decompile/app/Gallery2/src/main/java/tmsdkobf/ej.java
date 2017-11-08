package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ej extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    static ArrayList<ef> kT;
    static ek kU;
    public ArrayList<ef> kR = null;
    public ek kS = null;

    static {
        boolean z = false;
        if (!ej.class.desiredAssertionStatus()) {
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
        foVar.a(this.kR, "vctSofts");
        foVar.a(this.kS, "softListInfo");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        ej ejVar = (ej) obj;
        if (ft.equals(this.kR, ejVar.kR) && ft.equals(this.kS, ejVar.kS)) {
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
        if (kT == null) {
            kT = new ArrayList();
            kT.add(new ef());
        }
        this.kR = (ArrayList) fqVar.b(kT, 0, true);
        if (kU == null) {
            kU = new ek();
        }
        this.kS = (ek) fqVar.a(kU, 1, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.kR, 0);
        if (this.kS != null) {
            frVar.a(this.kS, 1);
        }
    }
}
