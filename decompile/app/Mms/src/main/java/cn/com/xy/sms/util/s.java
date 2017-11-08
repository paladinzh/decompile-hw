package cn.com.xy.sms.util;

import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import java.util.Map;

/* compiled from: Unknown */
final class s extends Thread {
    private final /* synthetic */ String a;
    private final /* synthetic */ Map b;

    s(String str, Map map) {
        this.a = str;
        this.b = map;
    }

    public final void run() {
        try {
            setName("xiaoyuan-parseSmsToBubble1");
            try {
                int intValue = Integer.valueOf(this.a).intValue();
                Map handerValueMapByType = DexUtil.handerValueMapByType(intValue, this.b);
                if (handerValueMapByType != null) {
                    DuoquUtils.getSdkDoAction().parseMsgCallBack(intValue, handerValueMapByType);
                }
            } catch (Throwable th) {
            }
        } catch (Throwable th2) {
            th2.getMessage();
        }
    }
}
