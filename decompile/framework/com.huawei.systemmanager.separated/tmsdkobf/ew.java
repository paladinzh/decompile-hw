package tmsdkobf;

/* compiled from: Unknown */
public final class ew extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public int lk = 2;
    public int timestamp = 0;
    public int version = 0;

    static {
        boolean z = false;
        if (!ew.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public ew() {
        W(this.timestamp);
        setVersion(this.version);
        X(this.lk);
    }

    public void W(int i) {
        this.timestamp = i;
    }

    public void X(int i) {
        this.lk = i;
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
        ew ewVar = (ew) obj;
        if (ft.equals(this.timestamp, ewVar.timestamp) && ft.equals(this.version, ewVar.version) && ft.equals(this.lk, ewVar.lk)) {
            z = true;
        }
        return z;
    }

    public int h() {
        return this.timestamp;
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
        W(fqVar.a(this.timestamp, 0, true));
        setVersion(fqVar.a(this.version, 1, true));
        X(fqVar.a(this.lk, 2, false));
    }

    public void setVersion(int i) {
        this.version = i;
    }

    public void writeTo(fr frVar) {
        frVar.write(this.timestamp, 0);
        frVar.write(this.version, 1);
        frVar.write(this.lk, 2);
    }
}
