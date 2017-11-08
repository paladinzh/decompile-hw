package com.google.android.gms.common.data;

import android.database.CharArrayBuffer;
import android.database.CursorIndexOutOfBoundsException;
import android.database.CursorWindow;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import com.google.android.gms.common.annotation.KeepName;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@KeepName
/* compiled from: Unknown */
public final class DataHolder implements SafeParcelable {
    public static final zze CREATOR = new zze();
    private static final zza zzajq = new zza(new String[0], null) {
    };
    boolean mClosed;
    private final int mVersionCode;
    private final int zzade;
    private final String[] zzaji;
    Bundle zzajj;
    private final CursorWindow[] zzajk;
    private final Bundle zzajl;
    int[] zzajm;
    int zzajn;
    private Object zzajo;
    private boolean zzajp;

    /* compiled from: Unknown */
    public static class zza {
        private final String[] zzaji;
        private final ArrayList<HashMap<String, Object>> zzajr;
        private final String zzajs;
        private final HashMap<Object, Integer> zzajt;
        private boolean zzaju;
        private String zzajv;

        private zza(String[] strArr, String str) {
            this.zzaji = (String[]) zzx.zzz(strArr);
            this.zzajr = new ArrayList();
            this.zzajs = str;
            this.zzajt = new HashMap();
            this.zzaju = false;
            this.zzajv = null;
        }
    }

    /* compiled from: Unknown */
    public static class zzb extends RuntimeException {
        public zzb(String str) {
            super(str);
        }
    }

    DataHolder(int versionCode, String[] columns, CursorWindow[] windows, int statusCode, Bundle metadata) {
        this.mClosed = false;
        this.zzajp = true;
        this.mVersionCode = versionCode;
        this.zzaji = columns;
        this.zzajk = windows;
        this.zzade = statusCode;
        this.zzajl = metadata;
    }

    private DataHolder(zza builder, int statusCode, Bundle metadata) {
        this(builder.zzaji, zza(builder, -1), statusCode, metadata);
    }

    public DataHolder(String[] columns, CursorWindow[] windows, int statusCode, Bundle metadata) {
        this.mClosed = false;
        this.zzajp = true;
        this.mVersionCode = 1;
        this.zzaji = (String[]) zzx.zzz(columns);
        this.zzajk = (CursorWindow[]) zzx.zzz(windows);
        this.zzade = statusCode;
        this.zzajl = metadata;
        zzqd();
    }

    public static DataHolder zza(int i, Bundle bundle) {
        return new DataHolder(zzajq, i, bundle);
    }

    private static CursorWindow[] zza(zza zza, int i) {
        int i2 = 0;
        if (zza.zzaji.length == 0) {
            return new CursorWindow[0];
        }
        ArrayList subList = (i >= 0 && i < zza.zzajr.size()) ? zza.zzajr.subList(0, i) : zza.zzajr;
        ArrayList arrayList = subList;
        int size = arrayList.size();
        CursorWindow cursorWindow = new CursorWindow(false);
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(cursorWindow);
        cursorWindow.setNumColumns(zza.zzaji.length);
        int i3 = 0;
        int i4 = 0;
        while (i3 < size) {
            try {
                int i5;
                int i6;
                if (!cursorWindow.allocRow()) {
                    Log.d("DataHolder", "Allocating additional cursor window for large data set (row " + i3 + ")");
                    cursorWindow = new CursorWindow(false);
                    cursorWindow.setStartPosition(i3);
                    cursorWindow.setNumColumns(zza.zzaji.length);
                    arrayList2.add(cursorWindow);
                    if (!cursorWindow.allocRow()) {
                        Log.e("DataHolder", "Unable to allocate row to hold data.");
                        arrayList2.remove(cursorWindow);
                        return (CursorWindow[]) arrayList2.toArray(new CursorWindow[arrayList2.size()]);
                    }
                }
                CursorWindow cursorWindow2 = cursorWindow;
                Map map = (Map) arrayList.get(i3);
                boolean z = true;
                for (int i7 = 0; i7 < zza.zzaji.length && z; i7++) {
                    String str = zza.zzaji[i7];
                    Object obj = map.get(str);
                    if (obj == null) {
                        z = cursorWindow2.putNull(i3, i7);
                    } else if (obj instanceof String) {
                        z = cursorWindow2.putString((String) obj, i3, i7);
                    } else if (obj instanceof Long) {
                        z = cursorWindow2.putLong(((Long) obj).longValue(), i3, i7);
                    } else if (obj instanceof Integer) {
                        z = cursorWindow2.putLong((long) ((Integer) obj).intValue(), i3, i7);
                    } else if (obj instanceof Boolean) {
                        z = cursorWindow2.putLong(!((Boolean) obj).booleanValue() ? 0 : 1, i3, i7);
                    } else if (obj instanceof byte[]) {
                        z = cursorWindow2.putBlob((byte[]) obj, i3, i7);
                    } else if (obj instanceof Double) {
                        z = cursorWindow2.putDouble(((Double) obj).doubleValue(), i3, i7);
                    } else if (obj instanceof Float) {
                        z = cursorWindow2.putDouble((double) ((Float) obj).floatValue(), i3, i7);
                    } else {
                        throw new IllegalArgumentException("Unsupported object for column " + str + ": " + obj);
                    }
                }
                if (z) {
                    i5 = i3;
                    i6 = 0;
                } else if (i4 == 0) {
                    Log.d("DataHolder", "Couldn't populate window data for row " + i3 + " - allocating new window.");
                    cursorWindow2.freeLastRow();
                    cursorWindow2 = new CursorWindow(false);
                    cursorWindow2.setStartPosition(i3);
                    cursorWindow2.setNumColumns(zza.zzaji.length);
                    arrayList2.add(cursorWindow2);
                    i5 = i3 - 1;
                    i6 = 1;
                } else {
                    throw new zzb("Could not add the value to a new CursorWindow. The size of value may be larger than what a CursorWindow can handle.");
                }
                i3 = i5 + 1;
                i4 = i6;
                cursorWindow = cursorWindow2;
            } catch (RuntimeException e) {
                RuntimeException runtimeException = e;
                int size2 = arrayList2.size();
                while (i2 < size2) {
                    ((CursorWindow) arrayList2.get(i2)).close();
                    i2++;
                }
                throw runtimeException;
            }
        }
        return (CursorWindow[]) arrayList2.toArray(new CursorWindow[arrayList2.size()]);
    }

