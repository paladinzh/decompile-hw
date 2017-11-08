package com.google.android.gms.location.places.internal;

import android.os.Parcelable.Creator;
import android.util.Log;
import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.common.data.zzc;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.zzsk;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* compiled from: Unknown */
public abstract class zzt extends zzc {
    private final String TAG = "SafeDataBufferRef";

    public zzt(DataHolder dataHolder, int i) {
        super(dataHolder, i);
    }

    protected String zzG(String str, String str2) {
        return (zzcz(str) && !zzcB(str)) ? getString(str) : str2;
    }

    protected <E extends SafeParcelable> E zza(String str, Creator<E> creator) {
        byte[] zzc = zzc(str, null);
        return zzc != null ? com.google.android.gms.common.internal.safeparcel.zzc.zza(zzc, creator) : null;
    }

    protected <E extends SafeParcelable> List<E> zza(String str, Creator<E> creator, List<E> list) {
        byte[] zzc = zzc(str, null);
        if (zzc == null) {
            return list;
        }
        try {
            zzsk zzB = zzsk.zzB(zzc);
            if (zzB.zzbtV == null) {
                return list;
            }
            List<E> arrayList = new ArrayList(zzB.zzbtV.length);
            for (byte[] zza : zzB.zzbtV) {
                arrayList.add(com.google.android.gms.common.internal.safeparcel.zzc.zza(zza, creator));
            }
            return arrayList;
        } catch (Throwable e) {
            if (Log.isLoggable("SafeDataBufferRef", 6)) {
                Log.e("SafeDataBufferRef", "Cannot parse byte[]", e);
            }
            return list;
        }
    }

    protected List<Integer> zza(String str, List<Integer> list) {
        byte[] zzc = zzc(str, null);
        if (zzc == null) {
            return list;
        }
        try {
            zzsk zzB = zzsk.zzB(zzc);
            if (zzB.zzbtU == null) {
                return list;
            }
            List<Integer> arrayList = new ArrayList(zzB.zzbtU.length);
            for (int valueOf : zzB.zzbtU) {
                arrayList.add(Integer.valueOf(valueOf));
            }
            return arrayList;
        } catch (Throwable e) {
            if (Log.isLoggable("SafeDataBufferRef", 6)) {
                Log.e("SafeDataBufferRef", "Cannot parse byte[]", e);
            }
            return list;
        }
    }

    protected float zzb(String str, float f) {
        return (zzcz(str) && !zzcB(str)) ? getFloat(str) : f;
    }

    protected List<String> zzb(String str, List<String> list) {
        byte[] zzc = zzc(str, null);
        if (zzc == null) {
            return list;
        }
        try {
            zzsk zzB = zzsk.zzB(zzc);
            return zzB.zzbtT != null ? Arrays.asList(zzB.zzbtT) : list;
        } catch (Throwable e) {
            if (Log.isLoggable("SafeDataBufferRef", 6)) {
                Log.e("SafeDataBufferRef", "Cannot parse byte[]", e);
            }
            return list;
        }
    }

    protected byte[] zzc(String str, byte[] bArr) {
        return (zzcz(str) && !zzcB(str)) ? getByteArray(str) : bArr;
    }

    protected boolean zzl(String str, boolean z) {
        return (zzcz(str) && !zzcB(str)) ? getBoolean(str) : z;
    }

    protected int zzz(String str, int i) {
        return (zzcz(str) && !zzcB(str)) ? getInteger(str) : i;
    }
}
