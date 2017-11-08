package com.google.android.gms.drive.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ConflictEvent;

/* compiled from: Unknown */
public class OnEventResponse implements SafeParcelable {
    public static final Creator<OnEventResponse> CREATOR = new ac();
    final int Dm;
    final ChangeEvent Eb;
    final ConflictEvent Ec;
    final int wj;

    OnEventResponse(int versionCode, int eventType, ChangeEvent changeEvent, ConflictEvent conflictEvent) {
        this.wj = versionCode;
        this.Dm = eventType;
        this.Eb = changeEvent;
        this.Ec = conflictEvent;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        ac.a(this, dest, flags);
    }
}
