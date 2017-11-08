package cn.com.xy.sms.sdk.ui.popu.util;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import cn.com.xy.sms.sdk.util.StringUtils;

/* compiled from: Unknown */
public class ResourceCacheUtil {
    private static LruCache<String, Integer> a = new LruCache(40);
    private static LruCache<String, BitmapDrawable> b = new LruCache(30);
    private static LruCache<String, Drawable> c = new LruCache(60);

    public static void clearCache() {
        a.evictAll();
        c.evictAll();
        b.evictAll();
    }

    public static Drawable getColorDrawable(String str) {
        return str != null ? (Drawable) c.get(str) : null;
    }

    public static BitmapDrawable getImgDrawable(String str) {
        return str != null ? (BitmapDrawable) b.get(str) : null;
    }

    public static int parseColor(String str) {
        int parseColor;
        try {
            if (StringUtils.isNull(str) || str.indexOf(".") != -1) {
                return -1;
            }
            Integer num = (Integer) a.get(str);
            if (num == null) {
                parseColor = Color.parseColor(str);
                try {
                    a.put(str, Integer.valueOf(parseColor));
                } catch (Throwable th) {
                }
            } else {
                parseColor = num.intValue();
            }
            return parseColor;
        } catch (Throwable th2) {
            parseColor = -1;
        }
    }

    public static void putColorDrawable(String str, Drawable drawable) {
        if (str != null && drawable != null) {
            synchronized (c) {
                c.put(str, drawable);
            }
        }
    }

    public static void putImgDrawable(String str, BitmapDrawable bitmapDrawable) {
        if (str != null && bitmapDrawable != null) {
            synchronized (b) {
                b.put(str, bitmapDrawable);
            }
        }
    }
}
