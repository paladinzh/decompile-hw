package tmsdkobf;

/* compiled from: Unknown */
public final class ep extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public String cL = "";

    static {
        boolean z = false;
        if (!ep.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public ep() {
        R(this.cL);
    }

    public ep(String str) {
        R(str);
    }

    public void R(String str) {
        this.cL = str;
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
        return ft.equals(this.cL, ((ep) obj).cL);
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
        R(fqVar.a(0, false));
    }

    public void writeTo(fr frVar) {
        if (this.cL != null) {
            frVar.a(this.cL, 0);
        }
    }
}
