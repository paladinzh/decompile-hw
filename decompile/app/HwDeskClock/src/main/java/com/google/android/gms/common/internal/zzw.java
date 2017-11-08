package com.google.android.gms.common.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* compiled from: Unknown */
public final class zzw {

    /* compiled from: Unknown */
    public static final class zza {
        private final Object zzIr;
        private final List<String> zzaen;

        private zza(Object obj) {
            this.zzIr = zzx.zzv(obj);
            this.zzaen = new ArrayList();
        }

        public String toString() {
            StringBuilder append = new StringBuilder(100).append(this.zzIr.getClass().getSimpleName()).append('{');
            int size = this.zzaen.size();
            for (int i = 0; i < size; i++) {
                append.append((String) this.zzaen.get(i));
                if (i < size - 1) {
                    append.append(", ");
                }
            }
            return append.append('}').toString();
        }

        public zza zzg(String str, Object obj) {
            this.zzaen.add(((String) zzx.zzv(str)) + "=" + String.valueOf(obj));
            return this;
        }
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

    public static zza zzu(Object obj) {
        return new zza(obj);
    }
}
