package android.net.wifi.nan;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SubscribeSettings implements Parcelable {
    public static final Creator<SubscribeSettings> CREATOR = new Creator<SubscribeSettings>() {
        public SubscribeSettings[] newArray(int size) {
            return new SubscribeSettings[size];
        }

        public SubscribeSettings createFromParcel(Parcel in) {
            return new SubscribeSettings(in.readInt(), in.readInt(), in.readInt());
        }
    };
    public static final int SUBSCRIBE_TYPE_ACTIVE = 1;
    public static final int SUBSCRIBE_TYPE_PASSIVE = 0;
    public final int mSubscribeCount;
    public final int mSubscribeType;
    public final int mTtlSec;

    public static final class Builder {
        int mSubscribeCount;
        int mSubscribeType;
        int mTtlSec;

        public Builder setSubscribeType(int subscribeType) {
            if (subscribeType < 0 || subscribeType > 1) {
                throw new IllegalArgumentException("Invalid subscribeType - " + subscribeType);
            }
            this.mSubscribeType = subscribeType;
            return this;
        }

        public Builder setSubscribeCount(int subscribeCount) {
            if (subscribeCount < 0) {
                throw new IllegalArgumentException("Invalid subscribeCount - must be non-negative");
            }
            this.mSubscribeCount = subscribeCount;
            return this;
        }

        public Builder setTtlSec(int ttlSec) {
            if (ttlSec < 0) {
                throw new IllegalArgumentException("Invalid ttlSec - must be non-negative");
            }
            this.mTtlSec = ttlSec;
            return this;
        }

        public SubscribeSettings build() {
            return new SubscribeSettings(this.mSubscribeType, this.mSubscribeCount, this.mTtlSec);
        }
    }

    private SubscribeSettings(int subscribeType, int publichCount, int ttlSec) {
        this.mSubscribeType = subscribeType;
        this.mSubscribeCount = publichCount;
        this.mTtlSec = ttlSec;
    }

    public String toString() {
        return "SubscribeSettings [mSubscribeType=" + this.mSubscribeType + ", mSubscribeCount=" + this.mSubscribeCount + ", mTtlSec=" + this.mTtlSec + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSubscribeType);
        dest.writeInt(this.mSubscribeCount);
        dest.writeInt(this.mTtlSec);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubscribeSettings)) {
            return false;
        }
        SubscribeSettings lhs = (SubscribeSettings) o;
        if (this.mSubscribeType != lhs.mSubscribeType || this.mSubscribeCount != lhs.mSubscribeCount) {
            z = false;
        } else if (this.mTtlSec != lhs.mTtlSec) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((((this.mSubscribeType + 527) * 31) + this.mSubscribeCount) * 31) + this.mTtlSec;
    }
}
