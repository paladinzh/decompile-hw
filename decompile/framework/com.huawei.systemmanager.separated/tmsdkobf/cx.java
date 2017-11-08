package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class cx extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    static ArrayList<cw> gx;
    public ArrayList<cw> gw = null;

    static {
        boolean z = false;
        if (!cx.class.desiredAssertionStatus()) {
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
        new fo(stringBuilder, i).a(this.gw, "vctCommList");
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return ft.equals(this.gw, ((cx) obj).gw);
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
        if (gx == null) {
            gx = new ArrayList();
            gx.add(new cw());
        }
        this.gw = (ArrayList) fqVar.b(gx, 0, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.gw, 0);
    }
}
