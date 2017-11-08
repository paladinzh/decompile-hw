package tmsdkobf;

/* compiled from: Unknown */
public final class es extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public String lb = "";
    public int seq = 0;
    public String url = "";
    public int version = 0;

    static {
        boolean z = false;
        if (!es.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public es() {
        setUrl(this.url);
        S(this.lb);
        R(this.seq);
        setVersion(this.version);
    }

    public es(String str, String str2, int i, int i2) {
        setUrl(str);
        S(str2);
        R(i);
        setVersion(i2);
    }

    public void R(int i) {
        this.seq = i;
    }

    public void S(String str) {
        this.lb = str;
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
        es esVar = (es) obj;
        if (ft.equals(this.url, esVar.url) && ft.equals(this.lb, esVar.lb) && ft.equals(this.seq, esVar.seq) && ft.equals(this.version, esVar.version)) {
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
        setUrl(fqVar.a(0, true));
        S(fqVar.a(1, false));
        R(fqVar.a(this.seq, 2, false));
        setVersion(fqVar.a(this.version, 3, false));
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public void setVersion(int i) {
        this.version = i;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.url, 0);
        if (this.lb != null) {
            frVar.a(this.lb, 1);
        }
        frVar.write(this.seq, 2);
        frVar.write(this.version, 3);
    }
}
