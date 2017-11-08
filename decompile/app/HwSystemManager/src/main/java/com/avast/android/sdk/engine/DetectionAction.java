package com.avast.android.sdk.engine;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* compiled from: Unknown */
public enum DetectionAction {
    NOTHING((short) 0),
    IGNORE((short) 1),
    DELETE((short) 2),
    FP_REPORT((short) 3);
    
    private static final Map<Short, DetectionAction> a = null;
    private final short b;

    static {
        a = new HashMap();
        Iterator it = EnumSet.allOf(DetectionAction.class).iterator();
        while (it.hasNext()) {
            DetectionAction detectionAction = (DetectionAction) it.next();
            a.put(Short.valueOf(detectionAction.getId()), detectionAction);
        }
    }

    private DetectionAction(short s) {
        this.b = (short) s;
    }

    public static DetectionAction get(short s) {
        return (DetectionAction) a.get(Short.valueOf(s));
    }

    public final short getId() {
        return this.b;
    }
}
