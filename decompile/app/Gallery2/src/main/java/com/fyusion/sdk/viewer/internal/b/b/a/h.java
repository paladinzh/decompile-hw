package com.fyusion.sdk.viewer.internal.b.b.a;

import android.content.Context;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.viewer.internal.b.b.a.a.b;
import java.io.File;

/* compiled from: Unknown */
public class h implements b {
    private final a a;

    /* compiled from: Unknown */
    interface a {
        File a();
    }

    public h(Context context) {
        final File cacheDir = context.getCacheDir();
        this.a = new a(this) {
            final /* synthetic */ h b;

            public File a() {
                return cacheDir != null ? new File(cacheDir, "fyuse/frames") : null;
            }
        };
    }

    public a a() {
        File a = this.a.a();
        if (a != null) {
            if (!a.mkdirs()) {
                if (!a.exists() || !a.isDirectory()) {
                    DLog.w("FramesDiskCacheFactory", "Unable to create cache dir " + a);
                    return null;
                }
            }
            File file = new File(a, "trash");
            if (!file.mkdirs()) {
                if (!file.exists() || !file.isDirectory()) {
                    DLog.w("FramesDiskCacheFactory", "Unable to create trash dir " + file);
                    return null;
                }
            }
            return new g(a, file);
        }
        DLog.w("FramesDiskCacheFactory", "Something is not right. Cache dir is null.");
        return null;
    }
}
