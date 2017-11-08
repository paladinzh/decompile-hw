package com.avast.android.sdk.engine.internal;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* compiled from: Unknown */
public enum i {
    FALSE_POSITIVE_SERVER_ID((short) 0),
    WEBSHIELD_SERVER_ID((short) 1),
    UPDATE_SERVER_ID((short) 2),
    COMMUNITY_IQ_SERVER_ID((short) 3),
    AI_REPORTING_SERVER_ID((short) 4),
    ACCOUNT_PAIR_SERVER_ID((short) 5),
    TYPOSQUATTING_CONFIRMATION_SERVER_ID((short) 6),
    ACCOUNT_UNPAIR_SERVER_ID((short) 7);
    
    private static final Map<Short, i> i = null;
    private final short j;

    static {
        i = new HashMap();
        Iterator it = EnumSet.allOf(i.class).iterator();
        while (it.hasNext()) {
            i iVar = (i) it.next();
            i.put(Short.valueOf(iVar.a()), iVar);
        }
    }

    private i(short s) {
        this.j = (short) s;
    }

    public final short a() {
        return this.j;
    }
}
