package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class OobData implements Parcelable {
    public static final Creator<OobData> CREATOR = new Creator<OobData>() {
        public OobData createFromParcel(Parcel in) {
            return new OobData(in);
        }

        public OobData[] newArray(int size) {
            return new OobData[size];
        }
    };
    private byte[] securityManagerTk;

    public byte[] getSecurityManagerTk() {
        return this.securityManagerTk;
    }

    public void setSecurityManagerTk(byte[] securityManagerTk) {
        this.securityManagerTk = securityManagerTk;
    }

    private OobData(Parcel in) {
        this.securityManagerTk = in.createByteArray();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByteArray(this.securityManagerTk);
    }
}
