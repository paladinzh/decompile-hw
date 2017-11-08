package tmsdkobf;

/* compiled from: Unknown */
public final class al extends fs {
    static byte[] bs = new byte[1];
    public int bm = 0;
    public long bn = 0;
    public String bo = "";
    public byte[] bp = null;
    public boolean bq = false;
    public short br = (short) 0;
    public int i = 0;
    public int valueType = 0;

    static {
        bs[0] = (byte) 0;
    }

    public fs newInit() {
        return new al();
    }

    public void readFrom(fq fqVar) {
        this.valueType = fqVar.a(this.valueType, 0, false);
        this.bm = fqVar.a(this.bm, 1, false);
        this.i = fqVar.a(this.i, 2, false);
        this.bn = fqVar.a(this.bn, 3, false);
        this.bo = fqVar.a(4, false);
        this.bp = fqVar.a(bs, 5, false);
        this.bq = fqVar.a(this.bq, 6, false);
        this.br = (short) fqVar.a(this.br, 7, false);
    }

    public void writeTo(fr frVar) {
        if (this.valueType != 0) {
            frVar.write(this.valueType, 0);
        }
        if (this.bm != 0) {
            frVar.write(this.bm, 1);
        }
        if (this.i != 0) {
            frVar.write(this.i, 2);
        }
        if (this.bn != 0) {
            frVar.b(this.bn, 3);
        }
        if (this.bo != null) {
            frVar.a(this.bo, 4);
        }
        if (this.bp != null) {
            frVar.a(this.bp, 5);
        }
        if (this.bq) {
            frVar.a(this.bq, 6);
        }
        if (this.br != (short) 0) {
            frVar.a(this.br, 7);
        }
    }
}
