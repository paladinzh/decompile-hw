package com.amap.api.services.poisearch;

import android.content.Context;
import com.amap.api.services.core.r;

/* compiled from: PoiHandler */
abstract class g<T, V> extends r<T, V> {
    public g(Context context, T t) {
        super(context, t);
    }

    protected boolean a(String str) {
        if (str == null || str.equals("") || str.equals("[]")) {
            return true;
        }
        return false;
    }
}
