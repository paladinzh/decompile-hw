package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ca extends fs {
    static ArrayList<cc> eo = new ArrayList();
    public ArrayList<cc> en = null;

    static {
        eo.add(new cc());
    }

    public fs newInit() {
        return new ca();
    }

    public void readFrom(fq fqVar) {
        this.en = (ArrayList) fqVar.b(eo, 0, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.en, 0);
    }
}
