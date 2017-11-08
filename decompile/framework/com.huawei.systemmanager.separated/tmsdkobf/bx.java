package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class bx extends fs {
    static ArrayList<cd> ei = new ArrayList();
    public ArrayList<cd> eh = null;

    static {
        ei.add(new cd());
    }

    public fs newInit() {
        return new bx();
    }

    public void readFrom(fq fqVar) {
        this.eh = (ArrayList) fqVar.b(ei, 0, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.eh, 0);
    }
}
