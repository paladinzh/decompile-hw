package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ob extends fs {
    static ArrayList<oc> DV = new ArrayList();
    public ArrayList<oc> DS = null;
    public String DT = "";
    public String DU = "";

    static {
        DV.add(new oc());
    }

    public fs newInit() {
        return new ob();
    }

    public void readFrom(fq fqVar) {
        this.DS = (ArrayList) fqVar.b(DV, 0, true);
        this.DT = fqVar.a(1, true);
        this.DU = fqVar.a(2, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.DS, 0);
        frVar.a(this.DT, 1);
        frVar.a(this.DU, 2);
    }
}
