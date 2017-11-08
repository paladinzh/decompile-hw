package tmsdkobf;

/* compiled from: Unknown */
public final class dk extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    static ep iw;
    public String iu = "";
    public ep iv = null;

    static {
        boolean z = false;
        if (!dk.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public dk() {
        w(this.iu);
        a(this.iv);
    }

    public void a(ep epVar) {
        this.iv = epVar;
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
        dk dkVar = (dk) obj;
        if (ft.equals(this.iu, dkVar.iu) && ft.equals(this.iv, dkVar.iv)) {
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
        w(fqVar.a(0, true));
        if (iw == null) {
            iw = new ep();
        }
        a((ep) fqVar.a(iw, 1, false));
    }

    public void w(String str) {
        this.iu = str;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.iu, 0);
        if (this.iv != null) {
            frVar.a(this.iv, 1);
        }
    }
}
