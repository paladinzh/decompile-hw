package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class j extends fs {
    static ArrayList<o> F = new ArrayList();
    public ArrayList<o> E = null;

    static {
        F.add(new o());
    }

    public fs newInit() {
        return new j();
    }

    public void readFrom(fq fqVar) {
        this.E = (ArrayList) fqVar.b(F, 0, false);
    }

    public void writeTo(fr frVar) {
        if (this.E != null) {
            frVar.a(this.E, 0);
        }
    }
}
