package tmsdkobf;

/* compiled from: Unknown */
public final class ax extends fs {
    static byte[] bR = new byte[1];
    public int bN = 0;
    public int bO = 0;
    public String bP = "";
    public byte[] bQ = null;

    static {
        bR[0] = (byte) 0;
    }

    public fs newInit() {
        return new ax();
    }

    public void readFrom(fq fqVar) {
        this.bN = fqVar.a(this.bN, 0, true);
        this.bO = fqVar.a(this.bO, 1, false);
        this.bP = fqVar.a(2, false);
        this.bQ = fqVar.a(bR, 3, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.bN, 0);
        if (this.bO != 0) {
            frVar.write(this.bO, 1);
        }
        if (this.bP != null) {
            frVar.a(this.bP, 2);
        }
        if (this.bQ != null) {
            frVar.a(this.bQ, 3);
        }
    }
}
