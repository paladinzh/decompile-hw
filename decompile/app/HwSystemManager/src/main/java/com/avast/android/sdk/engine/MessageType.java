package com.avast.android.sdk.engine;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* compiled from: Unknown */
public enum MessageType {
    UNKNOWN((short) 0),
    SMS((short) 1),
    MMS((short) 2),
    CELL_BROADCAST((short) 3),
    EMAIL((short) 4);
    
    private static final Map<Short, MessageType> a = null;
    private final short b;

    static {
        a = new HashMap();
        Iterator it = EnumSet.allOf(MessageType.class).iterator();
        while (it.hasNext()) {
            MessageType messageType = (MessageType) it.next();
            a.put(Short.valueOf(messageType.getId()), messageType);
        }
    }

    private MessageType(short s) {
        this.b = (short) s;
    }

    public static MessageType get(short s) {
        return (MessageType) a.get(Short.valueOf(s));
    }

    public final short getId() {
        return this.b;
    }
}
