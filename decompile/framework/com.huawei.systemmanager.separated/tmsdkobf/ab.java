package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ab extends fs {
    static ArrayList<ac> az = new ArrayList();
    public ArrayList<ac> ay = null;

    static {
        az.add(new ac());
    }

    public fs newInit() {
        return new ab();
    }

    public void readFrom(fq fqVar) {
        this.ay = (ArrayList) fqVar.b(az, 0, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.ay, 0);
    }
}
