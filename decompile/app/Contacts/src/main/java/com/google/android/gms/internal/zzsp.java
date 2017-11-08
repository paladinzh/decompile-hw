package com.google.android.gms.internal;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class zzsp<M extends zzso<M>, T> {
    public final int tag;
    protected final int type;
    protected final Class<T> zzbuk;
    protected final boolean zzbul;

    private zzsp(int i, Class<T> cls, int i2, boolean z) {
        this.type = i;
        this.zzbuk = cls;
        this.tag = i2;
        this.zzbul = z;
    }

    private T zzK(List<zzsw> list) {
        int i = 0;
        List arrayList = new ArrayList();
        for (int i2 = 0; i2 < list.size(); i2++) {
            zzsw zzsw = (zzsw) list.get(i2);
            if (zzsw.zzbuv.length != 0) {
                zza(zzsw, arrayList);
            }
        }
        int size = arrayList.size();
        if (size == 0) {
            return null;
        }
        T cast = this.zzbuk.cast(Array.newInstance(this.zzbuk.getComponentType(), size));
        while (i < size) {
            Array.set(cast, i, arrayList.get(i));
            i++;
        }
        return cast;
    }

    private T zzL(List<zzsw> list) {
        if (list.isEmpty()) {
            return null;
        }
        return this.zzbuk.cast(zzP(zzsm.zzD(((zzsw) list.get(list.size() - 1)).zzbuv)));
    }

    public static <M extends zzso<M>, T extends zzsu> zzsp<M, T> zza(int i, Class<T> cls, long j) {
        return new zzsp(i, cls, (int) j, false);
    }

    final T zzJ(List<zzsw> list) {
        if (list == null) {
            return null;
        }
        return !this.zzbul ? zzL(list) : zzK(list);
    }

    protected Object zzP(zzsm zzsm) {
        Class componentType = !this.zzbul ? this.zzbuk : this.zzbuk.getComponentType();
        try {
            zzsu zzsu;
            switch (this.type) {
                case 10:
                    zzsu = (zzsu) componentType.newInstance();
                    zzsm.zza(zzsu, zzsx.zzmJ(this.tag));
                    return zzsu;
                case 11:
                    zzsu = (zzsu) componentType.newInstance();
                    zzsm.zza(zzsu);
                    return zzsu;
                default:
                    throw new IllegalArgumentException("Unknown type " + this.type);
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException("Error creating instance of class " + componentType, e);
        } catch (Throwable e2) {
            throw new IllegalArgumentException("Error creating instance of class " + componentType, e2);
        } catch (Throwable e22) {
            throw new IllegalArgumentException("Error reading extension field", e22);
        }
    }

    int zzY(Object obj) {
        return !this.zzbul ? zzaa(obj) : zzZ(obj);
    }

    protected int zzZ(Object obj) {
        int i = 0;
        int length = Array.getLength(obj);
        for (int i2 = 0; i2 < length; i2++) {
            if (Array.get(obj, i2) != null) {
                i += zzaa(Array.get(obj, i2));
            }
        }
        return i;
    }

    protected void zza(zzsw zzsw, List<Object> list) {
        list.add(zzP(zzsm.zzD(zzsw.zzbuv)));
    }

    void zza(Object obj, zzsn zzsn) throws IOException {
        if (this.zzbul) {
            zzc(obj, zzsn);
        } else {
            zzb(obj, zzsn);
        }
    }

    protected int zzaa(Object obj) {
        int zzmJ = zzsx.zzmJ(this.tag);
        switch (this.type) {
            case 10:
                return zzsn.zzb(zzmJ, (zzsu) obj);
            case 11:
                return zzsn.zzc(zzmJ, (zzsu) obj);
            default:
                throw new IllegalArgumentException("Unknown type " + this.type);
        }
    }

    protected void zzb(Object obj, zzsn zzsn) {
        try {
            zzsn.zzmB(this.tag);
            switch (this.type) {
                case 10:
                    zzsu zzsu = (zzsu) obj;
                    int zzmJ = zzsx.zzmJ(this.tag);
                    zzsn.zzb(zzsu);
                    zzsn.zzE(zzmJ, 4);
                    return;
                case 11:
                    zzsn.zzc((zzsu) obj);
                    return;
                default:
                    throw new IllegalArgumentException("Unknown type " + this.type);
            }
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
        throw new IllegalStateException(e);
    }

    protected void zzc(Object obj, zzsn zzsn) {
        int length = Array.getLength(obj);
        for (int i = 0; i < length; i++) {
            Object obj2 = Array.get(obj, i);
            if (obj2 != null) {
                zzb(obj2, zzsn);
            }
        }
    }
}
