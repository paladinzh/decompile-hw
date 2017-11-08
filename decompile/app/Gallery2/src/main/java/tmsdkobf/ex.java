package tmsdkobf;

/* compiled from: Unknown */
public final class ex extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public short in = (short) 0;
    public String io = "";

    static {
        boolean z = false;
        if (!ex.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public ex() {
        a(this.in);
        o(this.io);
    }

    public void a(short s) {
        this.in = (short) s;
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
        ex exVar = (ex) obj;
        if (ft.a(this.in, exVar.in) && ft.equals(this.io, exVar.io)) {
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

    public void o(String str) {
        this.io = str;
    }

    public void readFrom(fq fqVar) {
        a(fqVar.a(this.in, 0, true));
        o(fqVar.a(1, true));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.in, 0);
        frVar.a(this.io, 1);
    }
}
