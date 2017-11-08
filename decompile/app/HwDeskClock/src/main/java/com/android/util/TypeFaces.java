package com.android.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import java.io.File;
import java.util.Hashtable;

public class TypeFaces {
    private static final Hashtable<String, Typeface> cache = new Hashtable();

    public static synchronized Typeface get(Context context, String fontPath) {
        Typeface typeface;
        synchronized (TypeFaces.class) {
            if (!cache.containsKey(fontPath)) {
                Typeface typeface2;
                if (new File(fontPath).exists()) {
                    typeface2 = Typeface.createFromFile(fontPath);
                    Log.e("ty", fontPath);
                } else {
                    typeface2 = Typeface.create("HwChinese-light", 0);
                    fontPath = "HwChinese-light";
                    Log.e("ty", fontPath);
                }
                cache.put(fontPath, typeface2);
            }
            typeface = (Typeface) cache.get(fontPath);
        }
        return typeface;
    }
}
