package tmsdkobf;

import java.util.HashMap;
import tmsdk.common.TMSDKContext;

/* compiled from: Unknown */
public final class ib {
    private static final HashMap<String, ic> rB = new HashMap(5);

    static {
        rB.put("ConfigProvider", new ic(0, new ih()));
        rB.put("MeriExtProvider", new ic(0, new ii()));
        rB.put("QQSecureProvider", new ic(1, new id()));
        rB.put("SpProvider", new ic(0, new km(TMSDKContext.getApplicaionContext())));
    }

    public static ic bk(String str) {
        return str == null ? null : (ic) rB.get(str);
    }
}
