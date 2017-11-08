package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class og extends fs {
    static oh Eg = new oh();
    static ArrayList<of> Eh = new ArrayList();
    static ArrayList<of> Ei = new ArrayList();
    public oh Ed = null;
    public ArrayList<of> Ee = null;
    public ArrayList<of> Ef = null;

    static {
        Eh.add(new of());
        Ei.add(new of());
    }

    public fs newInit() {
        return new og();
    }

    public void readFrom(fq fqVar) {
        this.Ed = (oh) fqVar.a(Eg, 0, true);
        this.Ee = (ArrayList) fqVar.b(Eh, 1, true);
        this.Ef = (ArrayList) fqVar.b(Ei, 2, true);
    }

    public String toString() {
        return "SCCloudResp [scResult=" + this.Ed + ", vecBlacks=" + this.Ee + ", vecWhites=" + this.Ef + "]";
    }

    public void writeTo(fr frVar) {
        frVar.a(this.Ed, 0);
        frVar.a(this.Ee, 1);
        frVar.a(this.Ef, 2);
    }
}
