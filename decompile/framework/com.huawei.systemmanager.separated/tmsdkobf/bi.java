package tmsdkobf;

/* compiled from: Unknown */
public final class bi extends fs {
    static az cB = new az();
    public int bC = 0;
    public int cA = 0;
    public String cu = "";
    public String cv = "";
    public int cw = 0;
    public az cx = null;
    public int cy = 0;
    public String cz = "";
    public String imsi = "";
    public int status = 0;

    public fs newInit() {
        return new bi();
    }

    public void readFrom(fq fqVar) {
        this.cu = fqVar.a(0, true);
        this.cv = fqVar.a(1, true);
        this.cw = fqVar.a(this.cw, 2, true);
        this.cx = (az) fqVar.a(cB, 3, true);
        this.cy = fqVar.a(this.cy, 4, true);
        this.cz = fqVar.a(5, true);
        this.status = fqVar.a(this.status, 6, false);
        this.imsi = fqVar.a(7, false);
        this.cA = fqVar.a(this.cA, 8, false);
        this.bC = fqVar.a(this.bC, 9, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.cu, 0);
        frVar.a(this.cv, 1);
        frVar.write(this.cw, 2);
        frVar.a(this.cx, 3);
        frVar.write(this.cy, 4);
        frVar.a(this.cz, 5);
        frVar.write(this.status, 6);
        if (this.imsi != null) {
            frVar.a(this.imsi, 7);
        }
        frVar.write(this.cA, 8);
        if (this.bC != 0) {
            frVar.write(this.bC, 9);
        }
    }
}
