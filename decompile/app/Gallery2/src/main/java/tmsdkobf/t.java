package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class t extends fs {
    static ArrayList<p> ad = new ArrayList();
    public ArrayList<p> ac = null;
    public int result = 0;

    static {
        ad.add(new p());
    }

    public fs newInit() {
        return new t();
    }

    public void readFrom(fq fqVar) {
        this.result = fqVar.a(this.result, 0, false);
        this.ac = (ArrayList) fqVar.b(ad, 1, false);
    }

    public void writeTo(fr frVar) {
        if (this.result != 0) {
            frVar.write(this.result, 0);
        }
        if (this.ac != null) {
            frVar.a(this.ac, 1);
        }
    }
}
