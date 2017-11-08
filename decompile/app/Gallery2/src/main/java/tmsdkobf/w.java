package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class w extends fs {
    static ArrayList<z> ah = new ArrayList();
    public ArrayList<z> ag = null;

    static {
        ah.add(new z());
    }

    public fs newInit() {
        return new w();
    }

    public void readFrom(fq fqVar) {
        this.ag = (ArrayList) fqVar.b(ah, 0, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.ag, 0);
    }
}
