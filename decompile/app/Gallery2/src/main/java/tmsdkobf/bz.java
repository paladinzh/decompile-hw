package tmsdkobf;

/* compiled from: Unknown */
public final class bz extends fs {
    public int el = 0;
    public int em = 0;

    public fs newInit() {
        return new bz();
    }

    public void readFrom(fq fqVar) {
        this.el = fqVar.a(this.el, 0, true);
        this.em = fqVar.a(this.em, 1, true);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.el, 0);
        frVar.write(this.em, 1);
    }
}
