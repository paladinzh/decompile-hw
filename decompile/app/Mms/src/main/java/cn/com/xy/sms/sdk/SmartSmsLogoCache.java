package cn.com.xy.sms.sdk;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SmartSmsLogoCache {
    private final Map<String, Drawable> cache = Collections.synchronizedMap(new LinkedHashMap(10, 1.5f, true));
    private long limit = 1000000;
    private long size = 0;

    public SmartSmsLogoCache() {
        setLimit(Runtime.getRuntime().maxMemory() / 10);
    }

    public void setLimit(long new_limit) {
        this.limit = new_limit;
    }

    public Drawable get(String id) {
        return (Drawable) this.cache.get(id);
    }

    public void put(String id, Drawable bitmap) {
        try {
            if (this.cache.containsKey(id)) {
                this.size -= getSizeInBytes((Drawable) this.cache.get(id));
            }
            this.cache.put(id, bitmap);
            this.size += getSizeInBytes(bitmap);
            checkSize();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void checkSize() {
        if (this.size > this.limit) {
            Iterator<Entry<String, Drawable>> iter = this.cache.entrySet().iterator();
            while (iter.hasNext()) {
                this.size -= getSizeInBytes((Drawable) ((Entry) iter.next()).getValue());
                iter.remove();
                if (this.size <= this.limit) {
                    return;
                }
            }
        }
    }

    public void clear() {
        this.cache.clear();
    }

    long getSizeInBytes(Drawable drawable) {
        if (drawable == null) {
            return 0;
        }
        BitmapDrawable bitmap = (BitmapDrawable) drawable;
        return ((long) bitmap.getBitmap().getRowBytes()) * ((long) bitmap.getBitmap().getHeight());
    }
}
