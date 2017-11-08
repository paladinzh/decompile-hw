package tmsdkobf;

/* compiled from: Unknown */
public final class ch extends fs {
    static cj fi = new cj();
    public cj fh = null;

    public fs newInit() {
        return new ch();
    }

    public void readFrom(fq fqVar) {
        this.fh = (cj) fqVar.a(fi, 0, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.fh, 0);
    }
}
