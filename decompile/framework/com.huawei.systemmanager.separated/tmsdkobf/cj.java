package tmsdkobf;

/* compiled from: Unknown */
public final class cj extends fs {
    static ck fl = new ck();
    static cf fm = new cf();
    public ck fj = null;
    public cf fk = null;
    public int level = 0;
    public int linkType = 0;
    public int riskType = 0;
    public String url = "";

    public fs newInit() {
        return new cj();
    }

    public void readFrom(fq fqVar) {
        this.url = fqVar.a(0, true);
        this.level = fqVar.a(this.level, 1, true);
        this.linkType = fqVar.a(this.linkType, 2, true);
        this.riskType = fqVar.a(this.riskType, 3, false);
        this.fj = (ck) fqVar.a(fl, 4, false);
        this.fk = (cf) fqVar.a(fm, 5, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.url, 0);
        frVar.write(this.level, 1);
        frVar.write(this.linkType, 2);
        frVar.write(this.riskType, 3);
        if (this.fj != null) {
            frVar.a(this.fj, 4);
        }
        if (this.fk != null) {
            frVar.a(this.fk, 5);
        }
    }
}
