package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class er extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    static ArrayList<eq> la;
    public String id = "";
    public ArrayList<eq> kZ = null;

    static {
        boolean z = false;
        if (!er.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public er() {
        b(this.id);
        i(this.kZ);
    }

    public void b(String str) {
        this.id = str;
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
        er erVar = (er) obj;
        if (ft.equals(this.id, erVar.id) && ft.equals(this.kZ, erVar.kZ)) {
            z = true;
        }
        return z;
    }

    public ArrayList<eq> g() {
        return this.kZ;
    }

    public String getId() {
        return this.id;
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void i(ArrayList<eq> arrayList) {
        this.kZ = arrayList;
    }

    public void readFrom(fq fqVar) {
        b(fqVar.a(0, true));
        if (la == null) {
            la = new ArrayList();
            la.add(new eq());
        }
        i((ArrayList) fqVar.b(la, 1, true));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.id, 0);
        frVar.a(this.kZ, 1);
    }
}
