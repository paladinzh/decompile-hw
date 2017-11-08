package com.huawei.rcs.commonInterface.metadata;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.view.Surface;

public class ParcelableSurface implements Parcelable {
    public static final Creator<ParcelableSurface> CREATOR = new Creator<ParcelableSurface>() {
        public ParcelableSurface createFromParcel(Parcel arg0) {
            ParcelableSurface surface = new ParcelableSurface();
            surface.setRemoteSurface((Surface) arg0.readParcelable(Surface.class.getClassLoader()));
            return surface;
        }

        public ParcelableSurface[] newArray(int arg0) {
            return new ParcelableSurface[arg0];
        }
    };
    private Surface remoteSurface;

    public void setRemoteSurface(Surface remoteSurface) {
        this.remoteSurface = remoteSurface;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeParcelable(this.remoteSurface, 1);
    }
}
