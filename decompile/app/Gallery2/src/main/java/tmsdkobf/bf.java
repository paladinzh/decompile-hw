package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class bf extends fs {
    static ArrayList<bb> co = new ArrayList();
    public ArrayList<bb> cn = null;

    static {
        co.add(new bb());
    }

    public fs newInit() {
        return new bf();
    }

    public void readFrom(fq fqVar) {
        this.cn = (ArrayList) fqVar.b(co, 0, false);
    }

    public void writeTo(fr frVar) {
        if (this.cn != null) {
            frVar.a(this.cn, 0);
        }
    }
}
