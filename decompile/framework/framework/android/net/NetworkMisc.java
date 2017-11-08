package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class NetworkMisc implements Parcelable {
    public static final Creator<NetworkMisc> CREATOR = new Creator<NetworkMisc>() {
        public NetworkMisc createFromParcel(Parcel in) {
            boolean z;
            boolean z2 = true;
            NetworkMisc networkMisc = new NetworkMisc();
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            networkMisc.allowBypass = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            networkMisc.explicitlySelected = z;
            if (in.readInt() == 0) {
                z2 = false;
            }
            networkMisc.acceptUnvalidated = z2;
            networkMisc.subscriberId = in.readString();
            return networkMisc;
        }

        public NetworkMisc[] newArray(int size) {
            return new NetworkMisc[size];
        }
    };
    public boolean acceptUnvalidated;
    public boolean allowBypass;
    public boolean explicitlySelected;
    public String subscriberId;

    public NetworkMisc(NetworkMisc nm) {
        if (nm != null) {
            this.allowBypass = nm.allowBypass;
            this.explicitlySelected = nm.explicitlySelected;
            this.acceptUnvalidated = nm.acceptUnvalidated;
            this.subscriberId = nm.subscriberId;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = 1;
        if (this.allowBypass) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (this.explicitlySelected) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (!this.acceptUnvalidated) {
            i2 = 0;
        }
        out.writeInt(i2);
        out.writeString(this.subscriberId);
    }
}
