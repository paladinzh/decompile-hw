package com.google.android.gms.common.internal;

import java.util.Iterator;

/* compiled from: Unknown */
public class zzv {
    private final String separator;

    private zzv(String str) {
        this.separator = str;
    }

    public static zzv zzcL(String str) {
        return new zzv(str);
    }

    public final String zza(Iterable<?> iterable) {
        return zza(new StringBuilder(), iterable).toString();
    }

    public final StringBuilder zza(StringBuilder stringBuilder, Iterable<?> iterable) {
        Iterator it = iterable.iterator();
        if (it.hasNext()) {
            stringBuilder.append(zzx(it.next()));
            while (it.hasNext()) {
                stringBuilder.append(this.separator);
                stringBuilder.append(zzx(it.next()));
            }
        }
        return stringBuilder;
    }

    CharSequence zzx(Object obj) {
        return !(obj instanceof CharSequence) ? obj.toString() : (CharSequence) obj;
    }
}
