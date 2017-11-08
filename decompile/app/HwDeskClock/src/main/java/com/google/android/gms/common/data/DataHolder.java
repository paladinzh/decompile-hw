package com.google.android.gms.common.data;

import android.database.CursorIndexOutOfBoundsException;
import android.database.CursorWindow;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzx;
import java.util.ArrayList;
import java.util.HashMap;

/* compiled from: Unknown */
public final class DataHolder implements SafeParcelable {
    public static final zze CREATOR = new zze();
    private static final zza zzabt = new zza(new String[0], null) {
    };
    boolean mClosed = false;
    private final int mVersionCode;
    private final int zzWu;
    private final String[] zzabl;
    Bundle zzabm;
    private final CursorWindow[] zzabn;
    private final Bundle zzabo;
    int[] zzabp;
    int zzabq;
    private Object zzabr;
    private boolean zzabs = true;

    /* compiled from: Unknown */
    public static class zza {
        private final String[] zzabl;
        private final ArrayList<HashMap<String, Object>> zzabu;
        private final String zzabv;
        private final HashMap<Object, Integer> zzabw;
        private boolean zzabx;
        private String zzaby;

        private zza(String[] strArr, String str) {
            this.zzabl = (String[]) zzx.zzv(strArr);
            this.zzabu = new ArrayList();
            this.zzabv = str;
            this.zzabw = new HashMap();
            this.zzabx = false;
            this.zzaby = null;
        }
    }

    /* compiled from: Unknown */
    public static class zzb extends RuntimeException {
        public zzb(String str) {
            super(str);
        }
    }

    DataHolder(int versionCode, String[] columns, CursorWindow[] windows, int statusCode, Bundle metadata) {
        this.mVersionCode = versionCode;
        this.zzabl = columns;
        this.zzabn = windows;
        this.zzWu = statusCode;
        this.zzabo = metadata;
    }

    private void zzh(String str, int i) {
        if (this.zzabm == null || !this.zzabm.containsKey(str)) {
            throw new IllegalArgumentException("No such column: " + str);
        } else if (isClosed()) {
            throw new IllegalArgumentException("Buffer is closed.");
        } else if (i < 0 || i >= this.zzabq) {
            throw new CursorIndexOutOfBoundsException(i, this.zzabq);
        }
    }

    public void close() {
        synchronized (this) {
            if (!this.mClosed) {
                this.mClosed = true;
                for (CursorWindow close : this.zzabn) {
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
            if (this.zzabs) {
                if (this.zzabn.length > 0 && !isClosed()) {
                    Log.e("DataBuffer", "Internal data leak within a DataBuffer object detected!  Be sure to explicitly call release() on all DataBuffer extending objects when you are done with them. (" + (this.zzabr != null ? this.zzabr.toString() : "internal object: " + toString()) + ")");
                    close();
                }
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public int getCount() {
        return this.zzabq;
    }

    public int getStatusCode() {
        return this.zzWu;
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

    public int zzbo(int i) {
        int i2 = 0;
        boolean z = i >= 0 && i < this.zzabq;
        zzx.zzY(z);
        while (i2 < this.zzabp.length) {
            if (i < this.zzabp[i2]) {
                i2--;
                break;
            }
            i2++;
        }
        return i2 != this.zzabp.length ? i2 : i2 - 1;
    }

    public int zzc(String str, int i, int i2) {
        zzh(str, i);
        return this.zzabn[i2].getInt(i, this.zzabm.getInt(str));
    }

    public String zzd(String str, int i, int i2) {
        zzh(str, i);
        return this.zzabn[i2].getString(i, this.zzabm.getInt(str));
    }

    public byte[] zzg(String str, int i, int i2) {
        zzh(str, i);
        return this.zzabn[i2].getBlob(i, this.zzabm.getInt(str));
    }

    public Bundle zznQ() {
        return this.zzabo;
    }

    public void zznU() {
        int i;
        int i2 = 0;
        this.zzabm = new Bundle();
        for (i = 0; i < this.zzabl.length; i++) {
            this.zzabm.putInt(this.zzabl[i], i);
        }
        this.zzabp = new int[this.zzabn.length];
        i = 0;
        while (i2 < this.zzabn.length) {
            this.zzabp[i2] = i;
            i += this.zzabn[i2].getNumRows() - (i - this.zzabn[i2].getStartPosition());
            i2++;
        }
        this.zzabq = i;
    }

    String[] zznV() {
        return this.zzabl;
    }

    CursorWindow[] zznW() {
        return this.zzabn;
    }

    public void zzq(Object obj) {
        this.zzabr = obj;
    }
}
