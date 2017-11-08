package tmsdkobf;

/* compiled from: Unknown */
public final class da extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public String cC = "";
    public String cD = "";
    public String cO = "";
    public int gH = 0;
    public String gI = "";
    public String gJ = "";
    public String gK = "";
    public String gL = "";
    public String iccid = "";
    public String imsi = "";

    static {
        boolean z = false;
        if (!da.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public da() {
        e(this.cC);
        f(this.imsi);
        g(this.cD);
        h(this.iccid);
        i(this.cO);
        j(this.gH);
        j(this.gI);
        k(this.gJ);
        l(this.gK);
        m(this.gL);
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

    public void e(String str) {
        this.cC = str;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        da daVar = (da) obj;
        if (ft.equals(this.cC, daVar.cC) && ft.equals(this.imsi, daVar.imsi) && ft.equals(this.cD, daVar.cD) && ft.equals(this.iccid, daVar.iccid) && ft.equals(this.cO, daVar.cO) && ft.equals(this.gH, daVar.gH) && ft.equals(this.gI, daVar.gI) && ft.equals(this.gJ, daVar.gJ) && ft.equals(this.gK, daVar.gK) && ft.equals(this.gL, daVar.gL)) {
            z = true;
        }
        return z;
    }

    public void f(String str) {
        this.imsi = str;
    }

    public void g(String str) {
        this.cD = str;
    }

    public void h(String str) {
        this.iccid = str;
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void i(String str) {
        this.cO = str;
    }

    public void j(int i) {
        this.gH = i;
    }

    public void j(String str) {
        this.gI = str;
    }

    public void k(String str) {
        this.gJ = str;
    }

    public void l(String str) {
        this.gK = str;
    }

    public void m(String str) {
        this.gL = str;
    }

    public void readFrom(fq fqVar) {
        e(fqVar.a(0, true));
        f(fqVar.a(1, false));
        g(fqVar.a(2, false));
        h(fqVar.a(3, false));
        i(fqVar.a(4, false));
        j(fqVar.a(this.gH, 5, false));
        j(fqVar.a(6, false));
        k(fqVar.a(7, false));
        l(fqVar.a(8, false));
        m(fqVar.a(9, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.cC, 0);
        if (this.imsi != null) {
            frVar.a(this.imsi, 1);
        }
        if (this.cD != null) {
            frVar.a(this.cD, 2);
        }
        if (this.iccid != null) {
            frVar.a(this.iccid, 3);
        }
        if (this.cO != null) {
            frVar.a(this.cO, 4);
        }
        frVar.write(this.gH, 5);
        if (this.gI != null) {
            frVar.a(this.gI, 6);
        }
        if (this.gJ != null) {
            frVar.a(this.gJ, 7);
        }
        if (this.gK != null) {
            frVar.a(this.gK, 8);
        }
        if (this.gL != null) {
            frVar.a(this.gL, 9);
        }
    }
}
