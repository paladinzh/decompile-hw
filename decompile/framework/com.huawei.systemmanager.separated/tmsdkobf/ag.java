package tmsdkobf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public final class ag extends fs {
    static ArrayList<Map<Integer, String>> aU = new ArrayList();
    public ArrayList<Map<Integer, String>> aT = null;

    static {
        Map hashMap = new HashMap();
        hashMap.put(Integer.valueOf(0), "");
        aU.add(hashMap);
    }

    public fs newInit() {
        return new ag();
    }

    public void readFrom(fq fqVar) {
        this.aT = (ArrayList) fqVar.b(aU, 0, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.aT, 0);
    }
}
