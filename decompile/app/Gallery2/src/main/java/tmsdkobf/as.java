package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class as extends fs {
    static ArrayList<bh> bE = new ArrayList();
    public int bC = 0;
    public ArrayList<bh> bD = null;
    public String imsi = "";

    static {
        bE.add(new bh());
    }

    public fs newInit() {
        return new as();
    }

    public void readFrom(fq fqVar) {
        this.bD = (ArrayList) fqVar.b(bE, 0, true);
        this.imsi = fqVar.a(1, false);
        this.bC = fqVar.a(this.bC, 2, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.bD, 0);
        if (this.imsi != null) {
            frVar.a(this.imsi, 1);
        }
        if (this.bC != 0) {
            frVar.write(this.bC, 2);
        }
    }
}
