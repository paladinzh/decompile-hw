package com.avast.android.sdk.engine;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* compiled from: Unknown */
public enum UrlSource {
    MESSAGE((short) 0),
    STOCK((short) 1),
    STOCK_JB((short) 2),
    CHROME((short) 3),
    DOLPHIN_MINI((short) 4),
    DOLPHIN((short) 5),
    SILK((short) 6),
    BOAT_MINI((short) 7),
    BOAT((short) 8),
    SBROWSER((short) 9),
    AVAST_DOWNLOAD_MANAGER((short) 10),
    CHROME_M((short) 10);
    
    private static final Map<Short, UrlSource> a = null;
    private final short b;

    static {
        a = new HashMap();
        Iterator it = EnumSet.allOf(UrlSource.class).iterator();
        while (it.hasNext()) {
            UrlSource urlSource = (UrlSource) it.next();
            a.put(Short.valueOf(urlSource.getId()), urlSource);
        }
    }

    private UrlSource(short s) {
        this.b = (short) s;
    }

    public static UrlSource get(short s) {
        return (UrlSource) a.get(Short.valueOf(s));
    }

    public final short getId() {
        return this.b;
    }
}
