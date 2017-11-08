package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class du extends fs {
    static final /* synthetic */ boolean fJ;
    static ArrayList<et> iS;
    public ArrayList<et> iR = null;

    static {
        boolean z = false;
        if (!du.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public du() {
        d(this.iR);
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

    public ArrayList<et> d() {
        return this.iR;
    }

    public void d(ArrayList<et> arrayList) {
        this.iR = arrayList;
    }

    public boolean equals(Object obj) {
        return ft.equals(this.iR, ((du) obj).iR);
    }

    public void readFrom(fq fqVar) {
        if (iS == null) {
            iS = new ArrayList();
            iS.add(new et());
        }
        d((ArrayList) fqVar.b(iS, 0, false));
    }

    public void writeTo(fr frVar) {
        if (this.iR != null) {
            frVar.a(this.iR, 0);
        }
    }
}
