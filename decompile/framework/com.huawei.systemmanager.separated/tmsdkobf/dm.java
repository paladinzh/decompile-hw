package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class dm extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    static ArrayList<dl> iB;
    public ArrayList<dl> iA = null;

    static {
        boolean z = false;
        if (!dm.class.desiredAssertionStatus()) {
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
        new fo(stringBuilder, i).a(this.iA, "vctInterfaceInfos");
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return ft.equals(this.iA, ((dm) obj).iA);
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
        if (iB == null) {
            iB = new ArrayList();
            iB.add(new dl());
        }
        this.iA = (ArrayList) fqVar.b(iB, 0, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.iA, 0);
    }
}
