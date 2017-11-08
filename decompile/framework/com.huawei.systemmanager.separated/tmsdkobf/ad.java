package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ad extends fs {
    static ArrayList<ah> aD = new ArrayList();
    public ArrayList<ah> aC = null;

    static {
        aD.add(new ah());
    }

    public fs newInit() {
        return new ad();
    }

    public void readFrom(fq fqVar) {
        this.aC = (ArrayList) fqVar.b(aD, 0, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.aC, 0);
    }
}
