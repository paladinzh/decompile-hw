package com.google.android.gms.common.images;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.ep;

/* compiled from: Unknown */
public final class WebImage implements SafeParcelable {
    public static final Creator<WebImage> CREATOR = new b();
    private final Uri AR;
    private final int v;
    private final int w;
    private final int wj;

    WebImage(int versionCode, Uri url, int width, int height) {
        this.wj = versionCode;
        this.AR = url;
        this.w = width;
        this.v = height;
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
        if (ep.equal(this.AR, webImage.AR) && this.w == webImage.w) {
            if (this.v != webImage.v) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int getHeight() {
        return this.v;
    }

    public Uri getUrl() {
        return this.AR;
    }

    int getVersionCode() {
        return this.wj;
    }

    public int getWidth() {
        return this.w;
    }

    public int hashCode() {
        return ep.hashCode(this.AR, Integer.valueOf(this.w), Integer.valueOf(this.v));
    }

    public String toString() {
        return String.format("Image %dx%d %s", new Object[]{Integer.valueOf(this.w), Integer.valueOf(this.v), this.AR.toString()});
    }

    public void writeToParcel(Parcel out, int flags) {
        b.a(this, out, flags);
    }
}
