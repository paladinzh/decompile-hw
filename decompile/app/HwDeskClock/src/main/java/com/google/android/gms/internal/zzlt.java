package com.google.android.gms.internal;

import java.util.HashMap;

/* compiled from: Unknown */
public class zzlt {
    public static void zza(StringBuilder stringBuilder, HashMap<String, String> hashMap) {
        stringBuilder.append("{");
        Object obj = 1;
        for (String str : hashMap.keySet()) {
            Object obj2;
            if (obj != null) {
                obj2 = null;
            } else {
                stringBuilder.append(",");
                obj2 = obj;
            }
            String str2 = (String) hashMap.get(str);
            stringBuilder.append("\"").append(str).append("\":");
            if (str2 != null) {
                stringBuilder.append("\"").append(str2).append("\"");
            } else {
                stringBuilder.append("null");
            }
            obj = obj2;
        }
        stringBuilder.append("}");
    }
}
