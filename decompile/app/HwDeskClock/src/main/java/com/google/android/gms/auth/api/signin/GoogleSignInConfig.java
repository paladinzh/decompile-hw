package com.google.android.gms.auth.api.signin;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.ArrayList;

/* compiled from: Unknown */
public class GoogleSignInConfig implements SafeParcelable {
    public static final Creator<GoogleSignInConfig> CREATOR = new zzc();
    final int versionCode;
    private final ArrayList<Scope> zzRR;

    public GoogleSignInConfig() {
        this(1, new ArrayList());
    }

    GoogleSignInConfig(int versionCode, ArrayList<Scope> scopes) {
        this.versionCode = versionCode;
        this.zzRR = scopes;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzc.zza(this, out, flags);
    }

    public ArrayList<Scope> zzlE() {
        return new ArrayList(this.zzRR);
    }
}
