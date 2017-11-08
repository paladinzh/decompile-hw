package tmsdkobf;

/* compiled from: Unknown */
public final class bq extends fs {
    static byte[] dM = new byte[1];
    static bp dU = new bp();
    public int aZ = 0;
    public int dG = 0;
    public int dH = 0;
    public int dJ = 0;
    public int dK = 0;
    public bp dT = null;
    public byte[] data = null;

    static {
        dM[0] = (byte) 0;
    }

    public fs newInit() {
        return new bq();
    }

    public void readFrom(fq fqVar) {
        this.aZ = fqVar.a(this.aZ, 0, true);
        this.dG = fqVar.a(this.dG, 1, false);
        this.dH = fqVar.a(this.dH, 2, false);
        this.dJ = fqVar.a(this.dJ, 3, false);
        this.dK = fqVar.a(this.dK, 4, false);
        this.data = fqVar.a(dM, 5, false);
        this.dT = (bp) fqVar.a(dU, 6, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.aZ, 0);
        if (this.dG != 0) {
            frVar.write(this.dG, 1);
        }
        if (this.dH != 0) {
            frVar.write(this.dH, 2);
        }
        frVar.write(this.dJ, 3);
        if (this.dK != 0) {
            frVar.write(this.dK, 4);
        }
        if (this.data != null) {
            frVar.a(this.data, 5);
        }
        if (this.dT != null) {
            frVar.a(this.dT, 6);
        }
    }
}
