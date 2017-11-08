package tmsdkobf;

/* compiled from: Unknown */
public final class g extends fs {
    public int A = 0;
    public int B = 0;
    public int time = 0;
    public boolean z = true;

    public fs newInit() {
        return new g();
    }

    public void readFrom(fq fqVar) {
        this.time = fqVar.a(this.time, 0, true);
        this.z = fqVar.a(this.z, 1, true);
        this.A = fqVar.a(this.A, 2, false);
        this.B = fqVar.a(this.B, 3, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.time, 0);
        frVar.a(this.z, 1);
        if (this.A != 0) {
            frVar.write(this.A, 2);
        }
        if (this.B != 0) {
            frVar.write(this.B, 3);
        }
    }
}
