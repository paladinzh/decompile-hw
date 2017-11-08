package tmsdkobf;

/* compiled from: Unknown */
public final class m extends fs {
    static byte[] R = new byte[1];
    static u S = new u();
    static v T = new v();
    public int H = 0;
    public byte[] N = null;
    public int O = 0;
    public u P = null;
    public v Q = null;

    static {
        R[0] = (byte) 0;
    }

    public fs newInit() {
        return new m();
    }

    public void readFrom(fq fqVar) {
        this.H = fqVar.a(this.H, 0, false);
        this.N = fqVar.a(R, 1, false);
        this.O = fqVar.a(this.O, 2, false);
        this.P = (u) fqVar.a(S, 3, false);
        this.Q = (v) fqVar.a(T, 4, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.H, 0);
        if (this.N != null) {
            frVar.a(this.N, 1);
        }
        if (this.O != 0) {
            frVar.write(this.O, 2);
        }
        if (this.P != null) {
            frVar.a(this.P, 3);
        }
        if (this.Q != null) {
            frVar.a(this.Q, 4);
        }
    }
}
