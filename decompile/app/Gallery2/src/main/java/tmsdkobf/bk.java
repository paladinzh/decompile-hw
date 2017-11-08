package tmsdkobf;

/* compiled from: Unknown */
public final class bk extends fs {
    static bj dE = new bj();
    public bj dC = null;
    public String dD = "";

    public fs newInit() {
        return new bk();
    }

    public void readFrom(fq fqVar) {
        this.dC = (bj) fqVar.a(dE, 0, true);
        this.dD = fqVar.a(1, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.dC, 0);
        frVar.a(this.dD, 1);
    }
}
