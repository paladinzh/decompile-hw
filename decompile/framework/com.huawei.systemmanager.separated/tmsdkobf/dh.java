package tmsdkobf;

/* compiled from: Unknown */
public final class dh extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public String r = "";

    static {
        boolean z = false;
        if (!dh.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public dh() {
        t(this.r);
    }

    public String c() {
        return this.r;
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
        return ft.equals(this.r, ((dh) obj).r);
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
        t(fqVar.a(0, true));
    }

    public void t(String str) {
        this.r = str;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.r, 0);
    }
}
