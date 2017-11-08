package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class cb extends fs {
    static ArrayList<bz> eA = new ArrayList();
    static ArrayList<ce> eB = new ArrayList();
    public int ep = 0;
    public int eq = 0;
    public int er = 0;
    public int es = 0;
    public ArrayList<bz> et = null;
    public int eu = 0;
    public ArrayList<ce> ev = null;
    public String ew = "";
    public int ex = 0;
    public int ey = 0;
    public String ez = "ETS_NONE";
    public String sender = "";
    public String sms = "";

    static {
        eA.add(new bz());
        eB.add(new ce());
    }

    public fs newInit() {
        return new cb();
    }

    public void readFrom(fq fqVar) {
        this.sender = fqVar.a(0, true);
        this.sms = fqVar.a(1, true);
        this.ep = fqVar.a(this.ep, 2, true);
        this.eq = fqVar.a(this.eq, 3, true);
        this.er = fqVar.a(this.er, 4, true);
        this.es = fqVar.a(this.es, 5, false);
        this.et = (ArrayList) fqVar.b(eA, 6, false);
        this.eu = fqVar.a(this.eu, 7, false);
        this.ev = (ArrayList) fqVar.b(eB, 8, false);
        this.ew = fqVar.a(9, false);
        this.ex = fqVar.a(this.ex, 10, false);
        this.ey = fqVar.a(this.ey, 11, false);
        this.ez = fqVar.a(12, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.sender, 0);
        frVar.a(this.sms, 1);
        frVar.write(this.ep, 2);
        frVar.write(this.eq, 3);
        frVar.write(this.er, 4);
        if (this.es != 0) {
            frVar.write(this.es, 5);
        }
        if (this.et != null) {
            frVar.a(this.et, 6);
        }
        if (this.eu != 0) {
            frVar.write(this.eu, 7);
        }
        if (this.ev != null) {
            frVar.a(this.ev, 8);
        }
        if (this.ew != null) {
            frVar.a(this.ew, 9);
        }
        if (this.ex != 0) {
            frVar.write(this.ex, 10);
        }
        if (this.ey != 0) {
            frVar.write(this.ey, 11);
        }
        if (this.ez != null) {
            frVar.a(this.ez, 12);
        }
    }
}