    public static DataHolder zzbI(int i) {
        return zza(i, null);
    }

    private void zzg(String str, int i) {
        if (this.zzajj == null || !this.zzajj.containsKey(str)) {
            throw new IllegalArgumentException("No such column: " + str);
        } else if (isClosed()) {
            throw new IllegalArgumentException("Buffer is closed.");
        } else if (i < 0 || i >= this.zzajn) {
            throw new CursorIndexOutOfBoundsException(i, this.zzajn);
        }
    }

    public void close() {
        synchronized (this) {
            if (!this.mClosed) {
                this.mClosed = true;
                for (CursorWindow close : this.zzajk) {
                    close.close();
                }
            }
        }
    }

    public int describeContents() {
        return 0;
    }

    protected void finalize() throws Throwable {
        try {
            if (this.zzajp) {
                if (this.zzajk.length > 0 && !isClosed()) {
                    Log.e("DataBuffer", "Internal data leak within a DataBuffer object detected!  Be sure to explicitly call release() on all DataBuffer extending objects when you are done with them. (" + (this.zzajo != null ? this.zzajo.toString() : "internal object: " + toString()) + ")");
                    close();
                }
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public int getCount() {
        return this.zzajn;
    }

    public int getStatusCode() {
        return this.zzade;
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public boolean isClosed() {
        boolean z;
        synchronized (this) {
            z = this.mClosed;
        }
        return z;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zze.zza(this, dest, flags);
    }

    public void zza(String str, int i, int i2, CharArrayBuffer charArrayBuffer) {
        zzg(str, i);
        this.zzajk[i2].copyStringToBuffer(i, this.zzajj.getInt(str), charArrayBuffer);
    }

    public long zzb(String str, int i, int i2) {
        zzg(str, i);
        return this.zzajk[i2].getLong(i, this.zzajj.getInt(str));
    }

    public int zzbH(int i) {
        int i2 = 0;
        boolean z = i >= 0 && i < this.zzajn;
        zzx.zzab(z);
        while (i2 < this.zzajm.length) {
            if (i < this.zzajm[i2]) {
                i2--;
                break;
            }
            i2++;
        }
        return i2 != this.zzajm.length ? i2 : i2 - 1;
    }

    public int zzc(String str, int i, int i2) {
        zzg(str, i);
        return this.zzajk[i2].getInt(i, this.zzajj.getInt(str));
    }

    public boolean zzcz(String str) {
        return this.zzajj.containsKey(str);
    }

    public String zzd(String str, int i, int i2) {
        zzg(str, i);
        return this.zzajk[i2].getString(i, this.zzajj.getInt(str));
    }

    public boolean zze(String str, int i, int i2) {
        zzg(str, i);
        return Long.valueOf(this.zzajk[i2].getLong(i, this.zzajj.getInt(str))).longValue() == 1;
    }

    public float zzf(String str, int i, int i2) {
        zzg(str, i);
        return this.zzajk[i2].getFloat(i, this.zzajj.getInt(str));
    }

    public byte[] zzg(String str, int i, int i2) {
        zzg(str, i);
        return this.zzajk[i2].getBlob(i, this.zzajj.getInt(str));
    }

    public Uri zzh(String str, int i, int i2) {
        String zzd = zzd(str, i, i2);
        return zzd != null ? Uri.parse(zzd) : null;
    }

    public boolean zzi(String str, int i, int i2) {
        zzg(str, i);
        return this.zzajk[i2].isNull(i, this.zzajj.getInt(str));
    }

    public Bundle zzpZ() {
        return this.zzajl;
    }

    public void zzqd() {
        int i;
        int i2 = 0;
        this.zzajj = new Bundle();
        for (i = 0; i < this.zzaji.length; i++) {
            this.zzajj.putInt(this.zzaji[i], i);
        }
        this.zzajm = new int[this.zzajk.length];
        i = 0;
        while (i2 < this.zzajk.length) {
            this.zzajm[i2] = i;
            i += this.zzajk[i2].getNumRows() - (i - this.zzajk[i2].getStartPosition());
            i2++;
        }
        this.zzajn = i;
    }

    String[] zzqe() {
        return this.zzaji;
    }

    CursorWindow[] zzqf() {
        return this.zzajk;
    }

    public void zzu(Object obj) {
        this.zzajo = obj;
    }
}
