package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class br extends fs {
    static ArrayList<bq> dW = new ArrayList();
    public int dG = 0;
    public int dH = 0;
    public ArrayList<bq> dV = null;

    static {
        dW.add(new bq());
    }

    public fs newInit() {
        return new br();
    }

    public void readFrom(fq fqVar) {
        this.dG = fqVar.a(this.dG, 0, false);
        this.dH = fqVar.a(this.dH, 1, false);
        this.dV = (ArrayList) fqVar.b(dW, 2, false);
    }

    public void writeTo(fr frVar) {
        if (this.dG != 0) {
            frVar.write(this.dG, 0);
        }
        if (this.dH != 0) {
            frVar.write(this.dH, 1);
        }
        if (this.dV != null) {
            frVar.a(this.dV, 2);
        }
    }
}
