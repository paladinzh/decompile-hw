package tmsdkobf;

/* compiled from: Unknown */
public final class en extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public int A = 0;
    public int B = 0;
    public int time = 0;
    public boolean z = true;

    static {
        boolean z = false;
        if (!en.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public en() {
        v(this.time);
        a(this.z);
        O(this.A);
        P(this.B);
    }

    public void O(int i) {
        this.A = i;
    }

    public void P(int i) {
        this.B = i;
    }

    public void a(boolean z) {
        this.z = z;
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
        en enVar = (en) obj;
        if (ft.equals(this.time, enVar.time) && ft.a(this.z, enVar.z) && ft.equals(this.A, enVar.A) && ft.equals(this.B, enVar.B)) {
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
        v(fqVar.a(this.time, 0, true));
        a(fqVar.a(this.z, 1, true));
        O(fqVar.a(this.A, 2, false));
        P(fqVar.a(this.B, 3, false));
    }

    public void v(int i) {
        this.time = i;
    }

    public void writeTo(fr frVar) {
        frVar.write(this.time, 0);
        frVar.a(this.z, 1);
        frVar.write(this.A, 2);
        frVar.write(this.B, 3);
    }
}
