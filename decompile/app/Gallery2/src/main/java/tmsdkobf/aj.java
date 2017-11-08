package tmsdkobf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public final class aj extends fs {
    static Map<Integer, ArrayList<String>> be = new HashMap();
    public int bc = 0;
    public Map<Integer, ArrayList<String>> bd = null;

    static {
        Integer valueOf = Integer.valueOf(0);
        ArrayList arrayList = new ArrayList();
        arrayList.add("");
        be.put(valueOf, arrayList);
    }

    public void readFrom(fq fqVar) {
        this.bc = fqVar.a(this.bc, 0, true);
        this.bd = (Map) fqVar.b(be, 1, true);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.bc, 0);
        frVar.a(this.bd, 1);
    }
}
