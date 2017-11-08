package cn.com.xy.sms.sdk.number;

import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.util.b;

/* compiled from: Unknown */
public class v extends b {
    private static b b = null;

    private v() {
        this.a = DexUtil.getUpdateCycleByType(42, 604800000);
    }

    public static b a() {
        synchronized (v.class) {
            if (b == null) {
                b = new v();
            }
        }
        return b;
    }

    public final void b() {
        ParseNumberManager.upgradeEmbedNumber(new w(this));
    }
}
