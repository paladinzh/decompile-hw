package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ec extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    static ArrayList<cv> jr;
    public ArrayList<cv> jo = null;
    public int jp = 0;
    public String jq = "";

    static {
        boolean z = false;
        if (!ec.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public ec() {
        f(this.jo);
        B(this.jp);
        N(this.jq);
    }

    public void B(int i) {
        this.jp = i;
    }

    public void N(String str) {
        this.jq = str;
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
        ec ecVar = (ec) obj;
        if (ft.equals(this.jo, ecVar.jo) && ft.equals(this.jp, ecVar.jp) && ft.equals(this.jq, ecVar.jq)) {
            z = true;
        }
        return z;
    }

    public String f() {
        return this.jq;
    }

    public void f(ArrayList<cv> arrayList) {
        this.jo = arrayList;
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
        if (jr == null) {
            jr = new ArrayList();
            jr.add(new cv());
        }
        f((ArrayList) fqVar.b(jr, 1, true));
        B(fqVar.a(this.jp, 2, true));
        N(fqVar.a(3, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.jo, 1);
        frVar.write(this.jp, 2);
        if (this.jq != null) {
            frVar.a(this.jq, 3);
        }
    }
}
