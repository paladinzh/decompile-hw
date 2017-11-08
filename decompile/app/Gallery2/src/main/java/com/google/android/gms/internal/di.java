package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

/* compiled from: Unknown */
public class di implements Parcelable {
    @Deprecated
    public static final Creator<di> CREATOR = new Creator<di>() {
        public /* synthetic */ Object createFromParcel(Parcel x0) {
            return i(x0);
        }

        @Deprecated
        public di i(Parcel parcel) {
            return new di(parcel);
        }

        public /* synthetic */ Object[] newArray(int x0) {
            return u(x0);
        }

        @Deprecated
        public di[] u(int i) {
            return new di[i];
        }
    };
    private String mValue;
    private String uS;
    private String uT;

    @Deprecated
    di(Parcel parcel) {
        readFromParcel(parcel);
    }

    @Deprecated
    private void readFromParcel(Parcel in) {
        this.uS = in.readString();
        this.uT = in.readString();
        this.mValue = in.readString();
    }

    @Deprecated
    public int describeContents() {
        return 0;
    }

    @Deprecated
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.uS);
        out.writeString(this.uT);
        out.writeString(this.mValue);
    }
}
