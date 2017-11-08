package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class bn extends fs {
    static f dR = new f();
    static ArrayList<bm> dS = new ArrayList();
    public int dG = 0;
    public int dH = 0;
    public int dO = 1;
    public f dP = null;
    public ArrayList<bm> dQ = null;

    static {
        dS.add(new bm());
    }

    public fs newInit() {
        return new bn();
    }

    public void readFrom(fq fqVar) {
        this.dG = fqVar.a(this.dG, 0, false);
        this.dH = fqVar.a(this.dH, 1, false);
        this.dO = fqVar.a(this.dO, 2, false);
        this.dP = (f) fqVar.a(dR, 3, false);
        this.dQ = (ArrayList) fqVar.b(dS, 4, false);
    }

    public void writeTo(fr frVar) {
        if (this.dG != 0) {
            frVar.write(this.dG, 0);
        }
        if (this.dH != 0) {
            frVar.write(this.dH, 1);
        }
        if (this.dO != 1) {
            frVar.write(this.dO, 2);
        }
        if (this.dP != null) {
            frVar.a(this.dP, 3);
        }
        if (this.dQ != null) {
            frVar.a(this.dQ, 4);
        }
    }
}
