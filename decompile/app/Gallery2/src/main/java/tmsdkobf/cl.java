package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class cl extends fs {
    static cn fp = new cn();
    static ArrayList<String> fq = new ArrayList();
    public cn fn = null;
    public ArrayList<String> fo = null;

    static {
        fq.add("");
    }

    public void readFrom(fq fqVar) {
        this.fn = (cn) fqVar.a(fp, 0, false);
        this.fo = (ArrayList) fqVar.b(fq, 1, false);
    }

    public void writeTo(fr frVar) {
        if (this.fn != null) {
            frVar.a(this.fn, 0);
        }
        if (this.fo != null) {
            frVar.a(this.fo, 1);
        }
    }
}
