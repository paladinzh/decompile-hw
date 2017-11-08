package tmsdkobf;

/* compiled from: Unknown */
public final class am extends fs {
    public int bf = 0;
    public int bt = 0;

    public fs newInit() {
        return new am();
    }

    public void readFrom(fq fqVar) {
        this.bf = fqVar.a(this.bf, 0, false);
        this.bt = fqVar.a(this.bt, 1, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.bf, 0);
        frVar.write(this.bt, 1);
    }
}
