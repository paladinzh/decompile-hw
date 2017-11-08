package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class i extends fs {
    static ArrayList<n> F = new ArrayList();
    public ArrayList<n> E = null;

    static {
        F.add(new n());
    }

    public fs newInit() {
        return new i();
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
