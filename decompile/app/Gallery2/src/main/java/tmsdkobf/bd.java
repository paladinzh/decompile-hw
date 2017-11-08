package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class bd extends fs {
    static bc cf = new bc();
    static ArrayList<bc> cg = new ArrayList();
    public bc cd = null;
    public ArrayList<bc> ce = null;

    static {
        cg.add(new bc());
    }

    public fs newInit() {
        return new bd();
    }

    public void readFrom(fq fqVar) {
        this.cd = (bc) fqVar.a(cf, 0, true);
        this.ce = (ArrayList) fqVar.b(cg, 1, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.cd, 0);
        if (this.ce != null) {
            frVar.a(this.ce, 1);
        }
    }
}
