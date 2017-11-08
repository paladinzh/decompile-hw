package tmsdkobf;

/* compiled from: Unknown */
public final class af extends fs {
    static byte[] aR = new byte[1];
    static byte[] aS = new byte[1];
    static byte[] aq = new byte[1];
    public boolean aN = false;
    public byte[] aO = null;
    public byte[] aP = null;
    public int aQ = 0;
    public int am = 0;
    public byte[] an = null;
    public int fileSize = 0;
    public int timestamp = 0;
    public String url = "";

    static {
        aq[0] = (byte) 0;
        aR[0] = (byte) 0;
        aS[0] = (byte) 0;
    }

    public fs newInit() {
        return new af();
    }

    public void readFrom(fq fqVar) {
        this.am = fqVar.a(this.am, 0, true);
        this.an = fqVar.a(aq, 1, true);
        this.timestamp = fqVar.a(this.timestamp, 2, true);
        this.url = fqVar.a(3, false);
        this.aN = fqVar.a(this.aN, 4, false);
        this.aO = fqVar.a(aR, 5, false);
        this.aP = fqVar.a(aS, 6, false);
        this.aQ = fqVar.a(this.aQ, 7, false);
        this.fileSize = fqVar.a(this.fileSize, 8, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.am, 0);
        frVar.a(this.an, 1);
        frVar.write(this.timestamp, 2);
        if (this.url != null) {
            frVar.a(this.url, 3);
        }
        if (this.aN) {
            frVar.a(this.aN, 4);
        }
        if (this.aO != null) {
            frVar.a(this.aO, 5);
        }
        if (this.aP != null) {
            frVar.a(this.aP, 6);
        }
        if (this.aQ != 0) {
            frVar.write(this.aQ, 7);
        }
        if (this.fileSize != 0) {
            frVar.write(this.fileSize, 8);
        }
    }
}
