package tmsdkobf;

/* compiled from: Unknown */
public final class cg extends fs {
    static ci fg = new ci();
    public int fd = 0;
    public int fe = 0;
    public ci ff = null;

    public fs newInit() {
        return new cg();
    }

    public void readFrom(fq fqVar) {
        this.fd = fqVar.a(this.fd, 0, true);
        this.fe = fqVar.a(this.fe, 1, true);
        this.ff = (ci) fqVar.a(fg, 2, true);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.fd, 0);
        frVar.write(this.fe, 1);
        frVar.a(this.ff, 2);
    }
}
