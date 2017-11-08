package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class av extends fs {
    static ArrayList<ay> bM = new ArrayList();
    public int bC = 0;
    public ArrayList<ay> bL = null;
    public String imsi = "";

    static {
        bM.add(new ay());
    }

    public fs newInit() {
        return new av();
    }

    public void readFrom(fq fqVar) {
        this.bL = (ArrayList) fqVar.b(bM, 0, true);
        this.imsi = fqVar.a(1, false);
        this.bC = fqVar.a(this.bC, 2, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.bL, 0);
        if (this.imsi != null) {
            frVar.a(this.imsi, 1);
        }
        if (this.bC != 0) {
            frVar.write(this.bC, 2);
        }
    }
}
