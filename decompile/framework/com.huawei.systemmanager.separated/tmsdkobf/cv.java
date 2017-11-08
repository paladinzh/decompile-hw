package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class cv extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    static cq gk;
    static en gl;
    static eo gm;
    static ArrayList<cu> gn;
    public cq gg = null;
    public en gh = null;
    public eo gi = null;
    public ArrayList<cu> gj = null;

    static {
        boolean z = false;
        if (!cv.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public cv() {
        a(this.gg);
        a(this.gh);
        a(this.gi);
        b(this.gj);
    }

    public void a(cq cqVar) {
        this.gg = cqVar;
    }

    public void a(en enVar) {
        this.gh = enVar;
    }

    public void a(eo eoVar) {
        this.gi = eoVar;
    }

    public void b(ArrayList<cu> arrayList) {
        this.gj = arrayList;
    }

    public Object clone() {
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException e) {
            if (!fJ) {
                throw new AssertionError();
            }
        }
        return obj;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        cv cvVar = (cv) obj;
        if (ft.equals(this.gg, cvVar.gg) && ft.equals(this.gh, cvVar.gh) && ft.equals(this.gi, cvVar.gi) && ft.equals(this.gj, cvVar.gj)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void readFrom(fq fqVar) {
        if (gk == null) {
            gk = new cq();
        }
        a((cq) fqVar.a(gk, 0, true));
        if (gl == null) {
            gl = new en();
        }
        a((en) fqVar.a(gl, 1, true));
        if (gm == null) {
            gm = new eo();
        }
        a((eo) fqVar.a(gm, 2, false));
        if (gn == null) {
            gn = new ArrayList();
            gn.add(new cu());
        }
        b((ArrayList) fqVar.b(gn, 3, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.gg, 0);
        frVar.a(this.gh, 1);
        if (this.gi != null) {
            frVar.a(this.gi, 2);
        }
        if (this.gj != null) {
            frVar.a(this.gj, 3);
        }
    }
}
