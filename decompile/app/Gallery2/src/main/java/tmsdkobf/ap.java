package tmsdkobf;

/* compiled from: Unknown */
public final class ap extends fs {
    static byte[] bz = new byte[1];
    public byte[] by = null;

    static {
        bz[0] = (byte) 0;
    }

    public fs newInit() {
        return new ap();
    }

    public void readFrom(fq fqVar) {
        this.by = fqVar.a(bz, 0, false);
    }

    public void writeTo(fr frVar) {
        if (this.by != null) {
            frVar.a(this.by, 0);
        }
    }
}
