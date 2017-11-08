package tmsdkobf;

/* compiled from: Unknown */
public final class cs extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public long fP = 0;
    public String fQ = "";
    public int state = 0;
    public float weight = 0.0f;

    static {
        boolean z = false;
        if (!cs.class.desiredAssertionStatus()) {
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

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        cs csVar = (cs) obj;
        if (ft.a(this.fP, csVar.fP) && ft.equals(this.weight, csVar.weight) && ft.equals(this.fQ, csVar.fQ) && ft.equals(this.state, csVar.state)) {
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
        this.fP = fqVar.a(this.fP, 0, true);
        this.weight = fqVar.a(this.weight, 1, true);
        this.fQ = fqVar.a(2, true);
        this.state = fqVar.a(this.state, 3, false);
    }

    public void writeTo(fr frVar) {
        frVar.b(this.fP, 0);
        frVar.a(this.weight, 1);
        frVar.a(this.fQ, 2);
        frVar.write(this.state, 3);
    }
}
