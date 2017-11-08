package com.google.android.gms.common.server.converter;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.server.response.FastJsonResponse.zza;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/* compiled from: Unknown */
public final class StringToIntConverter implements SafeParcelable, zza<String, Integer> {
    public static final zzb CREATOR = new zzb();
    private final int mVersionCode;
    private final HashMap<String, Integer> zzamG;
    private final HashMap<Integer, String> zzamH;
    private final ArrayList<Entry> zzamI;

    /* compiled from: Unknown */
    public static final class Entry implements SafeParcelable {
        public static final zzc CREATOR = new zzc();
        final int versionCode;
        final String zzamJ;
        final int zzamK;

        Entry(int versionCode, String stringValue, int intValue) {
            this.versionCode = versionCode;
            this.zzamJ = stringValue;
            this.zzamK = intValue;
        }

        Entry(String stringValue, int intValue) {
            this.versionCode = 1;
            this.zzamJ = stringValue;
            this.zzamK = intValue;
        }

        public int describeContents() {
            zzc zzc = CREATOR;
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            zzc zzc = CREATOR;
            zzc.zza(this, out, flags);
        }
    }

    public StringToIntConverter() {
        this.mVersionCode = 1;
        this.zzamG = new HashMap();
        this.zzamH = new HashMap();
        this.zzamI = null;
    }

    StringToIntConverter(int versionCode, ArrayList<Entry> serializedMap) {
        this.mVersionCode = versionCode;
        this.zzamG = new HashMap();
        this.zzamH = new HashMap();
        this.zzamI = null;
        zzd(serializedMap);
    }

    private void zzd(ArrayList<Entry> arrayList) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            zzh(entry.zzamJ, entry.zzamK);
        }
    }

    public /* synthetic */ Object convertBack(Object obj) {
        return zzb((Integer) obj);
    }

    public int describeContents() {
        zzb zzb = CREATOR;
        return 0;
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzb zzb = CREATOR;
        zzb.zza(this, out, flags);
    }

    public String zzb(Integer num) {
        String str = (String) this.zzamH.get(num);
        return (str == null && this.zzamG.containsKey("gms_unknown")) ? "gms_unknown" : str;
    }

    public StringToIntConverter zzh(String str, int i) {
        this.zzamG.put(str, Integer.valueOf(i));
        this.zzamH.put(Integer.valueOf(i), str);
        return this;
    }

    ArrayList<Entry> zzri() {
        ArrayList<Entry> arrayList = new ArrayList();
        for (String str : this.zzamG.keySet()) {
            arrayList.add(new Entry(str, ((Integer) this.zzamG.get(str)).intValue()));
        }
        return arrayList;
    }

    public int zzrj() {
        return 7;
    }

    public int zzrk() {
        return 0;
    }
}
