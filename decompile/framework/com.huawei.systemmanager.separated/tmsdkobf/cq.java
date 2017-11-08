package tmsdkobf;

/* compiled from: Unknown */
public final class cq extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public String fK = "";

    static {
        boolean z = false;
        if (!cq.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public cq() {
        a(this.fK);
    }

    public void a(String str) {
        this.fK = str;
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
        if (obj == null) {
            return false;
        }
        return ft.equals(this.fK, ((cq) obj).fK);
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
        a(fqVar.a(0, true));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.fK, 0);
    }
}
