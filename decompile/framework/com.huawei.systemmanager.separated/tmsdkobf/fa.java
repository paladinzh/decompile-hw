package tmsdkobf;

/* compiled from: Unknown */
public final class fa extends fs {
    static final /* synthetic */ boolean fJ;
    public String lD = "";
    public long lE = 0;
    public int lF = 0;
    public String lG = "";
    public int lH = 0;
    public int lI = 0;

    static {
        boolean z = false;
        if (!fa.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public fa() {
        W(this.lD);
        a(this.lE);
        Y(this.lF);
        X(this.lG);
        Z(this.lH);
        aa(this.lI);
    }

    public void W(String str) {
        this.lD = str;
    }

    public void X(String str) {
        this.lG = str;
    }

    public void Y(int i) {
        this.lF = i;
    }

    public void Z(int i) {
        this.lH = i;
    }

    public void a(long j) {
        this.lE = j;
    }

    public void aa(int i) {
        this.lI = i;
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
        fa faVar = (fa) obj;
        return ft.equals(this.lD, faVar.lD) && ft.a(this.lE, faVar.lE) && ft.equals(this.lF, faVar.lF) && ft.equals(this.lG, faVar.lG) && ft.equals(this.lH, faVar.lH) && ft.equals(this.lI, faVar.lI);
    }

    public void readFrom(fq fqVar) {
        W(fqVar.a(0, true));
        a(fqVar.a(this.lE, 1, true));
        Y(fqVar.a(this.lF, 2, true));
        X(fqVar.a(3, true));
        Z(fqVar.a(this.lH, 4, true));
        aa(fqVar.a(this.lI, 5, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.lD, 0);
        frVar.b(this.lE, 1);
        frVar.write(this.lF, 2);
        frVar.a(this.lG, 3);
        frVar.write(this.lH, 4);
        frVar.write(this.lI, 5);
    }
}
