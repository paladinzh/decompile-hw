package tmsdkobf;

/* compiled from: Unknown */
public final class eq extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public int pos = 0;
    public int size = 0;

    static {
        boolean z = false;
        if (!eq.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public eq() {
        Q(this.pos);
        setSize(this.size);
    }

    public void Q(int i) {
        this.pos = i;
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
        eq eqVar = (eq) obj;
        if (ft.equals(this.pos, eqVar.pos) && ft.equals(this.size, eqVar.size)) {
            z = true;
        }
        return z;
    }

    public int getPos() {
        return this.pos;
    }

    public int getSize() {
        return this.size;
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
        Q(fqVar.a(this.pos, 0, true));
        setSize(fqVar.a(this.size, 1, true));
    }

    public void setSize(int i) {
        this.size = i;
    }

    public void writeTo(fr frVar) {
        frVar.write(this.pos, 0);
        frVar.write(this.size, 1);
    }
}
