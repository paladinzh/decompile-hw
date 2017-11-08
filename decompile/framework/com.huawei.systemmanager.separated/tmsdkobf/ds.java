package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ds extends fs {
    static final /* synthetic */ boolean fJ;
    static ArrayList<es> iP;
    public ArrayList<es> iO = null;

    static {
        boolean z = false;
        if (!ds.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public ds() {
        c(this.iO);
    }

    public ds(ArrayList<es> arrayList) {
        c(arrayList);
    }

    public void c(ArrayList<es> arrayList) {
        this.iO = arrayList;
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
        return ft.equals(this.iO, ((ds) obj).iO);
    }

    public void readFrom(fq fqVar) {
        if (iP == null) {
            iP = new ArrayList();
            iP.add(new es());
        }
        c((ArrayList) fqVar.b(iP, 0, false));
    }

    public void writeTo(fr frVar) {
        if (this.iO != null) {
            frVar.a(this.iO, 0);
        }
    }
}
