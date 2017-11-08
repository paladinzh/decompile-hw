package cn.com.xy.sms.util;

import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import java.util.Set;

/* compiled from: Unknown */
final class e implements Runnable {
    private final /* synthetic */ Set a;

    e(Set set) {
        this.a = set;
    }

    public final void run() {
        try {
            for (Integer valueOf : this.a) {
                ParseRichBubbleManager.deleteBubbleDataFromCache("", String.valueOf(valueOf));
            }
            MatchCacheManager.deleteDataByMsgIds(this.a);
        } catch (Throwable th) {
        }
    }
}
