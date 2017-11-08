package tmsdkobf;

/* compiled from: Unknown */
public final class z extends fs {
    static byte[] aq = new byte[1];
    public int am = 0;
    public byte[] an = null;
    public int ao = 0;
    public int ap = 0;
    public int timestamp = 0;
    public int version = 0;

    static {
        aq[0] = (byte) 0;
    }

    public fs newInit() {
        return new z();
    }

    public void readFrom(fq fqVar) {
        this.am = fqVar.a(this.am, 0, true);
        this.an = fqVar.a(aq, 1, true);
        this.timestamp = fqVar.a(this.timestamp, 2, true);
        this.ao = fqVar.a(this.ao, 3, false);
        this.ap = fqVar.a(this.ap, 4, false);
        this.version = fqVar.a(this.version, 5, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.am, 0);
        frVar.a(this.an, 1);
        frVar.write(this.timestamp, 2);
        if (this.ao != 0) {
            frVar.write(this.ao, 3);
        }
        if (this.ap != 0) {
            frVar.write(this.ap, 4);
        }
        if (this.version != 0) {
            frVar.write(this.version, 5);
        }
    }
}
