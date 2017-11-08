package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class bv extends fs {
    static ArrayList<by> ed = new ArrayList();
    public ArrayList<by> ea = null;
    public int eb = 0;
    public int ec = 0;

    static {
        ed.add(new by());
    }

    public fs newInit() {
        return new bv();
    }

    public void readFrom(fq fqVar) {
        this.ea = (ArrayList) fqVar.b(ed, 0, true);
        this.eb = fqVar.a(this.eb, 1, false);
        this.ec = fqVar.a(this.ec, 2, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.ea, 0);
        if (this.eb != 0) {
            frVar.write(this.eb, 1);
        }
        if (this.ec != 0) {
            frVar.write(this.ec, 2);
        }
    }
}
