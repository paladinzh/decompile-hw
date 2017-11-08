package com.amap.api.services.core;

import android.content.Context;

/* compiled from: NearbyDeleteHandler */
public class u extends b<String, Integer> {
    private Context h;
    private String i;

    protected /* synthetic */ Object a(String str) throws AMapException {
        return d(str);
    }

    public u(Context context, String str) {
        super(context, str);
        this.h = context;
        this.i = str;
    }

    protected String e() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("key=").append(aj.f(this.h));
        stringBuffer.append("&userid=").append(this.i);
        return stringBuffer.toString();
    }

    protected Integer d(String str) throws AMapException {
        return Integer.valueOf(0);
    }

    public String g() {
        return h.b() + "/nearby/data/delete";
    }
}
