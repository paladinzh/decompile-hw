package tmsdkobf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public final class ah extends fs {
    static ArrayList<Map<Integer, String>> aX = new ArrayList();
    static byte[] aY = new byte[1];
    public ArrayList<Map<Integer, String>> aV = null;
    public byte[] aW = null;

    static {
        Map hashMap = new HashMap();
        hashMap.put(Integer.valueOf(0), "");
        aX.add(hashMap);
        aY[0] = (byte) 0;
    }

    public fs newInit() {
        return new ah();
    }

    public void readFrom(fq fqVar) {
        this.aV = (ArrayList) fqVar.b(aX, 0, true);
        this.aW = fqVar.a(aY, 1, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.aV, 0);
        if (this.aW != null) {
            frVar.a(this.aW, 1);
        }
    }
}
