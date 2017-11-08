package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class d extends fs {
    static ArrayList<String> g = new ArrayList();
    static ArrayList<String> j = new ArrayList();
    static ArrayList<String> k = new ArrayList();
    public int c = 0;
    public ArrayList<String> d = null;
    public ArrayList<String> e = null;
    public ArrayList<String> f = null;
    public int hash = 0;

    static {
        g.add("");
        j.add("");
        k.add("");
    }

    public fs newInit() {
        return new d();
    }

    public void readFrom(fq fqVar) {
        this.hash = fqVar.a(this.hash, 0, true);
        this.c = fqVar.a(this.c, 1, true);
        this.d = (ArrayList) fqVar.b(g, 2, true);
        this.e = (ArrayList) fqVar.b(j, 3, true);
        this.f = (ArrayList) fqVar.b(k, 4, true);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.hash, 0);
        frVar.write(this.c, 1);
        frVar.a(this.d, 2);
        frVar.a(this.e, 3);
        frVar.a(this.f, 4);
    }
}
