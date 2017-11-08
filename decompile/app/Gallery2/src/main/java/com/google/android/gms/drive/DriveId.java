package com.google.android.gms.drive;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.util.Base64;
import android.util.Log;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.drive.internal.y;
import com.google.android.gms.internal.er;
import com.google.android.gms.internal.ke;

/* compiled from: Unknown */
public class DriveId implements SafeParcelable {
    public static final Creator<DriveId> CREATOR = new d();
    final String Dc;
    final long Dd;
    final long De;
    private volatile String Df = null;
    final int wj;

    DriveId(int versionCode, String resourceId, long sqlId, long databaseInstanceId) {
        boolean z = false;
        this.wj = versionCode;
        this.Dc = resourceId;
        er.x(!"".equals(resourceId));
        if (resourceId == null) {
            if (sqlId != -1) {
            }
            er.x(z);
            this.Dd = sqlId;
            this.De = databaseInstanceId;
        }
        z = true;
        er.x(z);
        this.Dd = sqlId;
        this.De = databaseInstanceId;
    }

    public int describeContents() {
        return 0;
    }

    final byte[] eR() {
        ke yVar = new y();
        yVar.versionCode = this.wj;
        yVar.DV = this.Dc != null ? this.Dc : "";
        yVar.DW = this.Dd;
        yVar.DX = this.De;
        return ke.d(yVar);
    }

    public final String encodeToString() {
        if (this.Df == null) {
            this.Df = "DriveId:" + Base64.encodeToString(eR(), 10);
        }
        return this.Df;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof DriveId)) {
            return false;
        }
        DriveId driveId = (DriveId) obj;
        if (driveId.De != this.De) {
            Log.w("DriveId", "Attempt to compare invalid DriveId detected. Has local storage been cleared?");
            return false;
        } else if (driveId.Dd == -1 && this.Dd == -1) {
            return driveId.Dc.equals(this.Dc);
        } else {
            if (driveId.Dd == this.Dd) {
                z = true;
            }
            return z;
        }
    }

    public int hashCode() {
        return this.Dd == -1 ? this.Dc.hashCode() : (String.valueOf(this.De) + String.valueOf(this.Dd)).hashCode();
    }

    public String toString() {
        return encodeToString();
    }

    public void writeToParcel(Parcel out, int flags) {
        d.a(this, out, flags);
    }
}
