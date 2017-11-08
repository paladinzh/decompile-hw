package com.huawei.keyguard.util;

import android.content.Context;
import android.graphics.Typeface;
import java.util.Hashtable;

public class Typefaces {
    private static final Hashtable<String, Typeface> cache = new Hashtable();

    public static Typeface get(Context c, String filePath) {
        Typeface typeface;
        synchronized (cache) {
            if (!cache.containsKey(filePath)) {
                try {
                    cache.put(filePath, Typeface.createFromFile(filePath));
                } catch (Exception e) {
                    HwLog.e("Typefaces", "Could not get typeface '" + filePath + "' because " + e.toString());
                    return null;
                }
            }
            typeface = (Typeface) cache.get(filePath);
        }
        return typeface;
    }
}
