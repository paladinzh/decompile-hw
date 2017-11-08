package com.google.android.gms.common.server.response;

import android.os.Bundle;
import android.os.Parcel;
import com.android.deskclock.alarmclock.MetaballPath;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.common.server.response.FastJsonResponse.Field;
import com.google.android.gms.internal.zzli;
import com.google.android.gms.internal.zzlj;
import com.google.android.gms.internal.zzls;
import com.google.android.gms.internal.zzlt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/* compiled from: Unknown */
public class SafeParcelResponse extends FastJsonResponse implements SafeParcelable {
    public static final zze CREATOR = new zze();
    private final String mClassName;
    private final int mVersionCode;
    private final FieldMappingDictionary zzafa;
    private final Parcel zzafh;
    private final int zzafi = 2;
    private int zzafj;
    private int zzafk;

    SafeParcelResponse(int versionCode, Parcel parcel, FieldMappingDictionary fieldMappingDictionary) {
        this.mVersionCode = versionCode;
        this.zzafh = (Parcel) zzx.zzv(parcel);
        this.zzafa = fieldMappingDictionary;
        if (this.zzafa != null) {
            this.mClassName = this.zzafa.zzpt();
        } else {
            this.mClassName = null;
        }
        this.zzafj = 2;
    }

    private static HashMap<Integer, Entry<String, Field<?, ?>>> zzE(Map<String, Field<?, ?>> map) {
        HashMap<Integer, Entry<String, Field<?, ?>>> hashMap = new HashMap();
        for (Entry entry : map.entrySet()) {
            hashMap.put(Integer.valueOf(((Field) entry.getValue()).zzpk()), entry);
        }
        return hashMap;
    }

    private void zza(StringBuilder stringBuilder, int i, Object obj) {
        switch (i) {
            case 0:
            case 1:
            case 2:
            case 3:
            case MetaballPath.POINT_NUM /*4*/:
            case 5:
            case 6:
                stringBuilder.append(obj);
                return;
            case 7:
                stringBuilder.append("\"").append(zzls.zzcA(obj.toString())).append("\"");
                return;
            case 8:
                stringBuilder.append("\"").append(zzlj.zzi((byte[]) obj)).append("\"");
                return;
            case 9:
                stringBuilder.append("\"").append(zzlj.zzj((byte[]) obj));
                stringBuilder.append("\"");
                return;
            case 10:
                zzlt.zza(stringBuilder, (HashMap) obj);
                return;
            case 11:
                throw new IllegalArgumentException("Method does not accept concrete type.");
            default:
                throw new IllegalArgumentException("Unknown type = " + i);
        }
    }

    private void zza(StringBuilder stringBuilder, Field<?, ?> field, Parcel parcel, int i) {
        switch (field.zzpc()) {
            case 0:
                zzb(stringBuilder, (Field) field, zza(field, Integer.valueOf(zza.zzg(parcel, i))));
                return;
            case 1:
                zzb(stringBuilder, (Field) field, zza(field, zza.zzk(parcel, i)));
                return;
            case 2:
                zzb(stringBuilder, (Field) field, zza(field, Long.valueOf(zza.zzi(parcel, i))));
                return;
            case 3:
                zzb(stringBuilder, (Field) field, zza(field, Float.valueOf(zza.zzl(parcel, i))));
                return;
            case MetaballPath.POINT_NUM /*4*/:
                zzb(stringBuilder, (Field) field, zza(field, Double.valueOf(zza.zzm(parcel, i))));
                return;
            case 5:
                zzb(stringBuilder, (Field) field, zza(field, zza.zzn(parcel, i)));
                return;
            case 6:
                zzb(stringBuilder, (Field) field, zza(field, Boolean.valueOf(zza.zzc(parcel, i))));
                return;
            case 7:
                zzb(stringBuilder, (Field) field, zza(field, zza.zzo(parcel, i)));
                return;
            case 8:
            case 9:
                zzb(stringBuilder, (Field) field, zza(field, zza.zzr(parcel, i)));
                return;
            case 10:
                zzb(stringBuilder, (Field) field, zza(field, zzi(zza.zzq(parcel, i))));
                return;
            case 11:
                throw new IllegalArgumentException("Method does not accept concrete type.");
            default:
                throw new IllegalArgumentException("Unknown field out type = " + field.zzpc());
        }
    }

