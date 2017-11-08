package android.net.wifi.nan;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PublishSettings implements Parcelable {
    public static final Creator<PublishSettings> CREATOR = new Creator<PublishSettings>() {
        public PublishSettings[] newArray(int size) {
            return new PublishSettings[size];
        }

        public PublishSettings createFromParcel(Parcel in) {
            return new PublishSettings(in.readInt(), in.readInt(), in.readInt());
        }
    };
    public static final int PUBLISH_TYPE_SOLICITED = 1;
    public static final int PUBLISH_TYPE_UNSOLICITED = 0;
    public final int mPublishCount;
    public final int mPublishType;
    public final int mTtlSec;

    public static final class Builder {
        int mPublishCount;
        int mPublishType;
        int mTtlSec;

        public Builder setPublishType(int publishType) {
            if (publishType < 0 || publishType > 1) {
                throw new IllegalArgumentException("Invalid publishType - " + publishType);
            }
            this.mPublishType = publishType;
            return this;
        }

        public Builder setPublishCount(int publishCount) {
            if (publishCount < 0) {
                throw new IllegalArgumentException("Invalid publishCount - must be non-negative");
            }
            this.mPublishCount = publishCount;
            return this;
        }

        public Builder setTtlSec(int ttlSec) {
            if (ttlSec < 0) {
                throw new IllegalArgumentException("Invalid ttlSec - must be non-negative");
            }
            this.mTtlSec = ttlSec;
            return this;
        }

        public PublishSettings build() {
            return new PublishSettings(this.mPublishType, this.mPublishCount, this.mTtlSec);
        }
    }

    private PublishSettings(int publishType, int publichCount, int ttlSec) {
        this.mPublishType = publishType;
        this.mPublishCount = publichCount;
        this.mTtlSec = ttlSec;
    }

    public String toString() {
        return "PublishSettings [mPublishType=" + this.mPublishType + ", mPublishCount=" + this.mPublishCount + ", mTtlSec=" + this.mTtlSec + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPublishType);
        dest.writeInt(this.mPublishCount);
        dest.writeInt(this.mTtlSec);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof PublishSettings)) {
            return false;
        }
        PublishSettings lhs = (PublishSettings) o;
        if (this.mPublishType != lhs.mPublishType || this.mPublishCount != lhs.mPublishCount) {
            z = false;
        } else if (this.mTtlSec != lhs.mTtlSec) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((((this.mPublishType + 527) * 31) + this.mPublishCount) * 31) + this.mTtlSec;
    }
}
