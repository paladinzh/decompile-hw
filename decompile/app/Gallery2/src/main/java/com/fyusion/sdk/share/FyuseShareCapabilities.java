package com.fyusion.sdk.share;

import com.fyusion.sdk.common.ext.Key;
import com.fyusion.sdk.common.ext.internal.Settings;
import com.fyusion.sdk.common.ext.internal.a;
import java.util.List;

/* compiled from: Unknown */
public class FyuseShareCapabilities {
    private static FyuseShareCapabilities a;
    private Settings b = new Settings();

    private FyuseShareCapabilities() {
        a.a(new String[]{"com.fyusion.sdk.ext.highres.UploadCapabilities"}, this.b);
    }

    public static synchronized FyuseShareCapabilities getInstance() {
        FyuseShareCapabilities fyuseShareCapabilities;
        synchronized (FyuseShareCapabilities.class) {
            if (a == null) {
                a = new FyuseShareCapabilities();
            }
            fyuseShareCapabilities = a;
        }
        return fyuseShareCapabilities;
    }

    public <T> T get(Key<T> key) {
        return this.b.get(key) != null ? this.b.get(key) : null;
    }

    public List<Key> getKeys() {
        return this.b.getKeys();
    }
}
