package com.google.android.gms.tagmanager;

import android.content.Context;
import android.net.Uri;
import java.util.Map;

/* compiled from: Unknown */
class d implements b {
    private final Context kL;

    public d(Context context) {
        this.kL = context;
    }

    public void v(Map<String, Object> map) {
        Object obj;
        String queryParameter;
        Object obj2 = map.get("gtm.url");
        if (obj2 == null) {
            obj = map.get("gtm");
            if (obj != null && (obj instanceof Map)) {
                obj = ((Map) obj).get("url");
                if (obj != null && (obj instanceof String)) {
                    queryParameter = Uri.parse((String) obj).getQueryParameter("referrer");
                    if (queryParameter != null) {
                        ay.e(this.kL, queryParameter);
                    }
                }
            }
        }
        obj = obj2;
        if (obj != null) {
            queryParameter = Uri.parse((String) obj).getQueryParameter("referrer");
            if (queryParameter != null) {
                ay.e(this.kL, queryParameter);
            }
        }
    }
}
