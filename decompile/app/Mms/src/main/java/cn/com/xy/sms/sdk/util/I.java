package cn.com.xy.sms.sdk.util;

import cn.com.xy.sms.sdk.constant.Constant;
import java.io.File;

/* compiled from: Unknown */
final class i implements Runnable {
    i() {
    }

    public final void run() {
        try {
            String parse_path = Constant.getPARSE_PATH();
            if (!StringUtils.isNull(parse_path)) {
                File[] listFiles = new File(parse_path).listFiles();
                if (listFiles != null && listFiles.length > 0) {
                    for (File absolutePath : listFiles) {
                        XyUtil.chmodSyn("640", absolutePath.getAbsolutePath());
                    }
                }
            }
        } catch (Throwable th) {
        }
    }
}
