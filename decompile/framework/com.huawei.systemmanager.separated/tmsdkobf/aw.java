package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class aw extends fs {
    static ArrayList<bh> bE = new ArrayList();
    static az bJ = new az();
    public int bC = 0;
    public ArrayList<bh> bD = null;
    public az bF = null;
    public int bI = 0;
    public String imsi = "";
    public String sms = "";
    public int time = 0;

    static {
        bE.add(new bh());
    }

    public fs newInit() {
        return new aw();
    }

    public void readFrom(fq fqVar) {
        this.sms = fqVar.a(0, true);
        this.time = fqVar.a(this.time, 1, true);
        this.bF = (az) fqVar.a(bJ, 2, true);
        this.bI = fqVar.a(this.bI, 3, true);
        this.bD = (ArrayList) fqVar.b(bE, 4, false);
        this.imsi = fqVar.a(5, false);
        this.bC = fqVar.a(this.bC, 6, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.sms, 0);
        frVar.write(this.time, 1);
        frVar.a(this.bF, 2);
        frVar.write(this.bI, 3);
        if (this.bD != null) {
            frVar.a(this.bD, 4);
        }
        if (this.imsi != null) {
            frVar.a(this.imsi, 5);
        }
        if (this.bC != 0) {
            frVar.write(this.bC, 6);
        }
    }
}
