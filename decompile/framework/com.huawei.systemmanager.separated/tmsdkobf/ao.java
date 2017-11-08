package tmsdkobf;

/* compiled from: Unknown */
public final class ao extends fs {
    public String aA = "";
    public long bu = 0;
    public boolean bv = true;
    public long bw = 0;
    public long bx = 0;
    public String certMd5 = "";
    public String dexSha1 = "";
    public String packageName = "";
    public String softName = "";
    public String version = "";

    public fs newInit() {
        return new ao();
    }

    public void readFrom(fq fqVar) {
        this.packageName = fqVar.a(0, false);
        this.softName = fqVar.a(1, false);
        this.certMd5 = fqVar.a(2, false);
        this.bu = fqVar.a(this.bu, 3, false);
        this.bv = fqVar.a(this.bv, 4, false);
        this.dexSha1 = fqVar.a(5, false);
        this.aA = fqVar.a(6, false);
        this.bw = fqVar.a(this.bw, 7, false);
        this.version = fqVar.a(8, false);
        this.bx = fqVar.a(this.bx, 9, false);
    }

    public void writeTo(fr frVar) {
        if (this.packageName != null) {
            frVar.a(this.packageName, 0);
        }
        if (this.softName != null) {
            frVar.a(this.softName, 1);
        }
        if (this.certMd5 != null) {
            frVar.a(this.certMd5, 2);
        }
        if (this.bu != 0) {
            frVar.b(this.bu, 3);
        }
        frVar.a(this.bv, 4);
        if (this.dexSha1 != null) {
            frVar.a(this.dexSha1, 5);
        }
        if (this.aA != null) {
            frVar.a(this.aA, 6);
        }
        if (this.bw != 0) {
            frVar.b(this.bw, 7);
        }
        if (this.version != null) {
            frVar.a(this.version, 8);
        }
        if (this.bx != 0) {
            frVar.b(this.bx, 9);
        }
    }
}
