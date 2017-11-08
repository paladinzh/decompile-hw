package tmsdkobf;

/* compiled from: Unknown */
public final class cp extends fs implements Cloneable {
    static dg fF;
    static eh fG;
    static ct fH;
    static er fI;
    static final /* synthetic */ boolean fJ;
    public int bj = 0;
    public ct fA = null;
    public int fB = 0;
    public er fC = null;
    public int fD = 0;
    public int fE = 0;
    public dg fy = null;
    public eh fz = null;

    static {
        boolean z = false;
        if (!cp.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public cp() {
        a(this.fy);
        a(this.fz);
        a(this.fA);
        a(this.fB);
        a(this.fC);
        b(this.fD);
        c(this.fE);
        d(this.bj);
    }

    public er a() {
        return this.fC;
    }

    public void a(int i) {
        this.fB = i;
    }

    public void a(ct ctVar) {
        this.fA = ctVar;
    }

    public void a(dg dgVar) {
        this.fy = dgVar;
    }

    public void a(eh ehVar) {
        this.fz = ehVar;
    }

    public void a(er erVar) {
        this.fC = erVar;
    }

    public int b() {
        return this.fE;
    }

    public void b(int i) {
        this.fD = i;
    }

    public void c(int i) {
        this.fE = i;
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

    public void d(int i) {
        this.bj = i;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        cp cpVar = (cp) obj;
        if (ft.equals(this.fy, cpVar.fy) && ft.equals(this.fz, cpVar.fz) && ft.equals(this.fA, cpVar.fA) && ft.equals(this.fB, cpVar.fB) && ft.equals(this.fC, cpVar.fC) && ft.equals(this.fD, cpVar.fD) && ft.equals(this.fE, cpVar.fE) && ft.equals(this.bj, cpVar.bj)) {
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
        if (fF == null) {
            fF = new dg();
        }
        a((dg) fqVar.a(fF, 0, true));
        if (fG == null) {
            fG = new eh();
        }
        a((eh) fqVar.a(fG, 1, true));
        if (fH == null) {
            fH = new ct();
        }
        a((ct) fqVar.a(fH, 2, true));
        a(fqVar.a(this.fB, 3, true));
        if (fI == null) {
            fI = new er();
        }
        a((er) fqVar.a(fI, 4, false));
        b(fqVar.a(this.fD, 5, false));
        c(fqVar.a(this.fE, 6, false));
        d(fqVar.a(this.bj, 7, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.fy, 0);
        frVar.a(this.fz, 1);
        frVar.a(this.fA, 2);
        frVar.write(this.fB, 3);
        if (this.fC != null) {
            frVar.a(this.fC, 4);
        }
        frVar.write(this.fD, 5);
        frVar.write(this.fE, 6);
        frVar.write(this.bj, 7);
    }
}
