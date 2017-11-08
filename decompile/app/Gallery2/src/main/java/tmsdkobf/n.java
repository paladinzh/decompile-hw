package tmsdkobf;

/* compiled from: Unknown */
public final class n extends fs {
    public int H = 0;
    public int O = 0;
    public long U = 0;
    public long V = 0;
    public int result = 0;

    public fs newInit() {
        return new n();
    }

    public void readFrom(fq fqVar) {
        this.U = fqVar.a(this.U, 0, false);
        this.V = fqVar.a(this.V, 1, false);
        this.O = fqVar.a(this.O, 2, false);
        this.H = fqVar.a(this.H, 3, false);
        this.result = fqVar.a(this.result, 4, false);
    }

    public void writeTo(fr frVar) {
        if (this.U != 0) {
            frVar.b(this.U, 0);
        }
        if (this.V != 0) {
            frVar.b(this.V, 1);
        }
        if (this.O != 0) {
            frVar.write(this.O, 2);
        }
        frVar.write(this.H, 3);
        if (this.result != 0) {
            frVar.write(this.result, 4);
        }
    }
}
