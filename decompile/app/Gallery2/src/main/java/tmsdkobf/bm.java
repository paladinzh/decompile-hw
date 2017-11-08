package tmsdkobf;

/* compiled from: Unknown */
public final class bm extends fs {
    static byte[] dM = new byte[1];
    static bl dN = new bl();
    public int aZ = 0;
    public int dG = 0;
    public int dH = 0;
    public long dI = 0;
    public int dJ = 0;
    public int dK = 0;
    public bl dL = null;
    public byte[] data = null;

    static {
        dM[0] = (byte) 0;
    }

    public fs newInit() {
        return new bm();
    }

    public void readFrom(fq fqVar) {
        this.aZ = fqVar.a(this.aZ, 0, true);
        this.dG = fqVar.a(this.dG, 1, false);
        this.dH = fqVar.a(this.dH, 2, false);
        this.data = fqVar.a(dM, 3, false);
        this.dI = fqVar.a(this.dI, 4, false);
        this.dJ = fqVar.a(this.dJ, 5, false);
        this.dK = fqVar.a(this.dK, 6, false);
        this.dL = (bl) fqVar.a(dN, 7, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.aZ, 0);
        if (this.dG != 0) {
            frVar.write(this.dG, 1);
        }
        if (this.dH != 0) {
            frVar.write(this.dH, 2);
        }
        if (this.data != null) {
            frVar.a(this.data, 3);
        }
        if (this.dI != 0) {
            frVar.b(this.dI, 4);
        }
        if (this.dJ != 0) {
            frVar.write(this.dJ, 5);
        }
        if (this.dK != 0) {
            frVar.write(this.dK, 6);
        }
        if (this.dL != null) {
            frVar.a(this.dL, 7);
        }
    }
}
