package com.google.android.gms.internal;

import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/* compiled from: Unknown */
public final class zzsv {
    private static void zza(String str, Object obj, StringBuffer stringBuffer, StringBuffer stringBuffer2) throws IllegalAccessException, InvocationTargetException {
        if (obj == null) {
            return;
        }
        if (obj instanceof zzsu) {
            int modifiers;
            int length = stringBuffer.length();
            if (str != null) {
                stringBuffer2.append(stringBuffer).append(zzgP(str)).append(" <\n");
                stringBuffer.append("  ");
            }
            Class cls = obj.getClass();
            for (Field field : cls.getFields()) {
                modifiers = field.getModifiers();
                String name = field.getName();
                if (!("cachedSize".equals(name) || (modifiers & 1) != 1 || (modifiers & 8) == 8 || name.startsWith("_") || name.endsWith("_"))) {
                    Class type = field.getType();
                    Object obj2 = field.get(obj);
                    if (!type.isArray()) {
                        zza(name, obj2, stringBuffer, stringBuffer2);
                    } else if (type.getComponentType() != Byte.TYPE) {
                        int length2 = obj2 != null ? Array.getLength(obj2) : 0;
                        for (modifiers = 0; modifiers < length2; modifiers++) {
                            zza(name, Array.get(obj2, modifiers), stringBuffer, stringBuffer2);
                        }
                    } else {
                        zza(name, obj2, stringBuffer, stringBuffer2);
                    }
                }
            }
            for (Method name2 : cls.getMethods()) {
                String name3 = name2.getName();
                if (name3.startsWith("set")) {
                    String substring = name3.substring(3);
                    try {
                        if (((Boolean) cls.getMethod("has" + substring, new Class[0]).invoke(obj, new Object[0])).booleanValue()) {
                            try {
                                zza(substring, cls.getMethod("get" + substring, new Class[0]).invoke(obj, new Object[0]), stringBuffer, stringBuffer2);
                            } catch (NoSuchMethodException e) {
                            }
                        }
                    } catch (NoSuchMethodException e2) {
                    }
                }
            }
            if (str != null) {
                stringBuffer.setLength(length);
                stringBuffer2.append(stringBuffer).append(">\n");
                return;
            }
            return;
        }
        stringBuffer2.append(stringBuffer).append(zzgP(str)).append(": ");
        if (obj instanceof String) {
            stringBuffer2.append("\"").append(zzbZ((String) obj)).append("\"");
        } else if (obj instanceof byte[]) {
            zza((byte[]) obj, stringBuffer2);
        } else {
            stringBuffer2.append(obj);
        }
        stringBuffer2.append("\n");
    }

    private static void zza(byte[] bArr, StringBuffer stringBuffer) {
        if (bArr != null) {
            stringBuffer.append('\"');
            for (byte b : bArr) {
                int i = b & 255;
                if (i == 92 || i == 34) {
                    stringBuffer.append('\\').append((char) i);
                } else if (i >= 32 && i < 127) {
                    stringBuffer.append((char) i);
                } else {
                    stringBuffer.append(String.format("\\%03o", new Object[]{Integer.valueOf(i)}));
                }
            }
            stringBuffer.append('\"');
            return;
        }
        stringBuffer.append("\"\"");
    }

    private static String zzbZ(String str) {
        if (!str.startsWith("http") && str.length() > SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) {
            str = str.substring(0, SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) + "[...]";
        }
        return zzcU(str);
    }

    private static String zzcU(String str) {
        int length = str.length();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            if (charAt >= ' ' && charAt <= '~' && charAt != '\"' && charAt != '\'') {
                stringBuilder.append(charAt);
            } else {
                stringBuilder.append(String.format("\\u%04x", new Object[]{Integer.valueOf(charAt)}));
            }
        }
        return stringBuilder.toString();
    }

    public static <T extends zzsu> String zzf(T t) {
        if (t == null) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        try {
            zza(null, t, new StringBuffer(), stringBuffer);
            return stringBuffer.toString();
        } catch (IllegalAccessException e) {
            return "Error printing proto: " + e.getMessage();
        } catch (InvocationTargetException e2) {
            return "Error printing proto: " + e2.getMessage();
        }
    }

    private static String zzgP(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (i == 0) {
                stringBuffer.append(Character.toLowerCase(charAt));
            } else if (Character.isUpperCase(charAt)) {
                stringBuffer.append('_').append(Character.toLowerCase(charAt));
            } else {
                stringBuffer.append(charAt);
            }
        }
        return stringBuffer.toString();
    }
}
