package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class bg extends fs {
    static ArrayList<bd> cr = new ArrayList();
    public long cp = 0;
    public ArrayList<bd> cq = null;

    static {
        cr.add(new bd());
    }

    public fs newInit() {
        return new bg();
    }

    public void readFrom(fq fqVar) {
        this.cp = fqVar.a(this.cp, 0, true);
        this.cq = (ArrayList) fqVar.b(cr, 1, false);
    }

    public void writeTo(fr frVar) {
        frVar.b(this.cp, 0);
        if (this.cq != null) {
            frVar.a(this.cq, 1);
        }
    }
}
