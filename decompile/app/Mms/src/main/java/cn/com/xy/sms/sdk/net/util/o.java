package cn.com.xy.sms.sdk.net.util;

import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.f;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.io.File;

/* compiled from: Unknown */
final class o implements Runnable {
    private final /* synthetic */ String a;

    o(String str) {
        this.a = str;
    }

    public final void run() {
        try {
            int lastIndexOf = this.a.lastIndexOf("_");
            String str = this.a;
            if (lastIndexOf != -1) {
                f.a(this.a.substring(0, lastIndexOf + 1), this.a.substring(lastIndexOf + 1));
            }
            File dir = Constant.getContext().getDir("outdex", 0);
            XyUtil.chmod("640", dir.getCanonicalPath() + File.separator + new StringBuilder(String.valueOf(this.a.substring(0, this.a.length() - 4))).append(".dex").toString());
        } catch (Throwable th) {
        }
    }
}
