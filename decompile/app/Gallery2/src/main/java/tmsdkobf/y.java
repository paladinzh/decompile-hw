package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class y extends fs {
    static ArrayList<String> al = new ArrayList();
    public ArrayList<String> ak = null;

    static {
        al.add("");
    }

    public fs newInit() {
        return new y();
    }

    public void readFrom(fq fqVar) {
        this.ak = (ArrayList) fqVar.b(al, 0, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.ak, 0);
    }
}
