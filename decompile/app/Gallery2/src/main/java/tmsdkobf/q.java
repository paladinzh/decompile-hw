package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class q extends fs {
    static ArrayList<String> ab = new ArrayList();
    public ArrayList<String> aa = null;

    static {
        ab.add("");
    }

    public fs newInit() {
        return new q();
    }

    public void readFrom(fq fqVar) {
        this.aa = (ArrayList) fqVar.b(ab, 0, false);
    }

    public void writeTo(fr frVar) {
        if (this.aa != null) {
            frVar.a(this.aa, 0);
        }
    }
}
