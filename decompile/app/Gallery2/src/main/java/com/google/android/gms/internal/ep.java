package com.google.android.gms.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* compiled from: Unknown */
public final class ep {

    /* compiled from: Unknown */
    public static final class a {
        private final List<String> Ce;
        private final Object Cf;

        private a(Object obj) {
            this.Cf = er.f(obj);
            this.Ce = new ArrayList();
        }

        public a a(String str, Object obj) {
            this.Ce.add(((String) er.f(str)) + "=" + String.valueOf(obj));
            return this;
        }

        public String toString() {
            StringBuilder append = new StringBuilder(100).append(this.Cf.getClass().getSimpleName()).append('{');
            int size = this.Ce.size();
            for (int i = 0; i < size; i++) {
                append.append((String) this.Ce.get(i));
                if (i < size - 1) {
                    append.append(", ");
                }
            }
            return append.append('}').toString();
        }
    }

    public static a e(Object obj) {
        return new a(obj);
    }

    public static boolean equal(Object a, Object b) {
        if (a != b) {
            if (a == null) {
                return false;
            }
            if (!a.equals(b)) {
                return false;
            }
        }
        return true;
    }

    public static int hashCode(Object... objects) {
        return Arrays.hashCode(objects);
    }
}
