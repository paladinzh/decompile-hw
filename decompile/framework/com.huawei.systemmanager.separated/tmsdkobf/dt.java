package tmsdkobf;

/* compiled from: Unknown */
public final class dt extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public String iQ = "";
    public int time = 0;

    static {
        boolean z = false;
        if (!dt.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public dt() {
        v(this.time);
        A(this.iQ);
    }

    public void A(String str) {
        this.iQ = str;
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
        dt dtVar = (dt) obj;
        if (ft.equals(this.time, dtVar.time) && ft.equals(this.iQ, dtVar.iQ)) {
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
        v(fqVar.a(this.time, 0, false));
        A(fqVar.a(1, false));
    }

    public void v(int i) {
        this.time = i;
    }

    public void writeTo(fr frVar) {
        frVar.write(this.time, 0);
        if (this.iQ != null) {
            frVar.a(this.iQ, 1);
        }
    }
}
