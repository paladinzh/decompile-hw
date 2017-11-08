package com.google.android.gms.common.internal.safeparcel;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

/* compiled from: Unknown */
public class zzb {
    private static int zzF(Parcel parcel, int i) {
        parcel.writeInt(-65536 | i);
        parcel.writeInt(0);
        return parcel.dataPosition();
    }

    private static void zzG(Parcel parcel, int i) {
        int dataPosition = parcel.dataPosition();
        int i2 = dataPosition - i;
        parcel.setDataPosition(i - 4);
        parcel.writeInt(i2);
        parcel.setDataPosition(dataPosition);
    }

    public static void zzH(Parcel parcel, int i) {
        zzG(parcel, i);
    }

    public static void zza(Parcel parcel, int i, byte b) {
        zzb(parcel, i, 4);
        parcel.writeInt(b);
    }

    public static void zza(Parcel parcel, int i, float f) {
        zzb(parcel, i, 4);
        parcel.writeFloat(f);
    }

    public static void zza(Parcel parcel, int i, long j) {
        zzb(parcel, i, 8);
        parcel.writeLong(j);
    }

    public static void zza(Parcel parcel, int i, Bundle bundle, boolean z) {
        if (bundle != null) {
            int zzF = zzF(parcel, i);
            parcel.writeBundle(bundle);
            zzG(parcel, zzF);
            return;
        }
        if (z) {
            zzb(parcel, i, 0);
        }
    }

    public static void zza(Parcel parcel, int i, IBinder iBinder, boolean z) {
        if (iBinder != null) {
            int zzF = zzF(parcel, i);
            parcel.writeStrongBinder(iBinder);
            zzG(parcel, zzF);
            return;
        }
        if (z) {
            zzb(parcel, i, 0);
        }
    }

    public static void zza(Parcel parcel, int i, Parcel parcel2, boolean z) {
        if (parcel2 != null) {
            int zzF = zzF(parcel, i);
            parcel.appendFrom(parcel2, 0, parcel2.dataSize());
            zzG(parcel, zzF);
            return;
        }
        if (z) {
            zzb(parcel, i, 0);
        }
    }

    public static void zza(Parcel parcel, int i, Parcelable parcelable, int i2, boolean z) {
        if (parcelable != null) {
            int zzF = zzF(parcel, i);
            parcelable.writeToParcel(parcel, i2);
            zzG(parcel, zzF);
            return;
        }
        if (z) {
            zzb(parcel, i, 0);
        }
    }

    public static void zza(Parcel parcel, int i, String str, boolean z) {
        if (str != null) {
            int zzF = zzF(parcel, i);
            parcel.writeString(str);
            zzG(parcel, zzF);
            return;
        }
        if (z) {
            zzb(parcel, i, 0);
        }
    }

    public static void zza(Parcel parcel, int i, List<Integer> list, boolean z) {
        if (list != null) {
            int zzF = zzF(parcel, i);
            int size = list.size();
            parcel.writeInt(size);
            for (int i2 = 0; i2 < size; i2++) {
                parcel.writeInt(((Integer) list.get(i2)).intValue());
            }
            zzG(parcel, zzF);
            return;
        }
        if (z) {
            zzb(parcel, i, 0);
        }
    }

    public static void zza(Parcel parcel, int i, boolean z) {
        int i2 = 0;
        zzb(parcel, i, 4);
        if (z) {
            i2 = 1;
        }
        parcel.writeInt(i2);
    }

    public static void zza(Parcel parcel, int i, byte[] bArr, boolean z) {
        if (bArr != null) {
            int zzF = zzF(parcel, i);
            parcel.writeByteArray(bArr);
            zzG(parcel, zzF);
            return;
        }
        if (z) {
            zzb(parcel, i, 0);
        }
    }

    public static <T extends Parcelable> void zza(Parcel parcel, int i, T[] tArr, int i2, boolean z) {
        if (tArr != null) {
            int zzF = zzF(parcel, i);
            parcel.writeInt(r3);
            for (Parcelable parcelable : tArr) {
                if (parcelable != null) {
                    zza(parcel, parcelable, i2);
                } else {
                    parcel.writeInt(0);
                }
            }
            zzG(parcel, zzF);
            return;
        }
        if (z) {
            zzb(parcel, i, 0);
        }
    }

    public static void zza(Parcel parcel, int i, String[] strArr, boolean z) {
        if (strArr != null) {
            int zzF = zzF(parcel, i);
            parcel.writeStringArray(strArr);
            zzG(parcel, zzF);
            return;
        }
        if (z) {
            zzb(parcel, i, 0);
        }
    }

    private static <T extends Parcelable> void zza(Parcel parcel, T t, int i) {
        int dataPosition = parcel.dataPosition();
        parcel.writeInt(1);
        int dataPosition2 = parcel.dataPosition();
        t.writeToParcel(parcel, i);
        int dataPosition3 = parcel.dataPosition();
        parcel.setDataPosition(dataPosition);
        parcel.writeInt(dataPosition3 - dataPosition2);
        parcel.setDataPosition(dataPosition3);
    }

    public static int zzak(Parcel parcel) {
        return zzF(parcel, 20293);
    }

    private static void zzb(Parcel parcel, int i, int i2) {
        if (i2 < 65535) {
            parcel.writeInt((i2 << 16) | i);
            return;
        }
        parcel.writeInt(-65536 | i);
        parcel.writeInt(i2);
    }

    public static void zzb(Parcel parcel, int i, List<String> list, boolean z) {
        if (list != null) {
            int zzF = zzF(parcel, i);
            parcel.writeStringList(list);
            zzG(parcel, zzF);
            return;
        }
        if (z) {
            zzb(parcel, i, 0);
        }
    }

    public static void zzc(Parcel parcel, int i, int i2) {
        zzb(parcel, i, 4);
        parcel.writeInt(i2);
    }

    public static <T extends Parcelable> void zzc(Parcel parcel, int i, List<T> list, boolean z) {
        if (list != null) {
            int zzF = zzF(parcel, i);
            int size = list.size();
            parcel.writeInt(size);
            for (int i2 = 0; i2 < size; i2++) {
                Parcelable parcelable = (Parcelable) list.get(i2);
                if (parcelable != null) {
                    zza(parcel, parcelable, 0);
                } else {
                    parcel.writeInt(0);
                }
            }
            zzG(parcel, zzF);
            return;
        }
        if (z) {
            zzb(parcel, i, 0);
        }
    }
}
