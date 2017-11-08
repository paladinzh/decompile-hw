package com.google.android.gms.internal;

import com.google.android.gms.common.internal.zze;
import java.util.regex.Pattern;

/* compiled from: Unknown */
public class zzni {
    private static final Pattern zzaok = Pattern.compile("\\$\\{(.*?)\\}");

    public static boolean zzcV(String str) {
        return str == null || zze.zzakF.zzb(str);
    }
}
