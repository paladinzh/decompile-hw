package tmsdkobf;

/* compiled from: Unknown */
public final class cu extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    static byte[] gf;
    public int gc = 0;
    public byte[] gd = null;
    public int ge = 0;

    static {
        boolean z = false;
        if (!cu.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public cu() {
        g(this.gc);
        a(this.gd);
        h(this.ge);
    }

    public void a(byte[] bArr) {
        this.gd = bArr;
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
        cu cuVar = (cu) obj;
        if (ft.equals(this.gc, cuVar.gc) && ft.equals(this.gd, cuVar.gd) && ft.equals(this.ge, cuVar.ge)) {
            z = true;
        }
        return z;
    }

    public void g(int i) {
        this.gc = i;
    }

    public void h(int i) {
        this.ge = i;
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
        g(fqVar.a(this.gc, 0, true));
        if (gf == null) {
            gf = new byte[1];
            gf[0] = (byte) 0;
        }
        a(fqVar.a(gf, 1, true));
        h(fqVar.a(this.ge, 2, true));
    }

    public void writeTo(fr frVar) {
        frVar.write(this.gc, 0);
        frVar.a(this.gd, 1);
        frVar.write(this.ge, 2);
    }
}
