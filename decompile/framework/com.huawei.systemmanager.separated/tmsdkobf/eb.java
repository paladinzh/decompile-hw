package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class eb extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    static ArrayList<ea> jn;
    public ArrayList<ea> jm = null;

    static {
        boolean z = false;
        if (!eb.class.desiredAssertionStatus()) {
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
        new fo(stringBuilder, i).a(this.jm, "vctscans");
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return ft.equals(this.jm, ((eb) obj).jm);
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
        if (jn == null) {
            jn = new ArrayList();
            jn.add(new ea());
        }
        this.jm = (ArrayList) fqVar.b(jn, 0, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.jm, 0);
    }
}
