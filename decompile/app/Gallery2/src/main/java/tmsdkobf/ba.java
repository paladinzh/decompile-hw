package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ba extends fs {
    static ArrayList<ax> bX = new ArrayList();
    public int bC = 0;
    public ArrayList<ax> bW = null;
    public String imsi = "";

    static {
        bX.add(new ax());
    }

    public fs newInit() {
        return new ba();
    }

    public void readFrom(fq fqVar) {
        this.bW = (ArrayList) fqVar.b(bX, 0, true);
        this.imsi = fqVar.a(1, false);
        this.bC = fqVar.a(this.bC, 2, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.bW, 0);
        if (this.imsi != null) {
            frVar.a(this.imsi, 1);
        }
        if (this.bC != 0) {
            frVar.write(this.bC, 2);
        }
    }
}
