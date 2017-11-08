package com.google.android.gms.cast;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.images.WebImage;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public final class ApplicationMetadata implements SafeParcelable {
    public static final Creator<ApplicationMetadata> CREATOR = new a();
    String mName;
    private final int wj;
    String wk;
    List<WebImage> wl;
    List<String> wm;
    String wn;
    Uri wo;

    private ApplicationMetadata() {
        this.wj = 1;
        this.wl = new ArrayList();
        this.wm = new ArrayList();
    }

    ApplicationMetadata(int versionCode, String applicationId, String name, List<WebImage> images, List<String> namespaces, String senderAppIdentifier, Uri senderAppLaunchUrl) {
        this.wj = versionCode;
        this.wk = applicationId;
        this.mName = name;
        this.wl = images;
        this.wm = namespaces;
        this.wn = senderAppIdentifier;
        this.wo = senderAppLaunchUrl;
    }

    public Uri cR() {
        return this.wo;
    }

    public int describeContents() {
        return 0;
    }

    public String getApplicationId() {
        return this.wk;
    }

    public List<WebImage> getImages() {
        return this.wl;
    }

    public String getName() {
        return this.mName;
    }

    public String getSenderAppIdentifier() {
        return this.wn;
    }

    int getVersionCode() {
        return this.wj;
    }

    public String toString() {
        return this.mName;
    }

    public void writeToParcel(Parcel out, int flags) {
        a.a(this, out, flags);
    }
}
