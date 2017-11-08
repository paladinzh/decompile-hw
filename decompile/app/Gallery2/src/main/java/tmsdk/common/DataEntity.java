package tmsdk.common;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

/* compiled from: Unknown */
public final class DataEntity implements Parcelable {
    public static final Creator<DataEntity> CREATOR = new Creator<DataEntity>() {
        public DataEntity[] bv(int i) {
            return new DataEntity[i];
        }

        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return e(parcel);
        }

        public DataEntity e(Parcel parcel) {
            return new DataEntity(parcel);
        }

        public /* synthetic */ Object[] newArray(int i) {
            return bv(i);
        }
    };
    private Bundle zL;
    private int zM;

    public DataEntity(int i) {
        this.zM = i;
        this.zL = new Bundle();
    }

    private DataEntity(Parcel parcel) {
        this.zM = parcel.readInt();
        this.zL = parcel.readBundle();
    }

    public Bundle bundle() {
        return this.zL;
    }

    public int describeContents() {
        return 0;
    }

    public int what() {
        return this.zM;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.zM);
        parcel.writeBundle(this.zL);
    }
}
