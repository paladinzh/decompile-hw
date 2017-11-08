package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ae extends fs {
    static g aJ = new g();
    static h aK = new h();
    static h aL = new h();
    static ArrayList<af> aM = new ArrayList();
    public String aE = "";
    public g aF = null;
    public h aG = null;
    public h aH = null;
    public ArrayList<af> aI = null;

    static {
        aM.add(new af());
    }

    public fs newInit() {
        return new ae();
    }

    public void readFrom(fq fqVar) {
        this.aE = fqVar.a(0, true);
        this.aF = (g) fqVar.a(aJ, 1, false);
        this.aG = (h) fqVar.a(aK, 2, false);
        this.aH = (h) fqVar.a(aL, 3, false);
        this.aI = (ArrayList) fqVar.b(aM, 4, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.aE, 0);
        if (this.aF != null) {
            frVar.a(this.aF, 1);
        }
        if (this.aG != null) {
            frVar.a(this.aG, 2);
        }
        if (this.aH != null) {
            frVar.a(this.aH, 3);
        }
        if (this.aI != null) {
            frVar.a(this.aI, 4);
        }
    }
}
