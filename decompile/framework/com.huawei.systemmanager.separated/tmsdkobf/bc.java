package tmsdkobf;

/* compiled from: Unknown */
public final class bc extends fs {
    static byte[] cb = new byte[1];
    static byte[] cc = new byte[1];
    public byte[] bY = null;
    public byte[] bZ = null;
    public float ca = -1.0f;

    static {
        cb[0] = (byte) 0;
        cc[0] = (byte) 0;
    }

    public fs newInit() {
        return new bc();
    }

    public void readFrom(fq fqVar) {
        this.bY = fqVar.a(cb, 0, false);
        this.bZ = fqVar.a(cc, 1, false);
        this.ca = fqVar.a(this.ca, 2, false);
    }

    public void writeTo(fr frVar) {
        if (this.bY != null) {
            frVar.a(this.bY, 0);
        }
        if (this.bZ != null) {
            frVar.a(this.bZ, 1);
        }
        if (this.ca != -1.0f) {
            frVar.a(this.ca, 2);
        }
    }
}
