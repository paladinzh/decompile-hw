package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class au extends fs {
    static ArrayList<bh> bE = new ArrayList();
    static az bJ = new az();
    static bf bK = new bf();
    public int bC = 0;
    public ArrayList<bh> bD = null;
    public az bF = null;
    public int bG = 3;
    public bf bH = null;
    public int bI = 1;
    public String imsi = "";
    public String sms = "";
    public int time = 0;
    public int type = 0;

    static {
        bE.add(new bh());
    }

    public fs newInit() {
        return new au();
    }

    public void readFrom(fq fqVar) {
        this.sms = fqVar.a(0, true);
        this.time = fqVar.a(this.time, 1, true);
        this.bF = (az) fqVar.a(bJ, 2, true);
        this.type = fqVar.a(this.type, 3, true);
        this.bD = (ArrayList) fqVar.b(bE, 4, false);
        this.bG = fqVar.a(this.bG, 5, false);
        this.bH = (bf) fqVar.a(bK, 6, false);
        this.bI = fqVar.a(this.bI, 7, false);
        this.imsi = fqVar.a(8, false);
        this.bC = fqVar.a(this.bC, 9, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.sms, 0);
        frVar.write(this.time, 1);
        frVar.a(this.bF, 2);
        frVar.write(this.type, 3);
        if (this.bD != null) {
            frVar.a(this.bD, 4);
        }
        if (3 != this.bG) {
            frVar.write(this.bG, 5);
        }
        if (this.bH != null) {
            frVar.a(this.bH, 6);
        }
        if (1 != this.bI) {
            frVar.write(this.bI, 7);
        }
        if (this.imsi != null) {
            frVar.a(this.imsi, 8);
        }
        if (this.bC != 0) {
            frVar.write(this.bC, 9);
        }
    }
}
