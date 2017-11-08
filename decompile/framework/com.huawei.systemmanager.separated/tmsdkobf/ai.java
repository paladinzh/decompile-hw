package tmsdkobf;

/* compiled from: Unknown */
public final class ai extends fs {
    static byte[] bb = new byte[1];
    public int aZ = 0;
    public byte[] ba = null;
    public int status = 0;

    static {
        bb[0] = (byte) 0;
    }

    public fs newInit() {
        return new ai();
    }

    public void readFrom(fq fqVar) {
        this.aZ = fqVar.a(this.aZ, 0, true);
        this.ba = fqVar.a(bb, 1, false);
        this.status = fqVar.a(this.status, 2, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.aZ, 0);
        if (this.ba != null) {
            frVar.a(this.ba, 1);
        }
        if (this.status != 0) {
            frVar.write(this.status, 2);
        }
    }
}
