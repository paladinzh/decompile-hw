package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class ConnectivityMetricsEvent implements Parcelable {
    public static final Creator<ConnectivityMetricsEvent> CREATOR = new Creator<ConnectivityMetricsEvent>() {
        public ConnectivityMetricsEvent createFromParcel(Parcel source) {
            return new ConnectivityMetricsEvent(source.readLong(), source.readInt(), source.readInt(), source.readParcelable(null));
        }

        public ConnectivityMetricsEvent[] newArray(int size) {
            return new ConnectivityMetricsEvent[size];
        }
    };
    public final int componentTag;
    public final Parcelable data;
    public final int eventTag;
    public final long timestamp;

    public static final class Reference implements Parcelable {
        public static final Creator<Reference> CREATOR = new Creator<Reference>() {
            public Reference createFromParcel(Parcel source) {
                return new Reference(source.readLong());
            }

            public Reference[] newArray(int size) {
                return new Reference[size];
            }
        };
        private long mValue;

        public Reference(long ref) {
            this.mValue = ref;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.mValue);
        }

        public void readFromParcel(Parcel in) {
            this.mValue = in.readLong();
        }

        public long getValue() {
            return this.mValue;
        }

        public void setValue(long val) {
            this.mValue = val;
        }
    }

    public ConnectivityMetricsEvent(long timestamp, int componentTag, int eventTag, Parcelable data) {
        this.timestamp = timestamp;
        this.componentTag = componentTag;
        this.eventTag = eventTag;
        this.data = data;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.timestamp);
        dest.writeInt(this.componentTag);
        dest.writeInt(this.eventTag);
        dest.writeParcelable(this.data, 0);
    }

    public String toString() {
        return String.format("ConnectivityMetricsEvent(%tT.%tL, %d, %d): %s", new Object[]{Long.valueOf(this.timestamp), Long.valueOf(this.timestamp), Integer.valueOf(this.componentTag), Integer.valueOf(this.eventTag), this.data});
    }
}
