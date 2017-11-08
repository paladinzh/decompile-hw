package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;

public final class IpReachabilityEvent extends IpConnectivityEvent implements Parcelable {
    public static final Creator<IpReachabilityEvent> CREATOR = new Creator<IpReachabilityEvent>() {
        public IpReachabilityEvent createFromParcel(Parcel in) {
            return new IpReachabilityEvent(in);
        }

        public IpReachabilityEvent[] newArray(int size) {
            return new IpReachabilityEvent[size];
        }
    };
    public static final int NUD_FAILED = 512;
    public static final int PROBE = 256;
    public static final int PROVISIONING_LOST = 768;
    public final int eventType;
    public final String ifName;

    static final class Decoder {
        static final SparseArray<String> constants = MessageUtils.findMessageNames(new Class[]{IpReachabilityEvent.class}, new String[]{"PROBE", "PROVISIONING_", "NUD_"});

        Decoder() {
        }
    }

    private IpReachabilityEvent(String ifName, int eventType) {
        this.ifName = ifName;
        this.eventType = eventType;
    }

    private IpReachabilityEvent(Parcel in) {
        this.ifName = in.readString();
        this.eventType = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.ifName);
        out.writeInt(this.eventType);
    }

    public int describeContents() {
        return 0;
    }

    public static void logProbeEvent(String ifName, int nlErrorCode) {
        IpConnectivityEvent.logEvent(new IpReachabilityEvent(ifName, (nlErrorCode & 255) | 256));
    }

    public static void logNudFailed(String ifName) {
        IpConnectivityEvent.logEvent(new IpReachabilityEvent(ifName, 512));
    }

    public static void logProvisioningLost(String ifName) {
        IpConnectivityEvent.logEvent(new IpReachabilityEvent(ifName, 768));
    }

    public String toString() {
        return String.format("IpReachabilityEvent(%s, %s)", new Object[]{this.ifName, Decoder.constants.get(this.eventType)});
    }
}