    private void zza(StringBuilder stringBuilder, String str, Field<?, ?> field, Parcel parcel, int i) {
        stringBuilder.append("\"").append(str).append("\":");
        if (field.zzpn()) {
            zza(stringBuilder, field, parcel, i);
        } else {
            zzb(stringBuilder, field, parcel, i);
        }
    }

    private void zza(StringBuilder stringBuilder, Map<String, Field<?, ?>> map, Parcel parcel) {
        HashMap zzE = zzE(map);
        stringBuilder.append('{');
        int zzaj = zza.zzaj(parcel);
        Object obj = null;
        while (parcel.dataPosition() < zzaj) {
            int zzai = zza.zzai(parcel);
            Entry entry = (Entry) zzE.get(Integer.valueOf(zza.zzbH(zzai)));
            if (entry != null) {
                if (obj != null) {
                    stringBuilder.append(",");
                }
                zza(stringBuilder, (String) entry.getKey(), (Field) entry.getValue(), parcel, zzai);
                obj = 1;
            }
        }
        if (parcel.dataPosition() == zzaj) {
            stringBuilder.append('}');
            return;
        }
        throw new zza.zza("Overread allowed size end=" + zzaj, parcel);
    }

    private void zzb(StringBuilder stringBuilder, Field<?, ?> field, Parcel parcel, int i) {
        if (field.zzpi()) {
            stringBuilder.append("[");
            switch (field.zzpc()) {
                case 0:
                    zzli.zza(stringBuilder, zza.zzu(parcel, i));
                    break;
                case 1:
                    zzli.zza(stringBuilder, zza.zzw(parcel, i));
                    break;
                case 2:
                    zzli.zza(stringBuilder, zza.zzv(parcel, i));
                    break;
                case 3:
                    zzli.zza(stringBuilder, zza.zzx(parcel, i));
                    break;
                case MetaballPath.POINT_NUM /*4*/:
                    zzli.zza(stringBuilder, zza.zzy(parcel, i));
                    break;
                case 5:
                    zzli.zza(stringBuilder, zza.zzz(parcel, i));
                    break;
                case 6:
                    zzli.zza(stringBuilder, zza.zzt(parcel, i));
                    break;
                case 7:
                    zzli.zza(stringBuilder, zza.zzA(parcel, i));
                    break;
                case 8:
                case 9:
                case 10:
                    throw new UnsupportedOperationException("List of type BASE64, BASE64_URL_SAFE, or STRING_MAP is not supported");
                case 11:
                    Parcel[] zzE = zza.zzE(parcel, i);
                    int length = zzE.length;
                    for (int i2 = 0; i2 < length; i2++) {
                        if (i2 > 0) {
                            stringBuilder.append(",");
                        }
                        zzE[i2].setDataPosition(0);
                        zza(stringBuilder, field.zzpp(), zzE[i2]);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown field type out.");
            }
            stringBuilder.append("]");
            return;
        }
        switch (field.zzpc()) {
            case 0:
                stringBuilder.append(zza.zzg(parcel, i));
                return;
            case 1:
                stringBuilder.append(zza.zzk(parcel, i));
                return;
            case 2:
                stringBuilder.append(zza.zzi(parcel, i));
                return;
            case 3:
                stringBuilder.append(zza.zzl(parcel, i));
                return;
            case MetaballPath.POINT_NUM /*4*/:
                stringBuilder.append(zza.zzm(parcel, i));
                return;
            case 5:
                stringBuilder.append(zza.zzn(parcel, i));
                return;
            case 6:
                stringBuilder.append(zza.zzc(parcel, i));
                return;
            case 7:
                stringBuilder.append("\"").append(zzls.zzcA(zza.zzo(parcel, i))).append("\"");
                return;
            case 8:
                stringBuilder.append("\"").append(zzlj.zzi(zza.zzr(parcel, i))).append("\"");
                return;
            case 9:
                stringBuilder.append("\"").append(zzlj.zzj(zza.zzr(parcel, i)));
                stringBuilder.append("\"");
                return;
            case 10:
                Bundle zzq = zza.zzq(parcel, i);
                Set<String> keySet = zzq.keySet();
                keySet.size();
                stringBuilder.append("{");
                int i3 = 1;
                for (String str : keySet) {
                    if (i3 == 0) {
                        stringBuilder.append(",");
                    }
                    stringBuilder.append("\"").append(str).append("\"");
                    stringBuilder.append(":");
                    stringBuilder.append("\"").append(zzls.zzcA(zzq.getString(str))).append("\"");
                    i3 = 0;
                }
                stringBuilder.append("}");
                return;
            case 11:
                Parcel zzD = zza.zzD(parcel, i);
                zzD.setDataPosition(0);
                zza(stringBuilder, field.zzpp(), zzD);
                return;
            default:
                throw new IllegalStateException("Unknown field type out");
        }
    }

    private void zzb(StringBuilder stringBuilder, Field<?, ?> field, Object obj) {
        if (field.zzph()) {
            zzb(stringBuilder, (Field) field, (ArrayList) obj);
        } else {
            zza(stringBuilder, field.zzpb(), obj);
        }
    }

    private void zzb(StringBuilder stringBuilder, Field<?, ?> field, ArrayList<?> arrayList) {
        stringBuilder.append("[");
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                stringBuilder.append(",");
            }
            zza(stringBuilder, field.zzpb(), arrayList.get(i));
        }
        stringBuilder.append("]");
    }

    public static HashMap<String, String> zzi(Bundle bundle) {
        HashMap<String, String> hashMap = new HashMap();
        for (String str : bundle.keySet()) {
            hashMap.put(str, bundle.getString(str));
        }
        return hashMap;
    }

    public int describeContents() {
        zze zze = CREATOR;
        return 0;
    }

    public int getVersionCode() {
        return this.mVersionCode;
    }

    public String toString() {
        zzx.zzb(this.zzafa, (Object) "Cannot convert to JSON on client side.");
        Parcel zzpv = zzpv();
        zzpv.setDataPosition(0);
        StringBuilder stringBuilder = new StringBuilder(100);
        zza(stringBuilder, this.zzafa.zzcx(this.mClassName), zzpv);
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zze zze = CREATOR;
        zze.zza(this, out, flags);
    }

    protected Object zzct(String str) {
        throw new UnsupportedOperationException("Converting to JSON does not require this method.");
    }

    protected boolean zzcu(String str) {
        throw new UnsupportedOperationException("Converting to JSON does not require this method.");
    }

    public Map<String, Field<?, ?>> zzpd() {
        return this.zzafa != null ? this.zzafa.zzcx(this.mClassName) : null;
    }

    public Parcel zzpv() {
        switch (this.zzafj) {
            case 0:
                this.zzafk = zzb.zzak(this.zzafh);
                zzb.zzH(this.zzafh, this.zzafk);
                break;
            case 1:
                zzb.zzH(this.zzafh, this.zzafk);
                break;
            default:
                return this.zzafh;
        }
        this.zzafj = 2;
        return this.zzafh;
    }

    FieldMappingDictionary zzpw() {
        switch (this.zzafi) {
            case 0:
                return null;
            case 1:
                return this.zzafa;
            case 2:
                return this.zzafa;
            default:
                throw new IllegalStateException("Invalid creation type: " + this.zzafi);
        }
    }
}
