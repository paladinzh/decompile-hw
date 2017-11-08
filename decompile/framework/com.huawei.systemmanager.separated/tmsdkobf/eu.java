package tmsdkobf;

/* compiled from: Unknown */
public final class eu extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public int eV = 0;
    public int time = 0;

    static {
        boolean z = false;
        if (!eu.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public eu() {
        V(this.eV);
        v(this.time);
    }

    public void V(int i) {
        this.eV = i;
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
        eu euVar = (eu) obj;
        if (ft.equals(this.eV, euVar.eV) && ft.equals(this.time, euVar.time)) {
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
        V(fqVar.a(this.eV, 0, true));
        v(fqVar.a(this.time, 1, true));
    }

    public void v(int i) {
        this.time = i;
    }

    public void writeTo(fr frVar) {
        frVar.write(this.eV, 0);
        frVar.write(this.time, 1);
    }
}
