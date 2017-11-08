package tmsdkobf;

/* compiled from: Unknown */
public final class ay extends fs {
    static ax bU = new ax();
    public ax bS = null;
    public boolean bT = true;

    public fs newInit() {
        return new ay();
    }

    public void readFrom(fq fqVar) {
        this.bS = (ax) fqVar.a(bU, 0, true);
        this.bT = fqVar.a(this.bT, 1, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.bS, 0);
        frVar.a(this.bT, 1);
    }
}
