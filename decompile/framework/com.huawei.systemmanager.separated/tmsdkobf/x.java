package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class x extends fs {
    static ArrayList<aa> aj = new ArrayList();
    public ArrayList<aa> ai = null;

    static {
        aj.add(new aa());
    }

    public fs newInit() {
        return new x();
    }

    public void readFrom(fq fqVar) {
        this.ai = (ArrayList) fqVar.b(aj, 0, false);
    }

    public void writeTo(fr frVar) {
        if (this.ai != null) {
            frVar.a(this.ai, 0);
        }
    }
}
