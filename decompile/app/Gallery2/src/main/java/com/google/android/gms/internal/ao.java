package com.google.android.gms.internal;

import java.util.Map;

/* compiled from: Unknown */
public final class ao implements ar {
    private final ap lV;

    public ao(ap apVar) {
        this.lV = apVar;
    }

    public void a(dd ddVar, Map<String, String> map) {
        String str = (String) map.get("name");
        if (str != null) {
            this.lV.onAppEvent(str, (String) map.get("info"));
        } else {
            da.w("App event with no name parameter.");
        }
    }
}
