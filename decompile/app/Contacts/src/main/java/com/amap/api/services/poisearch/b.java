package com.amap.api.services.poisearch;

import android.content.Context;

/* compiled from: PoiHandler */
abstract class b<T, V> extends com.amap.api.services.core.b<T, V> {
    public b(Context context, T t) {
        super(context, t);
    }

    protected boolean d(String str) {
        if (str == null || str.equals("") || str.equals("[]")) {
            return true;
        }
        return false;
    }
}
