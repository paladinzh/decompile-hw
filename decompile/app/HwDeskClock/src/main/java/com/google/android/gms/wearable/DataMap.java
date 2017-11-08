package com.google.android.gms.wearable;

import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.internal.zzrl;
import com.google.android.gms.internal.zzrl.zza;
import com.google.android.gms.internal.zzrm;
import com.google.android.gms.internal.zzrx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/* compiled from: Unknown */
public class DataMap {
    private final HashMap<String, Object> zzaYU = new HashMap();

    public static DataMap fromByteArray(byte[] bytes) {
        try {
            return zzrl.zza(new zza(zzrm.zzw(bytes), new ArrayList()));
        } catch (Throwable e) {
            throw new IllegalArgumentException("Unable to convert data", e);
        }
    }

    private void zza(String str, Object obj, String str2, ClassCastException classCastException) {
        zza(str, obj, str2, "<null>", classCastException);
    }

    private void zza(String str, Object obj, String str2, Object obj2, ClassCastException classCastException) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Key ");
        stringBuilder.append(str);
        stringBuilder.append(" expected ");
        stringBuilder.append(str2);
        stringBuilder.append(" but value was a ");
        stringBuilder.append(obj.getClass().getName());
        stringBuilder.append(".  The default value ");
        stringBuilder.append(obj2);
        stringBuilder.append(" was returned.");
        Log.w("DataMap", stringBuilder.toString());
        Log.w("DataMap", "Attempt to cast generated internal exception:", classCastException);
    }

    private static boolean zza(Asset asset, Asset asset2) {
        boolean z = false;
        if (asset != null && asset2 != null) {
            return TextUtils.isEmpty(asset.getDigest()) ? Arrays.equals(asset.getData(), asset2.getData()) : asset.getDigest().equals(asset2.getDigest());
        } else {
            if (asset == asset2) {
                z = true;
            }
            return z;
        }
    }

    private static boolean zza(DataMap dataMap, DataMap dataMap2) {
        if (dataMap.size() != dataMap2.size()) {
            return false;
        }
        for (String str : dataMap.keySet()) {
            Object obj = dataMap.get(str);
            Object obj2 = dataMap2.get(str);
            if (obj instanceof Asset) {
                if (!(obj2 instanceof Asset) || !zza((Asset) obj, (Asset) obj2)) {
                    return false;
                }
            } else if (obj instanceof String[]) {
                if (!(obj2 instanceof String[]) || !Arrays.equals((String[]) obj, (String[]) obj2)) {
                    return false;
                }
            } else if (obj instanceof long[]) {
                if (!(obj2 instanceof long[]) || !Arrays.equals((long[]) obj, (long[]) obj2)) {
                    return false;
                }
            } else if (obj instanceof float[]) {
                if (!(obj2 instanceof float[]) || !Arrays.equals((float[]) obj, (float[]) obj2)) {
                    return false;
                }
            } else if (obj instanceof byte[]) {
                if (!(obj2 instanceof byte[]) || !Arrays.equals((byte[]) obj, (byte[]) obj2)) {
                    return false;
                }
            } else if (obj == null || obj2 == null) {
                return obj == obj2;
            } else if (!obj.equals(obj2)) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object o) {
        return o instanceof DataMap ? zza(this, (DataMap) o) : false;
    }

    public <T> T get(String key) {
        return this.zzaYU.get(key);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        Object obj = this.zzaYU.get(key);
        if (obj == null) {
            return defaultValue;
        }
        try {
            return ((Integer) obj).intValue();
        } catch (ClassCastException e) {
            zza(key, obj, "Integer", e);
            return defaultValue;
        }
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public long getLong(String key, long defaultValue) {
        Object obj = this.zzaYU.get(key);
        if (obj == null) {
            return defaultValue;
        }
        try {
            return ((Long) obj).longValue();
        } catch (ClassCastException e) {
            zza(key, obj, "long", e);
            return defaultValue;
        }
    }

    public int hashCode() {
        return this.zzaYU.hashCode() * 29;
    }

    public Set<String> keySet() {
        return this.zzaYU.keySet();
    }

    public void putAsset(String key, Asset value) {
        this.zzaYU.put(key, value);
    }

    public void putBoolean(String key, boolean value) {
        this.zzaYU.put(key, Boolean.valueOf(value));
    }

    public void putByte(String key, byte value) {
        this.zzaYU.put(key, Byte.valueOf(value));
    }

    public void putByteArray(String key, byte[] value) {
        this.zzaYU.put(key, value);
    }

    public void putDataMap(String key, DataMap value) {
        this.zzaYU.put(key, value);
    }

    public void putDataMapArrayList(String key, ArrayList<DataMap> value) {
        this.zzaYU.put(key, value);
    }

    public void putDouble(String key, double value) {
        this.zzaYU.put(key, Double.valueOf(value));
    }

    public void putFloat(String key, float value) {
        this.zzaYU.put(key, Float.valueOf(value));
    }

    public void putFloatArray(String key, float[] value) {
        this.zzaYU.put(key, value);
    }

    public void putInt(String key, int value) {
        this.zzaYU.put(key, Integer.valueOf(value));
    }

    public void putIntegerArrayList(String key, ArrayList<Integer> value) {
        this.zzaYU.put(key, value);
    }

    public void putLong(String key, long value) {
        this.zzaYU.put(key, Long.valueOf(value));
    }

    public void putLongArray(String key, long[] value) {
        this.zzaYU.put(key, value);
    }

    public void putString(String key, String value) {
        this.zzaYU.put(key, value);
    }

    public void putStringArray(String key, String[] value) {
        this.zzaYU.put(key, value);
    }

    public void putStringArrayList(String key, ArrayList<String> value) {
        this.zzaYU.put(key, value);
    }

    public int size() {
        return this.zzaYU.size();
    }

    public byte[] toByteArray() {
        return zzrx.zzf(zzrl.zza(this).zzbbs);
    }

    public String toString() {
        return this.zzaYU.toString();
    }
}
