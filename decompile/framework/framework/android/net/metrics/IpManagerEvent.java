package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;

public final class IpManagerEvent extends IpConnectivityEvent implements Parcelable {
    public static final int COMPLETE_LIFECYCLE = 3;
    public static final Creator<IpManagerEvent> CREATOR = new Creator<IpManagerEvent>() {
        public IpManagerEvent createFromParcel(Parcel in) {
            return new IpManagerEvent(in);
        }

        public IpManagerEvent[] newArray(int size) {
            return new IpManagerEvent[size];
        }
    };
    public static final int PROVISIONING_FAIL = 2;
    public static final int PROVISIONING_OK = 1;
    public final long durationMs;
    public final int eventType;
    public final String ifName;

    static final class Decoder {
        static final SparseArray<String> constants = MessageUtils.findMessageNames(new Class[]{IpManagerEvent.class}, new String[]{"PROVISIONING_", "COMPLETE_"});

        Decoder() {
        }
    }

    private IpManagerEvent(String ifName, int eventType, long duration) {
        this.ifName = ifName;
        this.eventType = eventType;
        this.durationMs = duration;
    }

    private IpManagerEvent(Parcel in) {
        this.ifName = in.readString();
        this.eventType = in.readInt();
        this.durationMs = in.readLong();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.ifName);
        out.writeInt(this.eventType);
        out.writeLong(this.durationMs);
    }

    public int describeContents() {
        return 0;
    }

    public static void logEvent(int eventType, String ifName, long durationMs) {
        IpConnectivityEvent.logEvent(new IpManagerEvent(ifName, eventType, durationMs));
    }

    public String toString() {
        return String.format("IpManagerEvent(%s, %s, %dms)", new Object[]{this.ifName, Decoder.constants.get(this.eventType), Long.valueOf(this.durationMs)});
    }
}
