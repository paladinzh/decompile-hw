package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class bw extends fs {
    static ArrayList<cb> eg = new ArrayList();
    public ArrayList<cb> ee = null;
    public int ef = 0;

    static {
        eg.add(new cb());
    }

    public fs newInit() {
        return new bw();
    }

    public void readFrom(fq fqVar) {
        this.ee = (ArrayList) fqVar.b(eg, 0, true);
        this.ef = fqVar.a(this.ef, 1, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.ee, 0);
        if (this.ef != 0) {
            frVar.write(this.ef, 1);
        }
    }
}
