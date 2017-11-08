package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class p extends fs {
    static ArrayList<m> Z = new ArrayList();
    public long U = 0;
    public long V = 0;
    public ArrayList<m> Y = null;

    static {
        Z.add(new m());
    }

    public fs newInit() {
        return new p();
    }

    public void readFrom(fq fqVar) {
        this.U = fqVar.a(this.U, 0, false);
        this.V = fqVar.a(this.V, 1, false);
        this.Y = (ArrayList) fqVar.b(Z, 2, false);
    }

    public void writeTo(fr frVar) {
        if (this.U != 0) {
            frVar.b(this.U, 0);
        }
        if (this.V != 0) {
            frVar.b(this.V, 1);
        }
        if (this.Y != null) {
            frVar.a(this.Y, 2);
        }
    }
}
