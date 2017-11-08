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
    private final HashMap<String, Integer> zzaeN;
    private final HashMap<Integer, String> zzaeO;
    private final ArrayList<Entry> zzaeP;

    /* compiled from: Unknown */
    public static final class Entry implements SafeParcelable {
        public static final zzc CREATOR = new zzc();
        final int versionCode;
        final String zzaeQ;
        final int zzaeR;

        Entry(int versionCode, String stringValue, int intValue) {
            this.versionCode = versionCode;
            this.zzaeQ = stringValue;
            this.zzaeR = intValue;
        }

        Entry(String stringValue, int intValue) {
            this.versionCode = 1;
            this.zzaeQ = stringValue;
            this.zzaeR = intValue;
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
        this.zzaeN = new HashMap();
        this.zzaeO = new HashMap();
        this.zzaeP = null;
    }

    StringToIntConverter(int versionCode, ArrayList<Entry> serializedMap) {
        this.mVersionCode = versionCode;
        this.zzaeN = new HashMap();
        this.zzaeO = new HashMap();
        this.zzaeP = null;
        zzb((ArrayList) serializedMap);
    }

    private void zzb(ArrayList<Entry> arrayList) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            zzi(entry.zzaeQ, entry.zzaeR);
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
        String str = (String) this.zzaeO.get(num);
        return (str == null && this.zzaeN.containsKey("gms_unknown")) ? "gms_unknown" : str;
    }

    public StringToIntConverter zzi(String str, int i) {
        this.zzaeN.put(str, Integer.valueOf(i));
        this.zzaeO.put(Integer.valueOf(i), str);
        return this;
    }

    ArrayList<Entry> zzpa() {
        ArrayList<Entry> arrayList = new ArrayList();
        for (String str : this.zzaeN.keySet()) {
            arrayList.add(new Entry(str, ((Integer) this.zzaeN.get(str)).intValue()));
        }
        return arrayList;
    }
}
