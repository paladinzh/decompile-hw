package tmsdkobf;

import android.content.Context;
import java.util.HashMap;

/* compiled from: Unknown */
public class kk {
    private static final HashMap<String, lf> vM = new HashMap();

    public static lf a(Context context, String str) {
        lf lfVar;
        synchronized (vM) {
            lfVar = (lf) vM.get(str);
            if (lfVar == null) {
                lfVar = new kj(context, str, false);
                vM.put(str, lfVar);
            }
        }
        return lfVar;
    }
}
