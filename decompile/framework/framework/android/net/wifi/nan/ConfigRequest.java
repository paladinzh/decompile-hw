package android.net.wifi.nan;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ConfigRequest implements Parcelable {
    public static final int CLUSTER_ID_MAX = 65535;
    public static final int CLUSTER_ID_MIN = 0;
    public static final Creator<ConfigRequest> CREATOR = new Creator<ConfigRequest>() {
        public ConfigRequest[] newArray(int size) {
            return new ConfigRequest[size];
        }

        public ConfigRequest createFromParcel(Parcel in) {
            return new ConfigRequest(in.readInt() != 0, in.readInt(), in.readInt(), in.readInt());
        }
    };
    public final int mClusterHigh;
    public final int mClusterLow;
    public final int mMasterPreference;
    public final boolean mSupport5gBand;

    public static final class Builder {
        private int mClusterHigh = 65535;
        private int mClusterLow = 0;
        private int mMasterPreference = 0;
        private boolean mSupport5gBand = false;

        public Builder setSupport5gBand(boolean support5gBand) {
            this.mSupport5gBand = support5gBand;
            return this;
        }

        public Builder setMasterPreference(int masterPreference) {
            if (masterPreference < 0) {
                throw new IllegalArgumentException("Master Preference specification must be non-negative");
            } else if (masterPreference == 1 || masterPreference == 255 || masterPreference > 255) {
                throw new IllegalArgumentException("Master Preference specification must not exceed 255 or use 1 or 255 (reserved values)");
            } else {
                this.mMasterPreference = masterPreference;
                return this;
            }
        }

        public Builder setClusterLow(int clusterLow) {
            if (clusterLow < 0) {
                throw new IllegalArgumentException("Cluster specification must be non-negative");
            } else if (clusterLow > 65535) {
                throw new IllegalArgumentException("Cluster specification must not exceed 0xFFFF");
            } else {
                this.mClusterLow = clusterLow;
                return this;
            }
        }

        public Builder setClusterHigh(int clusterHigh) {
            if (clusterHigh < 0) {
                throw new IllegalArgumentException("Cluster specification must be non-negative");
            } else if (clusterHigh > 65535) {
                throw new IllegalArgumentException("Cluster specification must not exceed 0xFFFF");
            } else {
                this.mClusterHigh = clusterHigh;
                return this;
            }
        }

        public ConfigRequest build() {
            if (this.mClusterLow <= this.mClusterHigh) {
                return new ConfigRequest(this.mSupport5gBand, this.mMasterPreference, this.mClusterLow, this.mClusterHigh);
            }
            throw new IllegalArgumentException("Invalid argument combination - must have Cluster Low <= Cluster High");
        }
    }

    private ConfigRequest(boolean support5gBand, int masterPreference, int clusterLow, int clusterHigh) {
        this.mSupport5gBand = support5gBand;
        this.mMasterPreference = masterPreference;
        this.mClusterLow = clusterLow;
        this.mClusterHigh = clusterHigh;
    }

    public String toString() {
        return "ConfigRequest [mSupport5gBand=" + this.mSupport5gBand + ", mMasterPreference=" + this.mMasterPreference + ", mClusterLow=" + this.mClusterLow + ", mClusterHigh=" + this.mClusterHigh + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSupport5gBand ? 1 : 0);
        dest.writeInt(this.mMasterPreference);
        dest.writeInt(this.mClusterLow);
        dest.writeInt(this.mClusterHigh);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigRequest)) {
            return false;
        }
        ConfigRequest lhs = (ConfigRequest) o;
        if (this.mSupport5gBand != lhs.mSupport5gBand || this.mMasterPreference != lhs.mMasterPreference || this.mClusterLow != lhs.mClusterLow) {
            z = false;
        } else if (this.mClusterHigh != lhs.mClusterHigh) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (((((((this.mSupport5gBand ? 1 : 0) + 527) * 31) + this.mMasterPreference) * 31) + this.mClusterLow) * 31) + this.mClusterHigh;
    }
}
