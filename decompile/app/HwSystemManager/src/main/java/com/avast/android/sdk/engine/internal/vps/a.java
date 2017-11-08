package com.avast.android.sdk.engine.internal.vps;

import com.huawei.systemmanager.securitythreats.comm.SecurityThreatsConst;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* compiled from: Unknown */
public class a {

    /* compiled from: Unknown */
    public enum a {
        FILES_TO_SCAN_LIST_OF_STRINGS_ID((short) 0);
        
        private static final Map<Short, a> b = null;
        private final short c;

        static {
            b = new HashMap();
            Iterator it = EnumSet.allOf(a.class).iterator();
            while (it.hasNext()) {
                a aVar = (a) it.next();
                b.put(Short.valueOf(aVar.a()), aVar);
            }
        }

        private a(short s) {
            this.c = (short) s;
        }

        public final short a() {
            return this.c;
        }
    }

    /* compiled from: Unknown */
    public enum b {
        GUID_STRING_ID((short) 32763),
        OPTION_OVERRIDE_SHORT_ID((short) 32764),
        STRUCTURE_VERSION_INT_ID((short) 32765),
        CONTEXT_CONTEXT_ID((short) 32766),
        CONTEXT_ID_INTEGER_ID(Short.MAX_VALUE);
        
        private static final Map<Short, b> f = null;
        private final short g;

        static {
            f = new HashMap();
            Iterator it = EnumSet.allOf(b.class).iterator();
            while (it.hasNext()) {
                b bVar = (b) it.next();
                f.put(Short.valueOf(bVar.a()), bVar);
            }
        }

        private b(short s) {
            this.g = (short) s;
        }

        public static b a(short s) {
            return (b) f.get(Short.valueOf(s));
        }

        public final short a() {
            return this.g;
        }
    }

    /* compiled from: Unknown */
    public enum c {
        CONTEXT_CONTEXT_ID("context"),
        VPS_PATH_STRING_ID(SecurityThreatsConst.CHECK_UNINSTALL_PKG_PATH),
        OBJECT_STORAGE_CLASS_ID("storage"),
        SDK_API_KEY_STRING_ID("sdk_api_key");
        
        private static final Map<String, c> e = null;
        private final String f;

        static {
            e = new HashMap();
            Iterator it = EnumSet.allOf(c.class).iterator();
            while (it.hasNext()) {
                c cVar = (c) it.next();
                e.put(cVar.a(), cVar);
            }
        }

        private c(String str) {
            this.f = str;
        }

        public final String a() {
            return this.f;
        }
    }

    /* compiled from: Unknown */
    public enum d {
        DETECTION_PACKAGE_NAME_STRING_ID((short) 0),
        DETECTION_FILE_PATH_STRING_ID((short) 1),
        DETECTION_ACTION_SHORT_ID((short) 2);
        
        private static final Map<Short, d> d = null;
        private final short e;

        static {
            d = new HashMap();
            Iterator it = EnumSet.allOf(d.class).iterator();
            while (it.hasNext()) {
                d dVar = (d) it.next();
                d.put(Short.valueOf(dVar.a()), dVar);
            }
        }

        private d(short s) {
            this.e = (short) s;
        }

        public final short a() {
            return this.e;
        }
    }

    /* compiled from: Unknown */
    public enum e {
        DETECTION_PREFIX_GROUP_ENUM_STRING_ID((short) 0);
        
        private static final Map<Short, e> b = null;
        private final short c;

        static {
            b = new HashMap();
            Iterator it = EnumSet.allOf(e.class).iterator();
            while (it.hasNext()) {
                e eVar = (e) it.next();
                b.put(Short.valueOf(eVar.a()), eVar);
            }
        }

        private e(short s) {
            this.c = (short) s;
        }

        public final short a() {
            return this.c;
        }
    }

    /* compiled from: Unknown */
    public enum f {
        FILE_FILE_ID((short) 0),
        PACKAGE_NAME_STRING_ID((short) 1);
        
        private static final Map<Short, f> c = null;
        private final short d;

        static {
            c = new HashMap();
            Iterator it = EnumSet.allOf(f.class).iterator();
            while (it.hasNext()) {
                f fVar = (f) it.next();
                c.put(Short.valueOf(fVar.a()), fVar);
            }
        }

        private f(short s) {
            this.d = (short) s;
        }

        public final short a() {
            return this.d;
        }
    }

    /* compiled from: Unknown */
    public enum g {
        MESSAGE_TYPE_SHORT_ID((short) 0),
        SENDER_STRING_ID((short) 1),
        MESSAGE_CONTENT_STRING_ID((short) 2),
        ADDITIONAL_DATA_MAP_ID((short) 3),
        ADDITIONAL_FILES_MAP_ID((short) 4);
        
        private static final Map<Short, g> f = null;
        private final short g;

        static {
            f = new HashMap();
            Iterator it = EnumSet.allOf(g.class).iterator();
            while (it.hasNext()) {
                g gVar = (g) it.next();
                f.put(Short.valueOf(gVar.a()), gVar);
            }
        }

        private g(short s) {
            this.g = (short) s;
        }

        public final short a() {
            return this.g;
        }
    }

    /* compiled from: Unknown */
    public enum h {
        FILE_FILE_ID((short) 0),
        PACKAGE_NAME_STRING_ID((short) 1),
        SDK_VERSION_INT_ID((short) 2),
        BUFFER_BYTE_ARRAY_ID((short) 3),
        FLAGS_LONG_ID((short) 4),
        LANGUAGE_STRING_ID((short) 5),
        PUP_ENABLED_BOOLEAN_ID((short) 6),
        GUID_STRING_ID((short) 7),
        COMMUNITY_IQ_ENABLED_BOOLEAN_ID((short) 8),
        CLOUD_SCANNING_ENABLED_BOOLEAN_ID((short) 9);
        
        private static final Map<Short, h> k = null;
        private final short l;

        static {
            k = new HashMap();
            Iterator it = EnumSet.allOf(h.class).iterator();
            while (it.hasNext()) {
                h hVar = (h) it.next();
                k.put(Short.valueOf(hVar.a()), hVar);
            }
        }

        private h(short s) {
            this.l = (short) s;
        }

        public final short a() {
            return this.l;
        }
    }

    /* compiled from: Unknown */
    public enum i {
        SERVER_ADDRESS_STRING_ID((short) 0),
        SERVER_PROTOCOL_STRING_ID((short) 1),
        SERVER_PORT_INT_ID((short) 2),
        SERVER_PATH_STRING_ID((short) 3);
        
        private static final Map<Short, i> e = null;
        private final short f;

        static {
            e = new HashMap();
            Iterator it = EnumSet.allOf(i.class).iterator();
            while (it.hasNext()) {
                i iVar = (i) it.next();
                e.put(Short.valueOf(iVar.a()), iVar);
            }
        }

        private i(short s) {
            this.f = (short) s;
        }

        public final short a() {
            return this.f;
        }
    }

    /* compiled from: Unknown */
    public enum j {
        URL_STRING_ID((short) 0);
        
        private static final Map<Short, j> b = null;
        private final short c;

        static {
            b = new HashMap();
            Iterator it = EnumSet.allOf(j.class).iterator();
            while (it.hasNext()) {
                j jVar = (j) it.next();
                b.put(Short.valueOf(jVar.a()), jVar);
            }
        }

        private j(short s) {
            this.c = (short) s;
        }

        public final short a() {
            return this.c;
        }
    }
}
