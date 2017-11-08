package tmsdkobf;

import java.util.LinkedHashMap;
import java.util.Set;

/* compiled from: Unknown */
public class qh<K, V> {
    private int Dw = -1;
    private LinkedHashMap<K, V> Jr = new LinkedHashMap();

    public qh(int i) {
        this.Dw = i;
    }

    public V put(K k, V v) {
        if (this.Jr.size() >= this.Dw) {
            Set keySet = this.Jr.keySet();
            if (keySet != null) {
                this.Jr.remove(keySet.iterator().next());
            }
        }
        return this.Jr.put(k, v);
    }
}
