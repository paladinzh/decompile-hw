package tmsdkobf;

/* compiled from: Unknown */
public final class o extends fs {
    public int H = 0;
    public int O = 0;
    public long U = 0;
    public long V = 0;
    public int W = 0;
    public int X = 0;
    public int action = 0;
    public int result = 0;
    public int time = 0;

    public fs newInit() {
        return new o();
    }

    public void readFrom(fq fqVar) {
        this.U = fqVar.a(this.U, 0, false);
        this.V = fqVar.a(this.V, 1, false);
        this.action = fqVar.a(this.action, 2, false);
        this.O = fqVar.a(this.O, 3, false);
        this.H = fqVar.a(this.H, 4, false);
        this.W = fqVar.a(this.W, 5, false);
        this.result = fqVar.a(this.result, 6, false);
        this.X = fqVar.a(this.X, 7, false);
        this.time = fqVar.a(this.time, 8, false);
    }

    public void writeTo(fr frVar) {
        if (this.U != 0) {
            frVar.b(this.U, 0);
        }
        if (this.V != 0) {
            frVar.b(this.V, 1);
        }
        frVar.write(this.action, 2);
        if (this.O != 0) {
            frVar.write(this.O, 3);
        }
        frVar.write(this.H, 4);
        frVar.write(this.W, 5);
        frVar.write(this.result, 6);
        frVar.write(this.X, 7);
        if (this.time != 0) {
            frVar.write(this.time, 8);
        }
    }
}
