package tmsdkobf;

/* compiled from: Unknown */
public final class ek extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public String kV = "";
    public int kW = 0;

    static {
        boolean z = false;
        if (!ek.class.desiredAssertionStatus()) {
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
        fo foVar = new fo(stringBuilder, i);
        foVar.a(this.kV, "riskDesc");
        foVar.a(this.kW, "riskLevel");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        ek ekVar = (ek) obj;
        if (ft.equals(this.kV, ekVar.kV) && ft.equals(this.kW, ekVar.kW)) {
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
        this.kV = fqVar.a(0, false);
        this.kW = fqVar.a(this.kW, 1, false);
    }

    public void writeTo(fr frVar) {
        if (this.kV != null) {
            frVar.a(this.kV, 0);
        }
        frVar.write(this.kW, 1);
    }
}
