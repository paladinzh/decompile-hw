package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class dl extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    static ArrayList<String> iz;
    public String ix = "";
    public ArrayList<String> iy = null;

    static {
        boolean z = false;
        if (!dl.class.desiredAssertionStatus()) {
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
        foVar.a(this.ix, "typeName");
        foVar.a(this.iy, "keySet");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        dl dlVar = (dl) obj;
        if (ft.equals(this.ix, dlVar.ix) && ft.equals(this.iy, dlVar.iy)) {
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
        this.ix = fqVar.a(0, true);
        if (iz == null) {
            iz = new ArrayList();
            iz.add("");
        }
        this.iy = (ArrayList) fqVar.b(iz, 1, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.ix, 0);
        if (this.iy != null) {
            frVar.a(this.iy, 1);
        }
    }
}
