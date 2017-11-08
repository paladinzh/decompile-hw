package com.google.android.gms.common.data;

import android.database.CursorIndexOutOfBoundsException;
import android.database.CursorWindow;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.er;
import java.util.ArrayList;
import java.util.HashMap;

/* compiled from: Unknown */
public final class DataHolder implements SafeParcelable {
    private static final Builder Ai = new Builder(new String[0], null) {
    };
    public static final DataHolderCreator CREATOR = new DataHolderCreator();
    private final String[] Aa;
    Bundle Ab;
    private final CursorWindow[] Ac;
    private final Bundle Ad;
    int[] Ae;
    int Af;
    private Object Ag;
    private boolean Ah = true;
    boolean mClosed = false;
    private final int wj;
    private final int yJ;

    /* compiled from: Unknown */
    public static class Builder {
        private final String[] Aa;
        private final ArrayList<HashMap<String, Object>> Aj;
        private final String Ak;
        private final HashMap<Object, Integer> Al;
        private boolean Am;
        private String An;

        private Builder(String[] columns, String uniqueColumn) {
            this.Aa = (String[]) er.f(columns);
            this.Aj = new ArrayList();
            this.Ak = uniqueColumn;
            this.Al = new HashMap();
            this.Am = false;
            this.An = null;
        }
    }

    DataHolder(int versionCode, String[] columns, CursorWindow[] windows, int statusCode, Bundle metadata) {
        this.wj = versionCode;
        this.Aa = columns;
        this.Ac = windows;
        this.yJ = statusCode;
        this.Ad = metadata;
    }

    private void e(String str, int i) {
        if (this.Ab == null || !this.Ab.containsKey(str)) {
            throw new IllegalArgumentException("No such column: " + str);
        } else if (isClosed()) {
            throw new IllegalArgumentException("Buffer is closed.");
        } else if (i < 0 || i >= this.Af) {
            throw new CursorIndexOutOfBoundsException(i, this.Af);
        }
    }

    public int I(int i) {
        int i2 = 0;
        boolean z = i >= 0 && i < this.Af;
        er.v(z);
        while (i2 < this.Ae.length) {
            if (i < this.Ae[i2]) {
                i2--;
                break;
            }
            i2++;
        }
        return i2 != this.Ae.length ? i2 : i2 - 1;
    }

    public void close() {
        synchronized (this) {
            if (!this.mClosed) {
                this.mClosed = true;
                for (CursorWindow close : this.Ac) {
                    close.close();
                }
            }
        }
    }

    String[] dH() {
        return this.Aa;
    }

    CursorWindow[] dI() {
        return this.Ac;
    }

    public int describeContents() {
        return 0;
    }

    protected void finalize() throws Throwable {
        try {
            if (this.Ah) {
                if (this.Ac.length > 0 && !isClosed()) {
                    Log.e("DataBuffer", "Internal data leak within a DataBuffer object detected!  Be sure to explicitly call close() on all DataBuffer extending objects when you are done with them. (" + (this.Ag != null ? this.Ag.toString() : "internal object: " + toString()) + ")");
                    close();
                }
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public boolean getBoolean(String column, int row, int windowIndex) {
        e(column, row);
        return Long.valueOf(this.Ac[windowIndex].getLong(row, this.Ab.getInt(column))).longValue() == 1;
    }

    public byte[] getByteArray(String column, int row, int windowIndex) {
        e(column, row);
        return this.Ac[windowIndex].getBlob(row, this.Ab.getInt(column));
    }

    public int getCount() {
        return this.Af;
    }

    public int getInteger(String column, int row, int windowIndex) {
        e(column, row);
        return this.Ac[windowIndex].getInt(row, this.Ab.getInt(column));
    }

    public long getLong(String column, int row, int windowIndex) {
        e(column, row);
        return this.Ac[windowIndex].getLong(row, this.Ab.getInt(column));
    }

    public Bundle getMetadata() {
        return this.Ad;
    }

    public int getStatusCode() {
        return this.yJ;
    }

    public String getString(String column, int row, int windowIndex) {
        e(column, row);
        return this.Ac[windowIndex].getString(row, this.Ab.getInt(column));
    }

    int getVersionCode() {
        return this.wj;
    }

    public boolean hasColumn(String column) {
        return this.Ab.containsKey(column);
    }

    public boolean hasNull(String column, int row, int windowIndex) {
        e(column, row);
        return this.Ac[windowIndex].isNull(row, this.Ab.getInt(column));
    }

    public boolean isClosed() {
        boolean z;
        synchronized (this) {
            z = this.mClosed;
        }
        return z;
    }

    public Uri parseUri(String column, int row, int windowIndex) {
        String string = getString(column, row, windowIndex);
        return string != null ? Uri.parse(string) : null;
    }

    public void validateContents() {
        int i;
        int i2 = 0;
        this.Ab = new Bundle();
        for (i = 0; i < this.Aa.length; i++) {
            this.Ab.putInt(this.Aa[i], i);
        }
        this.Ae = new int[this.Ac.length];
        i = 0;
        while (i2 < this.Ac.length) {
            this.Ae[i2] = i;
            i += this.Ac[i2].getNumRows() - (i - this.Ac[i2].getStartPosition());
            i2++;
        }
        this.Af = i;
    }

    public void writeToParcel(Parcel dest, int flags) {
        DataHolderCreator.a(this, dest, flags);
    }
}
