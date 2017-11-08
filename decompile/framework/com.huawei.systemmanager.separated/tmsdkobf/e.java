package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class e extends fs {
    static ArrayList<Integer> o = new ArrayList();
    static ArrayList<c> p = new ArrayList();
    public int hash = 0;
    public int interval = 0;
    public ArrayList<Integer> l = null;
    public ArrayList<c> m = null;
    public int n = 0;

    static {
        o.add(Integer.valueOf(0));
        p.add(new c());
    }

    public fs newInit() {
        return new e();
    }

    public void readFrom(fq fqVar) {
        this.hash = fqVar.a(this.hash, 0, true);
        this.interval = fqVar.a(this.interval, 1, false);
        this.l = (ArrayList) fqVar.b(o, 2, false);
        this.m = (ArrayList) fqVar.b(p, 3, false);
        this.n = fqVar.a(this.n, 4, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.hash, 0);
        if (this.interval != 0) {
            frVar.write(this.interval, 1);
        }
        if (this.l != null) {
            frVar.a(this.l, 2);
        }
        if (this.m != null) {
            frVar.a(this.m, 3);
        }
        if (this.n != 0) {
            frVar.write(this.n, 4);
        }
    }
}
