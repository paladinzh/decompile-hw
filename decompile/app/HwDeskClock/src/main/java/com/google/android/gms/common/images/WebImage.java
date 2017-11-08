package com.google.android.gms.common.images;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;

/* compiled from: Unknown */
public final class WebImage implements SafeParcelable {
    public static final Creator<WebImage> CREATOR = new zzb();
    private final int mVersionCode;
    private final Uri zzacc;
    private final int zznP;
    private final int zznQ;

    WebImage(int versionCode, Uri url, int width, int height) {
        this.mVersionCode = versionCode;
        this.zzacc = url;
        this.zznP = width;
        this.zznQ = height;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (other == null || !(other instanceof WebImage)) {
            return false;
        }
        WebImage webImage = (WebImage) other;
        if (zzw.equal(this.zzacc, webImage.zzacc) && this.zznP == webImage.zznP) {
            if (this.zznQ != webImage.zznQ) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int getHeight() {
        return this.zznQ;
    }

    public Uri getUrl() {
        return this.zzacc;
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public int getWidth() {
        return this.zznP;
    }

    public int hashCode() {
        return zzw.hashCode(this.zzacc, Integer.valueOf(this.zznP), Integer.valueOf(this.zznQ));
    }

    public String toString() {
        return String.format("Image %dx%d %s", new Object[]{Integer.valueOf(this.zznP), Integer.valueOf(this.zznQ), this.zzacc.toString()});
    }

    public void writeToParcel(Parcel out, int flags) {
        zzb.zza(this, out, flags);
    }
}
